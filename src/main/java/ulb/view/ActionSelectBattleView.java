package ulb.view;

import java.io.InputStream;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.StackPane;
import javafx.scene.control.ContentDisplay;

/**
 * Menu principal de sélection d'action sur l'écran de combat.
 *
 * Cette vue n'affiche pas de bouton "Retour" car elle représente le menu racine.
 */
public class ActionSelectBattleView extends GridPane {

    private Button attackButton;
    private Button itemButton;
    private Button switchButton;
    private Button surrenderButton;

    /**
     * Construit le menu de sélection des actions de combat.
     */
    public ActionSelectBattleView() {

        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(15));
        this.setAlignment(Pos.CENTER);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        this.getColumnConstraints().addAll(col, col);

        RowConstraints row = new RowConstraints();
        row.setPercentHeight(50);
        this.getRowConstraints().addAll(row, row);

        initButtons();
    }

    /**
     * Crée les quatre boutons principaux du menu.
     */
    private void initButtons() {

        attackButton = createButton("ATTAQUER", "playBtn", ViewConfig.ATTACK_ICON_BUTTON_VIEW, 28, 28, 34, new Insets(3));
        itemButton = createButton("SAC", "nextBtn", ViewConfig.ITEM_ICON_BUTTON_VIEW, 28, 28, 34, new Insets(3));
        switchButton = createButton("CHANGER", "playBtn", ViewConfig.SWITCH_ICON_BUTTON_VIEW, 28, 28, 34, new Insets(3));
        surrenderButton = createButton("ABANDONNER", "backBtn", ViewConfig.SURRENDER_ICON_BUTTON_VIEW, 14, 14, 18,
                new Insets(7, 3, 1, 3));

        this.add(attackButton, 0, 0);
        this.add(itemButton, 1, 0);
        this.add(switchButton, 0, 1);
        this.add(surrenderButton, 1, 1);

        this.setAlignment(Pos.CENTER);
    }

    /**
     * Crée un bouton stylé avec icône si nécessaire.
     */
    private Button createButton(String name, String style, String iconPath,
            double iconWidth, double iconHeight,
            double boxSize, Insets padding) {

        Button btn = new Button(name);

        if (style != null && !style.isEmpty()) {
            btn.getStyleClass().add(style);
        }

        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setPrefHeight(60);
        btn.setMaxHeight(60);

        if (!Objects.equals(iconPath, "")) {
            btn.setGraphic(createIconContainer(iconPath, iconWidth, iconHeight, boxSize, padding));
            btn.setContentDisplay(ContentDisplay.LEFT);

        }

        btn.setGraphicTextGap(10);

        return btn;
    }

    /**
     * Crée le conteneur graphique de l'icône.
     */
    private StackPane createIconContainer(String path, double w, double h, double box, Insets p) {
        InputStream is = getClass().getResourceAsStream(path);
        if (is == null)
            return null;

        ImageView icon = new ImageView(new Image(is));
        icon.setFitWidth(w);
        icon.setFitHeight(h);
        icon.setPreserveRatio(true);
        icon.setSmooth(true);

        StackPane container = new StackPane(icon);
        container.setMinSize(box, box);
        container.setPrefSize(box, box);
        container.setMaxSize(box, box);
        container.setPadding(p);

        return container;
    }

    // ----------------- Accesseurs -----------------
    // Ces accesseurs permettent au contrôleur d'attacher les actions aux boutons.

    /**
     * Retourne le bouton d'attaque.
     *
     * @return bouton d'attaque.
     */
    public Button getAttackButton() {
        return attackButton;
    }

    /**
     * Retourne le bouton d'ouverture du sac.
     *
     * @return bouton du sac.
     */
    public Button getItemButton() {
        return itemButton;
    }

    /**
     * Retourne le bouton de changement de Bugémon.
     *
     * @return bouton de changement.
     */
    public Button getSwitchButton() {
        return switchButton;
    }

    /**
     * Retourne le bouton d'abandon du combat.
     *
     * @return bouton d'abandon.
     */
    public Button getSurrenderButton() {
        return surrenderButton;
    }
}
