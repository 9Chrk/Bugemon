package ulb.dto;

import ulb.models.game.RoomType;

/**
 * Snapshot d'une salle de la carte d'étage pour l'affichage.
 */
public record FloorRoomDTO(
        String id,
        RoomType type,
        int x,
        int y,
        boolean visited,
        boolean current,
        boolean available) {
}
