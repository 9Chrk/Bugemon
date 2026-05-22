package ulb.controller;

import java.io.IOException;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javafx.scene.control.Alert;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import ulb.audio.AudioManager;
import ulb.models.data.Difficulty;
import ulb.models.game.Reward;
import ulb.models.game.TowerNO;
import ulb.models.game.TowerNOState;
import ulb.models.game.RoomType;
import ulb.dto.FloorMapDTO;
import ulb.dto.RewardChoiceDTO;
import ulb.dto.RewardKind;
import ulb.dto.SkillTreeStateDTO;
import ulb.dto.RunSlotDTO;
import ulb.dto.TowerProgressDTO;
import ulb.models.skilltree.SkillTreeBonuses;
import ulb.models.game.BugemonInstance;
import ulb.view.*;
import ulb.controller.service.*;

import ulb.parsing.ItemData;
import ulb.view.RewardActionHandler;

/**
 * Orchestrateur principal de l'application.
 * Il coordonne la navigation entre les scènes, la gestion d'équipe, la
 * progression
 * des combats et l'avancement dans la Tour NO tout en gardant les vues
 * passives.
 */
public class MainController implements RewardActionHandler {

    /**
     * Actions disponibles pour le joueur dans le menu de combat.
     */
    public enum BattleActionType {
        ATTACK, USE_ITEM, SWITCH_BUGEMON, SURRENDER
    }

    private final SceneManager sceneManager;
    private final TeamManagerController teamManagerController;
    private TowerNO tower;
    private Difficulty difficulty;

    private TeamManagerView currentTeamView;
    private SkillTreeBonuses activeRunSkillBonuses;
    private final AudioManager audioManager;

    private final BugemonCreationService bugemonCreationService;
    private final UINotificationService uiNotificationService;
    private final SkillBonusApplicationService skillBonusApplicationService;
    private RewardApplicationService rewardApplicationService;
    private RewardProvider rewardProvider;
    private final AttackProvider attackProvider;
    private final FloorNamingService floorNamingService;
    private final BattleFlowController battleFlowController;
    private GameStateService gameStateService;
    private final RunLifecycleController runLifecycleController;
    private final NavigationController navigationController;
    private final ulb.controller.service.RunSlotService runSlotService;
    
    // Enregistrer les attaques et les objets pour la reconstitution du fournisseur de récompenses
    private final Map<String, ulb.models.data.Attack> attacksById;
    private final Map<String, ulb.models.data.ItemDefinition> itemsById;

    /**
     * Construit le contrôleur principal et initialise les services de navigation,
     * d'équipe, de tour et de données métier.
     *
     * @param stage stage principal JavaFX.
     */
    public MainController(Stage stage) {
        this.sceneManager = new SceneManager(stage);
        this.teamManagerController = new TeamManagerController();
        TowerNOState saved = teamManagerController.getPlayerProfile().getTowerState();
        // Au démarrage, on restaure la tour active si une run existe déjà.
        this.tower = saved != null ? saved.toTower() : new TowerNO();
        this.attacksById = new ulb.parsing.AttackData().getAllAttacks();
        this.itemsById = new ItemData().getAllItems();
        this.audioManager = new AudioManager(AudioConfig.DEFAULT_MUSIC_VOLUME);
        this.activeRunSkillBonuses = new SkillTreeBonuses();

        // Initialise les services.
        // --- Services ---
        this.gameStateService = new GameStateService(tower);
        this.uiNotificationService = new UINotificationService(stage);
        this.bugemonCreationService = new BugemonCreationService(teamManagerController);
        this.skillBonusApplicationService = new SkillBonusApplicationService(teamManagerController, itemsById);
        this.rewardApplicationService = new RewardApplicationService(teamManagerController, tower);
        this.rewardProvider = new RewardProvider(tower, teamManagerController, attacksById, itemsById);
        this.attackProvider = new AttackProvider(attacksById);
        this.floorNamingService = new FloorNamingService();
        this.battleFlowController = new BattleFlowController(this, teamManagerController, this.gameStateService,
                skillBonusApplicationService, audioManager);
        this.runLifecycleController = new RunLifecycleController(this, teamManagerController,
                skillBonusApplicationService);
        this.runSlotService = new ulb.controller.service.RunSlotService(teamManagerController);
        this.navigationController = new NavigationController(this, sceneManager, teamManagerController, audioManager);

        Font.loadFont(getClass().getResourceAsStream("/fonts/VipnagorgiallaBdIt.otf"), 40);
        Font.loadFont(getClass().getResourceAsStream("/fonts/VipnagorgiallaRgIt.otf"), 40);
    }

    // ----------------- Navigation -----------------

    /**
     * Affiche la scène du menu principal.
     */
    public void showMainMenu() {
        navigationController.showMainMenu();
    }

    /**
     * Affiche la scène du menu de nouvelle partie.
     * Sans équipe sauvegardée, redirige vers la gestion d'équipe.
     */
    public void showNewGameMenu() {
        navigationController.showNewGameMenu();
    }

    /**
     * Choix d'emplacement et du nom de partie après sélection d'une équipe.
     */
    public void showRunSlotSelection(String teamName) {
        navigationController.showRunSlotSelection(teamName);
    }

    public void showRunSlotSelection(String teamName, Difficulty difficulty) {
        navigationController.showRunSlotSelection(teamName, difficulty);
    }

    /**
     * Menu de chargement des parties sauvegardées (emplacements 1 à 5).
     */
    public void showLoadGameMenu() {
        navigationController.showLoadGameMenu();
    }

    /**
     * Démarre une nouvelle partie sur l'emplacement choisi avec le nom indiqué.
     */
    public void startNewRunInSlot(int slotIndex, String runName, Difficulty difficulty) {
        runLifecycleController.startNewRunInSlot(slotIndex, runName, difficulty);
    }

    /**
     * Reprend une partie depuis un emplacement sauvegardé.
     *
     * @return false si l'emplacement est vide ou l'équipe est introuvable.
     */
    public boolean loadGameFromSlot(int slotIndex) {
        return runLifecycleController.loadGameFromSlot(slotIndex);
    }

    /**
     * Indique si les actions de chargement doivent être désactivées dans le menu.
     *
     * @return true si aucun run ne peut être chargé.
     */
    public boolean hasAnyLoadableRun() {
        return !runLifecycleController.hasAnyLoadableRun();
    }

    /**
     * Retourne les emplacements de partie sous une forme prête à afficher.
     *
     * @return liste des slots avec leur texte affichable.
     */
    public List<RunSlotDTO> getRunSlots() {
        return runSlotService.getRunSlots();
    }

    /**
     * Continue automatiquement la dernière partie pertinente: slot actif si
     * disponible, sinon premier slot occupé.
     */
    public void continueLastRun() {
        runLifecycleController.continueLastRun();
    }

    /**
     * Affiche la scène de création de Bugémon.
     */
    public void showBugemonCreatorMenu() {
        navigationController.showBugemonCreatorMenu();
    }

    /**
     * Affiche la scène de gestion d'équipe et réinitialise l'état de sélection.
     */
    public void showTeamManagement() {
        navigationController.showTeamManagement();
    }

    /**
     * Affiche l'écran de l'arbre de compétences.
     */
    public void showSkillTreeMenu() {
        navigationController.showSkillTreeMenu();
    }

    /**
     * Affiche la carte d'étage H14.
     */
    public void showFloorMap() {
        navigationController.showFloorMap();
    }

    /**
     * Affiche l'écran de selection des objets de preparation
     */
    public void showItemSelectMenu() {
        navigationController.showItemSelectMenu();
    }

    /**
     * Lance l'action correspondant à la salle courante.
     */
    public void handleNextStep() {
        battleFlowController.handleNextStep();
    }

    /**
     * Gère l'entrée dans une salle de la carte H14 selon son type.
     *
     * @param roomType type de la salle atteinte sur la carte.
     */
    public void handleMapRoom(RoomType roomType) {
        battleFlowController.handleMapRoom(roomType);
    }

    // ----------------- Battle Logic -----------------

    /**
     * Crée et configure une nouvelle instance de combat pour la salle courante.
     */
    public void startNewBattle(Difficulty difficulty) {
        battleFlowController.startNewBattle(difficulty);
    }

    // ----------------- Logique de combat -----------------
    /**
     * Point d'entrée d'une action de combat.
     * Orchestration de la séquence de tour selon la priorité et l'initiative.
     */
    public void executeAction(BattleActionType type, String targetId) {
        battleFlowController.executeAction(type, targetId);
    }

    /**
     * Force la victoire du joueur dans le combat en cours,
     * met à jour l'affichage et enchaîne la fin de combat.
     */
    public void forceWinBattle() {
        battleFlowController.forceWinBattle();
    }

    // ----------------- Gestion d'équipe -----------------

    /**
     * Ajoute un Bugémon à l'équipe en cours de constitution.
     *
     * @param id identifiant du Bugémon.
     */
    public void handleAddBugemon(String id) {
        if (!teamManagerController.addBugemonOnTeamById(id)) {
            showError("Ajout impossible", "Équipe pleine ou Bugémon déjà présent.");
        } else {
            // La vue reconstruit à la fois la grille et la colonne d'équipe.
            currentTeamView.refreshAll();
        }
    }

    /**
     * Retire un Bugémon de l'équipe en cours de constitution.
     *
     * @param name identifiant du Bugémon.
     */
    public void handleRemoveBugemon(String name) {
        teamManagerController.removeBugemonOnTeamById(name);
        currentTeamView.refreshAll();
    }

    /**
     * Charge une équipe sauvegardée et rafraîchit la vue associée.
     *
     * @param teamName nom de l'équipe sauvegardée.
     */
    public void handleLoadTeam(String teamName) {
        teamManagerController.loadTeamFromProfile(teamName);
        currentTeamView.refreshAll();
        // Un chargement peut produire un message de migration ou de persistance.
        showPendingPersistenceNotification();
        showNotification("Équipe chargée : " + teamName);
    }

    /**
     * Supprime une équipe sauvegardée et rafraîchit la vue associée.
     *
     * @param teamName nom de l'équipe sauvegardée.
     */
    public void handleDeleteTeam(String teamName) {
        boolean deleted = teamManagerController.deleteTeam(teamName);
        currentTeamView.refreshAll();
        showPendingPersistenceNotification();
        // Le retour utilisateur distingue une suppression réelle d'un nom introuvable.
        showNotification(deleted ? "Équipe supprimée : " + teamName : "Suppression impossible : " + teamName);
    }

    /**
     * Sauvegarde l'équipe temporaire actuelle.
     *
     * @param name nom de sauvegarde souhaité.
     */
    public void handleSaveTeam(String name) {
        if (name.isEmpty())
            return;
        if (!teamManagerController.saveCurrentTeam(name)) {
            showError("Sauvegarde", "Erreur lors de la sauvegarde.");
        } else {
            // Après sauvegarde, on vide la sélection pour préparer une nouvelle équipe.
            teamManagerController.startNewTeamSelection();
            currentTeamView.refreshAll();
            showPendingPersistenceNotification();
            showNotification("Équipe sauvegardée : " + name);
        }
    }

    // ----------------- Progression et état -----------------

    /**
     * Applique un choix de montée de niveau et continue à traiter les choix
     * restants si nécessaire.
     *
     * @param choiceIndex index du choix sélectionné.
     */
    public void handleLevelUpSelection(int choiceIndex) {
        battleFlowController.handleLevelUpSelection(choiceIndex);
    }

    /**
     * Applique une récompense de type objet choisie par le joueur.
     *
     * @param choiceIndex index de la récompense sélectionnée.
     */
    public void applyRewardChoiceItem(int choiceIndex) {
        rewardApplicationService.applyRewardChoiceItem(choiceIndex);
        persistRunState();
    }

    /**
     * Applique une récompense de statistiques permanentes sur un Bugémon ciblé.
     *
     * @param choiceIndex  index de la récompense sélectionnée.
     * @param bugemonIndex index du Bugémon bénéficiaire dans l'équipe.
     */
    public void applyRewardChoiceStats(int choiceIndex, int bugemonIndex) {
        rewardApplicationService.applyRewardChoiceStats(choiceIndex, bugemonIndex);
        persistRunState();
    }

    /**
     * Applique une récompense d'attaque en remplaçant une attaque existante.
     *
     * @param choiceIndex  index de la récompense sélectionnée.
     * @param bugemonIndex index du Bugémon bénéficiaire.
     * @param attackIndex  index de l'attaque à remplacer.
     */
    public void applyRewardChoiceReplaceAttack(int choiceIndex, int bugemonIndex, int attackIndex) {
        rewardApplicationService.applyRewardChoiceReplaceAttack(choiceIndex, bugemonIndex, attackIndex);
        persistRunState();
    }

    /**
     * Lance une nouvelle partie avec une difficulté spécifique.
     * Réinitialise la tour, l'inventaire et persiste le nouvel état.
     * 
     * @param difficulty Le niveau de difficulté choisi par l'utilisateur.
     */
    public void startNewRun(Difficulty difficulty) {
        runLifecycleController.startNewRun(difficulty);
    }

    /**
     * Surcharge pour la réinitialisation automatique.
     * Utilise la difficulté NORMAL par défaut.
     */
    public void startNewRun() {
        runLifecycleController.startNewRun();
    }

    /**
     * Reprend la navigation après l'écran de victoire d'un boss d'étage.
     */
    public void continueAfterFloorBossVictory() {
        battleFlowController.continueAfterFloorBossVictory();
    }

    /**
     * Arrête les lecteurs audio actifs lors de la fermeture de l'application.
     */
    public void shutdownAudio() {
        audioManager.stop();
    }

    void showError(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(content);
        a.showAndWait();
    }

    // --- Accesseurs ---
    /**
     * Retourne les options de récompense disponibles.
     */
    public List<RewardChoiceDTO> getCurrentRewardOptions() {
        List<Reward> rewards = getCurrentRewards();
        List<RewardChoiceDTO> options = new ArrayList<>(rewards.size());
        for (int i = 0; i < rewards.size(); i++) {
            Reward reward = rewards.get(i);
            // L'index du DTO doit rester aligné avec la liste stockée dans FloorInstance.
            options.add(new RewardChoiceDTO(i, reward.getDescription(), true, toRewardKind(reward)));
        }
        return options;
    }

    /**
     * Retourne les Bugémons de l'équipe pouvant recevoir la récompense sélectionnée.
     *
     * @param rewardIndex index de la récompense sélectionnée.
     * @return liste des cibles affichables.
     */
    public List<RewardChoiceDTO> getRewardTargets(int rewardIndex) {
        Reward reward = getRewardByIndex(rewardIndex);
        if (!(reward instanceof Reward.Stats) && !(reward instanceof Reward.AttackReward)) {
            return List.of();
        }

        List<RewardChoiceDTO> targets = new ArrayList<>();
        var team = getBattleController().getPlayerTeam();
        for (int i = 0; i < team.getSize(); i++) {
            BugemonInstance bugemon = team.getBugemonAt(i);
            // Les stats acceptent tout le monde, les attaques seulement les types compatibles.
            boolean selectable = reward instanceof Reward.Stats
                    || ((Reward.AttackReward) reward).canTeachTo(bugemon);
            targets.add(new RewardChoiceDTO(i,
                    bugemon.getName() + " (Niv." + bugemon.getLevel() + ")",
                    selectable,
                    null));
        }
        return targets;
    }

    /**
     * Retourne les attaques d'un Bugémon sous forme de libellés affichables pour le remplacement.
     *
     * @param rewardIndex index de la récompense sélectionnée.
     * @param bugemonIndex index du Bugémon ciblé.
     * @return liste des attaques affichables.
     */
    public List<String> getRewardAttackLabels(int rewardIndex, int bugemonIndex) {
        Reward reward = getRewardByIndex(rewardIndex);
        if (!(reward instanceof Reward.AttackReward)) {
            return List.of();
        }

        var team = getBattleController().getPlayerTeam();
        if (bugemonIndex < 0 || bugemonIndex >= team.getSize()) {
            return List.of();
        }

        BugemonInstance bugemon = team.getBugemonAt(bugemonIndex);
        return bugemon.getLearnedAttackIds().stream()
                .map(getBattleController()::getAttackDisplayName)
                .toList();
    }

    private Reward getRewardByIndex(int rewardIndex) {
        List<Reward> rewards = getCurrentRewards();
        if (rewardIndex < 0 || rewardIndex >= rewards.size()) {
            return null;
        }
        return rewards.get(rewardIndex);
    }

    private List<Reward> getCurrentRewards() {
        return rewardProvider.getCurrentRewardOptions();
    }

    private RewardKind toRewardKind(Reward reward) {
        if (reward instanceof Reward.Item) {
            return RewardKind.ITEM;
        }
        if (reward instanceof Reward.Stats) {
            return RewardKind.STATS;
        }
        return RewardKind.ATTACK;
    }

    // Le formatage des emplacements est délégué à RunSlotService pour garder MainController centré sur l'orchestration.
    /**
     * Retourne les informations de progression actuelles de la tour pour l'affichage.
     */
    public TowerProgressDTO getTowerProgress() {
        return new TowerProgressDTO(tower.getCurrentFloor(), tower.getCurrentStep(), 6, tower.getCurrentRoomType(),
                tower.isBossRoom(), "Étage " + tower.getCurrentFloor() + " : " + floorNamingService.getFloorName(tower.getCurrentFloor()));
    }

    /**
     * Retourne l'état de vue de l'arbre de compétences.
     *
     * @return état courant de l'arbre.
     */
    public SkillTreeStateDTO getSkillTreeState() {
        return teamManagerController.getSkillTreeStateDTO();
    }

    /**
     * Tente d'ajouter un point à un noeud.
     *
     * @param nodeId identifiant du noeud.
     * @return true si l'opération a réussi.
     */
    public boolean allocateSkillPoint(String nodeId) {
        return teamManagerController.allocateSkillPoint(nodeId);
    }

    /**
     * Tente de retirer un point à un noeud.
     *
     * @param nodeId identifiant du noeud.
     * @return true si l'opération a réussi.
     */
    public boolean removeSkillPoint(String nodeId) {
        return teamManagerController.removeSkillPoint(nodeId);
    }

    /**
     * Retourne la carte de l'étage courant sous forme DTO.
     *
     * @return carte affichable de l'étage courant.
     */
    public FloorMapDTO getCurrentFloorMap() {
        return tower.getFloorMap();
    }

    /**
     * Déplace le joueur sur la carte H14 et retourne le type de la salle atteinte.
     * Le branchement vers les vues spécifiques sera ajouté avec la vue de carte.
     *
     * @param roomId identifiant de la salle cible.
     * @return type de salle atteint, ou null si le mouvement est refusé.
     */
    public RoomType enterRoom(String roomId) {
        return tower.enterRoom(roomId);
    }

    /**
     * Expose le contrôleur de combat aux vues de combat.
     *
     * @return contrôleur de combat courant.
     */
    public BattleController getBattleController() {
        return battleFlowController.getBattleController();
    }

    /**
     * Expose le contrôleur de gestion d'équipe aux vues concernées.
     *
     * @return contrôleur de gestion d'équipe.
     */
    public TeamManagerController getTeamManagerController() {
        return teamManagerController;
    }

    TeamManagerView getCurrentTeamView() {
        return currentTeamView;
    }

    void setCurrentTeamView(TeamManagerView currentTeamView) {
        this.currentTeamView = currentTeamView;
    }

    TowerNO getTower() {
        return tower;
    }

    void setTower(TowerNO tower) {
        this.tower = tower;
        // Tous les services gardant une référence à la tour doivent être resynchronisés.
        this.rewardProvider = new RewardProvider(tower, teamManagerController, attacksById, itemsById);
        this.rewardApplicationService.setTower(tower);
        this.gameStateService.setTower(tower);
        this.battleFlowController.setGameStateServiceTower(tower);
    }

    void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    void setActiveRunSkillBonuses(SkillTreeBonuses activeRunSkillBonuses) {
        this.activeRunSkillBonuses = activeRunSkillBonuses;
    }

    Difficulty getDifficulty() {
        return difficulty;
    }

    SkillTreeBonuses getActiveRunSkillBonuses() {
        return activeRunSkillBonuses;
    }

    void switchScene(javafx.scene.Parent root) {
        sceneManager.switchScene(root);
    }

    void persistRunState() {
        teamManagerController.getPlayerProfile().attachTowerState(tower);
        teamManagerController.getPlayerProfile().saveToDisk();
    }

    void showPendingPersistenceNotification() {
        String message = teamManagerController.consumeLastPersistenceMessage();
        if (message != null && !message.isBlank()) {
            uiNotificationService.showNotification(message);
        }
    }

    private void showPendingTowerNotification() {
        String message = tower.consumeLastStatusMessage();
        if (message != null && !message.isBlank()) {
            uiNotificationService.showNotification(message);
        }
    }

    /**
     * Sauvegarde un Bugémon personnalisé, puis recharge la liste des Bugémons
     * disponibles.
     *
     * @param name      nom affiché du Bugémon.
     * @param type      type du Bugémon.
     * @param pv        points de vie de base.
     * @param atk       attaque de base.
     * @param def       défense de base.
     * @param ini       initiative de base.
     * @param moves     attaques sélectionnées.
     * @param spriteURL chemin local de l'image du sprite.
     */
    public void saveCustomBugemon(String name, String type, int pv, int atk, int def, int ini, List<String> moves,
            String spriteURL) {
        try {
            bugemonCreationService.saveCustomBugemon(name, type, pv, atk, def, ini, moves, spriteURL);
            showTeamManagement();

        } catch (IOException e) {
            uiNotificationService.showError("Erreur de sauvegarde", "Impossible d'enregistrer le Bugémon : " + e.getMessage());
        }
    }

    /**
     * Retourne les noms lisibles des attaques disponibles pour un type donné.
     */
    public List<String> getAttackNamesByType(String typeName) {
        return attackProvider.getAttackNamesByType(typeName);
    }

    /**
     * Valide les champs minimaux de création d'un Bugémon personnalisé.
     *
     * @param name          nom saisi.
     * @param imageFile     image sélectionnée.
     * @param selectedMoves attaques sélectionnées.
     * @return true si le formulaire contient les données minimales attendues.
     */
    public boolean isBugemonCreationValid(String name, File imageFile, List<String> selectedMoves) {
        return bugemonCreationService.isBugemonCreationValid(name, imageFile, selectedMoves);
    }

    /**
     * Affiche une notification brève non bloquante.
     *
     * @param message message à afficher.
     */
    public void showNotification(String message) {
        uiNotificationService.showNotification(message);
    }

    void applyRunStartSkillBonuses() {
        skillBonusApplicationService.applyRunStartSkillBonuses(activeRunSkillBonuses);
    }
}
