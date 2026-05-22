package ulb.controller.service;

import java.util.List;
import java.util.Map;

import ulb.models.game.Reward;
import ulb.models.game.RoomType;
import ulb.models.game.TowerNO;
import ulb.controller.TeamManagerController;
import ulb.models.data.Attack;
import ulb.models.data.ItemDefinition;

/**
 * Service d'accès aux options de récompenses pour la vue.
 */
public class RewardProvider {
    private final TowerNO tower;
    private final TeamManagerController teamManagerController;
    private final Map<String, Attack> attacksById;
    private final Map<String, ItemDefinition> itemsById;

    /**
     * Construit un fournisseur de récompenses pour la tour courante.
     *
     * @param tower                 tour en cours.
     * @param teamManagerController contrôleur donnant accès à l'équipe du joueur.
     * @param attacksById           attaques indexées par identifiant.
     * @param itemsById             objets indexés par identifiant.
     */
    public RewardProvider(TowerNO tower, TeamManagerController teamManagerController, Map<String, Attack> attacksById, Map<String, ItemDefinition> itemsById) {
        this.tower = tower;
        this.teamManagerController = teamManagerController;
        this.attacksById = attacksById;
        this.itemsById = itemsById;
    }

    /**
     * Retourne les options de récompense actuellement disponibles.
     *
     * @return options de récompense courantes.
     */
    public List<Reward> getCurrentRewardOptions() {
        if (tower.getCurrentMapRoom() == null || tower.getCurrentMapRoom().getType() != RoomType.REWARD) {
            return List.of();
        }

        List<Reward> rewards = tower.getFloorInstance().getRewardOptionsSnapshot();

        if (rewards.isEmpty()) {
            // Génération paresseuse : une salle garde les mêmes choix après affichage.
            tower.getFloorInstance().generateRewardOptions(attacksById, itemsById,
                    teamManagerController.getTeam());
            rewards = tower.getFloorInstance().getRewardOptionsSnapshot();
        }
        return List.copyOf(rewards);
    }
}


