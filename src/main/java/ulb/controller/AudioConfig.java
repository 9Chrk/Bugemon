package ulb.controller;

/**
 * Centralise les chemins audio et les réglages de volume utilisés par l'application.
 */
public final class AudioConfig {
    public static final String MENU_MUSIC_PATH = "/audio/thalassophobia.mp3";
    public static final String LEVEL_SELECT_MUSIC_PATH = "/audio/thalassophobia.mp3";
    public static final String BATTLE_MUSIC_PATH = "/audio/hxh.mp3";
    public static final String BOSS_BATTLE_MUSIC_PATH = "/audio/hxh.mp3";
    public static final String WIN_MUSIC_PATH = "/audio/Win.mp3";
    public static final String BOSS_WIN_MUSIC_PATH = "/audio/jul.mp3";
    public static final String LOSE_MUSIC_PATH = "/audio/Lose.mp3";
    public static final String ENDING_MUSIC_PATH = "/audio/TheEnd.mp3";
    public static final double DEFAULT_MUSIC_VOLUME = 0.1;

    /**
     * Empêche l'instanciation de cette classe de constantes.
     */
    private AudioConfig() {
    }
}
