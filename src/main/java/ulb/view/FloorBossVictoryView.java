package ulb.view;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import ulb.controller.MainController;

/**
 * Ecran intermediaire affiche apres la victoire contre un boss d'etage.
 */
public class FloorBossVictoryView extends VBox {

    /**
     * Construit l'ecran de victoire d'etage.
     *
     * @param controller controleur principal.
     * @param clearedFloor etage qui vient d'etre termine.
     */
    public FloorBossVictoryView(MainController controller, int clearedFloor) {
        super(30);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");

        Label title = new Label("VICTOIRE D'ETAGE");
        title.getStyleClass().add("victory-title");

        Label subtitle = new Label("Boss de l'etage " + clearedFloor + " vaincu !");
        subtitle.getStyleClass().add("victory-subtitle");

        Button continueBtn = new Button("Continuer");
        continueBtn.setMinWidth(240);
        continueBtn.getStyleClass().add("playBtn");
        continueBtn.setOnAction(e -> controller.continueAfterFloorBossVictory());

        this.getChildren().addAll(title, subtitle, continueBtn);
    }
}

