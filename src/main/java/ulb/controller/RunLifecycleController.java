package ulb.controller;

import ulb.models.data.Difficulty;
import ulb.models.game.TowerNO;
import ulb.models.game.TowerNOState;
import ulb.models.skilltree.SkillTreeBonuses;
import ulb.controller.service.SkillBonusApplicationService;

/**
 * Gère la création, le chargement et la continuité des runs.
 */
public class RunLifecycleController {
    private final MainController mainController;
    private final TeamManagerController teamManagerController;
    private final SkillBonusApplicationService skillBonusApplicationService;

    /**
     * Construit le contrôleur du cycle de vie des runs.
     *
     * @param mainController contrôleur principal.
     * @param teamManagerController contrôleur d'équipe.
     * @param skillBonusApplicationService service d'application des bonus de compétences.
     */
    public RunLifecycleController(MainController mainController, TeamManagerController teamManagerController,
            SkillBonusApplicationService skillBonusApplicationService) {
        this.mainController = mainController;
        this.teamManagerController = teamManagerController;
        this.skillBonusApplicationService = skillBonusApplicationService;
    }

    /**
     * Prépare une nouvelle run dans un emplacement donné.
     *
     * @param slotIndex index de l'emplacement.
     * @param runName nom de la run.
     * @param difficulty difficulté choisie.
     */
    public void startNewRunInSlot(int slotIndex, String runName, Difficulty difficulty) {
        String team = teamManagerController.getPlayerProfile().getCurrentTeamName();
        if (team == null || team.isBlank()) {
            mainController.showError("Équipe", "Aucune équipe sélectionnée.");
            return;
        }
        Difficulty resolvedDifficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        // Le slot mémorise le nom de run, l'équipe et la difficulté avant l'initialisation.
        teamManagerController.getPlayerProfile().prepareNewRunAtSlot(slotIndex, runName, team, resolvedDifficulty);
        startNewRun(resolvedDifficulty);
    }

    /**
     * Charge une run depuis un emplacement sauvegardé.
     *
     * @param slotIndex index de l'emplacement.
     * @return true si le chargement a réussi.
     */
    public boolean loadGameFromSlot(int slotIndex) {
        var profile = teamManagerController.getPlayerProfile();
        if (!profile.getRunSlot(slotIndex).isOccupied()) {
            return false;
        }
        profile.setActiveRunSlot(slotIndex);
        String teamName = profile.getRunSlot(slotIndex).getTeamName();
        if (!teamManagerController.loadTeamFromProfile(teamName)) {
            return false;
        }
        // Une ancienne sauvegarde sans difficulté retombe sur le mode normal.
        mainController.setDifficulty(profile.getRunSlot(slotIndex).getDifficulty() != null
                ? profile.getRunSlot(slotIndex).getDifficulty()
                : Difficulty.NORMAL);
        TowerNOState st = profile.getTowerStateForSlot(slotIndex);
        // L'état sérialisé de la tour est reconverti en modèle actif.
        mainController.setTower(st.toTower());
        mainController.setActiveRunSkillBonuses(profile.getSkillTreeProgress().computeBonuses());
        skillBonusApplicationService.applyLoadedRunSkillBonuses(mainController.getActiveRunSkillBonuses());
        return true;
    }

    /**
     * Indique si au moins une run est chargeable.
     *
     * @return true si une sauvegarde de run existe.
     */
    public boolean hasAnyLoadableRun() {
        return teamManagerController.getPlayerProfile().hasAnyLoadableRun();
    }

    /**
     * Reprend automatiquement la run chargeable la plus pertinente.
     */
    public void continueLastRun() {
        var profile = teamManagerController.getPlayerProfile();
        int preferredSlot = profile.getPreferredLoadableRunSlotIndex();
        if (preferredSlot < 0) {
            mainController.showNotification("Aucune partie sauvegardée à continuer.");
            return;
        }

        if (loadGameFromSlot(preferredSlot)) {
            mainController.showFloorMap();
            return;
        }

        // Si le slot préféré est invalide, on tente les autres sauvegardes occupées.
        for (int i = 0; i < ulb.models.game.PlayerProfile.RUN_SLOT_COUNT; i++) {
            if (i == preferredSlot || !profile.getRunSlot(i).isOccupied()) {
                continue;
            }
            if (loadGameFromSlot(i)) {
                mainController.showFloorMap();
                return;
            }
        }
        mainController.showNotification("Impossible de charger la sauvegarde.");
    }

    /**
     * Démarre une nouvelle run fraîche avec la difficulté indiquée.
     *
     * @param difficulty difficulté demandée, ou normal si null.
     */
    public void startNewRun(Difficulty difficulty) {
        Difficulty resolvedDifficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        mainController.setDifficulty(resolvedDifficulty);
        teamManagerController.getPlayerProfile().resetActiveRunToFreshTower();
        teamManagerController.getPlayerProfile().getRunSlot(
                teamManagerController.getPlayerProfile().getActiveRunSlot()).setDifficulty(resolvedDifficulty);
        mainController.setTower(new TowerNO());
        teamManagerController.resetInventoryForNewRun();
        // Les bonus actifs sont recalculés au démarrage pour refléter l'arbre persistant.
        mainController.setActiveRunSkillBonuses(teamManagerController.getPlayerProfile().getSkillTreeProgress().computeBonuses());
        mainController.applyRunStartSkillBonuses();
        mainController.persistRunState();
        String message = teamManagerController.consumeLastPersistenceMessage();
        if (message != null && !message.isBlank()) {
            mainController.showNotification(message);
        }
        mainController.showNotification("Nouvelle partie lancée en mode : " + resolvedDifficulty.name());
    }

    public void startNewRun() {
        startNewRun(Difficulty.NORMAL);
    }
}


