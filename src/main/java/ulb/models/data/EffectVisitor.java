package ulb.models.data;

/**
 * Visiteur permettant d'exécuter le bon comportement selon le type concret d'effet.
 */
public interface EffectVisitor {

    void visit(HealingEffect effect);

    void visit(StatModifierEffect effect);

    void visit(ResetMalusEffect effect);

    void visit(UnknownEffect effect);
}
