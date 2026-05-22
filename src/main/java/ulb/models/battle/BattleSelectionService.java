package ulb.models.battle;

import ulb.models.data.Attack;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Gère la sélection des attaques et des prochains Bugémons actifs.
 */
class BattleSelectionService {

    private final Map<String, Attack> attacksById;
    private final Random random;

    /**
     * Construit un service de sélection avec ses dépendances.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     * @param random générateur aléatoire utilisé pour les choix automatiques.
     */
    BattleSelectionService(Map<String, Attack> attacksById, Random random) {
        this.attacksById = attacksById;
        this.random = random;
    }

    /**
     * Sélectionne une attaque aléatoire parmi les attaques connues.
     *
     * @param bugemon Bugémon qui doit attaquer.
     * @return attaque tirée au hasard.
     */
    Attack getRandomAttack(BugemonInstance bugemon) {
        List<String> attackIds = bugemon.getLearnedAttackIds();

        if (attackIds.isEmpty()) {
            throw new IllegalStateException(BattleConfig.BUGEMON_HAS_NO_ATTACKS_ERROR);
        }

        // L'IA choisit uniformément parmi les attaques apprises.
        int index = random.nextInt(attackIds.size());
        String attackId = attackIds.get(index);
        Attack attack = attacksById.get(attackId);

        if (attack == null) {
            // Défense contre une sauvegarde ou définition d'attaque incohérente.
            throw new IllegalStateException(BattleConfig.UNKNOWN_ATTACK_ID_ERROR_PREFIX + attackId);
        }

        return attack;
    }

    /**
     * Retourne le prochain Bugémon encore apte au combat.
     *
     * @param team équipe à inspecter.
     * @return premier Bugémon vivant trouvé.
     */
    BugemonInstance getNextAvailableBugemon(Team team) {
        // La stratégie actuelle prend simplement le premier survivant.
        BugemonInstance next = team.getFirstAliveBugemon();

        if (next == null) {
            throw new IllegalStateException(BattleConfig.TEAM_HAS_NO_AVAILABLE_BUGEMON_ERROR);
        }

        return next;
    }
}
