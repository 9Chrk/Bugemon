package ulb.controller.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ulb.models.data.Attack;
import ulb.models.data.Type;

/**
 * Service de fournisseur d'information sur les attaques.
 */
public class AttackProvider {
    private final Map<String, Attack> attacksById;

    /**
     * Construit un fournisseur d'attaques à partir du registre chargé.
     *
     * @param attacksById attaques indexées par identifiant.
     */
    public AttackProvider(Map<String, Attack> attacksById) {
        this.attacksById = attacksById;
    }

    /**
     * Retourne les noms lisibles des attaques disponibles pour un type donné.
     *
     * @param typeName nom du type à filtrer.
     * @return noms des attaques de ce type.
     * Retourne le nom lisible d'une attaque à partir de son identifiant.
     */
    public String getAttackDisplayName(String attackId) {
        Attack attack = attacksById.get(attackId);
        if (attack != null) {
            return attack.getName();
        }
        return attackId == null ? "" : attackId.replace('_', ' ');
    }

    /**
     * Retourne les noms lisibles des attaques disponibles pour un type donné.
     */
    public List<String> getAttackNamesByType(String typeName) {
        if (typeName == null || typeName.isEmpty())
            return new ArrayList<>();

        Type selectedType = Type.valueOf(typeName);

        return attacksById.values().stream()
                .filter(attack -> attack.getType() == selectedType)
                .map(Attack::getName)
                .toList();
    }
}

