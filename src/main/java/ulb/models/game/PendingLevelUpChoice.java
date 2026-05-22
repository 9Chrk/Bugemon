package ulb.models.game;

import java.util.List;

/**
 * Représente un choix de montée de niveau en attente pour un Bugémon.
 */
public record PendingLevelUpChoice(
        BugemonInstance bugemon,
        int reachedLevel,
        List<LevelUpBonus> choices
) {
    public PendingLevelUpChoice {
        if (bugemon == null || choices == null || choices.isEmpty()) {
            throw new IllegalArgumentException("Bugemon and choices must be provided.");
        }
    }
}
