package ulb.controller.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ulb.controller.TeamManagerController;
import ulb.dto.RunSlotDTO;
import ulb.models.data.Difficulty;
import ulb.models.game.TowerNO;

import java.io.File;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class RunSlotServiceTest {

    private TeamManagerController teamManagerController;
    private RunSlotService runSlotService;

    @Before
    public void setUp() {
        cleanupSaveFile();
        teamManagerController = new TeamManagerController();
        runSlotService = new RunSlotService(teamManagerController);
    }

    @After
    public void tearDown() {
        cleanupSaveFile();
    }

    @Test
    public void getRunSlotsReturnsAllSlotsInOrderAndFormatsOccupiedAndFreeStates() {
        teamManagerController.getPlayerProfile().prepareNewRunAtSlot(1, "Run Alpha", "teamA", Difficulty.HARD);
        teamManagerController.getPlayerProfile().attachTowerState(new TowerNO(6, 2));

        teamManagerController.getPlayerProfile().prepareNewRunAtSlot(4, "Run Beta", "teamB", null);
        teamManagerController.getPlayerProfile().attachTowerState(new TowerNO(3, 1));

        List<RunSlotDTO> slots = runSlotService.getRunSlots();

        assertEquals(5, slots.size());

        assertEquals(0, slots.get(0).index());
        assertFalse(slots.get(0).occupied());
        assertEquals("1.  Libre", slots.get(0).displayText());

        assertEquals(1, slots.get(1).index());
        assertTrue(slots.get(1).occupied());
        assertEquals("2.  Run Alpha - teamA - Etage 6 - Difficile", slots.get(1).displayText());

        assertEquals(2, slots.get(2).index());
        assertFalse(slots.get(2).occupied());
        assertEquals("3.  Libre", slots.get(2).displayText());

        assertEquals(3, slots.get(3).index());
        assertFalse(slots.get(3).occupied());
        assertEquals("4.  Libre", slots.get(3).displayText());

        assertEquals(4, slots.get(4).index());
        assertTrue(slots.get(4).occupied());
        assertEquals("5.  Run Beta - teamB - Etage 3 - Normal", slots.get(4).displayText());
    }

    @Test
    public void getRunSlotsDefaultsNullDifficultyToNormalWhenOccupied() {
        teamManagerController.getPlayerProfile().prepareNewRunAtSlot(2, "Run Gamma", "teamC", null);
        teamManagerController.getPlayerProfile().attachTowerState(new TowerNO(8, 4));

        List<RunSlotDTO> slots = runSlotService.getRunSlots();

        assertTrue(slots.get(2).occupied());
        assertEquals("3.  Run Gamma - teamC - Etage 8 - Normal", slots.get(2).displayText());
    }

    private void cleanupSaveFile() {
        File file = new File("teams_save.json");
        if (file.exists() && !file.delete()) {
            throw new AssertionError("Impossible de supprimer teams_save.json");
        }
    }
}


