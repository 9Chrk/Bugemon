package ulb.models.battle;


/**
 * Action représentant le choix d'une attaque par le joueur.
 */
public class AttackAction implements BattleAction {
    // Données
    private final String attackId;
    private final String actorBugemonId;

    // ----------------- Constructeurs -----------------

    /**
     * Construit une action d'attaque sans verrouiller l'acteur.
     *
     * @param attackId identifiant de l'attaque à lancer.
     */
    public AttackAction(String attackId) {
        this(attackId, null);
    }

    /**
     * Construit une action d'attaque.
     *
     * @param attackId       identifiant de l'attaque à lancer.
     * @param actorBugemonId identifiant du Bugémon censé exécuter l'action.
     */
    public AttackAction(String attackId, String actorBugemonId) {
        // Validation
        if (attackId == null || attackId.isEmpty()) {
            throw new IllegalArgumentException(BattleConfig.ATTACK_ID_NULL_OR_EMPTY_ERROR);
        }
        this.attackId = attackId;
        this.actorBugemonId = actorBugemonId;
    }

    // ----------------- Méthodes -----------------

    /**
     * Exécute l'attaque sur le combat courant.
     *
     * @param battle combat courant.
     * @param battleService service de résolution du combat.
     * @return journal de combat produit par l'attaque.
     */
    @Override
    public String execute(Battle battle, BattleService battleService) {
        // Validation
        if (battle == null || battleService == null) {
            throw new IllegalArgumentException(BattleConfig.BATTLE_SERVICE_NULL_ERROR);
        }
        return battleService.playerAttack(battle, attackId);
    }

    /**
     * Vérifie que l'action peut encore être exécutée par le Bugémon attendu.
     *
     * @param battle combat courant.
     * @return true si l'action est encore valide.
     */
    public boolean canBeExecutedBy(Battle battle) {
        if (battle == null) {
            throw new IllegalArgumentException(BattleConfig.BATTLE_NULL_ERROR);
        }
        if (actorBugemonId == null) {
            return true;
        }

        var activeBugemon = battle.getPlayerActive();
        return activeBugemon != null
                && !activeBugemon.isKo()
                && actorBugemonId.equals(activeBugemon.getId());
    }

    /**
     * Indique si cette action consomme le tour.
     *
     * @return true, car une attaque consomme toujours le tour.
     */
    @Override
    public boolean consumesTurn() {
        return true;
    }
}
