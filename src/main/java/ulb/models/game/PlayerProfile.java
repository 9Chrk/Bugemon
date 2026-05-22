package ulb.models.game;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import ulb.models.data.Difficulty;
import ulb.models.skilltree.SkillTreeProgress;

/**
 * Représente le profil du joueur, ses équipes sauvegardées et son état de progression.
 */
public class PlayerProfile {

    public static final int RUN_SLOT_COUNT = 5;

    private Team currentTeam;
    private String currentTeamName;
    private final Map<String, Team> savedTeams;
    private GameSlotData[] runSlots;
    private int activeRunSlot;
    private SkillTreeProgress skillTreeProgress;
    private String lastPersistenceMessage;
    private static final String SAVE_FILE = "teams_save.json";

    /**
     * Structure sérialisée contenant les équipes, les runs et la progression persistante.
     */
    private static class SaveData {
        Map<String, Team> savedTeams;
        String currentTeamName;
        /** @deprecated Ancien format ; migré vers {@code runSlots[0]}. */
        Integer towerLevel;
        Integer towerFloor;
        Inventory inventory;
        GameSlotData[] runSlots;
        Integer activeRunSlot;
        Integer availableSkillPoints;
        Map<String, Integer> allocatedSkillLevels;
    }

    /**
     * Construit un profil joueur et tente de charger les données persistées.
     */
    public PlayerProfile() {
        this.savedTeams = new HashMap<>();
        this.currentTeam = null;
        this.currentTeamName = null;
        this.runSlots = new GameSlotData[RUN_SLOT_COUNT];
        Arrays.setAll(this.runSlots, i -> new GameSlotData());
        this.activeRunSlot = 0;
        this.skillTreeProgress = new SkillTreeProgress();
        this.lastPersistenceMessage = null;
        loadFromDisk();
    }

    /**
     * Construit un profil en mémoire à partir d'équipes déjà connues.
     *
     * @param teams           équipes sauvegardées à copier.
     * @param currentTeamName nom de l'équipe active.
     */
    public PlayerProfile(Map<String, Team> teams, String currentTeamName) {
        if (teams == null) {
            this.savedTeams = new HashMap<>();
        } else {
            this.savedTeams = new HashMap<>(teams);
        }

        this.runSlots = new GameSlotData[RUN_SLOT_COUNT];
        Arrays.setAll(this.runSlots, i -> new GameSlotData());
        this.activeRunSlot = 0;
        this.skillTreeProgress = new SkillTreeProgress();
        this.currentTeamName = currentTeamName;
        this.lastPersistenceMessage = null;

        if (currentTeamName != null && this.savedTeams.containsKey(currentTeamName)) {
            this.currentTeam = new Team(this.savedTeams.get(currentTeamName));
        } else {
            this.currentTeam = null;
        }
    }

    private void validateSlotIndex(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= RUN_SLOT_COUNT) {
            throw new IllegalArgumentException("Slot index must be between 0 and " + (RUN_SLOT_COUNT - 1));
        }
    }

    /**
     * Retourne l'état d'un emplacement de partie (0 à {@value RUN_SLOT_COUNT}-1).
     *
     * @param slotIndex index de l'emplacement.
     * @return données de l'emplacement.
     */
    public GameSlotData getRunSlot(int slotIndex) {
        validateSlotIndex(slotIndex);
        return runSlots[slotIndex];
    }

    /**
     * Retourne l'emplacement de run actuellement actif.
     *
     * @return index de l'emplacement actif.
     */
    public int getActiveRunSlot() {
        return activeRunSlot;
    }

    /**
     * Définit l'emplacement de run actif.
     *
     * @param slotIndex index de l'emplacement à activer.
     */
    public void setActiveRunSlot(int slotIndex) {
        validateSlotIndex(slotIndex);
        this.activeRunSlot = slotIndex;
    }

    /**
     * Au moins une partie sauvegardée peut être chargée depuis le menu.
     *
     * @return true si au moins un emplacement est occupé.
     */
    public boolean hasAnyLoadableRun() {
        for (int i = 0; i < RUN_SLOT_COUNT; i++) {
            if (runSlots[i].isOccupied()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retourne l'index de l'emplacement actif s'il est chargeable, sinon le premier
     * emplacement occupé.
     *
     * @return index de slot, ou -1 si aucun slot n'est chargeable.
     */
    public int getPreferredLoadableRunSlotIndex() {
        if (runSlots[activeRunSlot].isOccupied()) {
            return activeRunSlot;
        }
        for (int i = 0; i < RUN_SLOT_COUNT; i++) {
            if (runSlots[i].isOccupied()) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Prépare un emplacement pour une nouvelle partie (nom + équipe), inventaire de départ.
     *
     * @param slotIndex index de l'emplacement à préparer.
     * @param runName   nom de la partie.
     * @param teamName  nom de l'équipe associée.
     */
    public void prepareNewRunAtSlot(int slotIndex, String runName, String teamName) {
        prepareNewRunAtSlot(slotIndex, runName, teamName, Difficulty.NORMAL);
    }

    /**
     * Prépare un emplacement pour une nouvelle partie (nom + équipe + difficulté),
     * inventaire de départ.
     *
     * @param slotIndex  index de l'emplacement à préparer.
     * @param runName    nom de la partie.
     * @param teamName   nom de l'équipe associée.
     * @param difficulty difficulté choisie.
     */
    public void prepareNewRunAtSlot(int slotIndex, String runName, String teamName, Difficulty difficulty) {
        validateSlotIndex(slotIndex);
        if (runName == null || runName.isBlank()) {
            throw new IllegalArgumentException("Run name cannot be null or empty.");
        }
        if (teamName == null || teamName.isBlank()) {
            throw new IllegalArgumentException("Team name cannot be null or empty.");
        }
        Difficulty resolvedDifficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        GameSlotData slot = runSlots[slotIndex];
        slot.clear();
        slot.setRunName(runName.trim());
        slot.setTeamName(teamName);
        slot.setDifficulty(resolvedDifficulty);
        slot.setInventory(new Inventory());
        this.activeRunSlot = slotIndex;
    }

    /**
     * Vide l'emplacement de partie actif (plus de sauvegarde chargeable pour cette partie).
     */
    public void clearActiveRunSlot() {
        runSlots[activeRunSlot].clear();
        saveToDisk();
    }

    /**
     * Réinitialise la progression de la tour sur l'emplacement actif (même nom d'équipe / de partie).
     */
    public void resetActiveRunToFreshTower() {
        GameSlotData slot = runSlots[activeRunSlot];
        if (slot.getTeamName() == null || slot.getTeamName().isBlank()) {
            return;
        }
        slot.setTowerLevel(TowerNO.FIRST_PLAYABLE_FLOOR);
        slot.setTowerFloor(0);
        ensureInventoryForSlot(activeRunSlot).resetToStartingInventory();
        saveToDisk();
    }

    /**
     * Sauvegarde l'équipe courante sous un nom donné.
     *
     * @param teamName nom de sauvegarde.
     * @return true si l'équipe a été sauvegardée.
     */
    public boolean saveTeam(String teamName) {
        if (teamName == null || teamName.isEmpty() || currentTeam == null) {
            return false;
        }

        Team snapshot = new Team(currentTeam);
        snapshot.setName(teamName);
        savedTeams.put(teamName, snapshot);
        this.currentTeam = new Team(snapshot);
        this.currentTeamName = teamName;
        saveToDisk();
        return true;
    }

    /**
     * Charge une équipe sauvegardée comme équipe courante.
     *
     * @param teamName nom de l'équipe à charger.
     * @return true si l'équipe existe et a été chargée.
     */
    public boolean loadTeam(String teamName) {
        Team team = savedTeams.get(teamName);
        if (team == null) {
            return false;
        }

        this.currentTeam = new Team(team);
        this.currentTeamName = teamName;
        saveToDisk();
        return true;
    }

    /**
     * Supprime une équipe sauvegardée et les runs qui l'utilisent.
     *
     * @param teamName nom de l'équipe à supprimer.
     * @return true si une équipe a été supprimée.
     */
    public boolean deleteTeam(String teamName) {
        if (teamName == null || teamName.isEmpty()) {
            return false;
        }

        boolean removed = savedTeams.remove(teamName) != null;
        if (removed) {
            if (teamName.equals(currentTeamName)) {
                currentTeam = null;
                currentTeamName = null;
            }
            for (GameSlotData slot : runSlots) {
                if (teamName.equals(slot.getTeamName())) {
                    slot.clear();
                }
            }
            saveToDisk();
        }
        return removed;
    }

    /**
     * Écrit le profil joueur sur disque.
     */
    public void saveToDisk() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(SAVE_FILE)) {
            SaveData data = new SaveData();
            data.savedTeams = this.savedTeams;
            data.currentTeamName = this.currentTeamName;
            data.runSlots = this.runSlots;
            data.activeRunSlot = this.activeRunSlot;
            data.availableSkillPoints = this.skillTreeProgress.getAvailablePoints();
            data.allocatedSkillLevels = this.skillTreeProgress.getAllocatedLevelsSnapshot();
            gson.toJson(data, writer);
            lastPersistenceMessage = null;
        } catch (IOException e) {
            lastPersistenceMessage = "Erreur lors de la sauvegarde : " + e.getMessage();
        }
    }

    private void loadFromDisk() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return;
        }

        Gson gson = new Gson();
        try {
            String json = Files.readString(file.toPath());

            SaveData loadedData = gson.fromJson(json, SaveData.class);
            if (loadedData != null && loadedData.savedTeams != null) {
                // Format actuel : profil complet avec slots de run.
                this.savedTeams.clear();
                this.savedTeams.putAll(loadedData.savedTeams);

                this.currentTeamName = null;
                this.currentTeam = null;

                if (loadedData.runSlots != null && loadedData.runSlots.length == RUN_SLOT_COUNT) {
                    this.runSlots = loadedData.runSlots;
                    for (int i = 0; i < RUN_SLOT_COUNT; i++) {
                        if (this.runSlots[i] == null) {
                            this.runSlots[i] = new GameSlotData();
                        }
                        normalizeDifficulty(this.runSlots[i]);
                    }
                    this.activeRunSlot = loadedData.activeRunSlot != null
                            ? Math.min(RUN_SLOT_COUNT - 1, Math.max(0, loadedData.activeRunSlot))
                            : 0;
                } else {
                    // Anciennes sauvegardes : tour et inventaire étaient stockés hors slots.
                    migrateLegacyTowerToSlots(loadedData);
                }
                this.skillTreeProgress = new SkillTreeProgress(
                        loadedData.availableSkillPoints == null ? 0 : loadedData.availableSkillPoints,
                        loadedData.allocatedSkillLevels == null ? Map.of() : loadedData.allocatedSkillLevels);

                lastPersistenceMessage = null;
                return;
            }

            Type mapType = new TypeToken<HashMap<String, Team>>() {
            }.getType();
            // Très ancien format : le fichier contenait seulement la map d'équipes.
            Map<String, Team> loadedTeams = gson.fromJson(json, mapType);
            if (loadedTeams != null) {
                this.savedTeams.clear();
                this.savedTeams.putAll(loadedTeams);
            }

            this.currentTeamName = null;
            this.currentTeam = null;
            this.activeRunSlot = 0;
            this.skillTreeProgress = new SkillTreeProgress();
            lastPersistenceMessage = null;
        } catch (IOException e) {
            lastPersistenceMessage = "Erreur lors du chargement : " + e.getMessage();
        }
    }

    private void migrateLegacyTowerToSlots(SaveData loadedData) {
        Arrays.setAll(this.runSlots, i -> new GameSlotData());
        this.activeRunSlot = 0;
        if (loadedData.towerLevel != null && loadedData.towerFloor != null) {
            // La progression historique est déplacée dans le premier slot.
            GameSlotData s0 = runSlots[0];
            s0.setRunName("Partie");
            String team = loadedData.currentTeamName;
            if (team == null || team.isBlank()) {
                team = savedTeams.keySet().stream().sorted().findFirst().orElse(null);
            }
            s0.setTeamName(team);
            s0.setTowerLevel(loadedData.towerLevel);
            s0.setTowerFloor(loadedData.towerFloor);
            s0.setDifficulty(Difficulty.NORMAL);
            s0.setInventory(loadedData.inventory != null ? loadedData.inventory : new Inventory());
        } else if (loadedData.inventory != null) {
            runSlots[0].setInventory(loadedData.inventory);
        }
    }

    private void normalizeDifficulty(GameSlotData slot) {
        if (slot != null && slot.isOccupied() && slot.getDifficulty() == null) {
            slot.setDifficulty(Difficulty.NORMAL);
        }
    }

    /**
     * Retourne l'équipe courante du profil.
     *
     * @return équipe courante, ou null si aucune équipe n'est active.
     */
    public Team getCurrentTeam() {
        return currentTeam;
    }

    /**
     * Définit l'équipe courante sans l'associer à une sauvegarde existante.
     *
     * @param team équipe à utiliser.
     */
    public void setCurrentTeam(Team team) {
        this.currentTeam = team;
        this.currentTeamName = null;
    }

    /**
     * Persiste la progression de l'équipe courante dans sa sauvegarde.
     */
    public void persistCurrentTeamProgress() {
        if (currentTeam == null) {
            return;
        }

        if (currentTeamName != null && savedTeams.containsKey(currentTeamName)) {
            // On remplace la sauvegarde par un snapshot pour éviter les références partagées.
            Team snapshot = new Team(currentTeam);
            snapshot.setName(currentTeamName);
            savedTeams.put(currentTeamName, snapshot);
        }

        saveToDisk();
    }

    /**
     * Retourne les équipes sauvegardées.
     *
     * @return copie immuable des équipes par nom.
     */
    public Map<String, Team> getSavedTeams() {
        return Map.copyOf(savedTeams);
    }

    /**
     * Retourne le nom de l'équipe courante.
     *
     * @return nom de l'équipe courante, ou null.
     */
    public String getCurrentTeamName() {
        return currentTeamName;
    }

    /**
     * Attache l'état de la tour courante à l'emplacement actif.
     *
     * @param tower tour dont la progression doit être persistée.
     */
    public void attachTowerState(TowerNO tower) {
        if (tower == null) {
            throw new IllegalArgumentException("Tower cannot be null.");
        }
        GameSlotData slot = runSlots[activeRunSlot];
        slot.setTowerLevel(tower.getCurrentFloor());
        slot.setTowerFloor(tower.getCurrentStep());
    }

    /**
     * Retourne l'étage courant stocké dans l'emplacement actif.
     *
     * @return étage courant, ou null si aucun run n'est actif.
     */
    public Integer getTowerLevel() {
        return runSlots[activeRunSlot].getTowerLevel();
    }

    /**
     * Retourne l'index de salle courant stocké dans l'emplacement actif.
     *
     * @return index de salle, ou null si aucun run n'est actif.
     */
    public Integer getTowerFloor() {
        return runSlots[activeRunSlot].getTowerFloor();
    }

    /**
     * Retourne l'état de tour de l'emplacement actif.
     *
     * @return état de tour, ou null si l'emplacement n'en contient pas.
     */
    public TowerNOState getTowerState() {
        return getTowerStateForSlot(activeRunSlot);
    }

    /**
     * Retourne l'état de tour d'un emplacement précis.
     *
     * @param slotIndex index de l'emplacement.
     * @return état de tour, ou null si l'emplacement n'en contient pas.
     */
    public TowerNOState getTowerStateForSlot(int slotIndex) {
        validateSlotIndex(slotIndex);
        GameSlotData slot = runSlots[slotIndex];
        if (slot.getTowerLevel() == null || slot.getTowerFloor() == null) {
            return null;
        }
        return new TowerNOState(slot.getTowerLevel(), slot.getTowerFloor());
    }

    /**
     * Retourne l'inventaire de l'emplacement actif.
     *
     * @return inventaire courant.
     */
    public Inventory getInventory() {
        return ensureInventoryForSlot(activeRunSlot);
    }

    private Inventory ensureInventoryForSlot(int slotIndex) {
        GameSlotData slot = runSlots[slotIndex];
        if (slot.getInventory() == null) {
            slot.setInventory(new Inventory());
        }
        return slot.getInventory();
    }

    /**
     * Réinitialise l'inventaire de l'emplacement actif pour une nouvelle run.
     */
    public void resetInventoryForNewRun() {
        ensureInventoryForSlot(activeRunSlot).resetToStartingInventory();
        saveToDisk();
    }

    /**
     * Retourne la progression persistante de l'arbre de compétences.
     *
     * @return arbre de compétences du profil.
     */
    public SkillTreeProgress getSkillTreeProgress() {
        return skillTreeProgress;
    }

    /**
     * Ajoute un point de compétence persistant au profil.
     */
    public void grantSkillPoint() {
        skillTreeProgress.grantPoint();
        saveToDisk();
    }

    /**
     * Indique si le profil contient au moins une équipe sauvegardée.
     *
     * @return true si une équipe peut être chargée.
     */
    public boolean hasAvailableSavedTeams() {
        return !savedTeams.isEmpty();
    }

    /**
     * Consomme le dernier message de persistance enregistré.
     *
     * @return message de persistance, ou null s'il n'y en a pas.
     */
    public String consumeLastPersistenceMessage() {
        String message = lastPersistenceMessage;
        lastPersistenceMessage = null;
        return message;
    }
}
