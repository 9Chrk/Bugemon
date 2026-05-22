package ulb.view;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;

import java.util.Objects;

/**
 * Popup léger en jeu pour afficher des messages courts au joueur.
 */
public class GameNotificationPopup {
    private static final Duration DEFAULT_DISPLAY_DURATION = Duration.seconds(1.8);

    private final Popup popup;
    private final StackPane container;
    private final Label messageLabel;

    /**
     * Construit le composant de notification.
     */
    public GameNotificationPopup() {
        this.popup = new Popup();
        this.popup.setAutoFix(true);
        this.popup.setAutoHide(false);
        this.popup.setHideOnEscape(false);

        this.messageLabel = new Label();
        this.messageLabel.setId("gameNotificationMessage");
        this.messageLabel.setWrapText(true);
        this.messageLabel.getStyleClass().add("game-popup-text");
        this.messageLabel.setMaxWidth(320);

        this.container = new StackPane(messageLabel);
        this.container.setId("gameNotificationPopup");
        this.container.setPadding(new Insets(14, 18, 14, 18));
        this.container.getStyleClass().add("game-popup");
        this.container.getStylesheets().add(Objects.requireNonNull(getClass().getResource(ViewConfig.CSS_STYLE_PATH)).toExternalForm());

        this.popup.getContent().add(container);
    }

    /**
     * Affiche une notification avec la durée par défaut dans une fenêtre donnée.
     *
     * @param window fenêtre propriétaire.
     * @param message message à afficher.
     */
    public void show(Window window, String message) {
        show(window, message, DEFAULT_DISPLAY_DURATION);
    }

    /**
     * Affiche une notification dans une fenêtre donnée.
     *
     * @param window fenêtre propriétaire.
     * @param message message à afficher.
     * @param displayDuration durée d'affichage.
     */
    public void show(Window window, String message, Duration displayDuration) {
        if (window == null) {
            return;
        }

        messageLabel.setText(message == null ? "" : message);
        container.setOpacity(0);
        popup.hide();
        popup.show(window);

        container.applyCss();
        container.layout();

        double x = window.getX() + (window.getWidth() - container.getWidth()) / 2;
        double y = window.getY() + 40;
        popup.setX(x);
        popup.setY(y);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(180), container);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        PauseTransition stayVisible = new PauseTransition(displayDuration);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(220), container);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(event -> popup.hide());

        new SequentialTransition(fadeIn, stayVisible, fadeOut).play();
    }
}
