package ulb.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import ulb.dto.BugemonActiveStateDTO;

/**
 * Zone d'affichage d'un Bugémon actif dans l'écran de combat.
 */

public class BugemonZoneBattleView extends GridPane {

    private final HealthBarStackPane hpBar;
    private final Label nameLabel;
    private final ImageView bugemonSprite;
    private final Text damageValueText = new Text();

    /**
     * Construit la zone visuelle d'un Bugémon actif.
     */
    public BugemonZoneBattleView() {
        this.hpBar = new HealthBarStackPane();
        this.nameLabel = new Label();
        this.bugemonSprite = new ImageView();

        this.nameLabel.getStyleClass().add("miniTitle");
        this.nameLabel.setMaxWidth(300);

        initStructureGridPane();
    }

    /**
     * Construit la structure interne de la zone de combat.
     */
    private void initStructureGridPane() {
        RowConstraints rowTop = new RowConstraints();
        rowTop.setPercentHeight(35);
        RowConstraints rowBottom = new RowConstraints();
        rowBottom.setPercentHeight(65);
        this.getRowConstraints().addAll(rowTop, rowBottom);

        ColumnConstraints col = new ColumnConstraints();
        col.setPercentWidth(100);
        this.getColumnConstraints().add(col);

        VBox healthBarArea = new VBox(5);
        healthBarArea.setAlignment(Pos.CENTER_LEFT);
        healthBarArea.setPadding(new Insets(10));
        healthBarArea.getStyleClass().add("health-bar-area");

        healthBarArea.getChildren().addAll(nameLabel, hpBar);

        this.add(healthBarArea, 0, 0);
        this.add(bugemonSprite, 0, 1);
        this.add(damageValueText, 0, 1);

        GridPane.setHalignment(bugemonSprite, HPos.CENTER);
        GridPane.setValignment(bugemonSprite, VPos.CENTER);

        GridPane.setHalignment(damageValueText, HPos.CENTER);
        GridPane.setValignment(damageValueText, VPos.CENTER);
    }

    /**
     * Met à jour l'affichage de la zone à partir d'un DTO d'état actif.
     *
     * @param bugemonState état actif du Bugémon.
     */
    public void updateUI(BugemonActiveStateDTO bugemonState) {
        if (bugemonState == null)
            return;

        hpBar.render(bugemonState.hp(), bugemonState.maxHp());
        nameLabel.setText(bugemonState.name() + " Nv." + bugemonState.level());
        loadSprite(bugemonState.spritePath());
        showDamageTaken(bugemonState.damageTaken());
    }

    /**
     * Charge le sprite associé au Bugémon.
     *
     * @param spritePath chemin du sprite.
     */
    private void loadSprite(String spritePath) {
        InputStream is = getClass().getResourceAsStream(spritePath);
        try{
            if (is == null) {
                File file = new File("./target/classes" + spritePath);
                if (file.exists()) {
                    is = new FileInputStream(file);
                }else{
                    throw new IllegalArgumentException("Resource not found: " + spritePath);
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JSON resource: " + spritePath, e);
        }

        Image img = new Image(is);
        bugemonSprite.setImage(img);
        bugemonSprite.setFitWidth(150);
        bugemonSprite.setFitHeight(150);
        bugemonSprite.setPreserveRatio(true);
        bugemonSprite.setSmooth(true);

    }

    /**
     * Affiche l'animation de dégâts flottants si nécessaire.
     *
     * @param damage quantité de dégâts reçue.
     */
    private void showDamageTaken(int damage) {
        // Affiche une animation flottante de dégâts.

        if (damage > 0) {

            damageValueText.setText("- " + damage);
            TranslateTransition moveUp = new TranslateTransition(Duration.seconds(1),
                    damageValueText);
            moveUp.setByY(-50);
            moveUp.setAutoReverse(true);
            moveUp.setCycleCount(2);
            FadeTransition fade = new FadeTransition(Duration.seconds(1),
                    damageValueText);
            fade.setFromValue(1.0);
            fade.setToValue(0.0);
            ParallelTransition animation = new ParallelTransition(moveUp, fade);
            animation.play();

        }

    }
}
