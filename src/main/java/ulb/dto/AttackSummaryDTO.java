package ulb.dto;

import ulb.models.data.Type;

/**
 * Données minimales d'une attaque nécessaires à l'interface.
 */
public record AttackSummaryDTO(
        String id,
        String name,
        Type type) {
}
