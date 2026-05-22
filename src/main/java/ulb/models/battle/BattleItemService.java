package ulb.models.battle;

import ulb.models.data.ItemDefinition;
import ulb.models.data.HealingEffect;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

/**
 * Gère la validation et l'application des objets de combat.
 * Responsable de vérifier si un objet peut être utilisé et de déléguer l'application de ses effets.
 */
class BattleItemService {

    private final BattleEffectService effectService;
    private final String itemHasNoUsableEffectMessage;
    private final String activeBugemonAlreadyFullHpMessage;
    private final String teamAlreadyFullHpMessage;

    /**
     * Construit le service d'objet avec ses dépendances.
     *
     * @param effectService service responsable de l'application des effets.
     * @param itemHasNoUsableEffectMessage message d'erreur pour objet sans effet.
     * @param activeBugemonAlreadyFullHpMessage message d'erreur pour Bugémon à PV max.
     * @param teamAlreadyFullHpMessage message d'erreur pour équipe à PV max.
     */
    BattleItemService(BattleEffectService effectService,
                      String itemHasNoUsableEffectMessage,
                      String activeBugemonAlreadyFullHpMessage,
                      String teamAlreadyFullHpMessage) {
        this.effectService = effectService;
        this.itemHasNoUsableEffectMessage = itemHasNoUsableEffectMessage;
        this.activeBugemonAlreadyFullHpMessage = activeBugemonAlreadyFullHpMessage;
        this.teamAlreadyFullHpMessage = teamAlreadyFullHpMessage;
    }

    /**
     * Vérifie si l'objet peut être utilisé dans le contexte courant.
     *
     * @param battle combat courant.
     * @param item objet à valider.
     * @return message d'erreur si l'objet ne peut pas être utilisé, sinon null.
     */
    String validateItemUsage(Battle battle, ItemDefinition item) {
        var effect = item.getEffect();
        if (!(effect instanceof HealingEffect healingEffect)) {
            return itemHasNoUsableEffectMessage;
        }

        String target = healingEffect.getTarget();
        if (target == null) {
            return itemHasNoUsableEffectMessage;
        }

        // Chaque cible possède sa propre condition d'inutilité.
        switch (target) {
            case BattleConfig.EFFECT_TARGET_CASTER:
                if (isActiveBugemonFullyHealed(battle.getPlayerActive())) {
                    return activeBugemonAlreadyFullHpMessage;
                }
                break;
            case BattleConfig.EFFECT_TARGET_TEAM:
                if (isTeamFullyHealed(battle.getPlayerTeam())) {
                    return teamAlreadyFullHpMessage;
                }
                break;
            default:
                return itemHasNoUsableEffectMessage;
        }

        return null;
    }

    /**
     * Vérifie si le Bugémon actif a tous ses PV au maximum.
     *
     * @param bugemon Bugémon à vérifier.
     * @return true si le Bugémon a tous ses PV.
     */
    private boolean isActiveBugemonFullyHealed(BugemonInstance bugemon) {
        return bugemon.getCurrentHp() >= bugemon.getEffectiveStats().getHealth();
    }

    /**
     * Applique l'effet de l'objet via le moteur d'effets.
     *
     * @param battle combat courant.
     * @param user Bugémon utilisateur.
     * @param opponent Bugémon adverse.
     * @param item objet à utiliser.
     * @return journal de combat généré, ou null si l'effet n'est pas applicable.
     */
    String applyItemEffect(Battle battle,
                           BugemonInstance user,
                           BugemonInstance opponent,
                           ItemDefinition item) {
        return effectService.applyItemEffect(battle, user, opponent, item);
    }

    /**
     * Vérifie si toute l'équipe est déjà à ses PV maximums.
     *
     * @param team équipe à vérifier.
     * @return true si tous les Bugémons ont leurs PV au maximum.
     */
    private boolean isTeamFullyHealed(Team team) {
        // Si toute l'équipe est pleine vie, consommer l'objet serait inutile.
        return team.getBugemons().stream()
                .allMatch(bugemon -> bugemon.getCurrentHp() >= bugemon.getEffectiveStats().getHealth());
    }
}
