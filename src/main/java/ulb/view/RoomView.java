package ulb.view;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.DropShadow;
import java.io.InputStream;
import ulb.dto.FloorRoomDTO;
import ulb.models.game.RoomType;

/**
 * Représente un nœud cliquable sur la carte.
 */
public class RoomView extends StackPane {

    private ImageView sprite;
    private final VBox container;

    // Effet de halo doré au survol de la salle.

    private final DropShadow hoverGlow;

    /**
     * Construit une salle de carte à partir de son DTO d'affichage.
     *
     * @param dto données de la salle à afficher.
     */
    public RoomView(FloorRoomDTO dto) {

        this.hoverGlow = new DropShadow();
        this.hoverGlow.setColor(Color.web("#FFD700"));
        this.hoverGlow.setRadius(25);
        this.hoverGlow.setSpread(0.6);

        Label roomNameLabel = loadLabel(dto.type());

        this.container = new VBox(8); // 8px d'espace entre le sprite et le nom de la salle.
        this.container.setMinHeight(140);
        this.container.setMinWidth(100);
        this.container.setAlignment(Pos.CENTER);

        String spritePath = getRoomSpritePath(dto.type());
        if (spritePath != null) {
            this.sprite = loadSprite(spritePath);
            if (this.sprite != null) {
                this.container.getChildren().add(this.sprite);
            }
        }

        this.container.getChildren().add(roomNameLabel);

        this.getChildren().add(this.container);

        this.setCursor(Cursor.HAND);

        applyStatusStyle(dto);
    }

    /**
     * Charge l'image depuis les ressources.
     */
    private ImageView loadSprite(String path) {
        try {
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                return null;
            }
            ImageView iv = new ImageView(new Image(is));
            iv.setFitWidth(100);
            iv.setFitHeight(100);
            iv.setPreserveRatio(true);
            iv.setSmooth(true);
            return iv;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Crée le label
     * 
     * @param type donne un nom au label grâce à getRoomLabel
     */
    private Label loadLabel(RoomType type) {
        Label label = new Label(getRoomLabel(type));
        label.getStyleClass().add("map-label");
        return label;
    }

    // --- Configuration depuis le contrôleur ---

    /**
     * Définit l'action à exécuter lors d'un clic sur la salle.
     */
    public void setOnRoomAction(Runnable action) {
        this.setOnMouseClicked(event -> {
            if (action != null) {
                action.run();
            }
            event.consume(); // Empêche la propagation du clic au parent
        });
    }

    /**
     * Définit l'apparence et les interactions selon l'état de la salle.
     */
    private void applyStatusStyle(FloorRoomDTO dto) {
        if (dto.current()) {
            if (dto.type() != RoomType.START) {
                this.container.setEffect(hoverGlow);
            } else {
                this.container.setEffect(null);
            }
            this.setCursor(Cursor.DEFAULT);

        } else if (dto.available()) {
            // Salle accessible : halo uniquement au survol.
            this.setCursor(Cursor.HAND);

            this.setOnMouseEntered(e -> this.container.setEffect(hoverGlow));

            this.setOnMouseExited(e -> this.container.setEffect(null));

        } else {
            // Salle visitée ou verrouillée : assombrissement, pas de halo.
            ColorAdjust dimmed = new ColorAdjust();
            // -0.3 pour visité (gris léger), -0.7 pour verrouillé (très sombre)
            dimmed.setBrightness(dto.visited() ? -0.3 : -0.7);

            this.container.setEffect(dimmed);
            this.setCursor(Cursor.DEFAULT);
            this.setDisable(!dto.visited());

            // Le survol ne déclenche rien sur une salle indisponible.
            this.setOnMouseEntered(null);
            this.setOnMouseExited(null);
        }
    }

    /**
     * Retourne le libellé français à afficher dans le label d'une salle.
     *
     * @param type type de la salle.
     * @return libellé lisible (ex. "Combat", "Boss", "Bonus").
     */
    private String getRoomLabel(RoomType type) {
        return switch (type) {
            case START -> "Départ";
            case COMBAT -> "Combat";
            case BOSS -> "Boss";
            case REWARD -> "Bonus";
            case END -> "Fin";
        };
    }

    private String getRoomSpritePath(RoomType type) {
        return switch (type) {
            case COMBAT -> ViewConfig.COMBAT_ICON_FLOOR_VIEW;
            case REWARD -> ViewConfig.REWARD_ICON_FLOOR_VIEW;
            case BOSS -> ViewConfig.BOSS_ICON_FLOOR_VIEW;
            default -> null;
        };
    }

    // --- Accesseurs ---

    /**
     * Retourne le sprite affiché pour la salle.
     *
     * @return sprite de la salle.
     */
    public ImageView getSprite() {
        return sprite;
    }
}
