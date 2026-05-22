package ulb.dto;

import ulb.models.game.RoomType;

/**
 * Snapshot de la progression du joueur dans la Tour NO.
 */
public record TowerProgressDTO(
        int currentFloor,      // Étage actuel.
        int currentStep,       // Index de la salle actuelle sur la carte.
        int totalStepsInFloor, // Nombre de salles dans la carte.
        RoomType roomType,     // Type de salle courant.
        boolean isBossRoom,    // Indique si la salle est une salle de boss.
        String floorNameView   // Libellé prêt à être affiché pour l'étage.
) {
}
