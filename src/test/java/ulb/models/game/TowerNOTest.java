package ulb.models.game;

import org.junit.Test;
import ulb.dto.FloorMapDTO;
import ulb.dto.FloorRoomDTO;

import java.util.Map;

import static org.junit.Assert.*;

public class TowerNOTest {

    @Test
    public void testDefaultConstructorStartsAtMinimumFloorAndStartRoom() {
        TowerNO tower = new TowerNO();

        assertEquals(2, tower.getCurrentFloor());
        assertEquals(0, tower.getCurrentStep());
        assertEquals(RoomType.START, tower.getCurrentRoomType());
        assertEquals("start", tower.getCurrentMapRoom().getId());
    }

    @Test
    public void testOverloadedConstructorKeepsOldIncompleteSaveAtStartRoom() {
        TowerNO tower = new TowerNO(2, 4);

        assertEquals(2, tower.getCurrentFloor());
        assertNotNull(tower.getFloorInstance());
        assertEquals(4, tower.getCurrentStep());
        assertEquals("start", tower.getCurrentMapRoom().getId());
        assertEquals(RoomType.START, tower.getCurrentRoomType());
    }

    @Test
    public void testAdvanceToNextStepDoesNotMoveInsideMap() {
        TowerNO tower = new TowerNO(2, 0);

        tower.advanceToNextStep();

        assertEquals(2, tower.getCurrentFloor());
        assertEquals(0, tower.getCurrentStep());
        assertEquals(RoomType.START, tower.getCurrentRoomType());
    }

    @Test
    public void testAdvanceToNextStepWhenCompletedMovesToNextFloor() {
        TowerNO tower = new TowerNO(2, 6);

        tower.advanceToNextStep();

        assertEquals(3, tower.getCurrentFloor());
        assertEquals(0, tower.getCurrentStep());
        assertEquals(RoomType.START, tower.getCurrentRoomType());
    }

    @Test
    public void testAdvanceToNextFloorAtMaxFloorDoesNothing() {
        TowerNO tower = new TowerNO(10, 6);

        tower.advanceToNextStep();

        assertTrue(tower.isTowerCompleted());
        assertEquals(10, tower.getCurrentFloor());
        assertEquals(6, tower.getCurrentStep());
        assertEquals(RoomType.END, tower.getCurrentRoomType());
    }

    @Test
    public void testProcessBattleResultOnStandardMapCombatKeepsPlayerOnMapRoom() {
        TowerNO tower = new TowerNO();
        assertEquals(RoomType.COMBAT, tower.enterRoom("down_combat"));

        tower.processBattleResult(true);

        assertEquals(2, tower.getCurrentFloor());
        assertEquals("down_combat", tower.getCurrentMapRoom().getId());
        assertEquals(RoomType.COMBAT, tower.getCurrentRoomType());
    }

    @Test
    public void testProcessBattleResultOnNonFinalBossMovesToNextFloor() {
        TowerNO tower = new TowerNO(2, 0);
        assertEquals(RoomType.BOSS, tower.enterRoom("boss"));

        tower.processBattleResult(true);

        assertEquals(3, tower.getCurrentFloor());
        assertEquals("start", tower.getCurrentMapRoom().getId());
        assertEquals(RoomType.START, tower.getCurrentRoomType());
    }

    @Test
    public void testProcessBattleResultOnFinalMapBossMarksTowerCompleted() {
        TowerNO tower = new TowerNO(10, 0);
        assertEquals(RoomType.BOSS, tower.enterRoom("boss"));

        tower.processBattleResult(true);

        assertEquals(10, tower.getCurrentFloor());
        assertEquals(RoomType.END, tower.getCurrentRoomType());
        assertTrue(tower.isTowerCompleted());
    }

    @Test
    public void testGetFloorMapStartsOnStartRoomWithAdjacentRoomsAvailable() {
        TowerNO tower = new TowerNO();

        FloorMapDTO map = tower.getFloorMap();
        Map<String, FloorRoomDTO> roomsById = map.rooms().stream()
                .collect(java.util.stream.Collectors.toMap(FloorRoomDTO::id, room -> room));

        assertEquals(2, map.currentFloor());
        assertEquals("start", map.currentRoomId());
        assertTrue(roomsById.get("start").current());
        assertTrue(roomsById.get("start").visited());
        assertTrue(roomsById.get("left_combat_near").available());
        assertTrue(roomsById.get("down_combat").available());
        assertTrue(roomsById.get("boss").available());
        assertFalse(roomsById.get("left_bonus").available());
    }

    @Test
    public void testEnterRoomMovesOnMapAndReturnsReachedType() {
        TowerNO tower = new TowerNO();

        RoomType roomType = tower.enterRoom("down_combat");

        assertEquals(RoomType.COMBAT, roomType);
        assertEquals("down_combat", tower.getCurrentMapRoom().getId());
        assertTrue(tower.getCurrentMapRoom().isVisited());
    }

    @Test
    public void testEnterRoomRejectsNonAdjacentMapRoom() {
        TowerNO tower = new TowerNO();

        RoomType roomType = tower.enterRoom("left_bonus");

        assertNull(roomType);
        assertEquals("start", tower.getCurrentMapRoom().getId());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverloadedConstructorRejectsFloorBelowMin() {
        new TowerNO(1, 0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testOverloadedConstructorRejectsFloorAboveMax() {
        new TowerNO(11, 0);
    }
}
