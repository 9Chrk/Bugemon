package ulb.controller;

import java.util.*;
import ulb.models.battle.*;
import ulb.models.data.*;
import ulb.models.game.*;
import ulb.models.battle.EnemyTeamFactory;
import ulb.models.data.Difficulty;
import ulb.models.skilltree.SkillTreeBonuses;
import ulb.parsing.*;
import ulb.dto.*;
import ulb.controller.service.*;

/**
 * Orchestration d'une session de combat entre l'équipe du joueur et une équipe
 * adverse.
 *
 * Le contrôleur délègue la résolution du combat à {@link BattleService},
 * conserve
 * l'état des récompenses et des montées de niveau, et expose des DTO prêts pour
 * la vue.
 */
public class BattleController {
    // --- Composants internes ---
    private final Battle currentBattle;
    private final BattleService battleService;
    private final ExperienceService experienceService = new ExperienceService();
    private final LevelUpChoiceHandler levelUpChoiceHandler = new LevelUpChoiceHandler();

    // --- Données et état ---
    private final Inventory inventory;
    private final Map<String, Attack> attacksById;
    private final Map<String, ItemDefinition> itemsById;
    private final SkillTreeBonuses skillTreeBonuses;
    private GameMode mode;
    private boolean victoryRewardsResolved = false;

    // --- Services ---
    private final BattleStateMapper battleStateMapper;
    private final BattleUIDataProvider battleUIDataProvider;

    /**
     * Construit un contrôleur de combat prêt à l'usage pour une partie standard.
     *
     * @param team      équipe active du joueur.
     * @param inventory inventaire courant utilisé en combat.
     */
    public BattleController(Team team, Inventory inventory) {
        this(team, inventory, Difficulty.NORMAL, new SkillTreeBonuses());
    }

    /**
     * Construit un contrôleur de combat avec une difficulté explicite.
     *
     * @param team       équipe active du joueur.
     * @param inventory  inventaire courant utilisé en combat.
     * @param difficulty difficulté utilisée pour générer l'équipe ennemie.
     */
    public BattleController(Team team, Inventory inventory, Difficulty difficulty) {
        this(team, inventory, difficulty, new SkillTreeBonuses());
    }

    /**
     * Construit un contrôleur de combat avec difficulté et bonus de compétences.
     *
     * @param team             équipe active du joueur.
     * @param inventory        inventaire courant utilisé en combat.
     * @param difficulty       difficulté utilisée pour générer l'équipe ennemie.
     * @param skillTreeBonuses bonus de compétences actifs pour cette run.
     */
    public BattleController(Team team, Inventory inventory, Difficulty difficulty, SkillTreeBonuses skillTreeBonuses) {
        this.inventory = inventory;
        this.attacksById = new AttackData().getAllAttacks();
        this.itemsById = new ItemData().getAllItems();
        this.skillTreeBonuses = skillTreeBonuses == null ? new SkillTreeBonuses() : skillTreeBonuses;
        // Nettoie les états hérités d'un combat précédent avant la copie de Battle.
        team.resetTeamTemporaryBonuses();

        // Construit l'équipe ennemie via la fabrique.
        Team enemyTeam = new EnemyTeamFactory().buildEnemyTeam(team, new BugemonData().getAllBugemons(), difficulty);

        // Battle travaille sur ses propres copies pour isoler le combat de la sauvegarde.
        this.currentBattle = new Battle(team, enemyTeam);
        this.battleService = new BattleService(attacksById, itemsById);
        configureBattleBonuses();

        // Initialise les services d'exposition des données à la vue.
        this.battleStateMapper = new BattleStateMapper(attacksById, battleService);
        this.battleUIDataProvider = new BattleUIDataProvider(attacksById, battleService);
    }

    /**
     * Constructeur dédié aux tests et aux scénarios injectant un moteur de combat
     * personnalisé.
     *
     * @param playerTeam    équipe du joueur.
     * @param enemyTeam     équipe adverse.
     * @param battleService service de combat injecté.
     */
    public BattleController(Team playerTeam, Team enemyTeam, BattleService battleService) {
        if (playerTeam == null || enemyTeam == null || battleService == null) {
            throw new IllegalArgumentException("Battle controller dependencies cannot be null.");
        }

        this.inventory = new Inventory(Map.of());
        this.attacksById = battleService.getAttacksById();
        this.itemsById = battleService.getItemsById();
        this.skillTreeBonuses = new SkillTreeBonuses();
        playerTeam.resetTeamTemporaryBonuses();
        this.currentBattle = new Battle(playerTeam, enemyTeam);
        this.battleService = battleService;

        // Initialise les services d'exposition des données à la vue.
        this.battleStateMapper = new BattleStateMapper(attacksById, battleService);
        this.battleUIDataProvider = new BattleUIDataProvider(attacksById, battleService);
    }

    /**
     * Exécute une action dans le contexte du combat courant.
     *
     * @param action action choisie par l'acteur pour cette étape.
     * @return texte de journal généré par l'action.
     */
    public String executePlayerAction(BattleAction action) {
        if (action.consumesTurn()) {
            // Active les bonus différés juste avant l'action qui consomme le tour.
            battleService.startTurn(currentBattle);
        }

        String log = action.execute(currentBattle, battleService);

        // Finalise les effets de tour si l'action le consomme ou si le combat est terminé.
        if (action.consumesTurn() || currentBattle.isFinished()) {
            battleService.endTurn(currentBattle);
        }

        return log;
    }

    /**
     * Indique si le joueur agit avant l'adversaire selon l'initiative.
     *
     * @return true si le joueur agit en premier.
     */
    public boolean playerActsFirst() {
        int playerInit = currentBattle.getPlayerActive().getEffectiveStats().getInitiative();
        int enemyInit = currentBattle.getEnemyActive().getEffectiveStats().getInitiative();
        return playerInit >= enemyInit;
    }

    // ----------------- Récompenses et progression -----------------

    /**
     * Résout une seule fois les récompenses de victoire, répartit l'XP et prépare
     * les choix de montée de niveau.
     *
     * @param floor numéro de l'étage utilisé dans la formule d'XP.
     * @return résumé textuel des gains d'XP et des montées de niveau.
     */
    public String resolveVictoryRewards(int floor) {
        if (victoryRewardsResolved)
            return "";
        victoryRewardsResolved = true;

        // Le mode BOSS modifie la formule de base avant les bonus de compétence.
        int baseXp = experienceService.calculateVictoryXp(floor, mode == GameMode.BOSS,
                currentBattle.getEnemyTeam().getSize());
        int totalXp = (int) Math.max(0, Math.round(baseXp * skillTreeBonuses.getXpMultiplier()));
        ExperienceResolution res = experienceService.distributeVictoryXp(currentBattle.getPlayerTeam(), totalXp);

        StringBuilder summary = new StringBuilder();
        for (ExperienceGain gain : res.gains()) {
            summary.append(gain.bugemon().getName()).append(" gains ").append(gain.gainedXp()).append(" XP.\n");

            // Les choix de niveau sont mis en file pour être affichés un par un.
            levelUpChoiceHandler.queueLevelUpChoices(gain.bugemon(), gain.levelsGained(),
                    skillTreeBonuses, summary);
        }
        return summary.toString();
    }

    private void configureBattleBonuses() {
        DamageCalculator damageCalculator = battleService.getDamageCalculator();
        // Seuls les bonus calculés pour la run courante alimentent le calculateur.
        damageCalculator.setPlayerCriticalChanceBonus(skillTreeBonuses.getCriticalChanceBonus());
        damageCalculator.setPlayerTypeDamageMultipliers(skillTreeBonuses.getTypeDamageMultipliers());
    }

    /**
     * Applique le choix de montée de niveau en attente et le retire de la file.
     *
     * @param choiceIndex index du choix sélectionné.
     * @return ligne de journal décrivant le bonus permanent appliqué.
     */
    public String applyPendingLevelUpChoice(int choiceIndex) {
        return levelUpChoiceHandler.applyPendingLevelUpChoice(choiceIndex);
    }

    // ----------------- Données d'interface et DTO -----------------

    /**
     * Capture l'état courant du combat pour l'affichage.
     *
     * @return DTO décrivant les deux Bugémons actifs.
     */
    public BattleStateDTO getCurrentBattleState() {
        return battleStateMapper.mapCurrentBattleState(
                currentBattle.getPlayerActive(),
                currentBattle.getEnemyActive());
    }

    /**
     * Retourne les données des attaques apprises par un Bugémon.
     *
     * @param b Bugémon dont les attaques doivent être exposées.
     * @return données minimales des attaques.
     */
    public List<AttackSummaryDTO> getAttackSummaries(BugemonInstance b) {
        return b.getLearnedAttackIds().stream()
                .map(attacksById::get)
                .filter(Objects::nonNull)
                .map(attack -> new AttackSummaryDTO(attack.getId(), attack.getName(), attack.getType()))
                .toList();
    }

    /**
     * Calcule un aperçu de l'efficacité pour chaque attaque disponible du joueur.
     *
     * @return messages d'efficacité alignés avec l'ordre des attaques.
     */
    public List<String> getAttackEffectivenessPreview() {
        BugemonInstance attacker = currentBattle.getPlayerActive();
        BugemonInstance defender = currentBattle.getEnemyActive();
        return battleUIDataProvider.getAttackEffectivenessPreview(attacker, defender);
    }

    /**
     * Retourne les libellés de l'inventaire pour l'affichage du menu des objets.
     *
     * @return libellés formatés avec les quantités.
     */
    public List<String> getFormattedItemLabels() {
        return inventory.getItems().entrySet().stream()
                .filter(e -> e.getValue() > 0)
                // Le libellé reste simple : nom d'objet + quantité possédée.
                .map(e -> itemsById.get(e.getKey()).getName() + " x" + e.getValue())
                .toList();
    }

    /**
     * Retourne la description d'un objet pour l'aide contextuelle ou le survol.
     *
     * @param itemId identifiant de l'objet.
     * @return description localisée ou message de secours.
     */
    public String getItemDescription(String itemId) {
        ItemDefinition item = itemsById.get(itemId);
        return item != null ? item.getDescription() : "Description indisponible.";
    }

    /**
     * Retourne un nom d'attaque lisible à partir de son identifiant.
     *
     * @param attackId identifiant technique de l'attaque.
     * @return nom affichable de l'attaque.
     */
    public String getAttackDisplayName(String attackId) {
        Attack attack = attacksById.get(attackId);
        if (attack != null) {
            return attack.getName();
        }
        return attackId == null ? "" : attackId.replace('_', ' ');
    }

    /**
     * Vérifie si un membre de l'équipe est K.O.
     *
     * @param index index dans l'équipe.
     * @return true lorsque le Bugémon ciblé est K.O.
     */
    public boolean isBugemonDead(int index) {
        List<BugemonInstance> team = currentBattle.getPlayerTeam().getBugemons();
        return (index >= 0 && index < team.size()) && team.get(index).isKo();
    }

    /**
     * Vérifie si un membre de l'équipe est actuellement actif au combat.
     *
     * @param index index dans l'équipe.
     * @return true lorsque le Bugémon ciblé est l'actif.
     */
    public boolean isBugemonFighting(int index) {
        List<BugemonInstance> team = currentBattle.getPlayerTeam().getBugemons();
        return (index >= 0 && index < team.size()) && team.get(index) == currentBattle.getPlayerActive();
    }

    /**
     * Indique si le combat est terminé.
     *
     * @return true lorsque les conditions de fin de combat sont remplies.
     */
    public boolean isBattleOver() {
        return currentBattle.isFinished();
    }

    /**
     * Indique si un Bugémon ennemi donné est K.O.
     *
     * @param enemyBugemon Bugémon ennemi à vérifier.
     * @return true si le Bugémon est K.O.
     */
    public boolean isEnemyBugemonDead(BugemonInstance enemyBugemon) {
        return enemyBugemon.isKo();
    }

    /**
     * Indique si l'équipe du joueur a remporté le combat.
     *
     * @return true lorsque tous les Bugémons ennemis sont K.O.
     */
    public boolean playerHasWon() {
        return currentBattle.getEnemyTeam().getBugemons().stream().allMatch(BugemonInstance::isKo);
    }

    /**
     * Indique s'il reste au moins un choix de montée de niveau à résoudre.
     *
     * @return true lorsque la file des choix de montée de niveau n'est pas vide.
     */
    public boolean hasPendingLevelUpChoices() {
        return levelUpChoiceHandler.hasPendingChoices();
    }

    /**
     * Retourne les descriptions du premier ensemble de choix de montée de niveau en attente.
     *
     * @return liste des descriptions, ou liste vide s'il n'existe aucun choix en attente.
     */
    public List<String> getPendingLevelUpChoiceDescriptions() {
        return levelUpChoiceHandler.getCurrentChoiceDescriptions();
    }

    /**
     * Retourne le titre de la fenêtre de montée de niveau actuellement en attente.
     *
     * @return texte du titre, ou chaîne vide s'il n'existe aucun choix en attente.
     */
    public String getPendingLevelUpChoiceTitle() {
        return levelUpChoiceHandler.getCurrentChoiceTitle();
    }

    /**
     * Retourne l'équipe du joueur dans le combat courant.
     *
     * @return équipe du joueur.
     */
    public Team getPlayerTeam() {
        return currentBattle.getPlayerTeam();
    }

    /**
     * Retourne le Bugémon actif du joueur.
     *
     * @return Bugémon actif du joueur.
     */
    public BugemonInstance getPlayerActive() {
        return currentBattle.getPlayerActive();
    }

    /**
     * Retourne le Bugémon actif de l'ennemi.
     *
     * @return Bugémon actif de l'ennemi.
     */
    public BugemonInstance getEnemyActive() {
        return currentBattle.getEnemyActive();
    }

    /**
     * Accès au modèle de combat pour les classes du même package et les tests.
     *
     * @return instance de combat courante.
     */
    Battle getCurrentBattle() {
        return currentBattle;
    }

    /**
     * Définit le mode de jeu courant afin d'adapter les calculs de récompense.
     *
     * @param m mode de jeu pour ce combat.
     */
    public void setGameMode(GameMode m) {
        this.mode = m;
    }

    /**
     * Force la victoire du joueur en mettant tous les Bugémons ennemis K.O.
     */
    public void forceWin() {
        for (BugemonInstance enemy : currentBattle.getEnemyTeam().getBugemons()) {
            // Les dégâts exacts garantissent un K.O. sans dépendre des stats.
            enemy.takeDamage(enemy.getCurrentHp());
        }
        currentBattle.endBattle();
    }

    /**
     * Retourne la liste des DTOs d'affichage pour les Bugémons de l'équipe joueur.
     * Cela isole la vue du modèle en fournissant une vue schématique des Bugémons.
     *
     * @return liste des DTOs d'affichage.
     */
    public List<BugemonDisplayDTO> getPlayerTeamDisplayDTOs() {
        List<BugemonDisplayDTO> dtos = new ArrayList<>();
        Team playerTeam = currentBattle.getPlayerTeam();
        for (int i = 0; i < playerTeam.getSize(); i++) {
            BugemonInstance bugemon = playerTeam.getBugemonAt(i);
            dtos.add(new BugemonDisplayDTO(bugemon.getName(), bugemon.getLevel(), i));
        }
        return dtos;
    }
}
