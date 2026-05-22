package ulb.models.battle;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ulb.models.data.BugemonDefinition;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;
import ulb.models.data.Difficulty;

/**
 * Construit une équipe ennemie de taille identique à celle du joueur, avec des
 * espèces tirées aléatoirement.
 */
public class EnemyTeamFactory {

    // Données
    private final Random random;

    // Constructeurs

    /**
     * Construit la fabrique avec une source aléatoire par défaut.
     */
    public EnemyTeamFactory() {
        this(new Random());
    }

    /**
     * Construit la fabrique avec une source aléatoire fournie.
     *
     * @param random source aléatoire utilisée pour les tirages.
     */
    public EnemyTeamFactory(Random random) {
        this.random = random == null ? new Random() : random;
    }

    // ----------------- Fabrique -----------------

    /**
     * Génère une équipe ennemie du même format que l'équipe du joueur.
     *
     * @param playerTeam     équipe du joueur.
     * @param allDefinitions définitions de Bugémons disponibles.
     * @param difficulty difficulté appliquée aux niveaux ennemis.
     * @return équipe ennemie générée.
     */
    public Team buildEnemyTeam(Team playerTeam, Map<String, BugemonDefinition> allDefinitions, Difficulty difficulty) {
        if (playerTeam == null || allDefinitions == null || allDefinitions.isEmpty()) {
            throw new IllegalArgumentException(BattleConfig.PLAYER_TEAM_AND_DEFINITIONS_ERROR);
        }

        int desiredSize = playerTeam.getSize();
        List<BugemonDefinition> pool = new ArrayList<>(allDefinitions.values());
        List<BugemonInstance> picked = new ArrayList<>();

        while (picked.size() < desiredSize) {
            // Tirage avec remise : deux ennemis peuvent partager la même espèce.
            int index = random.nextInt(pool.size());
            picked.add(new BugemonInstance(pool.get(index)));
        }

        setDifficulty(playerTeam, picked, difficulty);

        return new Team("enemyTeam", picked);
    }

    /**
     * Ajuste le niveau de l'équipe ennemie en fonction de la difficulté choisie
     * et de la progression de l'équipe du joueur.
     *
     * @param playerTeam équipe du joueur pour calculer la référence de niveau.
     * @param enemyTeam  liste des Bugémons ennemis à ajuster.
     * @param difficulty niveau de difficulté sélectionné.
     */
    private void setDifficulty(Team playerTeam, List<BugemonInstance> enemyTeam, Difficulty difficulty) {
        // La difficulté se base sur la moyenne ou le meilleur niveau du joueur.
        int targetLevel = switch (difficulty) {
            case EASY -> (int) (playerTeam.getMoyLevel() * 0.9); // 10% de moins que la moyenne
            case NORMAL -> playerTeam.getMoyLevel(); // Égal à la moyenne
            case HARD -> (int) (playerTeam.getTopLevel() * 1.25); // 25% de plus que le meilleur Bugémon
        };
        // Assure un niveau minimum de 1.
        int finalLevel = Math.max(1, targetLevel);
        for (BugemonInstance bugemon : enemyTeam) {
            bugemon.setLevel(finalLevel);
        }
    }
}
