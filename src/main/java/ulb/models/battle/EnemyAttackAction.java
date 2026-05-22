package ulb.models.battle;

/**
 * Action automatique utilisée pour l'attaque de l'adversaire.
 */
public class EnemyAttackAction implements BattleAction {
    /**
     * Exécute l'attaque automatique de l'adversaire.
     *
     * @param battle  combat courant.
     * @param service service de résolution du combat.
     * @return journal généré par l'attaque.
     */
    @Override
    public String execute(Battle battle, BattleService service) {
        return service.enemyAutoAttack(battle);
    }

    /**
     * Indique que l'attaque ennemie consomme le tour.
     *
     * @return toujours true.
     */
    @Override
    public boolean consumesTurn() {
        return true; 
    }
}
