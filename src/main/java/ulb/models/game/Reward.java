package ulb.models.game;

import ulb.models.data.Attack;
import ulb.models.data.ItemDefinition;

/**
 * Récompense obtenue dans une salle de récompense.
 */
public sealed interface Reward permits Reward.Item, Reward.AttackReward, Reward.Stats {

    /**
     * Retourne la description affichable de la récompense.
     *
     * @return description lisible.
     */
    String getDescription();

    /**
     * Récompense ajoutant un objet à l'inventaire.
     *
     * @param item objet accordé.
     */
    record Item(ItemDefinition item) implements Reward {
        /**
         * Retourne la description affichable de l'objet accordé.
         *
         * @return description lisible.
         */
        public String getDescription() {
            return "Objet : " + item.getName();
        }

        /**
         * Ajoute l'objet à l'inventaire.
         *
         * @param inventory inventaire à modifier.
         */
        public void apply(Inventory inventory) {
            // Les récompenses d'objet ajoutent toujours un seul exemplaire.
            inventory.addItem(item.getId(), 1);
        }
    }

    /**
     * Récompense permettant d'apprendre une attaque.
     *
     * @param attack attaque accordée.
     */
    record AttackReward(Attack attack) implements Reward {
        /**
         * Retourne la description affichable de l'attaque accordée.
         *
         * @return description lisible.
         */
        public String getDescription() {
            return "Attaque : " + attack.getName();
        }

        /**
         * Indique si l'attaque peut être enseignée à ce Bugémon (même type que l'espèce).
         */
        public boolean canTeachTo(BugemonInstance bugemon) {
            // Règle actuelle : apprentissage limité au type du Bugémon.
            return bugemon.getSpecies().getType() == attack.getType();
        }

        /**
         * Remplace une attaque connue par la nouvelle attaque.
         *
         * @param bugemon Bugémon bénéficiaire.
         * @param attackIdToReplace identifiant de l'attaque à oublier.
         */
        public void apply(BugemonInstance bugemon, String attackIdToReplace) {
            // L'ordre oublie puis apprend pour conserver exactement quatre attaques.
            bugemon.forgetAttack(attackIdToReplace);
            bugemon.learnAttack(attack.getId());
        }
    }

    /**
     * Récompense ajoutant un bonus de statistiques permanent.
     *
     * @param bonus bonus accordé.
     */
    record Stats(StatBonus bonus) implements Reward {
        /**
         * Retourne la description affichable du bonus de statistiques.
         *
         * @return description lisible.
         */
        public String getDescription() {
            return "Stats : " + bonus.getDescription();
        }

        /**
         * Applique le bonus à un Bugémon.
         *
         * @param bugemon Bugémon bénéficiaire.
         */
        public void apply(BugemonInstance bugemon) {
            // Le bonus devient permanent sur l'instance ciblée.
            bonus.applyTo(bugemon);
        }
    }
}
