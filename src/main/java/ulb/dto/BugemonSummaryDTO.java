package ulb.dto;

/**
 * Entrée simplifiée d'un Bugémon affichée dans les menus de sélection.
 */
public record BugemonSummaryDTO(
        String id,           // Identifiant technique du Bugémon.
        String name,         // Nom lisible par le joueur.
        String spritePath,   // Chemin du sprite d'aperçu.
        BugemonStatsDTO stats) // Statistiques de base affichables.
{
}
