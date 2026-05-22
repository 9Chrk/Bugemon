package ulb.models.battle;

import ulb.models.game.BugemonInstance;


/**
 * Action représentant un changement de Bugémon côté joueur.
 *
 * @param newBugemon Bugémon à envoyer au combat.
 */
public record SwitchAction(BugemonInstance newBugemon) implements BattleAction {
    // Constructeurs

    /**
     * Construit une action de changement de Bugémon.
     *
     * @param newBugemon Bugémon à envoyer au combat.
     */
    public SwitchAction {
        if (newBugemon == null) {
            throw new IllegalArgumentException(BattleConfig.BUGEMON_NULL_ERROR);
        }
    }

    // Méthodes

    /**
     * Exécute le changement de Bugémon.
     *
     * @param battle combat courant.
     * @param battleService service de résolution du combat.
     * @return message décrivant le changement ou son refus.
     */
    @Override
    public String execute(Battle battle, BattleService battleService) {
        if (battle == null || battleService == null) {
            throw new IllegalArgumentException(BattleConfig.BATTLE_SERVICE_NULL_ERROR);
        }
        boolean success = battleService.switchPlayerBugemon(battle, newBugemon);
        if (!success) {
            return BattleConfig.IMPOSSIBLE_SWITCH_MESSAGE;
        }
        return newBugemon.getName() + BattleConfig.ENTERS_BATTLE_LOG;
    }

    /**
     * Indique si cette action consomme le tour.
     *
     * @return true, car un changement consomme le tour.
     */
    @Override
    public boolean consumesTurn() {
        return true;
    }
}
