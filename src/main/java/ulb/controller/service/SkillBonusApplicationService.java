package ulb.controller.service;

import java.util.List;
import java.util.Map;

import ulb.controller.TeamManagerController;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;
import ulb.models.data.ItemDefinition;
import ulb.models.skilltree.SkillTreeBonuses;

/**
 * Service de gestion et d'application des bonus d'arbre de compétences à l'équipe.
 */
public class SkillBonusApplicationService {
    private final TeamManagerController teamManagerController;
    private final Map<String, ItemDefinition> itemsById;

    /**
     * Construit le service d'application des bonus de compétences.
     *
     * @param teamManagerController contrôleur donnant accès à l'équipe et l'inventaire.
     * @param itemsById             objets indexés par identifiant.
     */
    public SkillBonusApplicationService(TeamManagerController teamManagerController,
            Map<String, ItemDefinition> itemsById) {
        this.teamManagerController = teamManagerController;
        this.itemsById = itemsById;
    }

    /**
     * Applique les bonus de compétences au lancement de la partie.
     *
     * @param skillBonuses bonus calculés depuis l'arbre de compétences.
     */
    public void applyRunStartSkillBonuses(SkillTreeBonuses skillBonuses) {
        applyTeamBonuses(skillBonuses, true);
        // Les objets bonus ne sont accordés qu'au démarrage d'une nouvelle run.
        addBonusItemsForRun(skillBonuses);
    }

    /**
     * Applique les bonus de compétences au chargement d'une partie.
     *
     * @param skillBonuses bonus calculés depuis l'arbre de compétences.
     */
    public void applyLoadedRunSkillBonuses(SkillTreeBonuses skillBonuses) {
        applyTeamBonuses(skillBonuses, false);
    }

    /**
     * Applique les bonus de régénération post-combat aux Bugémons.
     *
     * @param skillBonuses bonus calculés depuis l'arbre de compétences.
     * @param team         équipe à soigner.
     */
    public void applyPostCombatRegeneration(SkillTreeBonuses skillBonuses, Team team) {
        int regenPercent = skillBonuses.getPostCombatRegenPercent();
        if (regenPercent <= 0 || team == null) {
            return;
        }

        for (BugemonInstance bugemon : team.getBugemons()) {
            int maxHp = bugemon.getEffectiveStats().getHealth();
            // Minimum 1 PV pour qu'un faible pourcentage reste perceptible.
            int regenAmount = (int) Math.max(1, Math.round(maxHp * (regenPercent / 100.0)));
            bugemon.heal(regenAmount);
        }
    }

    /**
     * Applique les bonus de statistiques globales de l'équipe.
     */
    private void applyTeamBonuses(SkillTreeBonuses skillBonuses, boolean restoreFullHp) {
        Team team = teamManagerController.getTeam();
        if (team == null) {
            return;
        }

        for (BugemonInstance bugemon : team.getBugemons()) {
            // Recalcul propre : on retire l'ancien bonus de run avant d'appliquer le nouveau.
            bugemon.clearRunBonus();
            bugemon.setRunBonus(skillBonuses.getTeamStatsBonus());
            if (restoreFullHp) {
                bugemon.restoreFullHp();
            }
        }
    }

    /**
     * Ajoute des objets bonus d'équipe de compétences à l'inventaire.
     */
    private void addBonusItemsForRun(SkillTreeBonuses skillBonuses) {
        for (Map.Entry<String, Integer> entry : skillBonuses.getBonusItemsByCategory().entrySet()) {
            List<ItemDefinition> matchingItems = itemsById.values().stream()
                    .filter(item -> entry.getKey().equalsIgnoreCase(item.getCategory()))
                    // Tri stable pour que les bonus soient reproductibles.
                    .sorted(java.util.Comparator.comparing(ItemDefinition::getId))
                    .toList();

            if (matchingItems.isEmpty()) {
                continue;
            }

            for (int i = 0; i < entry.getValue(); i++) {
                // Si la quantité dépasse le nombre d'objets, on boucle sur la catégorie.
                ItemDefinition item = matchingItems.get(i % matchingItems.size());
                teamManagerController.getInventory().addItem(item.getId(), 1);
            }
        }
    }
}

