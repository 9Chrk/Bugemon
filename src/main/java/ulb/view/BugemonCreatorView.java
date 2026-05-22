package ulb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import ulb.controller.MainController;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Vue de création d'un nouveau Bugémon personnalisé.
 */
public class BugemonCreatorView extends VBox {
    private final MainController controller;
    private final int TOTAL_BUDGET = 250;

    // Éléments d'identité
    private TextField bugemonNameField;
    private ComboBox<String> typeBox;
    private ImageView imagePreview;
    private File selectedImageFile;

    // Éléments d'attaques
    private VBox attacksContainer;
    private final List<CheckBox> attackCheckBoxes = new ArrayList<>();

    // Éléments de statistiques
    private Slider pvSlider, atkSlider, defSlider, iniSlider;
    private Label pvVal, atkVal, defVal, iniVal;
    private Label budgetLabel;

    private Button btnSave;

    /**
     * Construit l'écran de création de Bugémon.
     *
     * @param controller contrôleur principal.
     */
    public BugemonCreatorView(MainController controller) {
        super(15); // Espacement global réduit pour gagner de la place
        this.controller = controller;
        this.setPadding(new Insets(20, 30, 20, 30));
        this.setAlignment(Pos.TOP_CENTER);
        initialize();
    }

    /**
     * Construit les composants principaux de l'écran de création.
     */
    private void initialize() {
        // --- TITRE ---
        Label title = new Label("Créer votre Bugémon");
        title.getStyleClass().add("title");

        // --- SECTION CENTRALE ---
        HBox middleContainer = new HBox(40);
        middleContainer.setAlignment(Pos.CENTER);
        middleContainer.setPadding(new Insets(10, 0, 10, 0));

        // BLOC GAUCHE : Identité
        VBox leftBlock = new VBox(10);
        leftBlock.setAlignment(Pos.CENTER);

        bugemonNameField = new TextField();
        bugemonNameField.setPromptText("Nom du Bugémon...");
        bugemonNameField.setPrefWidth(250);

        bugemonNameField.textProperty().addListener((obs, oldVal, newVal) -> refreshSaveButtonState());

        typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Flora", "Pyro", "Aqua", "Litho");
        typeBox.setPromptText("Choisir un type...");
        typeBox.setPrefWidth(250);
        // Les attaques proposées dépendent directement du type choisi.
        typeBox.setOnAction(e -> refreshAttackList(typeBox.getValue()));

        StackPane imageContainer = new StackPane();
        imageContainer.setPrefSize(130, 130);
        imageContainer.setMaxSize(130, 130);

        // Style du cadre : gris, bordure pointillée et fond légèrement transparent.
        imageContainer.setStyle("-fx-border-color: #aaaaaa; -fx-border-style: dashed; " +
                "-fx-border-width: 2; -fx-background-color: rgba(0,0,0,0.3);");

        imagePreview = new ImageView();
        imagePreview.setFitWidth(120);
        imagePreview.setFitHeight(120);
        imagePreview.setPreserveRatio(true);
        imageContainer.getChildren().add(imagePreview);

        Button btnImportImage = new Button("Importer image");
        btnImportImage.getStyleClass().add("nextBtn");
        btnImportImage.setOnAction(e -> handleImportImage());

        leftBlock.getChildren().addAll(bugemonNameField, typeBox, imageContainer, btnImportImage);

        // BLOC DROIT
        VBox rightBlock = new VBox(10);
        rightBlock.setAlignment(Pos.TOP_CENTER);
        rightBlock.setPrefWidth(300);

        Label attacksTitle = new Label("Attaques (max 3) :");
        attacksTitle.getStyleClass().add("statsText");

        attacksContainer = new VBox(5);
        attacksContainer.setAlignment(Pos.TOP_LEFT);
        attacksContainer.setPadding(new Insets(10));

        ScrollPane scrollAttacks = new ScrollPane(attacksContainer);
        scrollAttacks.setFitToWidth(true);
        scrollAttacks.setPrefHeight(180); // Hauteur fixe pour éviter que ça pousse les boutons
        scrollAttacks.getStyleClass().add("actions-area");
        scrollAttacks.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        rightBlock.getChildren().addAll(attacksTitle, scrollAttacks);

        middleContainer.getChildren().addAll(leftBlock, rightBlock);

        // --- SECTION STATISTIQUES ---
        VBox statsFormContainer = new VBox(5);
        statsFormContainer.setAlignment(Pos.CENTER);
        statsFormContainer.setMaxWidth(700);
        statsFormContainer.setPadding(new Insets(15));
        statsFormContainer.getStyleClass().add("actions-area");

        budgetLabel = new Label("Points restants : " + TOTAL_BUDGET);
        budgetLabel.getStyleClass().add("statsText");

        pvSlider = createConfiguredSlider(75, 150, 100);
        atkSlider = createConfiguredSlider(40, 85, 60);
        defSlider = createConfiguredSlider(20, 75, 50);
        iniSlider = createConfiguredSlider(20, 65, 45);

        pvVal = new Label("100");
        atkVal = new Label("60");
        defVal = new Label("50");
        iniVal = new Label("45");

        // Les sliders partagent un budget global de statistiques.
        setupLogic();

        statsFormContainer.getChildren().addAll(
                budgetLabel,
                createStatRow("PV", pvSlider, pvVal),
                createStatRow("ATK", atkSlider, atkVal),
                createStatRow("DEF", defSlider, defVal),
                createStatRow("INI", iniSlider, iniVal)
        );

        // --- BOUTONS D'ACTION ---
        HBox actionButtons = new HBox(20);
        actionButtons.setAlignment(Pos.CENTER);

        btnSave = new Button("Enregistrer");
        btnSave.getStyleClass().add("playBtn");
        btnSave.setMinWidth(200);
        btnSave.setDisable(true);
        btnSave.setOnAction(e -> handleSaveBugemon());

        Button btnBack = new Button("Retour");
        btnBack.getStyleClass().add("backBtn");
        btnBack.setMinWidth(200);
        btnBack.setOnAction(e -> controller.showMainMenu());

        actionButtons.getChildren().addAll(btnBack, btnSave);

        this.getChildren().addAll(title, middleContainer, statsFormContainer, actionButtons);
        updateBudget();
    }

    private void refreshAttackList(String type) {
        attacksContainer.getChildren().clear();
        attackCheckBoxes.clear();

        if (type == null) return;

        List<String> attacks = controller.getAttackNamesByType(type);

        for (String name : attacks) {
            CheckBox cb = new CheckBox(name);
            cb.getStyleClass().add("statsText");
            cb.setMinWidth(200);

            cb.setOnAction(e -> {
                long selectedCount = attackCheckBoxes.stream().filter(CheckBox::isSelected).count();
                if (selectedCount > 3) {
                    // La quatrième attaque est refusée immédiatement côté interface.
                    cb.setSelected(false);
                }
                refreshSaveButtonState();
            });

            attackCheckBoxes.add(cb);
            attacksContainer.getChildren().add(cb);
        }
    }

    private void refreshSaveButtonState() {
        List<String> selectedMoves = attackCheckBoxes.stream()
                .filter(CheckBox::isSelected)
                .map(CheckBox::getText)
                .collect(Collectors.toList());

        boolean isValid = controller.isBugemonCreationValid(
                bugemonNameField.getText(),
                selectedImageFile,
                selectedMoves
        );

        btnSave.setDisable(!isValid);
    }

    /**
     * Ouvre un sélecteur de fichier pour choisir une image.
     */
    private void handleImportImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choisir l'image du Bugemon");

        fileChooser.getExtensionFilters().addAll(
                                                 new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.jpeg")
                                                 );
        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            imagePreview.setImage(new Image(selectedFile.toURI().toString()));
            this.selectedImageFile = selectedFile;
            refreshSaveButtonState();
        }
    }

    private void handleSaveBugemon() {
        List<String> selectedMoves = attackCheckBoxes.stream()
            .filter(CheckBox::isSelected)
            .map(Labeled::getText)
            .collect(Collectors.toList());
        controller.saveCustomBugemon(bugemonNameField.getText(),
                                     typeBox.getValue(),
                                     (int)pvSlider.getValue(),
                                     (int)atkSlider.getValue(),
                                     (int)defSlider.getValue(),
                                     (int)iniSlider.getValue(),
                                     selectedMoves,
                                     selectedImageFile.toString()
                                     );
    }

    /**
     * Construit une ligne de formulaire pour une statistique.
     */
    private HBox createStatRow(String labelName, Slider slider, Label valueLabel) {
        HBox row = new HBox(15);
        row.setAlignment(Pos.CENTER_LEFT);
        Label lbl = new Label(labelName);
        lbl.setMinWidth(100);
        lbl.getStyleClass().add("statsText");
        valueLabel.getStyleClass().add("statsText");
        valueLabel.setMinWidth(30);
        slider.setMinWidth(350);
        row.getChildren().addAll(lbl, slider, valueLabel);
        return row;
    }

    /**
     * Construit un slider avec configuration standard.
     */
    private Slider createConfiguredSlider(int min, int max, int start) {
        Slider s = new Slider(min, max, start);
        s.setBlockIncrement(1);
        return s;
    }

    /**
     * Raccorde la logique de budget entre les différents sliders.
     */
    private void setupLogic() {
        List<Slider> allSliders = List.of(pvSlider, atkSlider, defSlider, iniSlider);
        for (Slider current : allSliders) {
            current.valueProperty().addListener((obs, oldVal, newVal) -> {
                int intValue = newVal.intValue();
                if (newVal.doubleValue() != intValue) {
                    current.setValue(intValue);
                }

                updateLabels();

                double total = allSliders.stream().mapToDouble(Slider::getValue).sum();

                if (total > TOTAL_BUDGET) {
                    int excess = (int)total - TOTAL_BUDGET;
                    List<Slider> others = allSliders.stream()
                            .filter(s -> s != current)
                            .toList();

                    for (Slider other : others) {
                        if (excess <= 0) break;

                        int currentOtherVal = (int)other.getValue();
                        if (currentOtherVal > other.getMin()) {
                            other.setValue(currentOtherVal - 1);
                            excess--;
                        }
                    }
                }
                updateBudget();
            });
        }
    }

    private void updateBudget() {
        int total = (int) (pvSlider.getValue() + atkSlider.getValue() + defSlider.getValue() + iniSlider.getValue());
        budgetLabel.setText("Points restants : " + Math.max(0, TOTAL_BUDGET - total));
    }

    private void updateLabels() {
        pvVal.setText(String.valueOf((int) pvSlider.getValue()));
        atkVal.setText(String.valueOf((int) atkSlider.getValue()));
        defVal.setText(String.valueOf((int) defSlider.getValue()));
        iniVal.setText(String.valueOf((int) iniSlider.getValue()));
    }
}
