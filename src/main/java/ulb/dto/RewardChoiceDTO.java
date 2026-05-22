package ulb.dto;

/**
 * Représentation générique d'une option affichable dans les écrans de récompense.
 * Contient uniquement les informations nécessaires à l'affichage et au choix.
 *
 * @param index index technique de l'option.
 * @param description libellé affichable.
 * @param selectable indique si l'option peut être choisie.
 * @param kind type métier de la récompense, ou null si non pertinent.
 */
public record RewardChoiceDTO(int index, String description, boolean selectable, RewardKind kind) {
}


