package ulb.dto;

import java.util.List;

/**
 * Snapshot de l'équipe en cours d'édition dans l'écran de gestion.
 */
public record TeamStateDTO(
        List<String> memberNames, // Identifiants des Bugemons dans l'ordre de l'équipe.
        boolean isFull,           // Indique si l'équipe a atteint la taille maximale.
        int currentSize,          // Taille actuelle de l'équipe.
        int maxSize,              // Taille maximale autorisée.
        String currentTeamName)   // Nom de l'équipe courante s'il existe.
{
}
