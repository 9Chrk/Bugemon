package ulb.models.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import ulb.models.data.Attack;
import ulb.models.data.ItemDefinition;
import ulb.models.data.Stats;

/**
 * Représente la progression dans un étage de la Tour NO.
 * L'étage est modélisé par une carte en croix où le joueur choisit la prochaine
 * salle accessible.
 */
public class FloorInstance {
    private static final int START_STEP_INDEX = 0;
    private static final int COMPLETED_STEP_INDEX = 6;

    private final Random random = new Random();
    private List<Reward> currentRewardOptions; // Options proposées dans la salle de récompense courante.

    private final Map<String, RoomCell> roomsById;
    private RoomCell currentRoom;
    private boolean completed;
    private int persistedStepIndex;

    /**
     * Crée un étage neuf positionné sur la salle de départ.
     */
    public FloorInstance() {
        this.currentRewardOptions = new ArrayList<>();
        this.roomsById = buildCrossMap();
        this.currentRoom = roomsById.get("start");
        this.completed = false;
        this.persistedStepIndex = START_STEP_INDEX;
    }

    /**
     * Recrée une instance depuis l'ancien index sauvegardé.
     * Les anciennes sauvegardes ne contenaient pas la position réelle sur la carte :
     * seule la valeur de fin d'étage est conservée.
     *
     * @param savedRoomIndex index de salle sauvegardé.
     */
    public FloorInstance(int savedRoomIndex) {
        if (savedRoomIndex < START_STEP_INDEX || savedRoomIndex > COMPLETED_STEP_INDEX) {
            throw new IllegalArgumentException(
                    "Invalid room index. Must be between " + START_STEP_INDEX + " and " + COMPLETED_STEP_INDEX);
        }
        this.currentRewardOptions = new ArrayList<>();
        this.roomsById = buildCrossMap();
        this.currentRoom = roomsById.get("start");
        this.completed = savedRoomIndex == COMPLETED_STEP_INDEX;
        this.persistedStepIndex = savedRoomIndex;
    }

    /**
     * Retourne la salle courante de l'étage.
     *
     * @return salle courante.
     */
    public RoomCell getCurrentRoom() {
        return currentRoom;
    }

    /**
     * Retourne les salles directement accessibles depuis la salle courante.
     *
     * @return liste des salles adjacentes.
     */
    public List<RoomCell> getAdjacentRooms() {
        return new ArrayList<>(currentRoom.getNeighbors().values());
    }

    /**
     * Vérifie si le joueur peut se déplacer vers une salle.
     *
     * @param roomId identifiant de la salle cible.
     * @return true si la salle est voisine de la salle courante.
     */
    public boolean canMoveTo(String roomId) {
        if (roomId == null || roomId.isBlank()) {
            return false;
        }
        return currentRoom.getNeighbors().values().stream()
                .anyMatch(room -> room.getId().equals(roomId));
    }

    /**
     * Déplace le joueur vers une salle voisine.
     *
     * @param roomId identifiant de la salle cible.
     * @return true si le déplacement a été effectué.
     */
    public boolean moveTo(String roomId) {
        if (!canMoveTo(roomId)) {
            return false;
        }
        // La salle quittée et la salle atteinte restent marquées sur la carte.
        currentRoom.setVisited(true);
        currentRoom = roomsById.get(roomId);
        currentRoom.setVisited(true);
        return true;
    }

    /**
     * Retourne les salles de l'étage indexées par identifiant.
     *
     * @return copie immuable de la carte des salles.
     */
    public Map<String, RoomCell> getRoomsById() {
        return Map.copyOf(roomsById);
    }

    /**
     * Retourne le type de la salle courante.
     *
     * @return type de salle, ou END si l'étage est terminé.
     */
    public RoomType getCurrentRoomType() {
        if (completed) {
            return RoomType.END;
        }
        return currentRoom.getType();
    }

    /**
     * Retourne les récompenses disponibles dans la salle courante.
     *
     * @return options de récompense.
     */
    public List<Reward> getRewards() {
        if (currentRoom == null || currentRoom.getType() != RoomType.REWARD) {
            throw new IllegalStateException("Current room is not a reward room. No rewards available.");
        }
        return currentRewardOptions;
    }

    /**
     * Retourne les récompenses courantes sans vérifier la salle active.
     * Utilisé par les services qui doivent encore appliquer un choix après
     * la navigation vers une autre vue.
     *
     * @return copie immuable des récompenses actuellement générées.
     */
    public List<Reward> getRewardOptionsSnapshot() {
        return List.copyOf(currentRewardOptions);
    }

    /**
     * Indique si l'étage est terminé.
     *
     * @return true si l'étage est terminé.
     */
    public boolean isCompleted() {
        return completed;
    }

    /**
     * Indique si la salle courante est une salle de boss.
     *
     * @return true si la salle courante contient le boss.
     */
    public boolean isBossRoom() {
        return currentRoom != null && currentRoom.getType() == RoomType.BOSS;
    }

    /**
     * Marque l'étage comme terminé.
     */
    public void markCompleted() {
        completed = true;
        persistedStepIndex = COMPLETED_STEP_INDEX;
    }

    /**
     * Retourne l'index sauvegardable de progression.
     *
     * @return index de progression persistant.
     */
    public int getCurrentStep() {
        return persistedStepIndex;
    }

    /**
     * Génère les options de récompense de la salle courante.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     * @param itemsById objets disponibles indexés par identifiant.
     * @param playerTeam équipe utilisée pour filtrer les attaques enseignables.
     */
    public void generateRewardOptions(Map<String, Attack> attacksById,
                                      Map<String, ItemDefinition> itemsById,
                                      Team playerTeam) {
        List<Reward> rewards = new ArrayList<>();
        List<Attack> attacks = new ArrayList<>(attacksById.values());
        List<ItemDefinition> items = new ArrayList<>(itemsById.values());

        // Une attaque proposée doit pouvoir servir à au moins un Bugémon du joueur.
        attacks.removeIf(attack -> !canBeTaughtToAtLeastOneMember(attack, playerTeam));

        if (!items.isEmpty()) {
            ItemDefinition item = items.get(random.nextInt(items.size()));
            rewards.add(new Reward.Item(item));
        }

        if (!attacks.isEmpty()) {
            Attack attack = attacks.get(random.nextInt(attacks.size()));
            rewards.add(new Reward.AttackReward(attack));
        }

        rewards.add(new Reward.Stats(generateRandomStatBonus()));

        Collections.shuffle(rewards, random);
        this.currentRewardOptions = rewards;
    }

    /**
     * Génère les options de récompense sans filtrage par équipe.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     * @param itemsById objets disponibles indexés par identifiant.
     */
    public void generateRewardOptions(Map<String, Attack> attacksById,
                                      Map<String, ItemDefinition> itemsById) {
        generateRewardOptions(attacksById, itemsById, null);
    }

    private StatBonus generateRandomStatBonus() {
        int i = random.nextInt(4);

        Stats stat;

        switch (i) {
            case 0 -> stat = new Stats(10, 0, 0, 0);
            case 1 -> stat = new Stats(0, 5, 0, 0);
            case 2 -> stat = new Stats(0, 0, 5, 0);
            case 3 -> stat = new Stats(0, 0, 0, 10);
            default -> throw new IllegalStateException("Unexpected value: " + i);
        }

        return new StatBonus(stat);
    }

    private Map<String, RoomCell> buildCrossMap() {
        // Ordre stable utile pour un rendu de carte prévisible.
        Map<String, RoomCell> map = new LinkedHashMap<>();

        RoomCell leftBonus = new RoomCell("left_bonus", RoomType.REWARD, -3, 0);
        RoomCell leftCombatFar = new RoomCell("left_combat_far", RoomType.COMBAT, -2, 0);
        RoomCell leftCombatNear = new RoomCell("left_combat_near", RoomType.COMBAT, -1, 0);
        RoomCell start = new RoomCell("start", RoomType.START, 0, 0);
        RoomCell boss = new RoomCell("boss", RoomType.BOSS, 1, 0);
        RoomCell downCombat = new RoomCell("down_combat", RoomType.COMBAT, 0, 1);
        RoomCell downBonus = new RoomCell("down_bonus", RoomType.REWARD, 0, 2);

        map.put(leftBonus.getId(), leftBonus);
        map.put(leftCombatFar.getId(), leftCombatFar);
        map.put(leftCombatNear.getId(), leftCombatNear);
        map.put(start.getId(), start);
        map.put(boss.getId(), boss);
        map.put(downCombat.getId(), downCombat);
        map.put(downBonus.getId(), downBonus);

        leftBonus.connectTo(RoomCell.Direction.RIGHT, leftCombatFar);
        leftCombatFar.connectTo(RoomCell.Direction.RIGHT, leftCombatNear);
        leftCombatNear.connectTo(RoomCell.Direction.RIGHT, start);
        start.connectTo(RoomCell.Direction.RIGHT, boss);
        start.connectTo(RoomCell.Direction.DOWN, downCombat);
        downCombat.connectTo(RoomCell.Direction.DOWN, downBonus);

        return map;
    }

    private boolean canBeTaughtToAtLeastOneMember(Attack attack, Team team) {
        if (team == null || team.isEmpty()) return true;
        for (BugemonInstance b : team.getBugemons()) {
            if (b.getSpecies().getType() == attack.getType()) return true;
        }
        return false;
    }
}
