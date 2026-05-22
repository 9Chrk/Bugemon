package ulb.models.battle;


/**
 * Représente une action exécutable pendant un combat.
 */
public interface BattleAction {

    /**
     * Exécute l'action dans le contexte de combat fourni.
     *
     * @param battle état courant du combat.
     * @param battleService service chargé d'appliquer les règles du combat.
     * @return message décrivant le résultat de l'action.
     */
    String execute(Battle battle, BattleService battleService);

    /**
     * Indique si cette action consomme le tour du lanceur.
     *
     * @return true si l'action consomme le tour.
     */
    boolean consumesTurn();
}
