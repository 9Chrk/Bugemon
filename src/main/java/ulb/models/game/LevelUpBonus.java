package ulb.models.game;

import ulb.models.data.Stats;

/**
 * Bonus de statistiques permanent accordé à la montée de niveau.
 */
public record LevelUpBonus(Stats statsBonus, String description) {
    /**
     * Construit un bonus de niveau.
     *
     * @param statsBonus  statistiques à ajouter.
     * @param description description affichable du bonus.
     */
    public LevelUpBonus(Stats statsBonus, String description) {
        if (statsBonus == null || description == null || description.isEmpty()) {
            throw new IllegalArgumentException("Bonus stats and description must be provided.");
        }
        this.statsBonus = statsBonus.copy();
        this.description = description;
    }

    /**
     * Applique le bonus au Bugémon ciblé.
     *
     * @param bugemon Bugémon bénéficiaire.
     */
    public void applyTo(BugemonInstance bugemon) {
        if (bugemon == null) {
            throw new IllegalArgumentException("Bugemon cannot be null.");
        }
        bugemon.applyPermanentBonus(statsBonus);
    }

    /**
     * Retourne la description du bonus.
     *
     * @return description affichable.
     */
    @Override
    public String description() {
        return description;
    }
}
