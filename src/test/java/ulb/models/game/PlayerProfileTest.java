package ulb.models.game;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Difficulty;
import ulb.models.data.Stats;
import ulb.models.data.Type;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.io.File;
import java.nio.file.Files;

import static org.junit.Assert.*;

public class PlayerProfileTest {

    private Team team1;
    private Team team2;

    @Before
    public void setUp() throws Exception {
        team1 = new Team("team1");
        team2 = new Team("team2");

        team1.addBugemon(new BugemonInstance(createDefinition("florachu", "Florachu")));
        team2.addBugemon(new BugemonInstance(createDefinition("moussil", "Moussil")));
    }

    @Before
    @After
    public void cleanup() {
        // Supprime le fichier de test s'il existe afin d'isoler les tests.
        File file = new File("teams_save.json");
        if (file.exists()) {
            file.delete();
        }
    }

    @Test
    public void testConstructorWithExistingCurrentTeam() {
        Map<String, Team> teams = new HashMap<>();
        teams.put("team1", team1);
        teams.put("team2", team2);

        PlayerProfile profile = new PlayerProfile(teams, "team1");

        assertNotNull(profile.getCurrentTeam());
        assertEquals("team1", profile.getCurrentTeam().getName());
    }

    @Test
    public void testConstructorWithUnknownCurrentTeam() {
        Map<String, Team> teams = new HashMap<>();
        teams.put("team1", team1);

        PlayerProfile profile = new PlayerProfile(teams, "unknown");

        assertNull(profile.getCurrentTeam());
    }

    @Test
    public void testSetCurrentTeam() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);

        profile.setCurrentTeam(team1);

        assertEquals(team1, profile.getCurrentTeam());
    }

    @Test
    public void testSaveTeam() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        profile.setCurrentTeam(team1);

        boolean saved = profile.saveTeam("team1");

        assertTrue(saved);
        assertTrue(profile.getSavedTeams().containsKey("team1"));
    }

    @Test
    public void testSaveTeamWithNullName() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        profile.setCurrentTeam(team1);

        boolean saved = profile.saveTeam(null);

        assertFalse(saved);
    }

    @Test
    public void testSaveTeamWithEmptyName() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        profile.setCurrentTeam(team1);

        boolean saved = profile.saveTeam("");

        assertFalse(saved);
    }

    @Test
    public void testSaveTeamWithoutCurrentTeam() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);

        boolean saved = profile.saveTeam("team1");

        assertFalse(saved);
    }

    @Test
    public void testLoadExistingTeam() {
        Map<String, Team> teams = new HashMap<>();
        teams.put("team1", team1);

        PlayerProfile profile = new PlayerProfile(teams, null);

        boolean loaded = profile.loadTeam("team1");

        assertTrue(loaded);
        assertEquals(team1.getName(), profile.getCurrentTeam().getName());
    }

    @Test
    public void testLoadNonExistingTeam() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);

        boolean loaded = profile.loadTeam("unknown");

        assertFalse(loaded);
        assertNull(profile.getCurrentTeam());
    }

    @Test
    public void testGetSavedTeamsReturnsImmutableCopy() {
        Map<String, Team> teams = new HashMap<>();
        teams.put("team1", team1);

        PlayerProfile profile = new PlayerProfile(teams, null);
        Map<String, Team> savedTeams = profile.getSavedTeams();

        // Vérifie que la map retournée est immuable.
        try {
            savedTeams.clear();
            fail("Expected UnsupportedOperationException when modifying returned map");
        } catch (UnsupportedOperationException e) {
            // Comportement attendu.
        }
        // Les données d'origine restent inchangées.
        assertFalse(profile.getSavedTeams().isEmpty());
    }

    @Test
    public void testAttachTowerState() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        TowerNO tower = new TowerNO(4, 2);

        profile.attachTowerState(tower);

        assertEquals(Integer.valueOf(4), profile.getTowerLevel());
        assertEquals(Integer.valueOf(2), profile.getTowerFloor());
        assertNotNull(profile.getTowerState());
        assertEquals(4, profile.getTowerState().towerLevel());
        assertEquals(2, profile.getTowerState().towerFloor());
    }

    @Test
    public void testSaveFileContainsTowerLevelAndFloor() throws Exception {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        profile.attachTowerState(new TowerNO(6, 1));

        profile.saveToDisk();
        String json = Files.readString(new File("teams_save.json").toPath());

        assertTrue(json.contains("\"runSlots\""));
        assertTrue(json.contains("\"towerLevel\""));
        assertTrue(json.contains("\"towerFloor\""));
        assertTrue(json.contains("6"));
        assertTrue(json.contains("1"));
    }

    @Test
    public void testLoadRestoresTowerLevelAndFloor() {
        PlayerProfile saved = new PlayerProfile(new HashMap<>(), null);
        saved.attachTowerState(new TowerNO(8, 3));
        saved.saveToDisk();

        PlayerProfile loaded = new PlayerProfile();

        assertEquals(Integer.valueOf(8), loaded.getTowerLevel());
        assertEquals(Integer.valueOf(3), loaded.getTowerFloor());
        assertNotNull(loaded.getTowerState());
    }

    @Test
    public void testClearActiveRunSlotEmptiesOccupiedSlot() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        profile.prepareNewRunAtSlot(2, "Ma run", "team1");
        profile.attachTowerState(new TowerNO(5, 1));
        assertTrue(profile.getRunSlot(2).isOccupied());
        profile.setActiveRunSlot(2);
        profile.clearActiveRunSlot();
        assertFalse(profile.getRunSlot(2).isOccupied());
        assertNull(profile.getRunSlot(2).getRunName());
        assertNull(profile.getTowerStateForSlot(2));
    }

    @Test
    public void testRunSlotsIndependentTowerState() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        profile.prepareNewRunAtSlot(0, "Run A", "team1");
        profile.attachTowerState(new TowerNO(4, 1));
        profile.prepareNewRunAtSlot(1, "Run B", "team1");
        profile.attachTowerState(new TowerNO(7, 2));

        assertEquals(Integer.valueOf(7), profile.getRunSlot(1).getTowerLevel());
        assertEquals(Integer.valueOf(2), profile.getRunSlot(1).getTowerFloor());
        profile.setActiveRunSlot(0);
        assertEquals(Integer.valueOf(4), profile.getTowerLevel());
        profile.setActiveRunSlot(1);
        assertEquals(Integer.valueOf(7), profile.getTowerLevel());
    }

    @Test
    public void testPrepareNewRunAtSlotDefaultsDifficultyToNormal() {
        PlayerProfile profile = new PlayerProfile(new HashMap<>(), null);
        profile.prepareNewRunAtSlot(0, "Run A", "team1");
        assertEquals(Difficulty.NORMAL, profile.getRunSlot(0).getDifficulty());
    }

    @Test
    public void testSaveAndLoadPreservesSlotDifficulty() {
        PlayerProfile saved = new PlayerProfile(new HashMap<>(), null);
        saved.prepareNewRunAtSlot(2, "Run Hard", "team1", Difficulty.HARD);
        saved.attachTowerState(new TowerNO(6, 1));
        saved.saveToDisk();

        PlayerProfile loaded = new PlayerProfile();
        assertEquals(Difficulty.HARD, loaded.getRunSlot(2).getDifficulty());
    }

    @Test
    public void testSaveAndReloadRestoresSelectedTeam() {
        PlayerProfile saved = new PlayerProfile(new HashMap<>(), null);
        saved.setCurrentTeam(team1);

        assertTrue(saved.saveTeam("team1"));

        PlayerProfile loaded = new PlayerProfile();

        loaded.loadTeam("team1");

        assertEquals("team1", loaded.getCurrentTeamName());
        assertNotNull(loaded.getCurrentTeam());
        assertEquals("team1", loaded.getCurrentTeam().getName());
        assertEquals(1, loaded.getCurrentTeam().getSize());
        assertEquals("florachu", loaded.getCurrentTeam().getBugemonAt(0).getId());
    }

    @Test
    public void testSaveAndReloadRestoresSkillTreeProgress() {
        PlayerProfile saved = new PlayerProfile(new HashMap<>(), null);
        saved.grantSkillPoint();
        saved.grantSkillPoint();
        assertTrue(saved.getSkillTreeProgress().allocatePoint("hp_1"));
        saved.saveToDisk();

        PlayerProfile loaded = new PlayerProfile();

        assertEquals(1, loaded.getSkillTreeProgress().getAvailablePoints());
        assertEquals(1, loaded.getSkillTreeProgress().getCurrentLevel("hp_1"));
        assertTrue(loaded.getSkillTreeProgress().isActive("start"));
    }

    private BugemonDefinition createDefinition(String id, String name) throws Exception {
        BugemonDefinition definition = new BugemonDefinition();

        setField(definition, "id", id);
        setField(definition, "name", name);
        setField(definition, "type", Type.Flora);
        setField(definition, "baseStats", new Stats(90, 55, 40, 50));
        setField(definition, "attackIds", Arrays.asList("a1", "a2", "a3"));
        setField(definition, "sprite", id + ".png");
        setField(definition, "starter", false);

        return definition;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
