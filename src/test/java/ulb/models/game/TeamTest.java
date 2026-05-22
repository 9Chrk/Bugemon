package ulb.models.game;

import org.junit.Before;
import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.models.data.Type;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class TeamTest {

    private BugemonInstance bugemon1;
    private BugemonInstance bugemon2;

    @Before
    public void setUp() throws Exception {
        bugemon1 = new BugemonInstance(createDefinition("florachu", "Florachu"));
        bugemon2 = new BugemonInstance(createDefinition("moussil", "Moussil"));
    }

    @Test
    public void testAddBugemon() {
        Team team = new Team("team1");

        boolean added = team.addBugemon(bugemon1);

        assertTrue(added);
        assertEquals(1, team.getSize());
    }

    @Test
    public void testNoDuplicateBugemon() {
        Team team = new Team("team1");

        assertTrue(team.addBugemon(bugemon1));
        assertFalse(team.addBugemon(bugemon1));
        assertEquals(1, team.getSize());
    }

    @Test
    public void testRemoveBugemon() {
        Team team = new Team("team1");
        team.addBugemon(bugemon1);
        boolean removed = team.removeBugemon(bugemon1);
        assertTrue(removed);
        assertTrue(team.isEmpty());
    }

    @Test
    public void testGetFirstBugemon() {
        Team team = new Team("team1");
        team.addBugemon(bugemon1);
        team.addBugemon(bugemon2);

        assertEquals(bugemon1, team.getFirstBugemon());
    }

    @Test
    public void testTeamCannotExceedSixMembers() throws Exception {
        Team team = new Team("team1");

        for (int i = 0; i < 6; i++) {
            BugemonInstance bugemon = new BugemonInstance(createDefinition("id" + i, "Bugemon" + i));
            assertTrue(team.addBugemon(bugemon));
        }

        BugemonInstance extra = new BugemonInstance(createDefinition("extra", "Extra"));
        assertFalse(team.addBugemon(extra));
        assertEquals(6, team.getSize());
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
