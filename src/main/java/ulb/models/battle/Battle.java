package ulb.models.battle;

import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

/**
 * Représente l'état courant d'un combat entre deux équipes.
 */
public class Battle {

    private final Team playerTeam;
    private final Team enemyTeam;

    private BugemonInstance playerActive;
    private BugemonInstance enemyActive;

    private boolean finished;

    /**
     * Construit un combat à partir des équipes du joueur et de l'adversaire.
     *
     * @param playerTeam équipe du joueur.
     * @param enemyTeam équipe adverse.
     */
    public Battle(Team playerTeam, Team enemyTeam) {

        if (playerTeam == null || enemyTeam == null) {
            throw new IllegalArgumentException(BattleConfig.TEAMS_NULL_ERROR);
        }

        if (playerTeam.isEmpty() || enemyTeam.isEmpty()) {
            throw new IllegalArgumentException(BattleConfig.TEAMS_EMPTY_ERROR);
        }

        if (playerTeam.getSize() != enemyTeam.getSize()) {
            throw new IllegalArgumentException(BattleConfig.TEAMS_SIZE_MISMATCH_ERROR);
        }

        this.playerTeam = new Team(playerTeam);
        this.enemyTeam = new Team(enemyTeam);

        this.playerActive = this.playerTeam.getBugemons().get(0);
        this.enemyActive = this.enemyTeam.getBugemons().get(0);
        this.playerActive.setParticipatedInBattle(true);

        this.finished = false;
    }

    /**
     * Retourne l'équipe du joueur.
     *
     * @return équipe du joueur.
     */
    public Team getPlayerTeam() {
        return playerTeam;
    }

    /**
     * Retourne l'équipe adverse.
     *
     * @return équipe adverse.
     */
    public Team getEnemyTeam() {
        return enemyTeam;
    }

    /**
     * Retourne le Bugémon actif du joueur.
     *
     * @return Bugémon actif du joueur.
     */
    public BugemonInstance getPlayerActive() {
        return playerActive;
    }

    /**
     * Retourne le Bugémon actif adverse.
     *
     * @return Bugémon actif adverse.
     */
    public BugemonInstance getEnemyActive() {
        return enemyActive;
    }

    /**
     * Définit le Bugémon actif du joueur.
     *
     * @param playerActive nouveau Bugémon actif du joueur.
     */
    public void setPlayerActive(BugemonInstance playerActive) {
        this.playerActive = playerActive;
    }

    /**
     * Définit le Bugémon actif adverse.
     *
     * @param enemyActive nouveau Bugémon actif adverse.
     */
    public void setEnemyActive(BugemonInstance enemyActive) {
        this.enemyActive = enemyActive;
    }

    /**
     * Indique si le combat est terminé.
     *
     * @return true si le combat est terminé.
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Marque le combat comme terminé.
     */
    public void endBattle() {
        this.finished = true;
    }
}
