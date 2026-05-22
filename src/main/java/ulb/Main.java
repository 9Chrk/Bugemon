package ulb;

import javafx.application.Application;
import javafx.stage.Stage;
import ulb.controller.MainController;

/**
 * Point d'entrée JavaFX de l'application Bugémon.
 */
public class Main extends Application {

    /**
     * Initialise la fenêtre principale et affiche le menu d'accueil.
     *
     * @param primaryStage fenêtre principale fournie par JavaFX.
     */
    @Override
    public void start(Stage primaryStage) {
        MainController controller = new MainController(primaryStage);

        primaryStage.setTitle("Bugémon");

        controller.showMainMenu();
    }

    /**
     * Lance l'application JavaFX.
     *
     * @param args arguments de ligne de commande.
     */
    public static void main(String[] args) {
        launch(args);
    }
}
