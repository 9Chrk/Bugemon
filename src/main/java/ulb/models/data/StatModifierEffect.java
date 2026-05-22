package ulb.models.data;

/**
 * Effet modifiant une statistique.
 */
public class StatModifierEffect extends Effect {

    /**
     * Constructeur requis par Jackson pour la désérialisation.
     */
    public StatModifierEffect() {
    }

    /**
     * Accepte le visiteur chargé d'appliquer le modificateur de statistique.
     *
     * @param visitor visiteur d'effet.
     */
    @Override
    public void accept(EffectVisitor visitor) {
        visitor.visit(this);
    }
}
