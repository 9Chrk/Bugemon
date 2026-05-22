package ulb.dto;

/**
 * Représentation légère d'un emplacement de partie pour l'affichage.
 *
 * @param index index du slot.
 * @param occupied indique si le slot contient une progression.
 * @param displayText texte à afficher pour ce slot.
 */
public record RunSlotDTO(int index, boolean occupied, String displayText) {
}

