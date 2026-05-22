package ulb.dto;

import java.util.List;

/**
 * Données de vue d'un noeud de l'arbre de compétences.
 */
public record SkillTreeNodeDTO(
        String id,
        String name,
        String description,
        int x,
        int y,
        int currentLevel,
        int maxLevel,
        int cost,
        boolean startNode,
        boolean active,
        boolean available,
        List<String> prerequisites) {
}
