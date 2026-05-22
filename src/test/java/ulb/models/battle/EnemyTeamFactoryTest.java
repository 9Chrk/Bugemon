package ulb.models.battle;

import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Difficulty;
import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import static org.junit.Assert.*;


public class EnemyTeamFactoryTest {

    @Test
    public void buildsEnemyTeamWithMatchingSize() throws Exception {
        Team player = new Team("player");
        player.addBugemon(new BugemonInstance(def("a")));
        player.addBugemon(new BugemonInstance(def("b")));

        Map<String, BugemonDefinition> defs = new HashMap<>();
        defs.put("a", def("a"));
        defs.put("b", def("b"));
        defs.put("c", def("c"));

        EnemyTeamFactory factory = new EnemyTeamFactory(new Random(1));
        Team enemy = factory.buildEnemyTeam(player, defs, Difficulty.NORMAL);

        assertEquals(player.getSize(), enemy.getSize());
        assertNotNull(enemy.getFirstBugemon());
    }

    private BugemonDefinition def(String id) throws Exception {
        BugemonDefinition d = new BugemonDefinition();

        setField(d, "id", id);
        setField(d, "name", id);
        setField(d, "type", Type.Flora);
        setField(d, "baseStats", new Stats(100, 50, 40, 30));
        setField(d, "attackIds", java.util.List.of("a1", "a2"));

        return d;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        var f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
