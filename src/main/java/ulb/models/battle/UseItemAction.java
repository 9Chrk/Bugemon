package ulb.models.battle;

import ulb.models.game.Inventory;

/**
 * Action représentant l'utilisation d'un objet par le joueur.
 */
public class UseItemAction implements BattleAction {

    // Données
    private final String itemId;
    private final Inventory inventory;

    // ----------------- Constructeurs -----------------

    /**
     * Construit une action d'utilisation d'objet.
     *
     * @param itemId identifiant de l'objet à utiliser.
     * @param inventory inventaire depuis lequel consommer l'objet.
     */
    public UseItemAction(String itemId, Inventory inventory) {
        if (itemId == null || itemId.isEmpty()) {
            throw new IllegalArgumentException(BattleConfig.ITEM_ID_NULL_OR_EMPTY_ERROR);
        }
        if (inventory == null) {
            throw new IllegalArgumentException(BattleConfig.INVENTORY_NULL_ERROR);
        }
        this.itemId = itemId;
        this.inventory = inventory;
    }

    // ----------------- Méthodes -----------------

    /**
     * Exécute l'utilisation de l'objet sur le combat courant.
     *
     * @param battle combat courant.
     * @param battleService service de résolution du combat.
     * @return journal décrivant l'effet de l'objet.
     */
    @Override
    public String execute(Battle battle, BattleService battleService) {
        if (battle == null || battleService == null) {
            throw new IllegalArgumentException(BattleConfig.BATTLE_AND_SERVICE_NULL_ERROR);
        }

        return battleService.useItem(battle, inventory, itemId);
    }

    /**
     * Indique si cette action consomme le tour.
     *
     * @return true, car utiliser un objet consomme le tour.
     */
    @Override
    public boolean consumesTurn() {
        return true;
    }
}
