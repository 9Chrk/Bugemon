package ulb.models.battle;


/**
 * Action représentant l'abandon du joueur.
 */
public class SurrenderAction implements BattleAction {

    // Méthodes

    /**
     * Exécute l'abandon et termine le combat.
     *
     * @param battle combat courant.
     * @param battleService service de résolution du combat.
     * @return message d'abandon.
     */
    @Override
    public String execute(Battle battle, BattleService battleService) {
        if (battle == null || battleService == null) {
            throw new IllegalArgumentException(BattleConfig.BATTLE_SERVICE_NULL_ERROR);
        }
        battleService.surrender(battle);
        return BattleConfig.SURRENDER_MESSAGE;
    }

    /**
     * Indique si cette action consomme le tour.
     *
     * @return false, car l'abandon termine directement le combat.
     */
    @Override
    public boolean consumesTurn() {
        return false;
    }
}
