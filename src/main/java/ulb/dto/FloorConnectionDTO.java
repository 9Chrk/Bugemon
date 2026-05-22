package ulb.dto;

/**
 * Représente une connexion entre deux salles de la carte d'étage.
 *
 * @param fromId identifiant de la première salle.
 * @param toId   identifiant de la seconde salle.
 */
public record FloorConnectionDTO(String fromId, String toId) {
}
