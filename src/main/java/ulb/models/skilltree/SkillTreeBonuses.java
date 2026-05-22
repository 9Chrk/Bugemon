package ulb.models.skilltree;

import ulb.models.data.Stats;
import ulb.models.data.Type;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Regroupe les bonus de run calculés à partir des noeuds actifs.
 */
public class SkillTreeBonuses {
    private final Stats teamStatsBonus;
    private double xpMultiplier;
    private int postCombatRegenPercent;
    private double criticalChanceBonus;
    private int levelUpChoiceCount;
    private final Map<Type, Double> typeDamageMultipliers;
    private final Map<String, Integer> bonusItemsByCategory;

    /**
     * Construit un ensemble de bonus avec les valeurs par défaut d'une run.
     */
    public SkillTreeBonuses() {
        this.teamStatsBonus = new Stats();
        this.xpMultiplier = 1.0;
        this.postCombatRegenPercent = 0;
        this.criticalChanceBonus = 0.0;
        this.levelUpChoiceCount = 3;
        this.typeDamageMultipliers = new EnumMap<>(Type.class);
        this.bonusItemsByCategory = new LinkedHashMap<>();
    }

    /**
     * Retourne le bonus de statistiques appliqué à toute l'équipe.
     *
     * @return copie du bonus de statistiques.
     */
    public Stats getTeamStatsBonus() {
        return teamStatsBonus.copy();
    }

    /**
     * Ajoute un bonus de statistiques global.
     *
     * @param bonus statistiques à ajouter.
     */
    public void addTeamStatsBonus(Stats bonus) {
        teamStatsBonus.add(bonus);
    }

    /**
     * Retourne le multiplicateur d'expérience.
     *
     * @return multiplicateur d'XP.
     */
    public double getXpMultiplier() {
        return xpMultiplier;
    }

    /**
     * Ajoute une variation au multiplicateur d'expérience.
     *
     * @param delta variation à ajouter.
     */
    public void addXpMultiplierDelta(double delta) {
        xpMultiplier += delta;
    }

    /**
     * Retourne le pourcentage de régénération après combat.
     *
     * @return pourcentage de points de vie restaurés.
     */
    public int getPostCombatRegenPercent() {
        return postCombatRegenPercent;
    }

    /**
     * Ajoute un pourcentage de régénération après combat.
     *
     * @param value pourcentage à ajouter.
     */
    public void addPostCombatRegenPercent(int value) {
        postCombatRegenPercent += value;
    }

    /**
     * Retourne le bonus de probabilité de coup critique.
     *
     * @return bonus de probabilité.
     */
    public double getCriticalChanceBonus() {
        return criticalChanceBonus;
    }

    /**
     * Ajoute un bonus de probabilité de coup critique.
     *
     * @param value bonus à ajouter.
     */
    public void addCriticalChanceBonus(double value) {
        criticalChanceBonus += value;
    }

    /**
     * Retourne le nombre de choix proposés lors d'une montée de niveau.
     *
     * @return nombre de choix.
     */
    public int getLevelUpChoiceCount() {
        return levelUpChoiceCount;
    }

    /**
     * Augmente le nombre de choix de montée de niveau si la valeur fournie est supérieure.
     *
     * @param levelUpChoiceCount nouveau nombre minimal de choix.
     */
    public void setLevelUpChoiceCount(int levelUpChoiceCount) {
        this.levelUpChoiceCount = Math.max(this.levelUpChoiceCount, levelUpChoiceCount);
    }

    /**
     * Ajoute un multiplicateur de dégâts pour un type précis.
     *
     * @param type  type concerné.
     * @param delta variation du multiplicateur.
     */
    public void addTypeDamageMultiplier(Type type, double delta) {
        if (type == null) {
            return;
        }
        typeDamageMultipliers.put(type, typeDamageMultipliers.getOrDefault(type, 1.0) + delta);
    }

    /**
     * Retourne les multiplicateurs de dégâts par type.
     *
     * @return vue immuable des multiplicateurs.
     */
    public Map<Type, Double> getTypeDamageMultipliers() {
        return Collections.unmodifiableMap(typeDamageMultipliers);
    }

    /**
     * Ajoute une quantité d'objets bonus pour une catégorie.
     *
     * @param category catégorie d'objet.
     * @param quantity quantité à ajouter.
     */
    public void addBonusItemCategory(String category, int quantity) {
        if (category == null || category.isBlank() || quantity <= 0) {
            return;
        }
        bonusItemsByCategory.put(category, bonusItemsByCategory.getOrDefault(category, 0) + quantity);
    }

    /**
     * Retourne les objets bonus à ajouter par catégorie.
     *
     * @return vue immuable des quantités par catégorie.
     */
    public Map<String, Integer> getBonusItemsByCategory() {
        return Collections.unmodifiableMap(bonusItemsByCategory);
    }
}
