package ulb.models.game;

import ulb.models.data.Stats;

/**
 * Bonus de statistiques proposé en récompense ou appliqué à un Bugémon.
 *
 * @param statBonus statistiques à ajouter.
 */
public record StatBonus(Stats statBonus) {

    public StatBonus {
        if (statBonus == null) {
            throw new IllegalArgumentException("Stats cannot be null");
        }
    }

    // ----------------- Affichage -----------------

    /**
     * Retourne une description courte des statistiques ajoutées.
     *
     * @return texte lisible du bonus.
     */
    public String getDescription() {
        StringBuilder sb = new StringBuilder();

        // Seules les statistiques non nulles apparaissent dans le libellé final.
        if (statBonus.getHealth() != 0) {
            sb.append("+").append(statBonus.getHealth()).append(" HP ");
        }
        if (statBonus.getAttack() != 0) {
            sb.append("+").append(statBonus.getAttack()).append(" ATK ");
        }
        if (statBonus.getDefense() != 0) {
            sb.append("+").append(statBonus.getDefense()).append(" DEF ");
        }
        if (statBonus.getInitiative() != 0) {
            sb.append("+").append(statBonus.getInitiative()).append(" INIT ");
        }

        return sb.toString().trim();
    }

    // ----------------- Application -----------------

    /**
     * Applique le bonus aux statistiques permanentes d'un Bugémon.
     *
     * @param bugemon Bugémon bénéficiaire.
     */
    public void applyTo(BugemonInstance bugemon) {
        bugemon.applyPermanentBonus(statBonus);
    }
}
