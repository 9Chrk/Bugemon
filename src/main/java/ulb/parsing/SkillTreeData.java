package ulb.parsing;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import ulb.models.skilltree.SkillTreeNode;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Charge la définition de l'arbre de compétences depuis les ressources JSON.
 */
public class SkillTreeData {
    private final Map<String, SkillTreeNode> nodesById;

    /**
     * Charge l'arbre de compétences depuis la ressource JSON par défaut.
     */
    public SkillTreeData() {
        this.nodesById = loadNodes();
    }

    /**
     * Retourne tous les noeuds de l'arbre de compétences.
     *
     * @return copie immuable des noeuds indexés par identifiant.
     */
    public Map<String, SkillTreeNode> getAllNodes() {
        return Map.copyOf(nodesById);
    }

    private Map<String, SkillTreeNode> loadNodes() {
        ObjectMapper mapper = new ObjectMapper();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream("data/skill_tree.json")) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: data/skill_tree.json");
            }

            SkillTreeRoot root = mapper.readValue(is, SkillTreeRoot.class);
            if (root == null || root.skillTree == null || root.skillTree.nodes == null || root.skillTree.nodes.isEmpty()) {
                throw new IllegalStateException("Skill tree definition is empty.");
            }

            Map<String, SkillTreeNode> result = new LinkedHashMap<>();
            for (SkillTreeNode node : root.skillTree.nodes) {
                if (result.containsKey(node.getId())) {
                    throw new IllegalStateException("Duplicate skill node id: " + node.getId());
                }
                result.put(node.getId(), node);
            }
            return result;
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load skill tree data.", e);
        }
    }

    /**
     * Conteneur racine utilisé uniquement pour la désérialisation JSON.
     */
    private static class SkillTreeRoot {
        @JsonProperty("skill_tree")
        private SkillTreeDefinition skillTree;
    }

    /**
     * Conteneur de la liste des noeuds utilisé uniquement pour la désérialisation JSON.
     */
    private static class SkillTreeDefinition {
        @JsonProperty("nodes")
        private List<SkillTreeNode> nodes;
    }
}
