package ulb.dto;

import java.util.ArrayList;

import ulb.models.data.Type;

/**
 * Synthèse des statistiques de base d'un Bugémon pour les écrans de sélection.
 */
public record BugemonStatsDTO(
        Type type,                 // Type d'origine.
        int hp,                    // Points de vie de base.
        int attack,                // Attaque de base.
        int defense,               // Défense de base.
        int initiative,            // Initiative de base.
        ArrayList<String> moves)   // Liste des attaques accessibles à la sélection.
{
}
