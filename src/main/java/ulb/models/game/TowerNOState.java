package ulb.models.game;

/**
 * Instantané sérialisable de la progression dans la Tour NO.
 * `towerLevel` correspond à l'étage courant, `towerFloor` à l'index de salle
 * dans la carte de cet étage.
 */
public record TowerNOState(int towerLevel, int towerFloor) {
    /**
     * Construit un état de tour sérialisable.
     *
     * @param towerLevel étage courant.
     * @param towerFloor index de salle courant dans la carte.
     */
    public TowerNOState {
    }

    /**
     * Retourne l'étage courant.
     *
     * @return étage courant.
     */
    @Override
    public int towerLevel() {
        return towerLevel;
    }

    /**
     * Retourne l'index de salle courant dans la carte.
     *
     * @return index de salle.
     */
    @Override
    public int towerFloor() {
        return towerFloor;
    }

    /**
     * Reconstruit une instance de tour à partir de cet état.
     *
     * @return instance de `TowerNO` alignée avec l'état stocké.
     */
    public TowerNO toTower() {
        return new TowerNO(towerLevel, towerFloor);
    }
}
