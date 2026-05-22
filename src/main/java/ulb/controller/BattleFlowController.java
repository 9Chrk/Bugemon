package ulb.controller;

import java.util.List;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import ulb.audio.AudioManager;
import ulb.controller.service.BattleOrchestrationService;
import ulb.controller.service.GameStateService;
import ulb.controller.service.SkillBonusApplicationService;
import ulb.models.battle.AttackAction;
import ulb.models.battle.BattleAction;
import ulb.models.battle.SurrenderAction;
import ulb.models.battle.SwitchAction;
import ulb.models.battle.UseItemAction;
import ulb.models.data.Difficulty;
import ulb.models.game.BugemonInstance;
import ulb.models.game.RoomType;
import ulb.models.game.TowerNO;
import ulb.view.BattleView;
import ulb.view.FloorBossVictoryView;
import ulb.view.GameOverView;
import ulb.view.RewardView;
import ulb.view.VictoryView;

/**
 * Gère le déroulé complet d'un combat et les transitions associées.
 */
public class BattleFlowController {
    private final MainController mainController;
    private final TeamManagerController teamManagerController;
    private final GameStateService gameStateService;
    private final SkillBonusApplicationService skillBonusApplicationService;
    private final AudioManager audioManager;

    private BattleController battleController;
    private BattleView battleView;
    private BattleOrchestrationService battleOrchestrationService;
    private boolean battleStartedFromMap = false;
    private boolean battleOutcomeApplied = false;
    private String lastBattleResolutionSummary = "";

    /**
     * Construit le contrôleur de flux de combat avec les services nécessaires.
     *
     * @param mainController contrôleur principal de l'application.
     * @param teamManagerController contrôleur de gestion de l'équipe du joueur.
     * @param gameStateService service de progression de la tour.
     * @param skillBonusApplicationService service d'application des bonus de compétences.
     * @param audioManager gestionnaire audio utilisé pendant les transitions.
     */
    public BattleFlowController(MainController mainController, TeamManagerController teamManagerController,
            GameStateService gameStateService, SkillBonusApplicationService skillBonusApplicationService,
            AudioManager audioManager) {
        this.mainController = mainController;
        this.teamManagerController = teamManagerController;
        this.gameStateService = gameStateService;
        this.skillBonusApplicationService = skillBonusApplicationService;
        this.audioManager = audioManager;
    }

    /**
     * Retourne le contrôleur du combat en cours.
     *
     * @return contrôleur de combat courant, ou null si aucun combat n'est lancé.
     */
    public BattleController getBattleController() {
        return battleController;
    }

    /**
     * Resynchronise la tour utilisée par le service de progression en cas de changement.
     *
     * @param tower nouvelle tour à utiliser pour les combats suivants.
     */
    public void setGameStateServiceTower(TowerNO tower) {
        gameStateService.setTower(tower);
        resetBattleState();
    }

    /**
     * Réinitialise complètement l'état du flux de combat pour éviter les contaminations entre parties.
     */
    private void resetBattleState() {
        this.battleOutcomeApplied = false;
        this.battleStartedFromMap = false;
        this.lastBattleResolutionSummary = "";
    }

    /**
     * Continue la progression linéaire depuis la salle courante.
     * Lance un combat, une récompense ou retourne à la carte selon le type de salle.
     */
    public void handleNextStep() {
        if (mainController.getTower().isTowerCompleted()) {
            resetGameAndShowVictory();
            return;
        }

        switch (mainController.getTower().getCurrentRoomType()) {
            case COMBAT, BOSS -> {
                battleStartedFromMap = false;
                startNewBattle(mainController.getDifficulty());
                createAndShowBattleView();
            }
            case REWARD -> mainController.switchScene(new RewardView(1024, 768, mainController, mainController::showFloorMap));
            default -> mainController.showFloorMap();
        }
    }

    /**
     * Gère l'entrée dans une salle sélectionnée depuis la carte d'étage.
     *
     * @param roomType type de la salle atteinte.
     */
    public void handleMapRoom(RoomType roomType) {
        if (mainController.getTower().isTowerCompleted()) {
            resetGameAndShowVictory();
            return;
        }

        switch (roomType) {
            case COMBAT, BOSS -> {
                battleStartedFromMap = true;
                startNewBattle(mainController.getDifficulty());
                createAndShowBattleView();
            }
            case REWARD -> mainController.switchScene(new RewardView(1024, 768, mainController, mainController::showFloorMap));
            default -> mainController.showFloorMap();
        }
    }

    /**
     * Initialise un nouveau combat avec l'équipe actuelle et la difficulté choisie.
     *
     * @param difficulty difficulté appliquée à la génération de l'équipe adverse.
     */
    public void startNewBattle(Difficulty difficulty) {
        this.battleController = new BattleController(
                teamManagerController.getTeam(),
                teamManagerController.getInventory(),
                difficulty,
                mainController.getActiveRunSkillBonuses());
        // Conserve battleStartedFromMap défini par l'appelant.
        this.battleOutcomeApplied = false;
        this.lastBattleResolutionSummary = "";

        gameStateService.resetForNewBattle();
        gameStateService.setGameModeForCurrentRoom(battleController);
        audioManager.play(mainController.getTower().isBossRoom() ? AudioConfig.BOSS_BATTLE_MUSIC_PATH : AudioConfig.BATTLE_MUSIC_PATH);
    }

    /**
     * Exécute l'action choisie par le joueur et orchestre la réponse ennemie.
     *
     * @param type type d'action demandée.
     * @param targetId identifiant de la cible ou de l'option sélectionnée.
     */
    public void executeAction(MainController.BattleActionType type, String targetId) {
        BattleAction playerAction = switch (type) {
            case ATTACK -> new AttackAction(targetId, battleController.getPlayerActive().getId());
            case USE_ITEM -> new UseItemAction(targetId, teamManagerController.getInventory());
            case SWITCH_BUGEMON -> new SwitchAction(
                    battleController.getPlayerTeam().getBugemonAt(Integer.parseInt(targetId)));
            case SURRENDER -> new SurrenderAction();
        };

        List<BattleAction> turnSequence = battleOrchestrationService.determineTurnSequence(playerAction);
        battleView.getActionMenu().setDisable(true);
        // On fige l'ennemi actif pour savoir si sa riposte reste valide.
        BugemonInstance enemyActiveAtBeginning = battleController.getEnemyActive();
        battleOrchestrationService.executeNextActionInSequence(turnSequence, 0, this::processBattleEnd, enemyActiveAtBeginning);
    }

    /**
     * Force la victoire du joueur puis déclenche le traitement de fin de combat.
     * Utilisé par les raccourcis de test ou de débogage.
     */
    public void forceWinBattle() {
        battleController.forceWin();
        battleView.updateLog("Victoire forcée !");
        battleView.updateUI();
        processBattleEnd();
    }

    /**
     * Applique un choix de montée de niveau et poursuit la navigation si tous les choix sont traités.
     *
     * @param choiceIndex index du bonus sélectionné.
     */
    public void handleLevelUpSelection(int choiceIndex) {
        String result = battleController.applyPendingLevelUpChoice(choiceIndex);
        syncBattleProgress();
        battleView.updateLog(result);
        battleView.updateUI();

        if (battleController.hasPendingLevelUpChoices()) {
            battleView.showLevelUpMenu(
                    battleController.getPendingLevelUpChoiceTitle(),
                    battleController.getPendingLevelUpChoiceDescriptions());
        } else {
            finalizeVictoriousBattleOutcome();
            navigateAfterVictory();
        }
    }

    /**
     * Reprend la navigation après l'écran de victoire d'un boss d'étage.
     */
    public void continueAfterFloorBossVictory() {
        mainController.showFloorMap();
    }

    private void createAndShowBattleView() {
        battleView = new BattleView(1024, 768, mainController);
        battleOrchestrationService = new BattleOrchestrationService(battleController, battleView);
        mainController.switchScene(battleView);
    }

    private void processBattleEnd() {
        if (battleController.playerHasWon()) {
            applyPostCombatSkillBonuses();
            audioManager.play(AudioConfig.WIN_MUSIC_PATH, false);
            // L'XP et les choix de niveau sont préparés avant toute navigation.
            lastBattleResolutionSummary = battleController.resolveVictoryRewards(mainController.getTower().getCurrentFloor());
            syncBattleProgress();
            battleView.updateLog(lastBattleResolutionSummary);
            battleView.updateUI();

            PauseTransition pause = getPauseTransition();
            pause.play();
        } else {
            resetGameAndReturnToMenu();
        }
    }

    private PauseTransition getPauseTransition() {
        PauseTransition pause = new PauseTransition(Duration.seconds(2.0));
        pause.setOnFinished(ev -> {
            if (battleController.hasPendingLevelUpChoices()) {
                battleView.showLevelUpMenu(
                        battleController.getPendingLevelUpChoiceTitle(),
                        battleController.getPendingLevelUpChoiceDescriptions());
            } else {
                finalizeVictoriousBattleOutcome();
                navigateAfterVictory();
            }
        });
        return pause;
    }

    private void finalizeVictoriousBattleOutcome() {
        if (battleOutcomeApplied) {
            return;
        }

        // À capturer avant la progression, car un boss vaincu peut changer l'étage courant.
        boolean bossRoomWon = mainController.getTower().isBossRoom();
        gameStateService.finalizeVictoriousBattleOutcome();

        if (bossRoomWon) {
            teamManagerController.getPlayerProfile().grantSkillPoint();
            mainController.showNotification("1 point de compétence gagné.");
        }

        mainController.persistRunState();
        battleOutcomeApplied = true;
        showPendingTowerNotification();
    }

    private void navigateAfterVictory() {
        if (mainController.getTower().isTowerCompleted()) {
            resetGameAndShowVictory();
            return;
        }

        if (gameStateService.hasFloorBossVictoryToShow()) {
            int floor = gameStateService.consumeFloorBossVictory();
            audioManager.play(AudioConfig.BOSS_WIN_MUSIC_PATH, false);
            mainController.switchScene(new FloorBossVictoryView(mainController, floor));
            return;
        }

        if (battleStartedFromMap) {
            // Les combats lancés depuis la carte y reviennent directement.
            mainController.showFloorMap();
        } else {
            handleNextStep();
        }
    }

    private void resetGameAndReturnToMenu() {
        int floor = mainController.getTower().getCurrentFloor();
        // Une défaite ferme seulement la run active, pas les équipes sauvegardées.
        teamManagerController.resetCurrentTeamProgressToLevelOne();
        teamManagerController.getPlayerProfile().clearActiveRunSlot();
        mainController.setTower(new TowerNO());
        audioManager.play(AudioConfig.LOSE_MUSIC_PATH, false);
        mainController.switchScene(new GameOverView(mainController, floor));
    }

    private void resetGameAndShowVictory() {
        int floor = mainController.getTower().getCurrentFloor();
        mainController.startNewRun(mainController.getDifficulty());
        audioManager.play(AudioConfig.ENDING_MUSIC_PATH);
        mainController.switchScene(new VictoryView(mainController, floor));
    }

    private void syncBattleProgress() {
        teamManagerController.syncBattleProgress(battleController.getPlayerTeam());
    }

    private void applyPostCombatSkillBonuses() {
        skillBonusApplicationService.applyPostCombatRegeneration(
                mainController.getActiveRunSkillBonuses(),
                battleController.getPlayerTeam());
    }

    private void showPendingTowerNotification() {
        String message = mainController.getTower().consumeLastStatusMessage();
        if (message != null && !message.isBlank()) {
            mainController.showNotification(message);
        }
    }
}




