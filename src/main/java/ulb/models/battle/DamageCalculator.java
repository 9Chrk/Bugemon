package ulb.models.battle;

import ulb.models.data.Attack;
import ulb.models.data.Type;
import ulb.models.game.BugemonInstance;

import java.util.EnumMap;
import java.util.Map;

/**
 * Calcule les dégâts de combat à partir des statistiques, du type et des multiplicateurs.
 */
public class DamageCalculator {

    // Activé par défaut en jeu ; les tests peuvent le désactiver pour des assertions déterministes.
    private boolean criticalHitsEnabled = true;
    private double playerCriticalChanceBonus = 0.0;
    private final Map<Type, Double> playerTypeDamageMultipliers = new EnumMap<>(Type.class);

    // ----------------- API publique -----------------

    /**
     * Calcule les dégâts infligés d'un attaquant vers un défenseur.
     *
     * @param attacker Bugémon attaquant.
     * @param defender Bugémon défenseur.
     * @param attack attaque utilisée.
     * @return dégâts arrondis et bornés à un minimum de 1.
     */
    public int calculateDamage(BugemonInstance attacker, BugemonInstance defender, Attack attack) {
        return calculateDamage(attacker, defender, attack, false);
    }

    /**
     * Calcule les dégâts en tenant compte des bonus réservés au joueur.
     *
     * @param attacker Bugémon attaquant.
     * @param defender Bugémon défenseur.
     * @param attack attaque utilisée.
     * @param attackerIsPlayer true si l'attaquant appartient à l'équipe du joueur.
     * @return dégâts arrondis et bornés à un minimum de 1.
     */
    public int calculateDamage(BugemonInstance attacker, BugemonInstance defender, Attack attack, boolean attackerIsPlayer) {
        // Échec rapide : une entrée nulle rendrait la formule trompeuse.
        if (attacker == null || defender == null || attack == null) {
            throw new IllegalArgumentException(BattleConfig.DAMAGE_INPUT_NULL_ERROR);
        }

        // Chaque composante reste un multiplicateur pour garder la formule lisible.
        double attackFactor = (BattleConfig.STAT_SCALE_BASE + attacker.getEffectiveStats().getAttack())
                / BattleConfig.STAT_SCALE_BASE;
        double reductionFactor = BattleConfig.STAT_SCALE_BASE
                / (BattleConfig.STAT_SCALE_BASE + defender.getEffectiveStats().getDefense());
        double typeMultiplier = computeTypeMultiplier(attack.getType(), defender.getSpecies().getType());
        if (attackerIsPlayer) {
            typeMultiplier *= playerTypeDamageMultipliers.getOrDefault(attack.getType(), BattleConfig.NEUTRAL_TYPE_MULTIPLIER);
        }

        double critChance = BattleConfig.CRITICAL_HIT_CHANCE + (attackerIsPlayer ? playerCriticalChanceBonus : 0.0);
        double criticalMultiplier = (criticalHitsEnabled && Math.random() < critChance)
                ? BattleConfig.CRITICAL_HIT_MULTIPLIER
                : BattleConfig.NEUTRAL_TYPE_MULTIPLIER;

        double rawDamage = attack.getPower() * attackFactor * reductionFactor * typeMultiplier * criticalMultiplier;

        return (int) Math.max(BattleConfig.MINIMUM_DAMAGE, Math.round(rawDamage));
    }


    // ----------------- Fonctions d'aide -----------------

    /**
     * Calcule le multiplicateur de type entre une attaque et sa cible.
     *
     * @param attackType type de l'attaque.
     * @param defenderType type du défenseur.
     * @return multiplicateur d'efficacité.
     */
    public double computeTypeMultiplier(Type attackType, Type defenderType) {
        if (attackType == null || defenderType == null) {
            return BattleConfig.NEUTRAL_TYPE_MULTIPLIER;
        }

        if (isSuperEffective(attackType, defenderType)) {
            return BattleConfig.SUPER_EFFECTIVE_MULTIPLIER;
        }

        if (isNotVeryEffective(attackType, defenderType)) {
            return BattleConfig.NOT_VERY_EFFECTIVE_MULTIPLIER;
        }
        return BattleConfig.NEUTRAL_TYPE_MULTIPLIER;
    }

    private boolean isSuperEffective(Type attackType, Type defenderType) {
        return attackType == Type.Flora && defenderType == Type.Aqua
                || attackType == Type.Aqua && defenderType == Type.Pyro
                || attackType == Type.Pyro && defenderType == Type.Litho
                || attackType == Type.Litho && defenderType == Type.Flora;
    }

    private boolean isNotVeryEffective(Type attackType, Type defenderType) {
        return attackType == Type.Flora && defenderType == Type.Litho
                || attackType == Type.Aqua && defenderType == Type.Flora
                || attackType == Type.Pyro && defenderType == Type.Aqua
                || attackType == Type.Litho && defenderType == Type.Pyro;
    }

    /**
     * Retourne un message lisible selon l'efficacité du type.
     *
     * @param multiplier multiplicateur d'efficacité.
     * @return message d'efficacité adapté.
     */

    public String getEffectivenessMessage(double multiplier) {
        if (multiplier == BattleConfig.SUPER_EFFECTIVE_MULTIPLIER) {
            return BattleConfig.SUPER_EFFECTIVE_MESSAGE;
        }
        if (multiplier == BattleConfig.NOT_VERY_EFFECTIVE_MULTIPLIER) {
            return BattleConfig.NOT_VERY_EFFECTIVE_MESSAGE;
        }
        return "";
    }
    // ----------------- Fonctions d'aide pour les tests -----------------

    /**
     * Désactive les coups critiques (utile pour les tests).
     */
    public void disableCriticalHits() {
        criticalHitsEnabled = false;
    }

    /**
     * Réactive les coups critiques.
     */
    public void enableCriticalHits() {
        criticalHitsEnabled = true;
    }

    /**
     * Définit le bonus de chance de critique appliqué aux attaques du joueur.
     *
     * @param playerCriticalChanceBonus bonus additif entre 0 et 1.
     */
    public void setPlayerCriticalChanceBonus(double playerCriticalChanceBonus) {
        this.playerCriticalChanceBonus = Math.max(0.0, playerCriticalChanceBonus);
    }

    /**
     * Définit les multiplicateurs de dégâts par type appliqués au joueur.
     *
     * @param multipliers map type -> multiplicateur final.
     */
    public void setPlayerTypeDamageMultipliers(Map<Type, Double> multipliers) {
        playerTypeDamageMultipliers.clear();
        if (multipliers == null) {
            return;
        }
        for (Map.Entry<Type, Double> entry : multipliers.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                playerTypeDamageMultipliers.put(entry.getKey(), entry.getValue());
            }
        }
    }
}
