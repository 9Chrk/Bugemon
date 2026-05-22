package ulb.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javafx.geometry.Insets;
import javafx.scene.control.Button;

/**
 * Menu de sélection pour changer de Bugémon.
 * Utilise le positionnement automatique fourni par la classe parente.
 */

public class SwitchSelectionView extends AbstractSelectionBattleView {

    private final Map<Integer, Button> bugemonButtons = new HashMap<>();

    /**
     * Construit le menu de changement de Bugémon.
     *
     * @param teamNames noms affichés des Bugémons de l'équipe.
     */
    public SwitchSelectionView(List<String> teamNames) {
        super();

        for (int i = 0; i < teamNames.size(); i++) {
            String name = teamNames.get(i);

            Button btn = addMenuButton(
                    name,
                    "", // Icône potentielle du Bugémon (non utilisée actuellement).
                    24, 24, 30,
                    new Insets(3));

            bugemonButtons.put(i, btn);
        }

        int rowForBack = (teamNames.size() + 1) / 2;
        this.add(backButton, 0, rowForBack, 2, 1);
    }

    /**
     * Retourne les boutons de changement indexés par position d'équipe.
     *
     * @return map index d'équipe -> bouton.
     */
    public Map<Integer, Button> getBugemonButtons() {
        return bugemonButtons;
    }
}
