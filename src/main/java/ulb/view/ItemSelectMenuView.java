package ulb.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import ulb.controller.MainController;

/**
 * Menu de sélection des objets de départ (Préparation).
 * Repensé pour s'afficher en plein écran en respectant la Direction Artistique du jeu.
 */
public class ItemSelectMenuView extends VBox {

    private final Map<String, Button> itemButtons = new HashMap<>();
    private final Label descriptionLabel;
    private final MainController controller;
    private int selectedItemsCount = 0;
    private static final int MAX_ITEMS = 1;

    /**
     * Construit le menu de préparation au combat
     *
     *  @param itemNames libellés d'objets affichés.
     *  @param itemIds identifiants d'objets associés.
     *  @param controller controleur princopal.
     */
    public ItemSelectMenuView(List<String> itemNames, List<String> itemIds, MainController controller) {
        super(24);
        this.controller = controller;

        this.setPadding(new Insets(50, 30, 40, 30));
        this.setAlignment(Pos.TOP_CENTER);
        this.setFillWidth(true);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("Bonus de Préparation");
        title.getStyleClass().add("title");

        VBox panel = new VBox(20);
        panel.setAlignment(Pos.TOP_CENTER);
        panel.getStyleClass().add("run-slot-panel");
        panel.setMaxWidth(650);
        panel.setPadding(new Insets(30));

        descriptionLabel = new Label("Choisissez 1 objet pour commencer votre aventure.");
        descriptionLabel.getStyleClass().add("miniTitle");
        descriptionLabel.setWrapText(true);

        Region divider = new Region();
        divider.setMaxWidth(550);
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: rgba(241, 196, 15, 0.4);");

        HBox itemsContainer = new HBox(20);
        itemsContainer.setAlignment(Pos.CENTER);
        itemsContainer.setPadding(new Insets(20, 0, 30, 0));

        for (int i = 0; i < itemIds.size(); i++) {
            String id = itemIds.get(i);
            String name = itemNames.get(i);

            Button btn = new Button(name);
            btn.getStyleClass().add("playBtn");
            btn.setMinSize(200, 80);

            btn.setOnAction(e -> {
                if (selectedItemsCount < MAX_ITEMS) {
                    controller.getTeamManagerController().getInventory().addItem(id, 1);
                    selectedItemsCount++;

                    btn.setStyle("-fx-border-color: #F1C40F; -fx-border-width: 2; -fx-opacity: 0.6; -fx-background-color: #2c2c2c;");
                    btn.setDisable(true);

                    descriptionLabel.setText("Objets sélectionnés : " + selectedItemsCount + " / " + MAX_ITEMS);

                    if (selectedItemsCount >= MAX_ITEMS) {
                        itemButtons.values().forEach(b -> b.setDisable(true));
                        descriptionLabel.setText("Sélection terminée ! Vous êtes prêt.");
                    }
                }
            });

            itemButtons.put(id, btn);
            itemsContainer.getChildren().add(btn);
        }

        Button finishBtn = new Button("Commencer l'aventure");
        finishBtn.getStyleClass().add("nextBtn");
        finishBtn.setMinWidth(280);
        finishBtn.setOnAction(e -> controller.showFloorMap());

        panel.getChildren().addAll(descriptionLabel, divider, itemsContainer, finishBtn);
        this.getChildren().addAll(title, panel);
    }

}