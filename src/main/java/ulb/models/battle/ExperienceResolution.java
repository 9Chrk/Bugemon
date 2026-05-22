package ulb.models.battle;

import java.util.List;

/**
 * Résultat de la répartition de l'XP après une victoire.
 */
public record ExperienceResolution(
        int totalXp,
        List<ExperienceGain> gains
) {
}
