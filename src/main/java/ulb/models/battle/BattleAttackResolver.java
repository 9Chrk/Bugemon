package ulb.models.battle;

import ulb.models.data.Attack;
import ulb.models.game.BugemonInstance;

/**
 * Orchestre la résolution complète d'une attaque pendant le combat.
 * Responsable du calcul des dégâts, application des effets, et génération du journal de combat.
 */
class BattleAttackResolver {

    private final DamageCalculator damageCalculator;
    private final BattleEffectService effectService;
    private final BattleKOHandler koHandler;

    /**
     * Construit le résolveur d'attaque avec ses dépendances.
     *
     * @param damageCalculator calculateur de dégâts.
     * @param effectService service d'application des effets.
     * @param koHandler gestionnaire de mise K.O.
     */
    BattleAttackResolver(DamageCalculator damageCalculator,
                         BattleEffectService effectService,
                         BattleKOHandler koHandler) {
        this.damageCalculator = damageCalculator;
        this.effectService = effectService;
        this.koHandler = koHandler;
    }

    /**
     * Résout une attaque et retourne le résultat avec journal et dégâts.
     *
     * @param battle combat courant.
     * @param attacker Bugémon attaquant.
     * @param defender Bugémon défenseur.
     * @param attack attaque utilisée.
     * @param attackerIsPlayer true si l'attaquant appartient à l'équipe du joueur.
     * @return résultat contenant le journal de combat et les dégâts infligés.
     */
    AttackResolutionResult resolveAttack(Battle battle,
                                         BugemonInstance attacker,
                                         BugemonInstance defender,
                                         Attack attack,
                                         boolean attackerIsPlayer) {
        StringBuilder log = new StringBuilder();
        int damage;

        // Étape 1 : Calculer et appliquer les dégâts
        damage = damageCalculator.calculateDamage(attacker, defender, attack, attackerIsPlayer);
        defender.takeDamage(damage);
        // Le marqueur sert ensuite à répartir l'XP seulement aux participants.
        attacker.setParticipatedInBattle(true);

        // Étape 2 : Construire le journal de l'attaque
        log.append(attacker.getName())
                .append(BattleConfig.USES_ATTACK_LOG)
                .append(attack.getName())
                .append(BattleConfig.ATTACK_SEPARATOR_LOG)
                .append(defender.getName())
                .append(BattleConfig.LOSES_HP_LOG)
                .append(damage)
                .append(BattleConfig.HP_LOG);

        // Étape 3 : Appliquer les effets de l'attaque
        effectService.applyAttackEffects(battle, attacker, defender, attack, attackerIsPlayer, log);

        // Étape 4 : Ajouter le message d'efficacité de type
        addEffectivenessMessage(attack, defender, log);

        // Étape 5 : Gérer la mise K.O. si nécessaire
        if (defender.isKo()) {
            log.append(defender.getName()).append(BattleConfig.KO_LOG);

            // Le gestionnaire de K.O. peut terminer le combat ou remplacer l'actif.
            if (attackerIsPlayer) {
                koHandler.handleEnemyKo(battle, log);
            } else {
                koHandler.handlePlayerKo(battle, log);
            }
        }

        return new AttackResolutionResult(log.toString(), damage, attackerIsPlayer);
    }

    /**
     * Ajoute le message d'efficacité de type au journal de combat.
     *
     * @param attack attaque utilisée.
     * @param defender Bugémon défenseur.
     * @param log journal de combat à enrichir.
     */
    private void addEffectivenessMessage(Attack attack, BugemonInstance defender, StringBuilder log) {
        double multiplier = damageCalculator.computeTypeMultiplier(
                attack.getType(),
                defender.getSpecies().getType());

        String effectivenessMessage = damageCalculator.getEffectivenessMessage(multiplier);
        if (!effectivenessMessage.isEmpty()) {
            log.append(effectivenessMessage).append(BattleConfig.LINE_BREAK);
        }
    }

    /**
         * Représente le résultat de la résolution d'une attaque.
         */
        record AttackResolutionResult(String combatLog, int damageInflicted, boolean wasAttackerPlayer) {
        /**
         * Construit le résultat d'une résolution d'attaque.
         *
         * @param combatLog         journal de combat généré.
         * @param damageInflicted   dégâts infligés.
         * @param wasAttackerPlayer true si l'attaquant était le joueur.
         */
        AttackResolutionResult {
        }

            /**
             * Retourne le journal de combat complet.
             *
             * @return journal de combat.
             */
            @Override
            public String combatLog() {
                return combatLog;
            }

            /**
             * Retourne les dégâts infligés lors de l'attaque.
             *
             * @return valeur des dégâts.
             */
            @Override
            public int damageInflicted() {
                return damageInflicted;
            }

            /**
             * Indique si l'attaquant était le joueur.
             *
             * @return true si le joueur a attaqué.
             */
            @Override
            public boolean wasAttackerPlayer() {
                return wasAttackerPlayer;
            }
        }
}
