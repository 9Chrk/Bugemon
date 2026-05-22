package ulb.models.skilltree;

import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.parsing.SkillTreeData;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * Gère l'état persistant de l'arbre de compétences du joueur.
 */
public class SkillTreeProgress {
    private final Map<String, SkillTreeNode> nodesById;
    private final Map<String, Integer> allocatedLevels;
    private int availablePoints;

    /**
     * Construit une progression vide avec les noeuds chargés depuis les ressources.
     */
    public SkillTreeProgress() {
        this(new SkillTreeData().getAllNodes(), 0, Map.of());
    }

    /**
     * Reconstruit une progression avec les points et niveaux persistés.
     *
     * @param availablePoints points de compétence disponibles.
     * @param allocatedLevels niveaux déjà alloués par identifiant de noeud.
     */
    public SkillTreeProgress(int availablePoints, Map<String, Integer> allocatedLevels) {
        this(new SkillTreeData().getAllNodes(), availablePoints, allocatedLevels);
    }

    /**
     * Construit une progression à partir d'un ensemble explicite de noeuds.
     *
     * @param nodesById       noeuds de l'arbre indexés par identifiant.
     * @param availablePoints points de compétence disponibles.
     * @param allocatedLevels niveaux déjà alloués par identifiant de noeud.
     */
    public SkillTreeProgress(Map<String, SkillTreeNode> nodesById, int availablePoints, Map<String, Integer> allocatedLevels) {
        if (nodesById == null || nodesById.isEmpty()) {
            throw new IllegalArgumentException("Skill tree nodes cannot be null or empty.");
        }
        if (availablePoints < 0) {
            throw new IllegalArgumentException("Available points cannot be negative.");
        }

        this.nodesById = new LinkedHashMap<>(nodesById);
        this.availablePoints = availablePoints;
        this.allocatedLevels = new LinkedHashMap<>();

        if (allocatedLevels != null) {
            for (Map.Entry<String, Integer> entry : allocatedLevels.entrySet()) {
                String nodeId = entry.getKey();
                int level = entry.getValue() == null ? 0 : entry.getValue();
                SkillTreeNode node = this.nodesById.get(nodeId);
                if (node == null || node.isStartNode() || level <= 0) {
                    continue;
                }
                this.allocatedLevels.put(nodeId, Math.min(level, node.getMaxLevel()));
            }
        }

        enforceConsistency();
    }

    /**
     * Retourne les noeuds connus de l'arbre.
     *
     * @return collection immuable des noeuds.
     */
    public Collection<SkillTreeNode> getNodes() {
        return List.copyOf(nodesById.values());
    }

    /**
     * Retourne les points de compétence disponibles.
     *
     * @return nombre de points disponibles.
     */
    public int getAvailablePoints() {
        return availablePoints;
    }

    /**
     * Ajoute un point de compétence disponible.
     */
    public void grantPoint() {
        availablePoints++;
    }

    /**
     * Retourne le niveau courant d'un noeud.
     *
     * @param nodeId identifiant du noeud.
     * @return niveau courant, ou 0 si le noeud est inconnu ou inactif.
     */
    public int getCurrentLevel(String nodeId) {
        SkillTreeNode node = nodesById.get(nodeId);
        if (node == null) {
            return 0;
        }
        if (node.isStartNode()) {
            return 1;
        }
        return allocatedLevels.getOrDefault(nodeId, 0);
    }

    /**
     * Indique si un noeud est actif.
     *
     * @param nodeId identifiant du noeud.
     * @return true si le noeud a au moins un niveau actif.
     */
    public boolean isActive(String nodeId) {
        return getCurrentLevel(nodeId) > 0;
    }

    /**
     * Indique si un noeud est accessible selon ses prérequis.
     *
     * @param nodeId identifiant du noeud.
     * @return true si le noeud peut être affiché comme disponible.
     */
    public boolean isAvailable(String nodeId) {
        SkillTreeNode node = nodesById.get(nodeId);
        if (node == null) {
            return false;
        }
        if (node.isStartNode()) {
            return true;
        }
        return hasSatisfiedPrerequisite(node);
    }

    /**
     * Vérifie si un point peut être alloué à un noeud.
     *
     * @param nodeId identifiant du noeud.
     * @return true si le point peut être dépensé.
     */
    public boolean canAllocatePoint(String nodeId) {
        SkillTreeNode node = nodesById.get(nodeId);
        if (node == null || node.isStartNode()) {
            return false;
        }
        return availablePoints >= node.getCost()
                && getCurrentLevel(nodeId) < node.getMaxLevel()
                && hasSatisfiedPrerequisite(node);
    }

    /**
     * Alloue un point de compétence à un noeud.
     *
     * @param nodeId identifiant du noeud.
     * @return true si l'allocation a réussi.
     */
    public boolean allocatePoint(String nodeId) {
        if (!canAllocatePoint(nodeId)) {
            return false;
        }

        SkillTreeNode node = nodesById.get(nodeId);
        allocatedLevels.put(nodeId, getCurrentLevel(nodeId) + 1);
        availablePoints -= node.getCost();
        return true;
    }

    /**
     * Vérifie si un point peut être retiré d'un noeud.
     *
     * @param nodeId identifiant du noeud.
     * @return true si le noeud peut perdre un niveau.
     */
    public boolean canRemovePoint(String nodeId) {
        SkillTreeNode node = nodesById.get(nodeId);
        return node != null && !node.isStartNode() && getCurrentLevel(nodeId) > 0;
    }

    /**
     * Retire un point de compétence d'un noeud et invalide les dépendances si nécessaire.
     *
     * @param nodeId identifiant du noeud.
     * @return true si un point a été retiré.
     */
    public boolean removePoint(String nodeId) {
        if (!canRemovePoint(nodeId)) {
            return false;
        }

        SkillTreeNode node = nodesById.get(nodeId);
        int currentLevel = getCurrentLevel(nodeId);
        // Le coût payé revient immédiatement dans la réserve de points.
        availablePoints += node.getCost();

        if (currentLevel <= 1) {
            allocatedLevels.remove(nodeId);
        } else {
            allocatedLevels.put(nodeId, currentLevel - 1);
        }

        cascadeInvalidNodes();
        return true;
    }

    /**
     * Retourne un instantané des niveaux alloués.
     *
     * @return niveaux alloués par identifiant de noeud.
     */
    public Map<String, Integer> getAllocatedLevelsSnapshot() {
        return Map.copyOf(allocatedLevels);
    }

    /**
     * Calcule les bonus actifs à partir des noeuds actuellement sélectionnés.
     *
     * @return bonus cumulés de l'arbre.
     */
    public SkillTreeBonuses computeBonuses() {
        SkillTreeBonuses bonuses = new SkillTreeBonuses();

        for (SkillTreeNode node : nodesById.values()) {
            if (!isActive(node.getId()) || node.getEffect() == null || node.isStartNode()) {
                continue;
            }

            int level = getCurrentLevel(node.getId());
            SkillEffect effect = node.getEffect();
            String effectType = effect.getType();
            if (effectType == null || effectType.isBlank()) {
                continue;
            }

            // Chaque type d'effet JSON alimente une famille de bonus différente.
            switch (effectType) {
                case "stat_bonus" -> applyStatBonus(bonuses, effect, level);
                case "xp_multiplicateur" -> bonuses.addXpMultiplierDelta((effect.getValueOrDefault(1.0) - 1.0) * level);
                case "regen_post_combat" -> bonuses.addPostCombatRegenPercent(effect.getPercentageValueOrDefault(0) * level);
                case "objets_bonus" -> bonuses.addBonusItemCategory(effect.getCategory(), effect.getQuantityOrDefault(0) * level);
                case "critique_bonus" -> bonuses.addCriticalChanceBonus((effect.getValueOrDefault(0.0) / 100.0) * level);
                case "type_multiplicateur" -> bonuses.addTypeDamageMultiplier(
                        parseType(effect.getTargetType()),
                        (effect.getValueOrDefault(1.0) - 1.0) * level);
                case "recompense_choix" -> bonuses.setLevelUpChoiceCount((int) Math.round(effect.getValueOrDefault(3.0)));
                default -> {
                    // Effet inconnu : ignoré de manière défensive.
                }
            }
        }

        return bonuses;
    }

    private void enforceConsistency() {
        cascadeInvalidNodes();
    }

    private void cascadeInvalidNodes() {
        ArrayDeque<String> queue = new ArrayDeque<>(allocatedLevels.keySet());
        while (!queue.isEmpty()) {
            String nodeId = queue.removeFirst();
            if (!allocatedLevels.containsKey(nodeId)) {
                continue;
            }

            SkillTreeNode node = nodesById.get(nodeId);
            if (!hasSatisfiedPrerequisite(node)) {
                // Retirer un prérequis rembourse aussi les noeuds devenus invalides.
                availablePoints += node == null ? 0 : node.getCost() * allocatedLevels.get(nodeId);
                allocatedLevels.remove(nodeId);
                queue.addAll(getDirectDependents(nodeId));
            }
        }
    }

    private List<String> getDirectDependents(String nodeId) {
        return nodesById.values().stream()
                .filter(node -> node.getPrerequisites().contains(nodeId))
                .map(SkillTreeNode::getId)
                .toList();
    }

    private boolean hasSatisfiedPrerequisite(SkillTreeNode node) {
        if (node == null) {
            return false;
        }
        if (node.isStartNode()) {
            return true;
        }

        List<String> prerequisites = node.getPrerequisites();
        if (prerequisites.isEmpty()) {
            return false;
        }

        return prerequisites.stream().anyMatch(this::isActive);
    }

    private void applyStatBonus(SkillTreeBonuses bonuses, SkillEffect effect, int level) {
        int bonusValue = (int) Math.round(effect.getValueOrDefault(0.0) * level);
        Stats stats = switch (normalizeStatKey(effect.getStat())) {
            case "hp", "pv", "maxhp", "health" -> new Stats(bonusValue, 0, 0, 0);
            case "attaque" -> new Stats(0, bonusValue, 0, 0);
            case "defense" -> new Stats(0, 0, bonusValue, 0);
            case "initiative" -> new Stats(0, 0, 0, bonusValue);
            default -> new Stats();
        };
        bonuses.addTeamStatsBonus(stats);
    }

    private String normalizeStatKey(String stat) {
        // Les JSON peuvent varier entre français, anglais et séparateurs.
        return Objects.toString(stat, "")
                .trim()
                .toLowerCase(Locale.ROOT)
                .replace("-", "")
                .replace("_", "");
    }

    private Type parseType(String rawType) {
        if (rawType == null || rawType.isBlank()) {
            return null;
        }
        try {
            return Type.valueOf(rawType);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
