package ulb.dto;

import java.util.List;

import ulb.models.data.Type;

/**
 * Snapshot d'un Bugémon actif pendant un combat.
 * Ce DTO fournit à l'interface toutes les données nécessaires à l'affichage.
 */
public record BugemonActiveStateDTO(
        String name,                 // Nom affiché dans l'interface.
        Type type,                   // Type utilisé pour l'icône ou la couleur.
        int hp,                      // Points de vie actuels.
        int maxHp,                   // Points de vie maximum.
        int damageTaken,             // Dégâts reçus affichés au dernier tour.
        int level,                   // Niveau actuel du Bugémon.
        String spritePath,           // Chemin du sprite à afficher.
        List<AttackSummaryDTO> attacks) // Attaques disponibles pour la vue.
{
}
