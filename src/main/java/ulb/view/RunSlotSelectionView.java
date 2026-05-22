package ulb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;

import ulb.controller.MainController;
import ulb.models.data.Difficulty;
import ulb.dto.RunSlotDTO;

/**
 * Choix du nom de partie et de l'un des cinq emplacements de sauvegarde.
 */
public class RunSlotSelectionView extends VBox {

    private static final double PANEL_MAX_WIDTH = 560;

    private final MainController controller;
    private final Difficulty difficulty;
    private final TextField runNameField = new TextField();
    private final ToggleGroup slotGroup = new ToggleGroup();

    /**
     * Construit la vue de sélection du nom et de l'emplacement de sauvegarde.
     *
     * @param controller contrôleur principal déclenchant la création de partie.
     * @param teamName   nom de l'équipe choisie.
     * @param difficulty difficulté sélectionnée.
     * @param slots      emplacements de sauvegarde disponibles.
     */
    public RunSlotSelectionView(MainController controller, String teamName, Difficulty difficulty, java.util.List<RunSlotDTO> slots) {
        super(24);
        this.controller = controller;
        this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        this.setPadding(new Insets(36, 24, 36, 24));
        this.setAlignment(Pos.TOP_CENTER);
        this.setFillWidth(true);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.38);");

        // Le chargement de l'équipe est réalisé par la couche navigation/contrôleur.

        Label title = new Label("Nouvelle partie");
        title.setId("manageTitle");
        title.getStyleClass().add("title");

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.getStyleClass().add("run-slot-panel");
        panel.setMaxWidth(PANEL_MAX_WIDTH);

        Label subtitle = new Label("Équipe : " + teamName);
        subtitle.getStyleClass().add("miniTitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(PANEL_MAX_WIDTH - 48);

        Region divider = new Region();
        divider.setMaxWidth(PANEL_MAX_WIDTH - 64);
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: rgba(241, 196, 15, 0.4);");

        Label nameLabel = new Label("Nom de la partie");
        nameLabel.getStyleClass().add("statsText");

        runNameField.setId("runSlotNameField");
        runNameField.setPromptText("Ex. : Ma première ligue");
        runNameField.setMaxWidth(PANEL_MAX_WIDTH - 64);
        runNameField.setPrefWidth(PANEL_MAX_WIDTH - 64);

        Label slotsLabel = new Label("Emplacement de sauvegarde");
        slotsLabel.getStyleClass().add("miniTitle");

        VBox slotList = new VBox(10);
        slotList.setAlignment(Pos.CENTER_LEFT);
        slotList.setMaxWidth(PANEL_MAX_WIDTH - 32);
        slotList.setFillWidth(true);

        RadioButton first = null;
        for (RunSlotDTO slot : slots) {
            RadioButton rb = new RadioButton(slot.displayText());
            rb.setToggleGroup(slotGroup);
            rb.setUserData(slot.index());
            rb.setMaxWidth(Double.MAX_VALUE);
            rb.setWrapText(true);

            // Le style de ligne indique immédiatement si le slot sera écrasé.
            HBox row = new HBox(rb);
            row.setAlignment(Pos.CENTER_LEFT);
            HBox.setHgrow(rb, Priority.ALWAYS);
            row.getStyleClass().add(slot.occupied() ? "run-slot-row-occupied" : "run-slot-row-free");

            slotList.getChildren().add(row);
            if (first == null) {
                first = rb;
            }
        }
        if (first != null) {
            // Pré-sélectionne le premier slot pour éviter un formulaire sans choix.
            first.setSelected(true);
        }

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(12, 0, 0, 0));

        Button start = new Button("Demarrer");
        start.getStyleClass().add("playBtn");
        start.setMinWidth(200);
        start.setOnAction(e -> onStart());

        Button back = new Button("Retour");
        back.getStyleClass().add("backBtn");
        back.setMinWidth(200);
        back.setOnAction(e -> controller.showNewGameMenu());

        actions.getChildren().addAll(start, back);

        panel.getChildren().addAll(
                subtitle,
                divider,
                nameLabel,
                runNameField,
                slotsLabel,
                slotList,
                actions);

        getChildren().addAll(title, panel);
    }

    private void onStart() {
        String name = runNameField.getText() != null ? runNameField.getText().trim() : "";
        if (name.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setTitle("Nom requis");
            a.setContentText("Veuillez entrer un nom pour cette partie.");
            a.showAndWait();
            return;
        }
        var selected = slotGroup.getSelectedToggle();
        if (selected == null) {
            return;
        }
        int slotIndex = (Integer) ((RadioButton) selected).getUserData();
        RunSlotDTO slot = controller.getRunSlots().get(slotIndex);
        if (slot.occupied()) {
            // Écraser un slot est destructif : confirmation explicite.
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Écraser la sauvegarde ?");
            confirm.setHeaderText(null);
            confirm.setContentText("L'emplacement " + (slotIndex + 1) + " contient déjà "
                    + "« " + slot.displayText() + " »"
                    + ". Elle sera définitivement remplacée par une nouvelle partie.");
            Optional<ButtonType> answer = confirm.showAndWait();
            if (answer.isEmpty() || answer.get() != ButtonType.OK) {
                return;
            }
        }
        controller.startNewRunInSlot(slotIndex, name, difficulty);
        controller.showItemSelectMenu(); // If preparation skill then allows to select two items
        //controller.showFloorMap();
    }
}
