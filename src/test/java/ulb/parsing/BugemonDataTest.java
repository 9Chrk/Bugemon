package ulb.parsing;

import org.junit.Before;
import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import java.util.Map;
import static org.junit.Assert.*;


public class BugemonDataTest {

    private BugemonData bugemonData;

    @Before
    public void setUp() {
        bugemonData = new BugemonData();
    }

    @Test
    public void testLoadBugemons_NotEmpty() {
        Map<String, BugemonDefinition> bugemons = bugemonData.getAllBugemons();
        assertNotNull("Bugemons map should not be null", bugemons);
        assertFalse("Bugemons map should not be empty", bugemons.isEmpty());
    }

    @Test
    public void testGetBugemon_ExistingId() {
        BugemonDefinition florachu = bugemonData.getBugemon("florachu");
        assertNotNull("Florachu should exist", florachu);
        assertEquals("Florachu", florachu.getName());
        assertEquals("florachu", florachu.getId());
    }

    @Test
    public void testGetBugemon_NonExistingId() {
        BugemonDefinition unknown = bugemonData.getBugemon("unknown_bugemon");
        assertNull("Unknown bugemon should return null", unknown);
    }

    @Test
    public void testBugemonHasCorrectStats() {
        BugemonDefinition florachu = bugemonData.getBugemon("florachu");
        assertNotNull(florachu);
        assertNotNull("Stats should not be null", florachu.getBaseStats());
        assertEquals(90, florachu.getBaseStats().getHealth());
        assertEquals(55, florachu.getBaseStats().getAttack());
        assertEquals(40, florachu.getBaseStats().getDefense());
        assertEquals(50, florachu.getBaseStats().getInitiative());
    }

    @Test
    public void testBugemonHasAttacks() {
        BugemonDefinition florachu = bugemonData.getBugemon("florachu");
        assertNotNull(florachu);
        assertNotNull("Attacks list should not be null", florachu.getAttackIds());
        assertFalse("Attacks list should not be empty", florachu.getAttackIds().isEmpty());
        assertTrue("Florachu should have fouet_liane attack",
                florachu.getAttackIds().contains("fouet_liane"));
    }

    @Test
    public void testStarterBugemon() {
        BugemonDefinition florachu = bugemonData.getBugemon("florachu");
        assertNotNull(florachu);
        assertTrue("Florachu should be a starter", florachu.isStarter());
        BugemonDefinition moussil = bugemonData.getBugemon("moussil");
        assertNotNull(moussil);
        assertFalse("Moussil should not be a starter", moussil.isStarter());
    }

    @Test
    public void testBugemonType() {
        BugemonDefinition florachu = bugemonData.getBugemon("florachu");
        assertNotNull(florachu);
        assertNotNull("Type should not be null", florachu.getType());
    }

    @Test
    public void testMultipleBugemonsLoaded() {
        Map<String, BugemonDefinition> bugemons = bugemonData.getAllBugemons();
        assertNotNull(bugemonData.getBugemon("florachu"));
        assertNotNull(bugemonData.getBugemon("moussil"));
        assertNotNull(bugemonData.getBugemon("exceflam"));
        assertTrue("Should have at least 3 bugemons", bugemons.size() >= 3);
    }
}
