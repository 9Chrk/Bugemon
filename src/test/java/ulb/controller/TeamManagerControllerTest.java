package ulb.controller;

import java.io.File;
import org.junit.Before;
import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.models.game.Team;
import ulb.parsing.BugemonData;
import java.lang.reflect.Field;
import java.util.*;
import static org.junit.Assert.*;

public class TeamManagerControllerTest {

    private TeamManagerController controller;

    @Before
    public void setUp() throws Exception {
        // 1. Suppression physique (Disque)
        java.io.File file = new java.io.File("teams_save.json");
        if (file.exists()) {
            file.delete();
        }

        // 2. Initialisation d'une instance neuve (Mémoire vive)
        controller = new TeamManagerController();

        // 3. Bouchons de test.
        BugemonData mockData = new BugemonData();
        Map<String, BugemonDefinition> bugemons = new HashMap<>();
        bugemons.put("florachu", createDefinition("florachu", "Florachu"));
        bugemons.put("moussil", createDefinition("moussil", "Moussil"));
        bugemons.put("verdurion", createDefinition("verdurion", "Verdurion"));
        bugemons.put("loopine", createDefinition("loopine", "Loopine"));
        bugemons.put("lichenox", createDefinition("lichenox", "Lichenox"));
        bugemons.put("bugzilla", createDefinition("bugzilla", "Bugzilla"));
        bugemons.put("extra1", createDefinition("extra1", "Extra1"));

        setField(mockData, "bugemons", bugemons);
        setField(controller, "bugemonData", mockData);
    }

    @Test
    public void testAddBugemon() {
        controller.addBugemonOnTeamById("florachu");

        assertEquals(1, controller.getCurrentTeamStateDTO().currentSize());
        assertEquals("florachu", controller.getCurrentTeamStateDTO().memberNames().get(0));
    }

    @Test
    public void testAddBugemonLimit() {

        String[] ids = { "florachu", "moussil", "verdurion", "loopine", "lichenox", "bugzilla", "florachu" };
        for (String id : ids) {
            controller.addBugemonOnTeamById(id);
        }

        assertEquals(6, controller.getCurrentTeamStateDTO().memberNames().size());
    }

    @Test
    public void testAddBugemonRejectsDuplicate() {
        assertTrue(controller.addBugemonOnTeamById("florachu"));
        assertFalse(controller.addBugemonOnTeamById("florachu"));
        assertEquals(1, controller.getCurrentTeamStateDTO().memberNames().size());
    }

    @Test
    public void testAddBugemonRejectsWhenFull() {
        String[] ids = { "florachu", "moussil", "verdurion", "loopine", "lichenox", "bugzilla", "extra1" };
        boolean lastResult = false;
        for (int i = 0; i < ids.length; i++) {
            boolean added = controller.addBugemonOnTeamById(ids[i]);
            if (i == ids.length - 1) {
                lastResult = added;
            }
        }
        assertFalse("Should reject adding beyond team size", lastResult);
        assertEquals(6, controller.getCurrentTeamStateDTO().memberNames().size());
    }

    @Test
    public void testRemoveBugemon() {

        controller.addBugemonOnTeamById("florachu");
        controller.addBugemonOnTeamById("moussil");

        controller.removeBugemonOnTeamById("florachu");

        assertEquals(1, controller.getCurrentTeamStateDTO().memberNames().size());
        assertEquals("moussil", controller.getCurrentTeamStateDTO().memberNames().get(0));
    }

    @Test
    public void testGetTeamCreatesInstances() {

        controller.addBugemonOnTeamById("florachu");
        controller.addBugemonOnTeamById("moussil");

        var team = controller.getTeam();

        assertNotNull(team);
        assertEquals(2, team.getTeam().size());
    }

    @Test
    public void testGetStatsToString() {

        String[] stats = controller.getStatsToString("florachu");

        assertTrue(stats[0].contains("Florachu"));
        assertTrue(stats[1].contains("PV"));
        assertTrue(stats[2].contains("Attaque"));
        assertTrue(stats[3].contains("Défense"));
    }

    @Test
    public void testDeleteTeamRemovesSavedTeam() {
        controller.addBugemonOnTeamById("florachu");
        controller.addBugemonOnTeamById("moussil");

        assertTrue(controller.saveCurrentTeam("teamA"));
        assertEquals(1, controller.getAllTeamNamesSaved().size());

        assertTrue(controller.deleteTeam("teamA"));
        assertEquals(0, controller.getAllTeamNamesSaved().size());
    }

    @Test
    public void testStartNewTeamSelectionClearsCurrentSelection() {
        controller.addBugemonOnTeamById("florachu");
        controller.addBugemonOnTeamById("moussil");

        controller.startNewTeamSelection();

        assertTrue(controller.getCurrentTeamStateDTO().memberNames().isEmpty());
        assertEquals(0, controller.getCurrentTeamStateDTO().currentSize());
    }

    @Test
    public void testNewControllerReloadsSavedTeamsFromDisk() {
        controller.addBugemonOnTeamById("florachu");
        controller.addBugemonOnTeamById("moussil");

        assertTrue(controller.saveCurrentTeam("teamA"));

        TeamManagerController reloadedController = new TeamManagerController();

        reloadedController.loadTeamFromProfile("teamA");

        assertTrue(reloadedController.getAllTeamNamesSaved().contains("teamA"));
        assertEquals(Arrays.asList("florachu", "moussil"), reloadedController.getCurrentTeamStateDTO().memberNames());
    }

    @Test
    public void testSyncBattleProgressPersistsUpdatedBugemonState() {
        controller.addBugemonOnTeamById("florachu");
        assertTrue(controller.saveCurrentTeam("teamA"));
        controller.loadTeamFromProfile("teamA");

        Team resolvedBattleTeam = new Team(controller.getTeam());
        var upgradedBugemon = resolvedBattleTeam.getBugemonById("florachu");
        upgradedBugemon.gainXp(50);
        upgradedBugemon.applyPermanentBonus(new Stats(10, 5, 0, 0));

        controller.syncBattleProgress(resolvedBattleTeam);

        TeamManagerController reloadedController = new TeamManagerController();
        reloadedController.loadTeamFromProfile("teamA");
        var persistedBugemon = reloadedController.getTeam().getBugemonById("florachu");

        assertEquals(2, persistedBugemon.getLevel());
        assertEquals(120, persistedBugemon.getEffectiveStats().getHealth());
        assertEquals(60, persistedBugemon.getEffectiveStats().getAttack());
    }

    private BugemonDefinition createDefinition(String id, String name) throws Exception {
        BugemonDefinition def = new BugemonDefinition();

        setField(def, "id", id);
        setField(def, "name", name);
        setField(def, "type", Type.Flora);
        setField(def, "baseStats", new Stats(100, 50, 40, 30));
        setField(def, "attackIds", List.of("fouet_liane", "pollen_sournois", "racines_vives")); // Doit correspondre aux attaques de test.
        setField(def, "sprite", id + ".png");
        setField(def, "starter", false); // Champ optionnel.

        return def;
    }

    private void setField(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }
}
