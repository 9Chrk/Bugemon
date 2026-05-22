package ulb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ulb.controller.MainController;
import ulb.models.data.Difficulty;

/**
 * Vue de démarrage d'une nouvelle partie à partir d'une équipe sauvegardée.
 * Intègre la sélection de difficulté et la liste des équipes.
 */
public class NewGameView extends VBox {

    private static final double PANEL_MAX_WIDTH = 560;

    private final MainController controller;
    private final VBox teamsContainer = new VBox(12);
    private final ToggleGroup difficultyGroup; // Groupe exclusif pour la difficulté sélectionnée.

    /**
     * Construit la vue de démarrage d'une nouvelle partie.
     *
     * @param controller contrôleur principal déclenchant la navigation.
     */
    public NewGameView(MainController controller) {
        super(24);
        this.controller = controller;
        this.setPadding(new Insets(36, 24, 36, 24));
        this.setAlignment(Pos.TOP_CENTER);
        this.setFillWidth(true);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.38);");

        Label manageTitle = new Label("Nouvelle partie");
        manageTitle.setId("manageTitle");
        manageTitle.getStyleClass().add("title");

        VBox panel = new VBox(16);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.getStyleClass().add("run-slot-panel");
        panel.setMaxWidth(PANEL_MAX_WIDTH);
        panel.setMinWidth(PANEL_MAX_WIDTH * 0.85);

        VBox difficultyBox = new VBox(10);
        difficultyBox.setAlignment(Pos.CENTER);
        Label diffLabel = new Label("Choisissez la difficulté :");
        diffLabel.getStyleClass().add("miniTitle");

        difficultyGroup = new ToggleGroup();
        difficultyGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null && oldToggle != null) {
                // Une difficulté doit toujours rester sélectionnée.
                oldToggle.setSelected(true);
            }
        });
        HBox diffButtons = new HBox(12);
        diffButtons.setAlignment(Pos.CENTER);

        // Création des boutons de difficulté.
        ToggleButton easyBtn = createDifficultyButton("Facile", Difficulty.EASY);
        ToggleButton normalBtn = createDifficultyButton("Normal", Difficulty.NORMAL);
        ToggleButton hardBtn = createDifficultyButton("Difficile", Difficulty.HARD);
        normalBtn.setSelected(true); // Difficulté sélectionnée par défaut.

        diffButtons.getChildren().addAll(easyBtn, normalBtn, hardBtn);
        difficultyBox.getChildren().addAll(diffLabel, diffButtons);

        Region divider = new Region();
        divider.setMaxWidth(PANEL_MAX_WIDTH - 64);
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: rgba(241, 196, 15, 0.4);");

        Label sectionTitle = new Label("Choisissez une équipe");
        sectionTitle.getStyleClass().add("miniTitle");

        teamsContainer.setAlignment(Pos.TOP_CENTER);
        teamsContainer.setPadding(new Insets(4, 8, 8, 8));
        teamsContainer.setFillWidth(true);

        ScrollPane teamsScroll = new ScrollPane(teamsContainer);
        teamsScroll.setFitToWidth(true);
        teamsScroll.setPrefViewportHeight(200);
        teamsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        teamsScroll.getStyleClass().addAll("scroll-pane", "new-game-scroll");

        refreshManagementList();

        Button btnBackToMenu = new Button("Retour au menu");
        btnBackToMenu.setId("backToMenuButton");
        btnBackToMenu.setMinWidth(200);
        btnBackToMenu.getStyleClass().add("backBtn");
        btnBackToMenu.setOnAction(e -> controller.showMainMenu());

        panel.getChildren().addAll(difficultyBox, divider, sectionTitle, teamsScroll, btnBackToMenu);

        getChildren().addAll(manageTitle, panel);
    }

    /**
     * Crée un bouton de difficulté relié au groupe exclusif.
     *
     * @param text       libellé du bouton.
     * @param difficulty difficulté associée.
     * @return bouton configuré.
     */
    private ToggleButton createDifficultyButton(String text, Difficulty difficulty) {
        ToggleButton btn = new ToggleButton(text);
        btn.setToggleGroup(difficultyGroup);
        btn.setUserData(difficulty);
        btn.setMinWidth(90);
        btn.getStyleClass().add("playBtn");

        btn.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
            if (isNowSelected) {
                // Gris foncé/anthracite pour le bouton sélectionné.
                btn.setStyle("-fx-background-color: #4A4A4A; -fx-text-fill: white; -fx-border-color: #777777;");
            } else {
                btn.setStyle("");
            }
        });
        return btn;
    }

    private void refreshManagementList() {
        teamsContainer.getChildren().clear();
        for (String teamName : controller.getTeamManagerController().getAllTeamNamesSaved()) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER);
            row.setPadding(new Insets(4, 0, 4, 0));

            // Chaque équipe sauvegardée devient un point d'entrée vers le choix de slot.
            Button playClassic = new Button("Démarrer avec " + teamName);
            playClassic.getStyleClass().add("playBtn");
            playClassic.setMinWidth(260);
            playClassic.setMaxWidth(PANEL_MAX_WIDTH - 80);

            playClassic.setOnAction(e -> {
                Difficulty selectedDiff = (Difficulty) difficultyGroup.getSelectedToggle().getUserData();
                // La difficulté choisie est transmise avant la création effective de la run.
                controller.showRunSlotSelection(teamName, selectedDiff);
            });

            row.getChildren().add(playClassic);
            teamsContainer.getChildren().add(row);
        }
    }
}
