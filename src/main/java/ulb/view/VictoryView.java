package ulb.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ulb.controller.MainController;


/**
 * Ecran de victoire affiche lorsque le joueur termine la Tour NO.
 *
 * Propose de demarrer une nouvelle partie ou de revenir au menu principal.
 */
public class VictoryView extends VBox {

    /**
     * Construit l'ecran de victoire.
     *
     * @param controller controleur principal du jeu.
     * @param floor numero de l'etage final atteint.
     */
    public VictoryView(MainController controller, int floor) {
        super(30);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("VICTOIRE");
        title.setId("victory-title");
        title.getStyleClass().add("victory-title");

        Label subtitle = new Label("Vous avez conquis la Tour NO jusqu'a l'etage " + floor + " !");
        subtitle.getStyleClass().add("victory-subtitle");

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

