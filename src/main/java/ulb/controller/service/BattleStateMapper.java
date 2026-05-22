package ulb.controller.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import ulb.dto.BattleStateDTO;
import ulb.dto.BugemonActiveStateDTO;
import ulb.models.data.Attack;
import ulb.models.game.BugemonInstance;
import ulb.models.battle.BattleService;
import ulb.view.ViewConfig;

/**
 * Service de mapping de l'état du combat vers les DTO pour l'affichage.
 */
public class BattleStateMapper {
    private final Map<String, Attack> attacksById;
    private final BattleService battleService;

    /**
     * Construit un mapper d'état de combat.
     *
     * @param attacksById   attaques indexées par identifiant.
     * @param battleService service fournissant les dégâts courants.
     */
    public BattleStateMapper(Map<String, Attack> attacksById, BattleService battleService) {
        this.attacksById = attacksById;
        this.battleService = battleService;
    }

    /**
     * Crée un DTO représentant l'état actuel du combat.
     *
     * @param playerActive Bugémon actif du joueur.
     * @param enemyActive  Bugémon actif adverse.
     * @return état affichable du combat.
     */
    public BattleStateDTO mapCurrentBattleState(BugemonInstance playerActive, BugemonInstance enemyActive) {
        // Les dégâts récents sont attachés au DTO pour l'animation de la vue.
        return new BattleStateDTO(
                mapToDTO(playerActive, battleService.getDamageTakenPlayer()),
                mapToDTO(enemyActive, battleService.getDamageTakenEnemy()));
    }

    /**
     * Convertit une instance de Bugémon en DTO pour l'affichage.
     */
    private BugemonActiveStateDTO mapToDTO(BugemonInstance b, int dmg) {
        // Le DTO embarque uniquement les données utiles à l'affichage.
        return new BugemonActiveStateDTO(
                b.getName(),
                b.getSpecies().getType(),
                b.getCurrentHp(),
                b.getEffectiveStats().getHealth(),
                dmg,
                b.getLevel(),
                ViewConfig.BUGEMON_SPRITE_PATH + b.getSpecies().getSprite(),
                getAttackSummaries(b));
    }

    /**
     * Retourne les données des attaques apprises par un Bugémon.
     */
    private List<ulb.dto.AttackSummaryDTO> getAttackSummaries(BugemonInstance b) {
        return b.getLearnedAttackIds().stream()
                .map(attacksById::get)
                // Ignore une attaque introuvable au lieu de casser toute la vue.
                .filter(Objects::nonNull)
                .map(attack -> new ulb.dto.AttackSummaryDTO(attack.getId(), attack.getName(), attack.getType()))
                .toList();
    }
}

