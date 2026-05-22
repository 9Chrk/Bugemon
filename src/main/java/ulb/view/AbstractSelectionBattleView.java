package ulb.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;

import java.io.InputStream;
import java.util.Objects;

/**
 * Vue abstraite commune aux sous-menus de combat.
 *
 * Les vues de sélection d'attaque, d'objet et de changement de Bugémon
 * partagent cette structure d'interface.
 */

public abstract class AbstractSelectionBattleView extends GridPane {

    protected final Button backButton;
    protected int buttonCount = 0;

    /**
     * Construit la grille de sélection avec un bouton retour standard.
     */
    public AbstractSelectionBattleView() {
        this.setHgap(10);
        this.setVgap(10);
        this.setPadding(new Insets(15));
        this.setAlignment(Pos.CENTER);

        setupConstraints();

        this.backButton = createButton("RETOUR", "backBtn", ViewConfig.BACK_ICON_BUTTON_VIEW, 14, 14, 18,
                new Insets(7, 3, 1, 3));
    }

    /**
     * Configure les contraintes de colonnes du menu.
     */
    private void setupConstraints() {
        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(50);
        this.getColumnConstraints().addAll(col, col);

    }

    /**
     * Ajoute un bouton de menu dans la grille avec positionnement automatique.
     *
     * @param name       texte du bouton.
     * @param iconPath   chemin de l'icône.
     * @param iconWidth  largeur de l'icône.
     * @param iconHeight hauteur de l'icône.
     * @param boxSize    taille du conteneur d'icône.
     * @param padding    padding du conteneur d'icône.
     * @return bouton créé et ajouté à la grille.
     */
    protected Button addMenuButton(String name, String iconPath,
                                   double iconWidth, double iconHeight,
                                   double boxSize, Insets padding) {

        Button btn = createButton(name, "playBtn", iconPath, iconWidth, iconHeight, boxSize, padding);

        int col = buttonCount % 2;
        int row = buttonCount / 2;

        this.add(btn, col, row);
        buttonCount++;

        return btn;
    }

    /**
     * Crée un bouton de menu stylé.
     *
     * @param name texte du bouton.
     * @param style classe CSS à appliquer.
     * @param iconPath chemin de l'icône.
     * @param iconWidth largeur de l'icône.
     * @param iconHeight hauteur de l'icône.
     * @param boxSize taille de la zone d'icône.
     * @param padding marge intérieure de l'icône.
     * @return bouton créé.
     */
    private Button createButton(String name, String style, String iconPath,
            double iconWidth, double iconHeight,
            double boxSize, Insets padding) {

        Button btn = new Button(name);

        if (style != null && !style.isEmpty()) {
            btn.getStyleClass().add(style);
        }

        btn.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        btn.setMinHeight(40);

        if (!Objects.equals(iconPath, "")) {
            btn.setGraphic(createIconContainer(iconPath, iconWidth, iconHeight, boxSize, padding));
            btn.setContentDisplay(ContentDisplay.LEFT);
        }

        btn.setGraphicTextGap(10);

        return btn;
    }

    /**
     * Crée le conteneur graphique de l'icône.
     *
     * @param path chemin de la ressource.
     * @param w largeur visuelle.
     * @param h hauteur visuelle.
     * @param box taille de la boîte.
     * @param p padding appliqué.
     * @return conteneur d'icône, ou null si la ressource est introuvable.
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

    /**
     * Retourne le bouton retour du sous-menu.
     *
     * @return bouton retour.
     */
    public Button getBackButton() {
        return backButton;
    }
}
