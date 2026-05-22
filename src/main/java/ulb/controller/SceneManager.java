package ulb.controller;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.geometry.Rectangle2D;
import ulb.view.ViewConfig;

import java.util.Objects;

/**
 * Gère les changements de scène et la configuration globale de l'interface pour l'application.
 * Centralise l'utilisation du Stage afin que les vues ne manipulent pas directement la navigation.
 */
public class SceneManager {
    private final Stage primaryStage;
    private static final double BASE_WIDTH = 1024;
    private static final double BASE_HEIGHT = 768;
    private static final double ASPECT_RATIO = BASE_WIDTH / BASE_HEIGHT;

    private boolean adjustingAspectRatio = false;

    /**
     * Initialise le gestionnaire de scènes avec le stage principal JavaFX.
     *
     * @param stage stage principal de l'application.
     */
    public SceneManager(Stage stage) {
        this.primaryStage = stage;
        // Impose les dimensions minimales de la fenêtre.
        this.primaryStage.setMinWidth(BASE_WIDTH);
        this.primaryStage.setMinHeight(BASE_HEIGHT);

        // Limite la taille max à l'écran visible en conservant le ratio 4:3.
        Rectangle2D visualBounds = Screen.getPrimary().getVisualBounds();
        double maxSceneWidth = Math.min(visualBounds.getWidth(), visualBounds.getHeight() * ASPECT_RATIO);
        double maxSceneHeight = Math.min(visualBounds.getHeight(), visualBounds.getWidth() / ASPECT_RATIO);
        this.primaryStage.setMaxWidth(maxSceneWidth);
        this.primaryStage.setMaxHeight(maxSceneHeight);
    }

    /**
     * Change le nœud racine affiché tout en conservant la Scene existante lorsque c'est possible.
     *
     * @param root nouveau nœud racine à afficher.
     */
    public void switchScene(Parent root) {
        Scene scene = primaryStage.getScene();

        if (scene == null) {
            // Première scène : création complète et installation du verrou de ratio.
            scene = new Scene(root, BASE_WIDTH, BASE_HEIGHT);
            primaryStage.setScene(scene);
            setupAspectRatioLock(scene);
        } else {
            // La vue reste responsable de son propre layout adaptatif.
            scene.setRoot(root);
        }

        applyCSS(scene);

        if (!primaryStage.isShowing()) {
            primaryStage.show();
        }
    }

    private void setupAspectRatioLock(Scene scene) {
        scene.widthProperty().addListener((obs, oldValue, newValue) -> {
            if (adjustingAspectRatio) {
                return;
            }

            adjustingAspectRatio = true;
            try {
                double targetSceneHeight = newValue.doubleValue() / ASPECT_RATIO;
                double decorationHeight = primaryStage.getHeight() - scene.getHeight();
                // On compense les bordures de fenêtre propres à l'OS.
                primaryStage.setHeight(targetSceneHeight + decorationHeight);
            } finally {
                adjustingAspectRatio = false;
            }
        });

        scene.heightProperty().addListener((obs, oldValue, newValue) -> {
            if (adjustingAspectRatio) {
                return;
            }

            adjustingAspectRatio = true;
            try {
                double targetSceneWidth = newValue.doubleValue() * ASPECT_RATIO;
                double decorationWidth = primaryStage.getWidth() - scene.getWidth();
                // Même correction côté largeur pour garder le ratio de la scène.
                primaryStage.setWidth(targetSceneWidth + decorationWidth);
            } finally {
                adjustingAspectRatio = false;
            }
        });
    }

    /**
     * Attache la feuille de style globale à une scène une seule fois.
     *
     * @param scene scène cible.
     */
    private void applyCSS(Scene scene) {
        String css = Objects.requireNonNull(getClass().getResource(ViewConfig.CSS_STYLE_PATH)).toExternalForm();
        // Evite de dupliquer la feuille de style lors des changements de scène.
        if (!scene.getStylesheets().contains(css)) {
            scene.getStylesheets().add(css);
        }
    }
}
