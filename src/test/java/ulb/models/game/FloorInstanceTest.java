package ulb.models.game;

import org.junit.Test;
import ulb.models.data.Attack;
import ulb.models.data.ItemDefinition;
import ulb.parsing.AttackData;
import ulb.parsing.ItemData;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class FloorInstanceTest {

    @Test
    public void testMapStartsAtStartRoom() {
        FloorInstance floor = new FloorInstance();

        assertNotNull(floor.getCurrentRoom());
        assertEquals("start", floor.getCurrentRoom().getId());
        assertEquals(RoomType.START, floor.getCurrentRoomType());
        assertTrue(floor.getCurrentRoom().isVisited());
        assertFalse(floor.isCompleted());
    }

    @Test
    public void testConstructorKeepsOldIncompleteSaveAtStartRoom() {
        FloorInstance floor = new FloorInstance(4);

        assertEquals("start", floor.getCurrentRoom().getId());
        assertEquals(RoomType.START, floor.getCurrentRoomType());
        assertEquals(4, floor.getCurrentStep());
    }

    @Test
    public void testConstructorRestoresCompletedFloor() {
        FloorInstance floor = new FloorInstance(6);

        assertTrue(floor.isCompleted());
        assertEquals(RoomType.END, floor.getCurrentRoomType());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsNegativeRoomIndex() {
        new FloorInstance(-1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructorRejectsRoomIndexOutOfRange() {
        new FloorInstance(7);
    }

    @Test
    public void testStartRoomHasThreeAdjacentRooms() {
        FloorInstance floor = new FloorInstance();

        var adjacentIds = floor.getAdjacentRooms().stream().map(RoomCell::getId).toList();

        assertEquals(3, adjacentIds.size());
        assertTrue(adjacentIds.contains("left_combat_near"));
        assertTrue(adjacentIds.contains("boss"));
        assertTrue(adjacentIds.contains("down_combat"));
    }

    @Test
    public void testCanMoveToRejectsNonAdjacentRoom() {
        FloorInstance floor = new FloorInstance();

        assertFalse(floor.canMoveTo("left_bonus"));
        assertFalse(floor.moveTo("left_bonus"));
        assertEquals("start", floor.getCurrentRoom().getId());
    }

    @Test
    public void testMoveToAdjacentRoomUpdatesCurrentRoomAndVisited() {
        FloorInstance floor = new FloorInstance();

        boolean moved = floor.moveTo("down_combat");

        assertTrue(moved);
        assertEquals("down_combat", floor.getCurrentRoom().getId());
        assertEquals(RoomType.COMBAT, floor.getCurrentRoomType());
        assertTrue(floor.getRoomsById().get("start").isVisited());
        assertTrue(floor.getRoomsById().get("down_combat").isVisited());
    }

    @Test
    public void testMapContainsBossRoomType() {
        FloorInstance floor = new FloorInstance();

        assertEquals(RoomType.BOSS, floor.getRoomsById().get("boss").getType());
    }

    @Test
    public void testMarkCompletedSwitchesCurrentRoomTypeToEnd() {
        FloorInstance floor = new FloorInstance();

        floor.markCompleted();

        assertTrue(floor.isCompleted());
        assertEquals(RoomType.END, floor.getCurrentRoomType());
    }

    @Test
    public void testGetRewardsAllowedWhenMapRoomIsReward() {
        FloorInstance floor = new FloorInstance();

        assertTrue(floor.moveTo("down_combat"));
        assertTrue(floor.moveTo("down_bonus"));

        assertNotNull(floor.getRewards());
    }

    @Test(expected = IllegalStateException.class)
    public void testGetRewardsRejectedOutsideRewardRoom() {
        FloorInstance floor = new FloorInstance();

        floor.getRewards();
    }

    @Test
    public void testGenerateRewardOptionsProvidesThreeOptions() {
        FloorInstance floor = new FloorInstance();
        assertTrue(floor.moveTo("down_combat"));
        assertTrue(floor.moveTo("down_bonus"));

        final Map<String, Attack> attacksById = new AttackData().getAllAttacks();
        final Map<String, ItemDefinition> itemsById = new ItemData().getAllItems();

        floor.generateRewardOptions(attacksById, itemsById);
        List<Reward> rewards = floor.getRewards();

        assertEquals(3, rewards.size());
        assertTrue(rewards.stream().anyMatch(r -> r instanceof Reward.Item));
        assertTrue(rewards.stream().anyMatch(r -> r instanceof Reward.AttackReward));
        assertTrue(rewards.stream().anyMatch(r -> r instanceof Reward.Stats));
    }

    @Test
    public void testRewardSnapshotRemainsAvailableAfterLeavingRewardRoomState() {
        FloorInstance floor = new FloorInstance();
        assertTrue(floor.moveTo("down_combat"));
        assertTrue(floor.moveTo("down_bonus"));

        final Map<String, Attack> attacksById = new AttackData().getAllAttacks();
        final Map<String, ItemDefinition> itemsById = new ItemData().getAllItems();

        floor.generateRewardOptions(attacksById, itemsById);
        List<Reward> rewards = floor.getRewardOptionsSnapshot();

        floor.markCompleted();

        assertEquals(3, rewards.size());
        assertEquals(rewards, floor.getRewardOptionsSnapshot());
    }
}
