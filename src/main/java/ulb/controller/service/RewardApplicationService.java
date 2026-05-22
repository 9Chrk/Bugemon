package ulb.controller.service;

import ulb.controller.TeamManagerController;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Reward;
import ulb.models.game.TowerNO;

/**
 * Service d'application des récompenses obtenues à chaque étage.
 */
public class RewardApplicationService {
    private final TeamManagerController teamManagerController;
    private TowerNO tower;

    /**
     * Construit le service d'application des récompenses.
     *
     * @param teamManagerController contrôleur donnant accès à l'équipe et l'inventaire.
     * @param tower                 tour contenant les récompenses courantes.
     */
    public RewardApplicationService(TeamManagerController teamManagerController, TowerNO tower) {
        this.teamManagerController = teamManagerController;
        this.tower = tower;
    }

    /**
     * Met à jour la tour utilisée pour lire et appliquer les récompenses.
     *
     * @param tower nouvelle tour active.
     */
    public void setTower(TowerNO tower) {
        this.tower = tower;
    }

    /**
     * Applique une récompense de type objet choisie par le joueur.
     *
     * @param choiceIndex index de la récompense choisie.
     */
    public void applyRewardChoiceItem(int choiceIndex) {
        Reward.Item reward = (Reward.Item) tower.getFloorInstance().getRewardOptionsSnapshot().get(choiceIndex);
        reward.apply(teamManagerController.getInventory());
        // Appel conservé pour compatibilité avec l'ancien flux linéaire.
        tower.advanceToNextStep();
    }

    /**
     * Applique une récompense de statistiques permanentes sur un Bugémon ciblé.
     *
     * @param choiceIndex  index de la récompense choisie.
     * @param bugemonIndex index du Bugémon ciblé.
     */
    public void applyRewardChoiceStats(int choiceIndex, int bugemonIndex) {
        Reward.Stats reward = (Reward.Stats) tower.getFloorInstance().getRewardOptionsSnapshot().get(choiceIndex);
        BugemonInstance bugemon = teamManagerController.getTeam().getBugemonAt(bugemonIndex);
        reward.apply(bugemon);
        // Les bonus permanents restent dans l'instance active de l'équipe.
        tower.advanceToNextStep();
    }

    /**
     * Applique une récompense d'attaque en remplaçant une attaque existante.
     *
     * @param choiceIndex  index de la récompense choisie.
     * @param bugemonIndex index du Bugémon ciblé.
     * @param attackIndex  index de l'attaque remplacée.
     */
    public void applyRewardChoiceReplaceAttack(int choiceIndex, int bugemonIndex, int attackIndex) {
        Reward.AttackReward reward = (Reward.AttackReward) tower.getFloorInstance().getRewardOptionsSnapshot().get(choiceIndex);
        BugemonInstance bugemon = teamManagerController.getTeam().getBugemonAt(bugemonIndex);
        // On garde l'ancien identifiant pour remplacer exactement le slot choisi.
        String attackIdToReplace = bugemon.getLearnedAttackIds().get(attackIndex);
        reward.apply(bugemon, attackIdToReplace);
        tower.advanceToNextStep();
    }
}

