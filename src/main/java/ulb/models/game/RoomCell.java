package ulb.models.game;

import java.util.HashMap;
import java.util.Map;

/**
 * Représente une salle d'un graphe d'exploration, avec ses voisins.
 */
public class RoomCell {

    /**
     * Directions possibles de liaison entre deux salles.
     */
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT;

        /**
         * Retourne la direction opposée.
         *
         * @return direction opposée.
         */
        public Direction getOpposite() {
            return switch (this) {
                case UP -> DOWN;
                case DOWN -> UP;
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
            };
        }
    }

    private final String id;
    private final RoomType type;
    private final int x;
    private final int y;
    private boolean visited;
    private final Map<Direction, RoomCell> neighbors = new HashMap<>();

    /**
     * Construit une salle.
     *
     * @param id identifiant de la salle.
     * @param type type de la salle.
     */

    public RoomCell(String id, RoomType type, int x, int y) {
        this.id = id;
        this.type = type;
        this.x = x;
        this.y = y;
        this.visited = type == RoomType.START;
    }

    /**
     * Connecte cette salle à une autre dans une direction donnée,
     * en créant aussi la liaison inverse.
     *
     * @param dir direction de la connexion.
     * @param other salle voisine à connecter.
     */
    public void connectTo(Direction dir, RoomCell other) {
        if (other == null) {
            return;
        }

        this.neighbors.put(dir, other);

        if (other.getNeighbors().get(dir.getOpposite()) != this) {
            other.connectTo(dir.getOpposite(), this);
        }
    }

    /**
     * Retourne les voisins de la salle, indexés par direction.
     *
     * @return map direction -> salle voisine.
     */
    public Map<Direction, RoomCell> getNeighbors() {
        return neighbors;
    }

    /**
     * Indique si la salle est déjà visitée.
     *
     * @return true si la salle est visitée.
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * Modifie l'état de visite de la salle.
     *
     * @param visited nouvel état de visite.
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * Retourne l'identifiant de la salle.
     *
     * @return identifiant de salle.
     */
    public String getId() {
        return id;
    }

    /**
     * Retourne le type de la salle.
     *
     * @return type de salle.
     */
    public RoomType getType() {
        return type;
    }

    /**
     * Retourne la coordonnée horizontale de la salle sur la carte.
     *
     * @return coordonnée x.
     */
    public int getX() {
        return x;
    }

    /**
     * Retourne la coordonnée verticale de la salle sur la carte.
     *
     * @return coordonnée y.
     */
    public int getY() {
        return y;
    }
}
