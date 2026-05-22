package ulb.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;

/**
 * Barre de vie graphique avec texte des PV courants.
 */
public class HealthBarStackPane extends StackPane {

    private final Rectangle fgBar;
    private final Label hpText;
    private final double maxWidth = 400;

    /**
     * Construit le composant de barre de vie.
     */
    public HealthBarStackPane() {
        this.getStyleClass().add("health-bar-container");
        this.setAlignment(Pos.CENTER_LEFT);
        this.setMaxWidth(maxWidth);

        double height = 30;
        Rectangle bgBar = new Rectangle(maxWidth, height);
        bgBar.getStyleClass().add("health-bar-bg");
        bgBar.setArcWidth(15);
        bgBar.setArcHeight(15);

        fgBar = new Rectangle(maxWidth, height);
        fgBar.getStyleClass().add("health-bar-fg");
        fgBar.setArcWidth(15);
        fgBar.setArcHeight(15);

        hpText = new Label("0 / 0");
        hpText.getStyleClass().add("health-bar-text");

        this.getChildren().addAll(bgBar, fgBar, hpText);
        StackPane.setAlignment(hpText, Pos.CENTER);

        render(100, 100);
    }

    /**
     * Met à jour l'affichage de la barre de vie.
     *
     * @param currentHP PV actuels.
     * @param maxHP PV maximum.
     */
    public void render(double currentHP, double maxHP) {
        double ratio = Math.max(0, Math.min(1, currentHP / maxHP));
        fgBar.setWidth(ratio * maxWidth);
        hpText.setText((int)currentHP + " / " + (int)maxHP);
        fgBar.getStyleClass().removeAll("hp-low", "hp-medium", "hp-high");

        if (ratio < 0.25) {
            fgBar.getStyleClass().add("hp-low");
        } else if (ratio < 0.50) {
            fgBar.getStyleClass().add("hp-medium");
        } else {
            fgBar.getStyleClass().add("hp-high");
        }
    }
}