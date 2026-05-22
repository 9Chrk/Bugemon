package ulb.models.battle;

import org.junit.Before;
import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class BattleTest {

    private Team playerTeam;
    private Team enemyTeam;

    @Before
    public void setUp() throws Exception {
        playerTeam = new Team("player");
        enemyTeam = new Team("enemy");

        playerTeam.addBugemon(new BugemonInstance(createDefinition("florachu", "Florachu")));
        enemyTeam.addBugemon(new BugemonInstance(createDefinition("moussil", "Moussil")));
    }

    @Test
    public void testBattleCreation() {
        Battle battle = new Battle(playerTeam, enemyTeam);

        assertNotNull(battle.getPlayerTeam());
        assertNotNull(battle.getEnemyTeam());
        assertNotNull(battle.getPlayerActive());
        assertNotNull(battle.getEnemyActive());
        assertFalse(battle.isFinished());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBattleWithNullTeam() {
        new Battle(null, enemyTeam);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBattleWithDifferentSizes() throws Exception {
        enemyTeam.addBugemon(new BugemonInstance(createDefinition("extra", "Extra")));
        new Battle(playerTeam, enemyTeam);
    }

    @Test
    public void testEndBattle() {
        Battle battle = new Battle(playerTeam, enemyTeam);

        battle.endBattle();

        assertTrue(battle.isFinished());
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
