package ulb.models.data;

/**
 * Effet supprimant les malus temporaires.
 */
public class ResetMalusEffect extends Effect {

    /**
     * Constructeur requis par Jackson pour la désérialisation.
     */
    public ResetMalusEffect() {
    }

    /**
     * Accepte le visiteur chargé de retirer les malus temporaires.
     *
     * @param visitor visiteur d'effet.
     */
    @Override
    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }
}
