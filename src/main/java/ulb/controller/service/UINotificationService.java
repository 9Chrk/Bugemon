package ulb.controller.service;

import javafx.scene.control.Alert;
import javafx.stage.Stage;
import ulb.view.GameNotificationPopup;

/**
 * Service d'affichage des notifications et popups au joueur.
 */
public class UINotificationService {
    private final GameNotificationPopup notificationPopup = new GameNotificationPopup();
    private final Stage primaryStage;

    /**
     * Construit le service avec la fenêtre principale comme point d'ancrage.
     *
     * @param primaryStage fenêtre principale de l'application.
     */
    public UINotificationService(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    /**
     * Affiche une notification brève non bloquante.
     *
     * @param message message à afficher.
     */
    public void showNotification(String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        notificationPopup.show(primaryStage, message);
    }

    /**
     * Affiche une boîte de dialogue d'erreur.
     *
     * @param title   titre de la boîte de dialogue.
     * @param content contenu détaillé de l'erreur.
     */
    public void showError(String title, String content) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle(title);
        a.setContentText(content);
        a.showAndWait();
    }
}

