package ulb.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

/**
 * Menu de sélection des objets.
 * Utilise le positionnement automatique fourni par la classe parente.
 */

public class ItemSelectionView extends AbstractSelectionBattleView {

    private final Map<String, Button> itemButtons = new HashMap<>();
    private final Label descriptionLabel; // Description de l'objet survolé.

    /**
     * Construit le menu de sélection des objets.
     *
     * @param itemNames libellés d'objets affichés.
     * @param itemIds identifiants d'objets associés.
     */
    public ItemSelectionView(List<String> itemNames, List<String> itemIds) {
        super();

        for (int i = 0; i < itemIds.size(); i++) {
            String id = itemIds.get(i);
            String name = itemNames.get(i);

            Button btn = addMenuButton(
                    name,
                    "",
                    24, 24, 30,
                    new Insets(3));

            itemButtons.put(id, btn);
        }

        descriptionLabel = new Label("Choisissez un objet...");
        descriptionLabel.getStyleClass().add("statsText");
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(Double.MAX_VALUE);

        int descRow = (itemIds.size() + 1) / 2;
        this.add(descriptionLabel, 0, descRow, 2, 1);

        this.add(backButton, 0, descRow + 1, 2, 1);
    }

    /**
     * Retourne les boutons d'objets indexés par identifiant.
     *
     * @return map id d'objet -> bouton.
     */
    public Map<String, Button> getItemButtons() {
        return itemButtons;
    }

    /**
     * Retourne le label de description affiché dans le menu.
     *
     * @return label de description.
     */
    public Label getDescriptionLabel() {
        return descriptionLabel;
    }
}
