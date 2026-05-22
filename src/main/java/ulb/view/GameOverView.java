package ulb.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ulb.controller.MainController;

/**
 * Écran de fin de partie affiché lorsque le joueur perd un combat dans la Tour NO.
 *
 * Présente un message de défaite indiquant l'étage atteint et propose
 * deux options au joueur :
 *     Nouvelle Partie : redirige vers le menu de sélection d'équipe.
 *     Menu Principal : retourne au menu principal du jeu.
 */
public class GameOverView extends VBox {

    /**
     * Construit l'écran de Game Over.
     *
     * @param controller contrôleur principal du jeu.
     * @param floor      numéro de l'étage où le joueur a été vaincu.
     */
    public GameOverView(MainController controller, int floor) {
        super(30);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("GAME OVER");
        title.setId("game-over-title");
        title.getStyleClass().add("game-over-title");

        Label subtitle = new Label("Vos Bugémons ont été vaincus à l'étage " + floor);
        subtitle.getStyleClass().add("game-over-subtitle");

        Button retryBtn = new Button("Nouvelle Partie");
        retryBtn.setMinWidth(240);
        retryBtn.getStyleClass().add("playBtn");
        retryBtn.setOnAction(e -> controller.showNewGameMenu());

        Button menuBtn = new Button("Menu Principal");
        menuBtn.setMinWidth(240);
        menuBtn.getStyleClass().add("backBtn");
        menuBtn.setOnAction(e -> controller.showMainMenu());

        this.getChildren().addAll(title, subtitle, retryBtn, menuBtn);
    }
}
