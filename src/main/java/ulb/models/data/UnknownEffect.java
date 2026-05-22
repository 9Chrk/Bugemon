package ulb.models.data;

/**
 * Effet non reconnu par le moteur de combat.
 */
public class UnknownEffect extends Effect {

    /**
     * Constructeur requis par Jackson pour la désérialisation.
     */
    public UnknownEffect() {
    }

    /**
     * Accepte le visiteur tout en conservant un comportement neutre.
     *
     * @param visitor visiteur d'effet.
     */
    @Override
    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }
}
