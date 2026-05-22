package ulb.dto;

import java.util.List;

/**
 * Données de vue globales de l'arbre de compétences.
 */
public record SkillTreeStateDTO(
        int availablePoints,
        List<SkillTreeNodeDTO> nodes) {
}
