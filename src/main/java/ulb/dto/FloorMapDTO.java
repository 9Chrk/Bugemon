package ulb.dto;

import java.util.List;

/**
 * Snapshot complet de la carte d'un étage.
 *
 * @param currentFloor numéro de l'étage courant.
 * @param currentRoomId identifiant de la salle où se trouve le joueur.
 * @param rooms         liste des salles de l'étage.
 * @param connections   liste des connexions entre salles voisines.
 */
public record FloorMapDTO(
        int currentFloor,
        String currentRoomId,
        List<FloorRoomDTO> rooms,
        List<FloorConnectionDTO> connections
) {
}
