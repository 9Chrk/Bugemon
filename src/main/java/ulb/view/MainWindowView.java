package ulb.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import ulb.controller.MainController;

import java.util.Objects;

/**
 * Vue du menu principal du jeu.
 */
public class MainWindowView extends StackPane {
    private final MainController controller;

    /**
     * Construit le menu principal.
     *
     * @param controller contrôleur principal.
     */
    public MainWindowView(MainController controller) {
        this.controller = controller;
        initialize();
    }

    /**
     * Initialise les composants du menu principal.
     */
    private void initialize() {
        // Overlay sombre pour la lisibilité
        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.45);");
        overlay.setMouseTransparent(true);

        // --- Vidéo d'arrière-plan (fallback si non disponible) ---
        try {
            Media media = new Media(Objects.requireNonNull(getClass().getResource("/images/arrière_plan_accueil.mp4")).toExternalForm());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            mediaPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            mediaPlayer.setMute(true);
            mediaPlayer.setAutoPlay(true);

            MediaView mediaView = new MediaView(mediaPlayer);
            mediaView.setPreserveRatio(false);
            mediaView.fitWidthProperty().bind(this.widthProperty());
            mediaView.fitHeightProperty().bind(this.heightProperty());

            this.getChildren().addAll(mediaView, overlay);
        } catch (Exception e) {
            // Vidéo non supportée (ex: CI sans GStreamer) — fond statique
            this.setStyle("-fx-background-color: #1a1a2e;");
            this.getChildren().add(overlay);
        }

        // --- Contenu du menu ---
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);

        Label titleName = new Label("Bugémon");
        titleName.setId("main-title");
        titleName.getStyleClass().add("title");

        Button continueGame = new Button("Continuer");
        continueGame.setMinWidth(320);
        continueGame.setId("continueGame");
        continueGame.getStyleClass().add("playBtn");
        // Le contrôleur renvoie true quand aucun run ne peut être chargé.
        continueGame.setDisable(controller.hasAnyLoadableRun());
        continueGame.setOnAction(e -> controller.continueLastRun());

        Button newGame = new Button("Nouvelle Partie");
        newGame.setMinWidth(320);
        newGame.setId("newGame");
        newGame.getStyleClass().add("nextBtn");
        newGame.setOnAction(e -> controller.showNewGameMenu());

        Button loadGame = new Button("Charger une partie");
        loadGame.setMinWidth(320);
        loadGame.setId("loadGame");
        loadGame.getStyleClass().add("nextBtn");
        loadGame.setOnAction(e -> controller.showLoadGameMenu());
        // Même règle d'activation que le bouton Continuer.
        loadGame.setDisable(controller.hasAnyLoadableRun());

        Button manageTeam = new Button("Gérer équipe");
        manageTeam.setMinWidth(320);
        manageTeam.getStyleClass().add("nextBtn");
        manageTeam.setId("manageTeam");
        manageTeam.setOnAction(e -> controller.showTeamManagement());

        Button skillTree = new Button("Arbre de competences");
        skillTree.setMinWidth(320);
        skillTree.getStyleClass().add("nextBtn");
        skillTree.setId("skillTree");
        skillTree.setOnAction(e -> controller.showSkillTreeMenu());

        Button exit = new Button("Quitter");
        exit.setMinWidth(320);
        exit.setId("Exit");
        exit.getStyleClass().add("backBtn");
        exit.setOnAction(e -> {
            controller.shutdownAudio();
            if (getScene() != null && getScene().getWindow() != null) {
                getScene().getWindow().hide();
            }
        });

        content.getChildren().addAll(titleName, continueGame, newGame, loadGame, manageTeam, skillTree, exit);

        this.getChildren().add(content);
    }
}
