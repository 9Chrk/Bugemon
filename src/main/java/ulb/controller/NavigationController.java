package ulb.controller;

import ulb.audio.AudioManager;
import ulb.models.data.Difficulty;
import ulb.view.BugemonCreatorView;
import ulb.view.FloorMapView;
import ulb.view.LoadGameView;
import ulb.view.MainWindowView;
import ulb.view.NewGameView;
import ulb.view.RunSlotSelectionView;
import ulb.view.SkillTreeView;
import ulb.view.TeamManagerView;
import ulb.view.ItemSelectMenuView;
import ulb.dto.SkillTreeStateDTO;
import ulb.dto.SkillTreeNodeDTO;
import java.util.List;

/**
 * Centralise la navigation entre les scènes et les vues de l'application.
 */
public class NavigationController {
    private final MainController mainController;
    private final SceneManager sceneManager;
    private final TeamManagerController teamManagerController;
    private final AudioManager audioManager;

    /**
     * Construit le contrôleur de navigation.
     *
     * @param mainController contrôleur principal.
     * @param sceneManager gestionnaire de scènes.
     * @param teamManagerController contrôleur de l'équipe.
     * @param audioManager gestionnaire audio.
     */
    public NavigationController(MainController mainController, SceneManager sceneManager,
            TeamManagerController teamManagerController, AudioManager audioManager) {
        this.mainController = mainController;
        this.sceneManager = sceneManager;
        this.teamManagerController = teamManagerController;
        this.audioManager = audioManager;
    }

    /**
     * Affiche le menu principal.
     */
    public void showMainMenu() {
        audioManager.play(AudioConfig.MENU_MUSIC_PATH);
        sceneManager.switchScene(new MainWindowView(mainController));
        mainController.showPendingPersistenceNotification();
    }

    /**
     * Affiche le menu de nouvelle partie.
     */
    public void showNewGameMenu() {
        if (!teamManagerController.hasAvailableSavedTeams()) {
            mainController.showNotification("Créez ou sauvegardez une équipe dans « Gérer équipe » pour lancer une nouvelle partie.");
            // Sans équipe persistée, la nouvelle partie ne peut pas choisir de base valide.
            showTeamManagement();
            return;
        }
        audioManager.play(AudioConfig.MENU_MUSIC_PATH);
        sceneManager.switchScene(new NewGameView(mainController));
    }

    /**
     * Affiche la sélection d'emplacement pour une nouvelle run.
     *
     * @param teamName nom de l'équipe choisie.
     * @param difficulty difficulté sélectionnée.
     */
    public void showRunSlotSelection(String teamName, Difficulty difficulty) {
        // Charge les données d'équipe avant de créer la vue.
        teamManagerController.loadTeamFromProfile(teamName);
        var slots = mainController.getRunSlots();
        // La vue reçoit un instantané des slots pour éviter de relire le profil.
        sceneManager.switchScene(new RunSlotSelectionView(mainController, teamName, difficulty, slots));
    }

    /**
     * Affiche le menu de chargement des parties.
     */
        public void showRunSlotSelection(String teamName) {
            showRunSlotSelection(teamName, Difficulty.NORMAL);
        }
    public void showLoadGameMenu() {
        sceneManager.switchScene(new LoadGameView(mainController));
    }

    /**
     * Affiche le menu de création de Bugémon.
     */
    public void showBugemonCreatorMenu() {
        audioManager.play(AudioConfig.MENU_MUSIC_PATH);
        sceneManager.switchScene(new BugemonCreatorView(mainController));
    }

    /**
     * Affiche la gestion d'équipe.
     */
    public void showTeamManagement() {
        teamManagerController.startNewTeamSelection();
        TeamManagerView currentTeamView = new TeamManagerView(1024, mainController);
        // MainController garde une référence pour rafraîchir la vue après certaines actions.
        mainController.setCurrentTeamView(currentTeamView);
        audioManager.play(AudioConfig.MENU_MUSIC_PATH);
        sceneManager.switchScene(currentTeamView);
        mainController.showPendingPersistenceNotification();
    }


    public void showItemSelectMenu() {
        SkillTreeStateDTO skillState = mainController.getSkillTreeState();
        boolean preparationActive = false;
        for (SkillTreeNodeDTO node : skillState.nodes()) {
            if ("objets_depart".equals(node.id()) && node.active()) {
                preparationActive = true;
                break;
            }
        }
        if ( preparationActive ){
            List<String> itemNames= List.of("Baie Revigorante", "Baie Tonique");
            List<String> itemIDs= List.of("baie_revigorante", "baie_tonique");
            sceneManager.switchScene(new ItemSelectMenuView(itemNames,itemIDs, mainController));
        }else{
            sceneManager.switchScene(new FloorMapView(1024, 768, mainController));
        }
    }

    /**
     * Affiche l'arbre de compétences.
     */
    public void showSkillTreeMenu() {
        sceneManager.switchScene(new SkillTreeView(1024, 768, mainController));
        mainController.showPendingPersistenceNotification();
    }

    /**
     * Affiche la carte de l'étage courant.
     */
    public void showFloorMap() {
        audioManager.play(AudioConfig.LEVEL_SELECT_MUSIC_PATH);
        sceneManager.switchScene(new FloorMapView(1024, 768, mainController));
    }
}

