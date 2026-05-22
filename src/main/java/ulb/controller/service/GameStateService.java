package ulb.controller.service;

import ulb.controller.BattleController;
import ulb.models.game.TowerNO;
import ulb.models.data.GameMode;

/**
 * Gère l'état de progression du jeu : finalisations de combat, récompenses, montées de niveau.
 */
public class GameStateService {
    private TowerNO tower;
    private boolean battleOutcomeApplied = false;
    private Integer lastClearedBossFloor = null;

    /**
     * Construit le service avec la tour à faire progresser.
     *
     * @param tower tour courante du joueur.
     */
    public GameStateService(TowerNO tower) {
        this.tower = tower;
    }

    /**
     * Met à jour la tour utilisée pour la progression et les vérifications d'état.
     *
     * @param tower nouvelle tour courante.
     */
    public void setTower(TowerNO tower) {
        this.tower = tower;
        this.battleOutcomeApplied = false;
        this.lastClearedBossFloor = null;
    }

    /**
     * Réinitialise l'état de progression pour un nouveau combat.
     */
    public void resetForNewBattle() {
        this.battleOutcomeApplied = false;
        this.lastClearedBossFloor = null;
    }

    /**
     * Valide exactement une fois le résultat d'un combat victorieux.
     * Capture l'étage et la salle avant l'avancement pour conserver l'information
     * nécessaire à l'écran de victoire de boss.
     */
    public void finalizeVictoriousBattleOutcome() {
        if (battleOutcomeApplied) {
            return;
        }

        // Ces valeurs sont lues avant que la tour avance.
        boolean bossRoomWon = tower.isBossRoom();
        int clearedFloor = tower.getCurrentFloor();

        tower.processBattleResult(true);

        if (bossRoomWon && !tower.isTowerCompleted()) {
            // Sert à afficher l'écran intermédiaire "boss d'étage vaincu".
            lastClearedBossFloor = clearedFloor;
        }

        battleOutcomeApplied = true;
    }

    /**
     * Indique si un boss d'étage a été remporté et doit afficher son écran de victoire.
     *
     * @return true si un écran de victoire de boss est en attente.
     */
    public boolean hasFloorBossVictoryToShow() {
        return lastClearedBossFloor != null;
    }

    /**
     * Retourne l'étage du boss remporté et le consomme.
     *
     * @return étage terminé par la victoire contre le boss.
     */
    public int consumeFloorBossVictory() {
        int floor = lastClearedBossFloor;
        lastClearedBossFloor = null;
        return floor;
    }

    /**
     * Configure le mode de jeu courant dans le contrôleur de bataille.
     *
     * @param battleController contrôleur de bataille à configurer.
     */
    public void setGameModeForCurrentRoom(BattleController battleController) {
        if (tower.isBossRoom()) {
            // Le mode influence notamment le calcul d'XP.
            battleController.setGameMode(GameMode.BOSS);
        } else {
            battleController.setGameMode(GameMode.NORMAL);
        }
    }
}

