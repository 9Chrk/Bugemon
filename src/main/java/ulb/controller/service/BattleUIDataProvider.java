package ulb.controller.service;

import java.util.List;
import java.util.Map;

import ulb.dto.AttackSummaryDTO;
import ulb.models.data.Attack;
import ulb.models.data.ItemDefinition;
import ulb.models.game.BugemonInstance;
import ulb.models.battle.BattleService;

/**
 * Service fournisseur de données pour l'interface utilisateur du combat.
 */
public class BattleUIDataProvider {
    private final Map<String, Attack> attacksById;
    private final BattleService battleService;
    //private final Map<String, ItemDefinition> itemsById;

    /**
     * Construit un fournisseur de données d'interface pour le combat.
     *
     * @param attacksById   attaques indexées par identifiant.
     * @param battleService service de combat utilisé pour les calculs d'efficacité.
     */
    public BattleUIDataProvider(Map<String, Attack> attacksById,
                                BattleService battleService) {
        this.attacksById = attacksById;
        //=======
        //    private final BattleService battleService;
        //

        this.battleService = battleService;
    }
    //public BattleUIDataProvider(Map<String, Attack> attacksById, Map<String, ItemDefinition> itemsById,
    //        BattleService battleService) {
    //    this.attacksById = attacksById;
    //    this.itemsById = itemsById;
    //    this.battleService = battleService;
    //}

    /**
     * Calcule un aperçu de l'efficacité pour chaque attaque disponible.
     *
     * @param attacker Bugémon dont les attaques sont listées.
     * @param defender Bugémon ciblé par les attaques.
     * @return messages d'efficacité dans l'ordre des attaques apprises.
     */
    public List<String> getAttackEffectivenessPreview(BugemonInstance attacker, BugemonInstance defender) {
        return attacker.getLearnedAttackIds().stream()
                .map(attacksById::get)
                // Même calcul que le moteur, mais réduit au message lisible pour la vue.
                .map(attack -> battleService.getDamageCalculator().getEffectivenessMessage(
                        battleService.getDamageCalculator().computeTypeMultiplier(
                                attack.getType(),
                                defender.getSpecies().getType())))
                .toList();
    }


    /**
     * Retourne la description d'un objet.
     */
    //public String getItemDescription(String itemId) {
    //    ItemDefinition item = itemsById.get(itemId);
    //    return item != null ? item.getDescription() : "Description indisponible.";
    //}

    /**
     * Retourne le nom lisible d'une attaque.
     */
    public String getAttackDisplayName(String attackId) {
        Attack attack = attacksById.get(attackId);
        if (attack != null) {
            return attack.getName();
        }
        return attackId == null ? "" : attackId.replace('_', ' ');
    }
}

