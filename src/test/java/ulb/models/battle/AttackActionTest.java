package ulb.models.battle;

import org.junit.After;
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
import java.util.Map;

import static org.junit.Assert.*;


public class AttackActionTest {

    private Battle battle;
    private BattleService battleService;
    private DamageCalculator damageCalculator;
    private Team playerTeam;

    @Before
    public void setUp() throws Exception {
        damageCalculator = new DamageCalculator();
        damageCalculator.disableCriticalHits();

        playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());

        playerTeam.addBugemon(new BugemonInstance(createDefinition("florachu", "Florachu")));
        playerTeam.addBugemon(new BugemonInstance(createDefinition("loopine", "Loopine")));
        enemyTeam.addBugemon(new BugemonInstance(createDefinition("moussil", "Moussil")));
        enemyTeam.addBugemon(new BugemonInstance(createDefinition("lichenox", "Lichenox")));

        battle = new Battle(playerTeam, enemyTeam);

        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("fouet_liane", createAttack("fouet_liane", "Fouet Liane", 30));
        attacks.put("pollen_sournois", createAttack("pollen_sournois", "Pollen Sournois", 20));
        attacks.put("racines_vives", createAttack("racines_vives", "Racines Vives", 10));

        battleService = new BattleService(attacks, Map.of(), new java.util.Random(), damageCalculator);
    }

    @After
    public void resetCriticalHits() {
        damageCalculator.enableCriticalHits();
    }

    @Test
    public void testExecuteAttackAction() {
        AttackAction action = new AttackAction("fouet_liane");

        String log = action.execute(battle, battleService);

        assertNotNull(log);
        assertTrue(log.contains("Fouet Liane"));
        assertEquals(68, battle.getEnemyActive().getCurrentHp());
    }

    @Test
    public void testActorBoundAttackCannotBeExecutedByReplacementBugemon() {
        AttackAction action = new AttackAction("fouet_liane", battle.getPlayerActive().getId());

        battle.setPlayerActive(battle.getPlayerTeam().getBugemonById("loopine"));

        assertFalse(action.canBeExecutedBy(battle));
    }

    private BugemonDefinition createDefinition(String id, String name) throws Exception {
        BugemonDefinition definition = new BugemonDefinition();

        setField(definition, "id", id);
        setField(definition, "name", name);
        setField(definition, "type", Type.Flora);
        setField(definition, "baseStats", new Stats(100, 50, 40, 30));
        setField(definition, "attackIds", Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives"));
        setField(definition, "sprite", id + ".png");
        setField(definition, "starter", false);

        return definition;
    }

    private Attack createAttack(String id, String name, int power) throws Exception {
        Attack attack = new Attack();

        setField(attack, "id", id);
        setField(attack, "name", name);
        setField(attack, "type", Type.Flora);
        setField(attack, "description", "test");
        setField(attack, "power", power);

        return attack;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
