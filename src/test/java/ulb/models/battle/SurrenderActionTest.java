package ulb.models.battle;

import org.junit.Before;
import org.junit.Test;
import ulb.models.data.Attack;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static org.junit.Assert.*;

public class SurrenderActionTest {

    private Battle battle;
    private BattleService battleService;

    @Before
    public void setUp() throws Exception {
        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());

        playerTeam.addBugemon(new BugemonInstance(createDefinition("florachu", "Florachu")));
        enemyTeam.addBugemon(new BugemonInstance(createDefinition("moussil", "Moussil")));

        battle = new Battle(playerTeam, enemyTeam);
        battleService = new BattleService(new HashMap<String, Attack>());
    }

    @Test
    public void testExecuteSurrenderAction() {
        SurrenderAction action = new SurrenderAction();

        String log = action.execute(battle, battleService);

        assertTrue(battle.isFinished());
        assertEquals("Combat abandonné.", log);
    }

    private BugemonDefinition createDefinition(String id, String name) throws Exception {
        BugemonDefinition definition = new BugemonDefinition();

        setField(definition, "id", id);
        setField(definition, "name", name);
        setField(definition, "type", Type.Flora);
        setField(definition, "baseStats", new Stats(100, 50, 40, 30));
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
