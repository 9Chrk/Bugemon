package ulb.models.game;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ulb.dto.FloorConnectionDTO;
import ulb.dto.FloorMapDTO;
import ulb.dto.FloorRoomDTO;

/**
 * Représente la progression du joueur dans la Tour NO.
 */
public class TowerNO {
    private static final String TOWER_COMPLETED_MESSAGE = "Vous avez terminé la tour.";
    /** Premier étage jouable (sauvegarde / reprise). */
    public static final int FIRST_PLAYABLE_FLOOR = 2;
    private int currentFloor;
    private FloorInstance floorInstance;
    private final int MAX_FLOORS = 10;
    private final int MIN_FLOORS = FIRST_PLAYABLE_FLOOR;
    private String lastStatusMessage;

    /**
     * Construit une nouvelle partie en commençant à l'étage minimal.
     */
    public TowerNO() {
        this.currentFloor = MIN_FLOORS; // Démarre à l'étage minimal.
        this.floorInstance = new FloorInstance();
        this.lastStatusMessage = null;
    }

    /**
     * Construit une tour à partir d'un étage et d'une étape sauvegardés.
     *
     * @param savedFloor étage sauvegardé.
     * @param savedStep  étape sauvegardée dans l'étage.
     */
    public TowerNO(int savedFloor, int savedStep) {
        if (savedFloor < MIN_FLOORS || savedFloor > MAX_FLOORS) {
            throw new IllegalArgumentException(
                    "Floor number must be between " + MIN_FLOORS + " and " + MAX_FLOORS + ".");
        }
        this.currentFloor = savedFloor;
        this.floorInstance = new FloorInstance(savedStep);
        this.lastStatusMessage = null;
    }

    /**
     * Retourne l'étage courant.
     *
     * @return étage courant.
     */
    public int getCurrentFloor() {
        return currentFloor;
    }

    /**
     * Retourne l'instance d'étage courante.
     *
     * @return instance d'étage.
     */
    public FloorInstance getFloorInstance() {
        return floorInstance;
    }

    /**
     * Retourne la salle courante de la carte H14.
     *
     * @return salle courante sur la carte.
     */
    public RoomCell getCurrentMapRoom() {
        return floorInstance.getCurrentRoom();
    }

    /**
     * Retourne le type de salle courant.
     *
     * @return type de salle.
     */
    public RoomType getCurrentRoomType() {
        return floorInstance.getCurrentRoomType();
    }

    /**
     * Déplace le joueur sur la carte H14 et retourne le type de la salle atteinte.
     *
     * @param roomId identifiant de la salle cible.
     * @return type de la salle atteinte, ou null si le déplacement est refusé.
     */
    public RoomType enterRoom(String roomId) {
        if (!floorInstance.moveTo(roomId)) {
            return null;
        }
        RoomType roomType = floorInstance.getCurrentRoom().getType();
        return roomType;
    }

    /**
     * Construit un instantané lisible de la carte d'étage pour la vue H14.
     *
     * @return DTO de carte.
     */
    public FloorMapDTO getFloorMap() {
        String currentRoomId = floorInstance.getCurrentRoom().getId();
        List<FloorRoomDTO> rooms = floorInstance.getRoomsById().values().stream()
                .map(room -> new FloorRoomDTO(
                        room.getId(),
                        room.getType(),
                        room.getX(),
                        room.getY(),
                        room.isVisited(),
                        room.getId().equals(currentRoomId),
                        floorInstance.canMoveTo(room.getId())))
                .toList();

        // Construire les connexions depuis les voisins du modèle
        List<FloorConnectionDTO> connections = getFloorConnectionDTOS();

        return new FloorMapDTO(currentFloor, currentRoomId, rooms, connections);
    }

    private List<FloorConnectionDTO> getFloorConnectionDTOS() {
        List<FloorConnectionDTO> connections = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (RoomCell room : floorInstance.getRoomsById().values()) {
            for (RoomCell neighbor : room.getNeighbors().values()) {
                // Une connexion est bidirectionnelle : on ne l'ajoute qu'une seule fois.
                String key = room.getId().compareTo(neighbor.getId()) < 0
                        ? room.getId() + "-" + neighbor.getId()
                        : neighbor.getId() + "-" + room.getId();
                if (seen.add(key)) {
                    connections.add(new FloorConnectionDTO(room.getId(), neighbor.getId()));
                }
            }
        }
        return connections;
    }

    /**
     * Retourne l'étape courante dans l'étage.
     *
     * @return index d'étape courant.
     */
    public int getCurrentStep() {
        return floorInstance.getCurrentStep();
    }

    /**
     * Indique si la salle courante est une salle de boss.
     *
     * @return true si la salle courante est un boss.
     */
    public boolean isBossRoom() {
        return floorInstance.isBossRoom();
    }

    /**
     * Indique si l'étage courant est le dernier étage de la tour.
     *
     * @return true si l'étage courant est l'étage final.
     */
    public boolean isFinalBossFloor() {
        return currentFloor == MAX_FLOORS;
    }

    /**
     * Ancien point d'entrée de progression, conservé pour compatibilité.
     * La progression dans l'étage se fait désormais par {@link #enterRoom(String)}.
     */
    public void advanceToNextStep() {
        if (floorInstance.isCompleted()) {
            advanceToNextFloor();
        }
    }

    /**
     * Fait avancer à l'étage suivant si possible.
     */
    public void advanceToNextFloor() {
        if (currentFloor < MAX_FLOORS) {
            currentFloor++;
            floorInstance = new FloorInstance();
            lastStatusMessage = null;
        } else {
            lastStatusMessage = TOWER_COMPLETED_MESSAGE;
        }
    }

    /**
     * Indique si la tour est entièrement terminée.
     *
     * @return true si l'étage final est atteint et complété.
     */
    public boolean isTowerCompleted() {
        return currentFloor == MAX_FLOORS && floorInstance.isCompleted();
    }

    /**
     * Applique la logique de progression à la fin d'un combat.
     *
     * @param playerWon true si le joueur a gagné le combat.
     */
    public void processBattleResult(boolean playerWon) {
        if (!playerWon) {
            return;
        }

        // En cas de victoire, on distingue un combat de boss d'un combat standard.
        if (isBossRoom()) {
            if (isFinalBossFloor()) {
                // Dernier boss : on marque la tour comme terminée au lieu de créer un étage.
                floorInstance.markCompleted();
            } else {
                advanceToNextFloor();
            }
        } else {
            // Les salles classiques gardent la progression dans le même étage.
            advanceToNextStep();
        }
    }

    /**
     * Consomme le dernier message d'état de la tour.
     *
     * @return message de statut, puis réinitialisation à null.
     */
    public String consumeLastStatusMessage() {
        String message = lastStatusMessage;
        lastStatusMessage = null;
        return message;
    }
}
