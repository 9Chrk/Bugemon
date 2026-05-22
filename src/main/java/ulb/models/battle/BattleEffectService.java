package ulb.models.battle;

import ulb.models.data.Attack;
import ulb.models.data.Effect;
import ulb.models.data.EffectVisitor;
import ulb.models.data.HealingEffect;
import ulb.models.data.ItemDefinition;
import ulb.models.data.ResetMalusEffect;
import ulb.models.data.Stats;
import ulb.models.data.StatModifierEffect;
import ulb.models.data.UnknownEffect;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

/**
 * Applique les effets d'attaques et d'objets pendant le combat.
 * Responsable de l'exécution des mécaniques d'effet comme les soins, modificateurs de stats, etc.
 */
class BattleEffectService {

    /**
     * Déclenche les effets d'une attaque en fonction de leurs types.
     *
     * @param battle combat courant.
     * @param attacker Bugémon attaquant.
     * @param defender Bugémon défenseur.
     * @param attack attaque utilisée.
     * @param attackerIsPlayer true si l'attaquant appartient au joueur.
     * @param log journal de combat à enrichir.
     */
    void applyAttackEffects(Battle battle,
                            BugemonInstance attacker,
                            BugemonInstance defender,
                            Attack attack,
                            boolean attackerIsPlayer,
                            StringBuilder log) {
        for (Effect effect : attack.getEffects()) {
            if (effect == null) {
                continue;
            }

            effect.accept(new AttackEffectVisitor(battle, attacker, defender, attackerIsPlayer, log));
        }
    }

    /**
     * Applique l'effet principal d'un objet.
     *
     * @param battle combat courant.
     * @param user Bugémon utilisateur.
     * @param opponent Bugémon adverse.
     * @param item objet utilisé.
     * @return journal de combat généré, ou null si l'effet n'est pas exploitable.
     */
    String applyItemEffect(Battle battle,
                           BugemonInstance user,
                           BugemonInstance opponent,
                           ItemDefinition item) {
        Effect effect = item.getEffect();
        if (effect == null) {
            return null;
        }

        StringBuilder log = new StringBuilder();
        log.append(user.getName())
                .append(BattleConfig.USES_ITEM_LOG)
                .append(item.getName())
                .append(BattleConfig.ITEM_LOG_END);

        ItemEffectVisitor visitor = new ItemEffectVisitor(battle, user, opponent, log);
        effect.accept(visitor);
        if (!visitor.wasApplied()) {
            return null;
        }

        return log.toString();
    }

    /**
     * Applique un effet de soin à une cible.
     *
     * @param battle combat courant.
     * @param attacker Bugémon lanceur.
     * @param effect effet de soin.
     * @param attackerIsPlayer true si le lanceur est du côté joueur.
     * @param log journal de combat à enrichir.
     */
    private void applyHealingEffect(Battle battle,
                                    BugemonInstance attacker,
                                    Effect effect,
                                    boolean attackerIsPlayer,
                                    StringBuilder log) {
        if (effect.getValue() == null || effect.getTarget() == null) {
            return;
        }

        String target = effect.getTarget().toLowerCase();
        int value = effect.getValue();

        if (BattleConfig.EFFECT_TARGET_CASTER.equals(target)) {
            attacker.heal(value);
            log.append(attacker.getName())
                    .append(BattleConfig.RECOVERS_HP_LOG)
                    .append(value)
                    .append(BattleConfig.HP_LOG);
            return;
        }

        if (BattleConfig.EFFECT_TARGET_TEAM.equals(target)) {
            Team team = attackerIsPlayer ? battle.getPlayerTeam() : battle.getEnemyTeam();
            team.healWholeTeam(value);
            log.append(BattleConfig.TEAM_OF_LOG)
                    .append(attacker.getName())
                    .append(BattleConfig.RECOVERS_HP_LOG)
                    .append(value)
                    .append(BattleConfig.HP_LOG);
        }
    }

    /**
     * Applique un modificateur de statistiques à une cible.
     *
     * @param attacker Bugémon lanceur.
     * @param defender Bugémon cible adverse.
     * @param effect effet à appliquer.
     * @param log journal de combat à enrichir.
     */
    private void applyStatModifierEffect(BugemonInstance attacker, BugemonInstance defender,
                                         Effect effect,
                                         StringBuilder log) {
        if (effect.getTarget() == null
                || effect.getStat() == null
                || effect.getModifier() == null
                || effect.getDuration() == null) {
            return;
        }

        BugemonInstance target = findEffectTarget(effect.getTarget(), attacker, defender);

        if (target == null) {
            return;
        }

        Stats modifier = buildStatsModifier(effect.getStat(), effect.getModifier());
        if (modifier == null) {
            return;
        }

        if (BattleConfig.EFFECT_DURATION_ONE_TURN.equalsIgnoreCase(effect.getDuration())) {
            target.queueNextTurnBonus(modifier);
            log.append(target.getName())
                    .append(BattleConfig.NEXT_TURN_EFFECT_LOG);
            return;
        }

        if (BattleConfig.EFFECT_DURATION_PERMANENT.equalsIgnoreCase(effect.getDuration())) {
            target.applyTemporaryBonus(modifier);
            log.append(target.getName())
                    .append(effect.getModifier() >= 0 ? BattleConfig.STAT_GAIN_LOG : BattleConfig.STAT_LOSS_LOG)
                    .append(Math.abs(effect.getModifier()))
                    .append(BattleConfig.STAT_NAME_PREFIX_LOG)
                    .append(effect.getStat())
                    .append(BattleConfig.PERMANENT_EFFECT_END_LOG);
        }
    }

    /**
     * Applique un effet qui retire les malus temporaires.
     *
     * @param attacker Bugémon lanceur.
     * @param effect effet à appliquer.
     * @param log journal de combat à enrichir.
     */
    private void applyResetMalusEffect(BugemonInstance attacker,
                                       Effect effect,
                                       StringBuilder log) {
        if (effect.getTarget() == null) {
            return;
        }

        if (!BattleConfig.EFFECT_TARGET_CASTER.equalsIgnoreCase(effect.getTarget())) {
            return;
        }

        attacker.clearNegativeTemporaryBonuses();
        log.append(attacker.getName())
                .append(BattleConfig.RESET_MALUS_LOG);
    }

    /**
     * Construit un bonus de statistiques à partir du nom d'une statistique.
     *
     * @param statName nom de la statistique.
     * @param modifier valeur du modificateur.
     * @return bonus correspondant, ou null si la statistique est inconnue.
     */
    private Stats buildStatsModifier(String statName, int modifier) {
        if (BattleConfig.STAT_ATTACK.equalsIgnoreCase(statName)) {
            return new Stats(0, modifier, 0, 0);
        }
        if (BattleConfig.STAT_DEFENSE.equalsIgnoreCase(statName)) {
            return new Stats(0, 0, modifier, 0);
        }
        if (BattleConfig.STAT_INITIATIVE.equalsIgnoreCase(statName)) {
            return new Stats(0, 0, 0, modifier);
        }
        if (BattleConfig.STAT_HEALTH.equalsIgnoreCase(statName)) {
            return new Stats(modifier, 0, 0, 0);
        }
        return null;
    }

    private BugemonInstance findEffectTarget(String targetName,
                                             BugemonInstance attacker,
                                             BugemonInstance defender) {
        if (BattleConfig.EFFECT_TARGET_CASTER.equalsIgnoreCase(targetName)) {
            return attacker;
        }
        if (BattleConfig.EFFECT_TARGET_OPPONENT.equalsIgnoreCase(targetName)) {
            return defender;
        }
        return null;
    }

    private class AttackEffectVisitor implements EffectVisitor {
        private final Battle battle;
        private final BugemonInstance attacker;
        private final BugemonInstance defender;
        private final boolean attackerIsPlayer;
        private final StringBuilder log;

        private AttackEffectVisitor(Battle battle,
                                    BugemonInstance attacker,
                                    BugemonInstance defender,
                                    boolean attackerIsPlayer,
                                    StringBuilder log) {
            this.battle = battle;
            this.attacker = attacker;
            this.defender = defender;
            this.attackerIsPlayer = attackerIsPlayer;
            this.log = log;
        }

        /**
         * Applique un effet de soin provenant d'une attaque.
         *
         * @param effect effet de soin à appliquer.
         */
        @Override
        public void visit(HealingEffect effect) {
            applyHealingEffect(battle, attacker, effect, attackerIsPlayer, log);
        }

        /**
         * Applique un modificateur de statistiques provenant d'une attaque.
         *
         * @param effect effet de statistique à appliquer.
         */
        @Override
        public void visit(StatModifierEffect effect) {
            applyStatModifierEffect(attacker, defender, effect, log);
        }

        /**
         * Ignore les effets de retrait de malus sur les attaques.
         *
         * @param effect effet ignoré.
         */
        @Override
        public void visit(ResetMalusEffect effect) {
        }

        /**
         * Ignore les effets inconnus de façon défensive.
         *
         * @param effect effet inconnu.
         */
        @Override
        public void visit(UnknownEffect effect) {
        }
    }

    private class ItemEffectVisitor implements EffectVisitor {
        private final Battle battle;
        private final BugemonInstance user;
        private final BugemonInstance opponent;
        private final StringBuilder log;
        private boolean applied;

        private ItemEffectVisitor(Battle battle,
                                  BugemonInstance user,
                                  BugemonInstance opponent,
                                  StringBuilder log) {
            this.battle = battle;
            this.user = user;
            this.opponent = opponent;
            this.log = log;
        }

        /**
         * Applique un effet de soin provenant d'un objet.
         *
         * @param effect effet de soin à appliquer.
         */
        @Override
        public void visit(HealingEffect effect) {
            applyHealingEffect(battle, user, effect, true, log);
            applied = true;
        }

        /**
         * Applique un modificateur de statistiques provenant d'un objet.
         *
         * @param effect effet de statistique à appliquer.
         */
        @Override
        public void visit(StatModifierEffect effect) {
            applyStatModifierEffect(user, opponent, effect, log);
            applied = true;
        }

        /**
         * Applique un effet de retrait des malus temporaires.
         *
         * @param effect effet de retrait de malus.
         */
        @Override
        public void visit(ResetMalusEffect effect) {
            applyResetMalusEffect(user, effect, log);
            applied = true;
        }

        /**
         * Ignore les effets inconnus de façon défensive.
         *
         * @param effect effet inconnu.
         */
        @Override
        public void visit(UnknownEffect effect) {
        }

        private boolean wasApplied() {
            return applied;
        }
    }
}
