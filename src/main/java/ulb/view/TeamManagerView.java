package ulb.view;

import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ulb.controller.MainController;
import ulb.controller.TeamManagerController;
import ulb.dto.BugemonStatsDTO;
import ulb.dto.BugemonSummaryDTO;
import ulb.dto.TeamStateDTO;
import ulb.models.data.Type;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
/**
 * Vue de gestion d'équipe : sélection, aperçu des stats et sauvegardes.
 */
public class TeamManagerView extends GridPane {

    private final MainController controller;
    private final TeamManagerController teamManagerController;

    // Données
    private final double sceneHeight;

    // Widgets de statistiques
    private final Label nameLabel = new Label("Nom : --");
    private final Label hpLabel = new Label("PV : --");
    private final Label atkLabel = new Label("Attaque : --");
    private final Label defLabel = new Label("Défense : --");
    private final Label iniLabel = new Label("Initiative : --");
    private final Label moveLabel = new Label("Attaques : \n - --\n - --");

    // Widgets de composition d'équipe
    private GridPane bugemonsSelection;
    private VBox teamCreationSelected;
    private TextField teamNameField;
    private final Button saveBtn = new Button("Sauvegarder");
    private VBox savedTeamsList;

    // ----------------- Constructeurs -----------------

    /**
     * Construit l'écran de gestion d'équipe.
     *
     * @param height     hauteur de la vue.
     * @param controller contrôleur principal.
     */
    public TeamManagerView(double height, MainController controller) {
        this.controller = controller;
        this.teamManagerController = controller.getTeamManagerController();
        this.sceneHeight = height;

        setupIdsForTestfx();
        applyStyles();

        setupConstraints();
        setupComponents();

        refreshAll();
    }

    private void applyStyles() {
        nameLabel.getStyleClass().add("statsText");
        hpLabel.getStyleClass().add("statsText");
        atkLabel.getStyleClass().add("statsText");
        defLabel.getStyleClass().add("statsText");
        iniLabel.getStyleClass().add("statsText");
        moveLabel.getStyleClass().add("statsText");
        moveLabel.setMaxWidth(200);
        nameLabel.setMaxWidth(200);
        saveBtn.getStyleClass().add("nextBtn");
    }

    /**
     * Rafraîchit tout l'état visuel de la page.
     */
    public void refreshAll() {
        populateBugemonsSelection();
        updateTeamDisplay();
        updateSavedTeamsDisplay();
        validateSaveButton();
    }

    // ----------------- Construction de la mise en page -----------------

    private void setupConstraints() {
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(25);
        col1.setHgrow(Priority.ALWAYS);

        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        col2.setHgrow(Priority.ALWAYS);
        col2.setHalignment(javafx.geometry.HPos.CENTER);

        ColumnConstraints col3 = new ColumnConstraints();
        col3.setPercentWidth(25);
        col3.setHgrow(Priority.ALWAYS);

        this.getColumnConstraints().setAll(col1, col2, col3);
        // Force le GridPane à occuper toute la largeur.
        this.setMaxWidth(Double.MAX_VALUE);
    }

    private void setupComponents() {
        setupLeftComponents();
        setupMiddleComponents();
        setupRightComponents();
    }

    private void setupLeftComponents() {
        VBox leftColumn = new VBox(10);
        leftColumn.setPadding(new Insets(15));
        leftColumn.setPrefHeight(sceneHeight);

        VBox bugemonStats = new VBox(15);
        bugemonStats.setAlignment(Pos.TOP_CENTER);

        Label statsLabel = new Label("Statistiques");
        statsLabel.getStyleClass().add("miniTitle");
        bugemonStats.getChildren().addAll(statsLabel, nameLabel, hpLabel, atkLabel, defLabel, iniLabel, moveLabel);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button backBtn = new Button("Retour");
        backBtn.setId("backButton");
        backBtn.setMinWidth(100);
        backBtn.getStyleClass().add("backBtn");
        backBtn.setOnAction(e -> controller.showMainMenu());

        leftColumn.getChildren().addAll(bugemonStats, spacer, backBtn);
        this.add(leftColumn, 0, 0);
    }

    private void setupMiddleComponents() {
        bugemonsSelection = new GridPane();
        bugemonsSelection.setHgap(10);
        bugemonsSelection.setVgap(10);
        bugemonsSelection.setAlignment(Pos.CENTER);

        this.add(bugemonsSelection, 1, 0);
    }

    private void setupRightComponents() {
        VBox rightCol = new VBox(10);
        rightCol.setPadding(new Insets(10));

        teamCreationSelected = new VBox(5);
        teamNameField = new TextField();
        teamNameField.setPromptText("Nom de l'équipe (obligatoire)...");
        teamNameField.textProperty().addListener((obs, old, nv) -> validateSaveButton());

        saveBtn.setDisable(true);
        saveBtn.setOnAction(e -> {
            controller.handleSaveTeam(teamNameField.getText().trim());
            teamNameField.clear();
        });

        savedTeamsList = new VBox(5);
        ScrollPane scrollTeams = new ScrollPane(savedTeamsList);
        scrollTeams.setFitToWidth(true);
        scrollTeams.setPrefHeight(200);
        scrollTeams.getStyleClass().add("scroll-pane");

        Label teamLabel = new Label("ÉQUIPE");
        teamLabel.getStyleClass().add("miniTitle");

        Label savedTeamsLabel = new Label("SAUVEGARDES");
        savedTeamsLabel.getStyleClass().add("miniTitle");

        Button addBugemonButton= new Button("Ajouter un Bugémon");
        addBugemonButton.getStyleClass().add("playBtn");
        addBugemonButton.setOnAction(e -> controller.showBugemonCreatorMenu());

        rightCol.getChildren().addAll(teamLabel, teamCreationSelected, new Separator(),
                teamNameField, saveBtn, savedTeamsLabel, scrollTeams, addBugemonButton);
        this.add(rightCol, 2, 0);
    }

    // ----------------- Rafraîchissement de l'interface -----------------

    private void populateBugemonsSelection() {
        bugemonsSelection.getChildren().clear();
        List<BugemonSummaryDTO> allBugemons = teamManagerController.getAllBugemonsSummaries();
        int numCols = 4;

        for (int i = 0; i < allBugemons.size(); i++) {
            BugemonSummaryDTO bugemonDTO = allBugemons.get(i);
            boolean isSelected = teamManagerController.getCurrentTeamStateDTO().memberNames().contains(bugemonDTO.id());

            ImageView imgView = new ImageView();
            InputStream is = getClass().getResourceAsStream(bugemonDTO.spritePath());
            try{
                if (is == null) {
                    // Les sprites personnalisés peuvent exister seulement dans target/classes.
                    File file = new File("./target/classes" + bugemonDTO.spritePath());
                    if (file.exists()) {
                        is = new FileInputStream(file);
                    }else{
                        throw new IllegalArgumentException("Resource not found: " + bugemonDTO.spritePath());
                    }
                }
            } catch (Exception e) {
                throw new IllegalStateException("Failed to load JSON resource: " + bugemonDTO.spritePath(), e);
            }
            imgView.setImage(new Image(is));
            imgView.setFitWidth(80);
            imgView.setFitWidth(80);
            imgView.setPreserveRatio(true);

            Button btn = new Button("");
            btn.setMinSize(100, 100);
            btn.setMaxSize(100, 100);
            btn.setPrefSize(100, 100);
            btn.setId("btn-bugemon-" + bugemonDTO.id());

            btn.setGraphic(imgView);
            btn.getStyleClass().add(getTypeStyle(bugemonDTO.stats().type()));

            if (isSelected) {
                // Un Bugémon déjà dans l'équipe reste visible mais non sélectionnable.
                btn.getStyleClass().add("disabledChoiceBtn");
                btn.setDisable(true);
                imgView.setOpacity(0.45);
            }

            btn.setOnMouseEntered(e -> updateStats(bugemonDTO));
            btn.setOnMouseExited(e -> setDefaultStats());
            btn.setOnAction(e -> controller.handleAddBugemon(bugemonDTO.id()));

            bugemonsSelection.add(btn, i % numCols, i / numCols);
        }
    }

    /**
     * Met à jour la zone affichant les membres de l'équipe en cours.
     */
    public void updateTeamDisplay() {
        teamCreationSelected.getChildren().clear();
        TeamStateDTO state = teamManagerController.getCurrentTeamStateDTO();

        for (int i = 0; i < state.maxSize(); i++) {
            String label = "-";
            if (i < state.currentSize()) {
                String bugemonId = state.memberNames().get(i);
                label = teamManagerController.getBugemonDisplayName(bugemonId);
            }

            // Les emplacements vides restent affichés pour matérialiser la taille maximale.
            Button memberBtn = new Button(label);
            memberBtn.setMaxWidth(Double.MAX_VALUE);
            memberBtn.setId("btn-member" + (i + 1));

            if (i < state.currentSize()) {
                String bugemonId = state.memberNames().get(i);
                memberBtn.getStyleClass().add("playBtn");
                memberBtn.setOnAction(e -> controller.handleRemoveBugemon(bugemonId));
            }
            teamCreationSelected.getChildren().add(memberBtn);
        }
    }

    private void updateSavedTeamsDisplay() {
        savedTeamsList.getChildren().clear();
        for (String teamName : teamManagerController.getAllTeamNamesSaved()) {
            HBox row = new HBox(5);
            Button loadBtn = new Button(teamName);
            loadBtn.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(loadBtn, Priority.ALWAYS);
            loadBtn.getStyleClass().add("playBtn");
            loadBtn.setOnAction(e -> {
                // Charger une sauvegarde remplit aussi le champ de nom pour réenregistrer.
                controller.handleLoadTeam(teamName);
                teamNameField.setText(teamName);
            });

            Button deleteBtn = new Button("X");
            deleteBtn.getStyleClass().add("backBtn");
            deleteBtn.setOnAction(e -> controller.handleDeleteTeam(teamName));

            row.getChildren().addAll(loadBtn, deleteBtn);
            savedTeamsList.getChildren().add(row);
        }
    }

    // ----------------- Fonctions d'aide d'état de l'interface -----------------

    private void validateSaveButton() {
        boolean isTeamFull = teamManagerController.getCurrentTeamStateDTO().isFull();
        boolean hasName = !teamNameField.getText().trim().isEmpty();
        saveBtn.setDisable(!(isTeamFull && hasName));
    }

    private void setDefaultStats() {
        nameLabel.setText("Nom : --");
        hpLabel.setText("PV : --");
        atkLabel.setText("Attaque : --");
        defLabel.setText("Défense : --");
        iniLabel.setText("Initiative : --");
        moveLabel.setText("Attaques : \n - --\n - --");
    }

    private void updateStats(BugemonSummaryDTO dto) {
        BugemonStatsDTO stats = dto.stats();
        nameLabel.setText("Nom : " + dto.name() + " (" + stats.type() + ")");
        hpLabel.setText("PV : " + stats.hp());
        atkLabel.setText("Attaque : " + stats.attack());
        defLabel.setText("Défense : " + stats.defense());
        iniLabel.setText("Initiative : " + stats.initiative());
        List<String> moveNames = stats.moves().stream()
                .map(teamManagerController::getAttackDisplayName)
                .toList();
        moveLabel.setText("Attaques : \n - " + String.join("\n - ", moveNames));
    }

    private String getTypeStyle(Type type) {
        return switch (type) {
            case Flora -> "floraStyle";
            case Pyro -> "pyroStyle";
            case Aqua -> "aquaStyle";
            case Litho -> "lithoStyle";
        };
    }

    private void setupIdsForTestfx() {
        nameLabel.setId("nameLabel");
        hpLabel.setId("hpLabel");
        atkLabel.setId("atkLabel");
        defLabel.setId("defLabel");
        iniLabel.setId("iniLabel");
        moveLabel.setId("moveLabel");
    }
}
