package ulb.dto;

/**
 * Snapshot de l'état du combat utilisé par les vues de combat.
 */
public record BattleStateDTO(
        BugemonActiveStateDTO player, // État visuel du Bugemon allié actif.
        BugemonActiveStateDTO enemy   // État visuel du Bugemon adverse actif.
) {
}
