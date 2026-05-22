package ulb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ulb.controller.MainController;
import ulb.dto.RunSlotDTO;

/**
 * Liste des emplacements de partie pour reprendre une sauvegarde.
 */
public class LoadGameView extends VBox {

    private static final double PANEL_MAX_WIDTH = 560;

    /**
     * Construit la vue de chargement des parties sauvegardées.
     *
     * @param controller contrôleur principal déclenchant la reprise d'une partie.
     */
    public LoadGameView(MainController controller) {
        super(24);
        this.setPadding(new Insets(36, 24, 36, 24));
        this.setAlignment(Pos.TOP_CENTER);
        this.setFillWidth(true);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.38);");

        Label title = new Label("Charger une partie");
        title.setId("loadGameTitle");
        title.getStyleClass().add("title");

        VBox panel = new VBox(18);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.getStyleClass().add("run-slot-panel");
        panel.setMaxWidth(PANEL_MAX_WIDTH);

        Label subtitle = new Label("Reprendre une progression sauvegardee");
        subtitle.getStyleClass().add("miniTitle");
        subtitle.setWrapText(true);
        subtitle.setMaxWidth(PANEL_MAX_WIDTH - 48);

        Region divider = new Region();
        divider.setMaxWidth(PANEL_MAX_WIDTH - 64);
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: rgba(241, 196, 15, 0.4);");

        Label slotsLabel = new Label("Emplacements");
        slotsLabel.getStyleClass().add("miniTitle");

        VBox slotList = new VBox(10);
        slotList.setAlignment(Pos.CENTER_LEFT);
        slotList.setMaxWidth(PANEL_MAX_WIDTH - 32);
        slotList.setFillWidth(true);

        for (RunSlotDTO slot : controller.getRunSlots()) {
            if (slot.occupied()) {
                // Un slot occupé affiche son résumé et une action de reprise.
                Label info = new Label(slot.displayText());
                info.getStyleClass().add("statsText");
                info.setWrapText(true);
                info.setMaxWidth(PANEL_MAX_WIDTH - 140);

                Button load = new Button("Continuer");
                load.getStyleClass().add("playBtn");
                load.setMinWidth(120);
                load.setOnAction(e -> {
                    if (controller.loadGameFromSlot(slot.index())) {
                        controller.showFloorMap();
                    }
                });

                HBox row = new HBox(12, info, load);
                row.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(info, Priority.ALWAYS);
                row.getStyleClass().add("run-slot-row-occupied");
                slotList.getChildren().add(row);
            } else {
                // Les slots libres restent visibles pour expliquer l'absence de bouton.
                Label info = new Label(slot.displayText());
                info.getStyleClass().add("statsText");
                info.setWrapText(true);
                info.setMaxWidth(Double.MAX_VALUE);

                HBox row = new HBox(info);
                row.setAlignment(Pos.CENTER_LEFT);
                HBox.setHgrow(info, Priority.ALWAYS);
                row.getStyleClass().add("run-slot-row-free");
                slotList.getChildren().add(row);
            }
        }

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER);
        actions.setPadding(new Insets(12, 0, 0, 0));

        Button back = new Button("Retour au menu");
        back.getStyleClass().add("backBtn");
        back.setMinWidth(200);
        back.setId("loadGameBack");
        back.setOnAction(e -> controller.showMainMenu());

        actions.getChildren().add(back);

        panel.getChildren().addAll(subtitle, divider, slotsLabel, slotList, actions);

        getChildren().addAll(title, panel);
    }

}
