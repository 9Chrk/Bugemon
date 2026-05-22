package ulb.models.battle;

import ulb.models.game.BugemonInstance;

/**
 * Gain d'XP et montées de niveau obtenus par un Bugémon.
 */
public record ExperienceGain(
        BugemonInstance bugemon,
        int gainedXp,
        int levelsGained
) {
}
