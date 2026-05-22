package ulb.audio;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;

/**
 * Gère la lecture des musiques de fond et des sons ponctuels.
 */
public class AudioManager {

    private MediaPlayer currentLoop;
    private MediaPlayer currentOneShot;
    private String currentLoopPath;
    private final double loopVolume;

    /**
     * Construit un gestionnaire audio avec un volume de lecture donné.
     *
     * @param loopVolume volume appliqué aux sons lancés.
     */
    public AudioManager(double loopVolume) {
        this.loopVolume = loopVolume;
    }

    /**
     * Lance une ressource audio en boucle.
     *
     * @param resourcePath chemin de la ressource audio.
     */
    public void play(String resourcePath) {
        play(resourcePath, true);
    }

    /**
     * Lance une ressource audio en boucle ou une seule fois.
     *
     * @param resourcePath chemin de la ressource audio.
     * @param loop true pour une lecture en boucle.
     */
    public void play(String resourcePath, boolean loop) {
        if (resourcePath == null || resourcePath.isBlank()) return;
        // Évite de relancer la même musique de fond à chaque changement de vue.
        if (loop && resourcePath.equals(currentLoopPath) && currentLoop != null) return;

        // Un seul son est actif à la fois pour éviter les superpositions.
        stop(currentLoop);
        stop(currentOneShot);
        currentLoop = currentOneShot = null;
        currentLoopPath = null;

        MediaPlayer player = createPlayer(resourcePath);
        if (player == null) return;

        player.setCycleCount(loop ? MediaPlayer.INDEFINITE : 1);
        player.setVolume(loopVolume);

        if (loop) {
            currentLoop = player;
            currentLoopPath = resourcePath;
        } else {
            // Un son ponctuel se libère dès la fin de lecture.
            currentOneShot = player;
            player.setOnEndOfMedia(() -> {
                stop(currentOneShot);
                currentOneShot = null;
            });
        }

        player.play();
    }

    /**
     * Arrête tous les sons en cours et libère les lecteurs associés.
     */
    public void stop() {
        stop(currentLoop);
        stop(currentOneShot);
        currentLoop = currentOneShot = null;
        currentLoopPath = null;
    }

    private void stop(MediaPlayer player) {
        if (player != null) {
            player.stop();
            player.dispose();
        }
    }

    private MediaPlayer createPlayer(String resourcePath) {
        try {
            URL resource = getClass().getResource(resourcePath);
            if (resource == null) {
                System.err.println("[Audio] Ressource introuvable: " + resourcePath);
                return null;
            }
            MediaPlayer player = new MediaPlayer(new Media(resource.toExternalForm()));
            player.setOnError(() -> {
                if (player.getError() != null)
                    System.err.println("[Audio] Erreur MediaPlayer: " + player.getError().getMessage());
            });
            return player;
        } catch (Exception e) {
            System.err.println("[Audio] Impossible de charger " + resourcePath + " : " + e.getMessage());
            return null;
        }
    }
}
