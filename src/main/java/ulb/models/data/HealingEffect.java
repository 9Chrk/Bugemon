package ulb.models.data;

/**
 * Effet de soin.
 */
public class HealingEffect extends Effect {

    /**
     * Constructeur requis par Jackson pour la désérialisation.
     */
    public HealingEffect() {
    }

    /**
     * Accepte le visiteur chargé d'appliquer l'effet de soin.
     *
     * @param visitor visiteur d'effet.
     */
    @Override
    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }
}
