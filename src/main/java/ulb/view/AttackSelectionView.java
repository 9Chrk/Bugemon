package ulb.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import ulb.dto.AttackSummaryDTO;
import ulb.models.data.Type;

/**
 * Menu de sélection des attaques.
 * Utilise le positionnement automatique fourni par la classe parente.
 */

public class AttackSelectionView extends AbstractSelectionBattleView {

    private final Map<String, Button> attackButtons = new HashMap<>();

    /**
     * Construit le menu de sélection des attaques.
     *
     * @param attacks attaques à afficher.
     */
    public AttackSelectionView(List<AttackSummaryDTO> attacks) {
        super();

        for (AttackSummaryDTO attack : attacks) {
            Button btn = addMenuButton(
                    formatAttackLabel(attack),
                    ViewConfig.ATTACK_ICON_BUTTON_VIEW,
                    28, 28, 34,
                    new Insets(3));

            attackButtons.put(attack.id(), btn);
        }

        int rowForBack = (attacks.size() + 1) / 2;
        this.add(backButton, 0, rowForBack, 2, 1);

        backButton.setMaxHeight(40);
    }

    /**
     * Formate le libellé d'une attaque pour le bouton.
     *
     * @param attack attaque à afficher.
     * @return libellé affichable.
     */
    private String formatAttackLabel(AttackSummaryDTO attack) {
        return attack.name() + " (" + toDisplay(attack.type()) + ")";
    }

    /**
     * Convertit un type technique en libellé affichable.
     *
     * @param type type à afficher.
     * @return nom lisible du type, ou `_TYPE_` si la valeur est absente.
     */
    private String toDisplay(Type type) {
        if (type == null) {
            return "_TYPE_";
        }
        return switch (type) {
            case Flora -> "Flora";
            case Pyro -> "Pyro";
            case Aqua -> "Aqua";
            case Litho -> "Litho";
        };
    }

    /**
     * Retourne les boutons d'attaque indexés par identifiant.
     *
     * @return map id d'attaque -> bouton.
     */
    public Map<String, Button> getAttackButtons() {
        return attackButtons;
    }
}
