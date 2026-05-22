package ulb.models.game;

import ulb.models.data.Difficulty;

/**
 * État d'une sauvegarde de partie dans un emplacement fixe (inventaire + tour + méta).
 */
public class GameSlotData {

    private String runName;
    private String teamName;
    private Integer towerLevel;
    private Integer towerFloor;
    private Difficulty difficulty;
    private Inventory inventory;

    /**
     * Constructeur vide requis pour la sérialisation JSON.
     */
    public GameSlotData() {
    }

    /**
     * Indique si cet emplacement contient une progression chargeable.
     *
     * @return true si l'emplacement contient une run chargeable.
     */
    public boolean isOccupied() {
        return teamName != null && !teamName.isBlank()
                && towerLevel != null && towerFloor != null;
    }

    /**
     * Retourne le nom affiché de la run.
     *
     * @return nom de run.
     */
    public String getRunName() {
        return runName;
    }

    /**
     * Définit le nom affiché de la run.
     *
     * @param runName nom de run.
     */
    public void setRunName(String runName) {
        this.runName = runName;
    }

    /**
     * Retourne le nom de l'équipe associée.
     *
     * @return nom d'équipe.
     */
    public String getTeamName() {
        return teamName;
    }

    /**
     * Définit le nom de l'équipe associée.
     *
     * @param teamName nom d'équipe.
     */
    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    /**
     * Retourne l'étage sauvegardé.
     *
     * @return étage sauvegardé.
     */
    public Integer getTowerLevel() {
        return towerLevel;
    }

    /**
     * Définit l'étage sauvegardé.
     *
     * @param towerLevel étage sauvegardé.
     */
    public void setTowerLevel(Integer towerLevel) {
        this.towerLevel = towerLevel;
    }

    /**
     * Retourne l'étape sauvegardée dans l'étage.
     *
     * @return étape sauvegardée.
     */
    public Integer getTowerFloor() {
        return towerFloor;
    }

    /**
     * Définit l'étape sauvegardée dans l'étage.
     *
     * @param towerFloor étape sauvegardée.
     */
    public void setTowerFloor(Integer towerFloor) {
        this.towerFloor = towerFloor;
    }

    /**
     * Retourne l'inventaire sauvegardé.
     *
     * @return inventaire sauvegardé.
     */
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * Définit l'inventaire sauvegardé.
     *
     * @param inventory inventaire sauvegardé.
     */
    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    /**
     * Retourne la difficulté sauvegardée.
     *
     * @return difficulté de la run.
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Définit la difficulté sauvegardée.
     *
     * @param difficulty difficulté de la run.
     */
    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    /**
     * Réinitialise l'emplacement (partie terminée ou effacement).
     */
    public void clear() {
        this.runName = null;
        this.teamName = null;
        this.towerLevel = null;
        this.towerFloor = null;
        this.difficulty = null;
        this.inventory = null;
    }
}
