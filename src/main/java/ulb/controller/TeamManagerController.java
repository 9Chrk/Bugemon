package ulb.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import ulb.models.data.BugemonDefinition;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Inventory;
import ulb.models.game.PlayerProfile;
import ulb.models.game.Team;
import ulb.parsing.BugemonData;
import ulb.parsing.AttackData;
import ulb.dto.BugemonStatsDTO;
import ulb.dto.BugemonSummaryDTO;
import ulb.dto.SkillTreeNodeDTO;
import ulb.dto.SkillTreeStateDTO;
import ulb.dto.TeamStateDTO;
import ulb.view.ViewConfig;
import ulb.models.data.Attack;
import ulb.models.skilltree.SkillTreeNode;


/**
 * Coordonne la composition des équipes, les équipes sauvegardées et la persistance du profil.
 * Ce contrôleur constitue le point d'entrée unique utilisé par les vues pour modifier l'état d'équipe.
 */
public class TeamManagerController {
    private final BugemonData bugemonData;
    private final List<BugemonDefinition> currentTeam;
    String currentName;
    private final PlayerProfile playerProfile;

    // Constructeur
    private final Map<String, Attack> attacksById;

    /**
     * Initialise le contrôleur d'équipe et synchronise l'état temporaire
     * avec l'équipe persistée du profil, si elle existe.
     */
    public TeamManagerController() {
        this.playerProfile = new PlayerProfile();
        this.bugemonData = new BugemonData();
        this.attacksById = new AttackData().getAllAttacks();
        this.currentTeam = new ArrayList<>();
        syncCurrentTeamFromProfile();
    }

    /**
     * Construit des résumés légers pour chaque définition de Bugémon disponible.
     *
     * @return liste de DTO utilisés par les écrans de création d'équipe.
     */
    public List<BugemonSummaryDTO> getAllBugemonsSummaries() {
        List<BugemonSummaryDTO> summaries = new ArrayList<>();
        Map<String, BugemonDefinition> allDefinitions = bugemonData.getAllBugemons();

        for (BugemonDefinition def : allDefinitions.values()) {
            String spritePath = getBugemonSpritePath(def.getSprite());

            BugemonSummaryDTO dto = getBugemonSummaryDTO(def, spritePath);

            summaries.add(dto);
        }

        return summaries;
    }

    private static BugemonSummaryDTO getBugemonSummaryDTO(BugemonDefinition def, String spritePath) {
        BugemonStatsDTO statsDto = new BugemonStatsDTO(
                def.getType(),
                def.getBaseStats().getHealth(),
                def.getBaseStats().getAttack(),
                def.getBaseStats().getDefense(),
                def.getBaseStats().getInitiative(), new ArrayList<>(def.getAttackIds()));

        return new BugemonSummaryDTO(
                def.getId(),
                def.getName(),
                spritePath,
                statsDto);
    }

    /**
     * Expose l'état courant de l'équipe pour l'affichage.
     *
     * @return DTO de type instantané consommé par la couche de vue.
     */
    public TeamStateDTO getCurrentTeamStateDTO() {
        return new TeamStateDTO(
                this.getCurrentTeamArrayListString(),
                currentTeam.size() == Team.MAX_TEAM_SIZE,
                currentTeam.size(),
                Team.MAX_TEAM_SIZE,
                currentName);
    }

    /**
     * Ajoute un Bugémon à l'équipe en cours si les contraintes sont respectées.
     *
     * @param id identifiant du Bugémon.
     * @return true lorsque le Bugémon a été ajouté, false sinon.
     */
    public boolean addBugemonOnTeamById(String id) {
        if (id == null) {
            return false;
        }
        if (currentTeam.size() >= Team.MAX_TEAM_SIZE) {
            return false;
        }
        // Une même espèce ne peut pas être sélectionnée deux fois dans l'équipe.
        if (currentTeam.stream().anyMatch(bugemon -> bugemon.getId().equals(id))) {
            return false;
        }
        BugemonDefinition definition = bugemonData.getBugemon(id);
        if (definition == null) {
            return false;
        }
        currentTeam.add(definition);
        return true;
    }

    /**
     * Retire un Bugémon de l'équipe en cours.
     *
     * @param id identifiant du Bugémon.
     */
    public void removeBugemonOnTeamById(String id) {
        if (id == null) {
            return;
        }
        currentTeam.removeIf(bugemon -> bugemon.getId().equals(id));
    }

    /**
     * Sauvegarde l'équipe actuellement sélectionnée sous un nom fourni par l'utilisateur.
     *
     * @param name nom de sauvegarde de l'équipe.
     * @return true lorsque la persistance réussit.
     */
    public boolean saveCurrentTeam(String name) {
        if (name == null || name.isEmpty() || currentTeam.isEmpty()) {
            return false;
        }
        Team teamToSave = new Team(name);
        // La sélection éditable contient des définitions ; la sauvegarde stocke des instances.
        for (BugemonDefinition def : currentTeam) {
            teamToSave.addBugemon(new BugemonInstance(def));
        }
        playerProfile.setCurrentTeam(teamToSave);
        return playerProfile.saveTeam(name);
    }

    /**
     * Charge une équipe sauvegardée et reconstruit la liste des définitions modifiables.
     *
     * @param name nom de l'équipe sauvegardée.
     * @return true si l'équipe existait et a été chargée.
     */
    public boolean loadTeamFromProfile(String name) {
        boolean success = playerProfile.loadTeam(name);

        if (success) {
            Team loadedTeam = playerProfile.getCurrentTeam();
            this.currentTeam.clear();

            // La vue d'équipe manipule les définitions, pas les instances persistées.
            for (BugemonInstance instance : loadedTeam.getTeam()) {
                BugemonDefinition definition = bugemonData.getBugemon(instance.getId());
                if (definition != null) {
                    this.currentTeam.add(definition);
                }
            }
        }
        return success;
    }

    /**
     * Supprime une équipe sauvegardée par son nom.
     *
     * @param name nom de l'équipe sauvegardée.
     * @return true lorsque la suppression réussit.
     */
    public boolean deleteTeam(String name) {
        boolean deleted = playerProfile.deleteTeam(name);
        if (deleted && currentName != null && currentName.equals(name)) {
            // L'équipe supprimée ne doit plus rester sélectionnée dans l'écran courant.
            currentTeam.clear();
            currentName = null;
        }
        return deleted;
    }

    /**
     * Retourne l'équipe active utilisée pour les combats.
     * Reconstruit l'équipe d'exécution lorsque l'état du profil et l'état temporaire divergent.
     *
     * @return instance de l'équipe active.
     */
    public Team getTeam() {
        Team activeTeam = playerProfile.getCurrentTeam();

        // Aligne les instances d'exécution avec la sélection éditable.
        if (activeTeam == null || activeTeam.getTeam().size() != currentTeam.size()) {
            List<BugemonInstance> instances = new ArrayList<>();
            for (BugemonDefinition definition : currentTeam) {
                instances.add(new BugemonInstance(definition));
            }
            activeTeam = new Team("Active Team", instances);
            playerProfile.setCurrentTeam(activeTeam);
        }
        return activeTeam;
    }

    /**
     * Réinitialise l'équipe temporaire utilisée par l'écran de création d'équipe.
     */
    public void startNewTeamSelection() {
        currentTeam.clear();
        currentName = null;
    }

    private String getBugemonSpritePath(String bugemonSprite) {
        return ViewConfig.BUGEMON_SPRITE_PATH + bugemonSprite;
    }

    /**
     * Indique s'il existe au moins une équipe sauvegardée.
     *
     * @return true lorsque des équipes sauvegardées sont disponibles.
     */
    public boolean hasAvailableSavedTeams() {
        return playerProfile.hasAvailableSavedTeams();
    }

    /**
     * Retourne les noms des équipes sauvegardées triés alphabétiquement.
     *
     * @return liste des noms des équipes sauvegardées.
     */
    public ArrayList<String> getAllTeamNamesSaved() {
        var allTeamSaved = playerProfile.getSavedTeams();
        return allTeamSaved.keySet().stream().sorted().collect(Collectors.toCollection(ArrayList::new));
    }

    private List<String> getCurrentTeamArrayListString() {
        ArrayList<String> result = new ArrayList<>();
        for (BugemonDefinition bugemon : currentTeam) {
            result.add(bugemon.getId());
        }
        return result;
    }

    /**
     * Retourne des lignes de statistiques formatées pour les panneaux de détail.
     *
     * @param id identifiant du Bugémon.
     * @return lignes localisées prêtes à être affichées.
     */
    public String[] getStatsToString(String id) {
        BugemonDefinition definition = bugemonData.getBugemon(id);
        if (definition == null) {
            return new String[0];
        }

        return new String[] {
                "Nom : " + definition.getName(),
                "PV : " + definition.getBaseStats().getHealth(),
                "Attaque : " + definition.getBaseStats().getAttack(),
                "Défense : " + definition.getBaseStats().getDefense(),
                "Initiative : " + definition.getBaseStats().getInitiative()
        };
    }

    /**
     * Retourne le nom lisible d'un Bugémon à partir de son identifiant.
     *
     * @param bugemonId identifiant technique du Bugémon.
     * @return nom lisible si connu, sinon l'identifiant d'origine.
     */
    public String getBugemonDisplayName(String bugemonId) {
        BugemonDefinition definition = bugemonData.getBugemon(bugemonId);
        return definition != null ? definition.getName() : bugemonId;
    }

    /**
     * Retourne le nom lisible d'une attaque à partir de son identifiant.
     *
     * @param attackId identifiant technique de l'attaque.
     * @return nom lisible si connu, sinon version nettoyée de l'identifiant.
     */
    public String getAttackDisplayName(String attackId) {
        Attack attack = attacksById.get(attackId);
        if (attack != null) {
            return attack.getName();
        }
        return attackId == null ? "" : attackId.replace('_', ' ');
    }

    /**
     * Fournit l'accès à la persistance du profil joueur et à l'état de la partie.
     *
     * @return profil joueur.
     */
    public PlayerProfile getPlayerProfile() {
        return this.playerProfile;
    }

    /**
     * Retourne un instantané DTO de l'arbre de compétences du joueur.
     *
     * @return état de vue de l'arbre.
     */
    public SkillTreeStateDTO getSkillTreeStateDTO() {
        var progress = playerProfile.getSkillTreeProgress();
        var nodes = progress.getNodes().stream()
                .map(this::toSkillTreeNodeDTO)
                .toList();
        // Le DTO garde la vue indépendante du modèle mutable de progression.
        return new SkillTreeStateDTO(progress.getAvailablePoints(), nodes);
    }

    /**
     * Tente d'ajouter un point à un noeud de compétence puis persiste l'état.
     *
     * @param nodeId identifiant du noeud.
     * @return true si l'ajout a réussi.
     */
    public boolean allocateSkillPoint(String nodeId) {
        boolean updated = playerProfile.getSkillTreeProgress().allocatePoint(nodeId);
        if (updated) {
            playerProfile.saveToDisk();
        }
        return updated;
    }

    /**
     * Tente de retirer un point à un noeud de compétence puis persiste l'état.
     *
     * @param nodeId identifiant du noeud.
     * @return true si le retrait a réussi.
     */
    public boolean removeSkillPoint(String nodeId) {
        boolean updated = playerProfile.getSkillTreeProgress().removePoint(nodeId);
        if (updated) {
            playerProfile.saveToDisk();
        }
        return updated;
    }

    /**
     * Retourne l'inventaire de la partie en cours.
     *
     * @return modèle d'inventaire.
     */
    public Inventory getInventory() {
        return playerProfile.getInventory();
    }

    /**
     * Réinitialise le contenu de l'inventaire pour une nouvelle partie.
     */
    public void resetInventoryForNewRun() {
        playerProfile.resetInventoryForNewRun();
    }

    /**
     * Consomme le dernier message de retour lié à la persistance.
     *
     * @return message, éventuellement vide.
     */
    public String consumeLastPersistenceMessage() {
        return playerProfile.consumeLastPersistenceMessage();
    }

    /**
     * Recopie l'état de combat résolu dans l'équipe active du profil, puis le persiste.
     *
     * @param resolvedBattleTeam état d'équipe produit par la résolution du combat.
     */
    public void syncBattleProgress(Team resolvedBattleTeam) {
        if (resolvedBattleTeam == null) {
            throw new IllegalArgumentException("Resolved battle team cannot be null.");
        }

        Team activeTeam = playerProfile.getCurrentTeam();
        if (activeTeam == null) {
            return;
        }

        // Copie chaque état par identifiant pour préserver XP, niveaux, PV et états temporaires.
        for (BugemonInstance source : resolvedBattleTeam.getTeam()) {
            BugemonInstance target = activeTeam.getBugemonById(source.getId());
            if (target != null) {
                target.copyStateFrom(source);
            }
        }

        playerProfile.persistCurrentTeamProgress();
    }

    /**
     * Réinitialise l'équipe active au niveau 1 (XP, PV et états de combat remis à zéro),
     * puis persiste ce nouvel état.
     */
    public void resetCurrentTeamProgressToLevelOne() {
        Team activeTeam = playerProfile.getCurrentTeam();
        if (activeTeam == null) {
            return;
        }

        List<BugemonInstance> resetBugemons = new ArrayList<>();
        for (BugemonInstance bugemon : activeTeam.getTeam()) {
            // Nouvelle instance pour effacer XP, PV courants et bonus de run.
            resetBugemons.add(new BugemonInstance(bugemon.getSpecies()));
        }

        activeTeam.replaceAll(resetBugemons);
        playerProfile.persistCurrentTeamProgress();
    }

    /**
     * Initialise l'équipe temporaire à partir des données persistées du profil lorsque c'est possible.
     */
    private void syncCurrentTeamFromProfile() {
        Team loadedTeam = playerProfile.getCurrentTeam();
        if (loadedTeam == null) {
            return;
        }

        currentTeam.clear();
        for (BugemonInstance instance : loadedTeam.getTeam()) {
            BugemonDefinition definition = bugemonData.getBugemon(instance.getId());
            if (definition != null) {
                currentTeam.add(definition);
            }
        }
        // Conserve le nom afin que la vue sache quelle équipe est chargée.
        currentName = playerProfile.getCurrentTeamName();
    }

    /**
     * Recharge la source des Bugémons afin d'inclure les Bugémons personnalisés
     * ajoutés pendant l'exécution.
     */
    public void refreshAvailableBugemons() {
        this.bugemonData.loadCustomBugemons();
    }

    private SkillTreeNodeDTO toSkillTreeNodeDTO(SkillTreeNode node) {
        var progress = playerProfile.getSkillTreeProgress();
        return new SkillTreeNodeDTO(
                node.getId(),
                node.getName(),
                node.getDescription(),
                node.getPosition().x(),
                node.getPosition().y(),
                progress.getCurrentLevel(node.getId()),
                node.getMaxLevel(),
                node.getCost(),
                node.isStartNode(),
                progress.isActive(node.getId()),
                progress.isAvailable(node.getId()),
                node.getPrerequisites());
    }
}
