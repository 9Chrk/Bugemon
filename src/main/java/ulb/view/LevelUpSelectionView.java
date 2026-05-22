package ulb.view;

import java.util.List;
import java.util.stream.Collectors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Menu de sélection des bonus de montée de niveau.
 */
public class LevelUpSelectionView extends AbstractSelectionBattleView {

    /**
     * Construit le menu de sélection des bonus de montée de niveau.
     *
     * @param title   titre affiché au-dessus des choix.
     * @param choices descriptions des bonus proposés.
     */
    public LevelUpSelectionView(String title, List<String> choices) {
        super();

        this.getChildren().remove(backButton);
        this.setAlignment(Pos.TOP_CENTER);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("miniTitle");
        titleLabel.setWrapText(true);
        this.add(titleLabel, 0, 0, 2, 1);
        buttonCount = 2;

        for (String choice : choices) {
            addMenuButton(
                    choice,
                    "",
                    24, 24, 30,
                    new Insets(3));
        }
    }

    /**
     * Retourne la liste des boutons de choix affichés.
     *
     * @return liste des boutons de choix.
     */
    public List<Button> getChoiceButtons() {
        return this.getChildren().stream()
                .filter(node -> node instanceof Button)
                .map(node -> (Button) node)
                .collect(Collectors.toList());
    }
}
