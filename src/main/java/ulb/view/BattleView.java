package ulb.view;

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.control.Button;

import ulb.controller.BattleController;
import ulb.controller.MainController;
import ulb.controller.MainController.BattleActionType;
import ulb.dto.BattleStateDTO;
import ulb.dto.BugemonActiveStateDTO;
import ulb.dto.BugemonDisplayDTO;

/**
 * Vue principale de combat.
 *
 * Cette vue affiche l'état courant du combat et les menus d'actions du joueur.
 */
public class BattleView extends GridPane {

    private BugemonZoneBattleView enemyZone;
    private BugemonZoneBattleView playerZone;
    private GridPane currentActionGrid;
    private Label currentActionLabel;

    private final BattleController battleController;
    private final MainController controller;

    /**
     * Construit la vue de combat.
     *
     * @param width largeur de la vue.
     * @param height hauteur de la vue.
     * @param controller contrôleur principal.
     */
    public BattleView(double width, double height, MainController controller) {
        this.setId("fightScene");
        this.controller = controller;
        this.battleController = controller.getBattleController();

        this.setPrefSize(width, height);
        setupConstraints();
        setupComponents();
        updateUI();
    }

    /**
     * Configure les contraintes de la grille principale.
     */
    private void setupConstraints() {
        ColumnConstraints col50 = new ColumnConstraints();
        col50.setPercentWidth(50);
        this.getColumnConstraints().addAll(col50, col50);

        RowConstraints row50 = new RowConstraints();
        row50.setPercentHeight(50);
        this.getRowConstraints().addAll(row50, row50);
    }

    /**
     * Construit l'ensemble des composants principaux de la vue.
     */
    private void setupComponents() {
        // --- En-tête : progression de la tour ---

        HBox towerProgress = new HBox(40);
        towerProgress.setAlignment(Pos.CENTER);
        towerProgress.setPadding(new Insets(5, 0, 10, 0));

        var progress = controller.getTowerProgress();
        Label floorLabel = new Label(progress.floorNameView());
        String roomName = progress.isBossRoom() ? "BOSS" : progress.roomType().toString();
        // L'affichage garde la notion de salle même si la carte n'est plus linéaire.
        Label roomLabel = new Label(String.format("Salle %d : %s", progress.currentStep() + 1, roomName));

        floorLabel.getStyleClass().add("miniTitle");
        roomLabel.getStyleClass().add("miniTitle");
        if (progress.isBossRoom())
            roomLabel.setStyle("-fx-text-fill: #ff4d4d; -fx-font-weight: bold;");

        towerProgress.getChildren().addAll(floorLabel, roomLabel);

        // --- Panneau gauche : journal ---
        VBox logActions = new VBox(10);
        logActions.setAlignment(Pos.TOP_CENTER);
        currentActionLabel = new Label("Le combat commence !");
        currentActionLabel.setWrapText(true);
        currentActionLabel.getStyleClass().add("statsText");
        logActions.getChildren().addAll(towerProgress, currentActionLabel);
        logActions.getStyleClass().add("actions-area");

        // --- Zones : joueur et ennemi ---

        playerZone = new BugemonZoneBattleView();
        enemyZone = new BugemonZoneBattleView();

        // Bouton invisible de victoire forcée (haut-droite, couleur transparente)
        Button winBtn = new Button("");
        winBtn.setStyle(
            "-fx-background-color: transparent; -fx-border-color: transparent; " +
            "-fx-text-fill: transparent; -fx-cursor: hand; -fx-opacity: 0;"
        );
        winBtn.setPrefSize(30, 30);
        winBtn.setOnAction(e -> controller.forceWinBattle());

        StackPane enemyWrapper = new StackPane(enemyZone, winBtn);
        // Le bouton de debug est superposé à la zone ennemie sans perturber le layout.
        StackPane.setAlignment(winBtn, Pos.TOP_RIGHT);

        this.add(logActions, 0, 0);
        this.add(enemyWrapper, 1, 0);
        this.add(playerZone, 0, 1);

        resetActionMenu();
    }

    /**
     * Met à jour les zones visuelles joueur/ennemi à partir de l'état courant.
     */
    public void updateUI() {
        if (battleController == null)
            return;

        BattleStateDTO state = battleController.getCurrentBattleState();
        playerZone.updateUI(state.player());
        enemyZone.updateUI(state.enemy());
    }

    /**
     * Met à jour le texte du journal de combat.
     *
     * @param log message à afficher.
     */
    public void updateLog(String log) {
        currentActionLabel.setText(log);
    }

    /**
     * Remplace la zone d'action actuellement affichée.
     *
     * @param newMenu nouveau menu à afficher.
     */
    private void setActionZone(GridPane newMenu) {
        if (this.currentActionGrid != null) {
            this.getChildren().remove(this.currentActionGrid);
        }
        this.currentActionGrid = newMenu;
        if (this.currentActionGrid != null) {
            this.currentActionGrid.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            GridPane.setVgrow(this.currentActionGrid, Priority.ALWAYS);
            this.add(this.currentActionGrid, 1, 1);
        }
    }

    // ----------------- Menus de Sélection -----------------

    /**
     * Affiche le menu de sélection des attaques.
     */
    private void showAttackMenu() {
        BugemonActiveStateDTO player = battleController.getCurrentBattleState().player();

        AttackSelectionView attackView = new AttackSelectionView(player.attacks());
        attackView.getBackButton().setOnAction(e -> resetActionMenu());

        attackView.getAttackButtons().forEach((id, btn) -> btn.setOnAction(event -> controller.executeAction(BattleActionType.ATTACK, id)));

        setActionZone(attackView);
    }

    /**
     * Affiche le menu de sélection des objets.
     */
    private void showItemMenu() {
        List<String> labels = battleController.getFormattedItemLabels();

        List<String> ids = new ArrayList<>(controller.getTeamManagerController().getInventory().getItems().keySet());

        ItemSelectionView itemView = new ItemSelectionView(labels, ids);
        itemView.getBackButton().setOnAction(e -> resetActionMenu());

        itemView.getItemButtons().forEach((id, btn) -> {
            btn.setOnMouseEntered(e -> itemView.getDescriptionLabel().setText(battleController.getItemDescription(id)));
            btn.setOnAction(e -> controller.executeAction(BattleActionType.USE_ITEM, id));
        });

        setActionZone(itemView);
    }

    /**
     * Affiche le menu de changement de Bugémon.
     */
    private void showSwitchMenu() {
        List<BugemonDisplayDTO> dtos = battleController.getPlayerTeamDisplayDTOs();
        List<String> names = new ArrayList<>();

        for (BugemonDisplayDTO dto : dtos) {
            names.add(dto.getDisplayText());
        }

        SwitchSelectionView switchView = new SwitchSelectionView(names);
        switchView.getBackButton().setOnAction(e -> resetActionMenu());

        switchView.getBugemonButtons().forEach((index, btn) -> {
            if (battleController.isBugemonDead(index)) {
                btn.setText(names.get(index) + " [KO]");
                btn.setDisable(true);
            } else if (battleController.isBugemonFighting(index)) {
                btn.setText(names.get(index) + " [ACTIF]");
                btn.setDisable(true);
            } else {
                btn.setOnAction(e -> controller.executeAction(BattleActionType.SWITCH_BUGEMON, String.valueOf(index)));
            }
        });

        setActionZone(switchView);
    }

    /**
     * Affiche le menu de choix de bonus de niveau.
     *
     * @param title titre du menu.
     * @param descriptions descriptions des choix disponibles.
     */
    public void showLevelUpMenu(String title, List<String> descriptions) {
        LevelUpSelectionView levelUpView = new LevelUpSelectionView(title, descriptions);
        var buttons = levelUpView.getChoiceButtons();
        for (int i = 0; i < buttons.size(); i++) {
            int index = i;
            buttons.get(i).setOnAction(e -> controller.handleLevelUpSelection(index));
        }
        setActionZone(levelUpView);
    }

    /**
     * Réaffiche le menu principal des actions de combat.
     */
    public void resetActionMenu() {
        ActionSelectBattleView mainMenu = new ActionSelectBattleView();
        mainMenu.getAttackButton().setOnAction(e -> showAttackMenu());
        mainMenu.getItemButton().setOnAction(e -> showItemMenu());
        mainMenu.getSwitchButton().setOnAction(e -> showSwitchMenu());

        mainMenu.getSurrenderButton().setOnAction(e -> controller.executeAction(BattleActionType.SURRENDER, null));

        setActionZone(mainMenu);
    }

    /**
     * Retourne le menu d'action actuellement affiché.
     *
     * @return grille du menu d'action courant.
     */
    public GridPane getActionMenu() {
        return currentActionGrid;
    }
}
