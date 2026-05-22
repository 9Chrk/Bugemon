package ulb.view;

import ulb.dto.RewardChoiceDTO;

import java.util.List;

/**
 * Contrat exposé par le contrôleur pour piloter l'écran de récompense.
 */
public interface RewardActionHandler {
    /**
     * Retourne les récompenses proposées dans la salle courante.
     *
     * @return options de récompense affichables.
     */
    List<RewardChoiceDTO> getCurrentRewardOptions();

    /**
     * Retourne les cibles possibles pour une récompense donnée.
     *
     * @param rewardIndex index de la récompense sélectionnée.
     * @return cibles disponibles, ou liste vide si aucune cible n'est requise.
     */
    List<RewardChoiceDTO> getRewardTargets(int rewardIndex);

    /**
     * Retourne les attaques remplaçables d'un Bugémon ciblé.
     *
     * @param rewardIndex  index de la récompense d'attaque.
     * @param bugemonIndex index du Bugémon ciblé.
     * @return libellés des attaques remplaçables.
     */
    List<String> getRewardAttackLabels(int rewardIndex, int bugemonIndex);

    /**
     * Applique une récompense de type objet.
     *
     * @param choiceIndex index de la récompense choisie.
     */
    void applyRewardChoiceItem(int choiceIndex);

    /**
     * Applique une récompense de statistiques à un Bugémon.
     *
     * @param choiceIndex  index de la récompense choisie.
     * @param bugemonIndex index du Bugémon ciblé.
     */
    void applyRewardChoiceStats(int choiceIndex, int bugemonIndex);

    /**
     * Applique une récompense d'attaque en remplaçant une attaque existante.
     *
     * @param choiceIndex  index de la récompense choisie.
     * @param bugemonIndex index du Bugémon ciblé.
     * @param attackIndex  index de l'attaque à remplacer.
     */
    void applyRewardChoiceReplaceAttack(int choiceIndex, int bugemonIndex, int attackIndex);
}

