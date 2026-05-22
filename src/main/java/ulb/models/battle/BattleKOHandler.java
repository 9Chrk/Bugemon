package ulb.models.battle;

import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

/**
 * Gère la logique de mise K.O. des Bugémons pendant le combat.
 * Responsable de déterminer si le combat est terminé et de sélectionner le prochain Bugémon.
 */
class BattleKOHandler {

    private final BattleSelectionService selectionService;

    /**
     * Construit le gestionnaire de K.O. avec ses dépendances.
     *
     * @param selectionService service responsable de la sélection du prochain Bugémon.
     */
    BattleKOHandler(BattleSelectionService selectionService) {
        this.selectionService = selectionService;
    }

    /**
     * Gère la mise K.O. d'un Bugémon adverse au combat.
     * Vérifie si l'équipe ennemie est entièrement K.O. et termine le combat si c'est le cas.
     * Sinon, sélectionne le prochain Bugémon de l'équipe ennemie.
     *
     * @param battle combat courant.
     * @param log journal de combat à enrichir.
     */
    void handleEnemyKo(Battle battle, StringBuilder log) {
        Team enemyTeam = battle.getEnemyTeam();

        if (enemyTeam.getAliveBugemons().isEmpty()) {
            // Plus aucun remplaçant ennemi : le combat est gagné.
            battle.endBattle();
            log.append(BattleConfig.VICTORY_MESSAGE);
            return;
        }

        // Sinon le moteur envoie automatiquement le prochain Bugémon disponible.
        BugemonInstance next = selectionService.getNextAvailableBugemon(enemyTeam);
        battle.setEnemyActive(next);
        log.append(next.getName()).append(BattleConfig.ENTERS_BATTLE_LINE_LOG);
    }

    /**
     * Gère la mise K.O. d'un Bugémon du joueur au combat.
     * Vérifie si l'équipe du joueur est entièrement K.O. et termine le combat si c'est le cas.
     * Sinon, sélectionne le prochain Bugémon de l'équipe du joueur.
     *
     * @param battle combat courant.
     * @param log journal de combat à enrichir.
     */
    void handlePlayerKo(Battle battle, StringBuilder log) {
        Team playerTeam = battle.getPlayerTeam();

        if (playerTeam.getAliveBugemons().isEmpty()) {
            // Toute l'équipe du joueur est K.O. : fin immédiate du combat.
            battle.endBattle();
            log.append(BattleConfig.DEFEAT_MESSAGE);
            return;
        }

        // Le changement forcé évite de laisser un Bugémon K.O. actif.
        BugemonInstance next = selectionService.getNextAvailableBugemon(playerTeam);
        battle.setPlayerActive(next);
        log.append(next.getName()).append(BattleConfig.ENTERS_BATTLE_LINE_LOG);
    }
}
