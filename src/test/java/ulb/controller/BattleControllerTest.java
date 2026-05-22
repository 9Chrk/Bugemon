package ulb.controller;

import org.junit.Before;
import org.junit.Test;
import ulb.models.battle.AttackAction;
import ulb.models.data.Attack;
import ulb.models.battle.BattleService;
import ulb.models.battle.EnemyAttackAction;
import ulb.dto.AttackSummaryDTO;
import ulb.dto.BattleStateDTO;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Team;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class BattleControllerTest {

    private BattleController controller;

    @Before
    public void setUp() throws Exception {
        Team playerTeam = new Team("playerTeam");
        BugemonDefinition def = createDefinition("florachu", "Florachu", Type.Flora);
        boolean addedBugemon = playerTeam.addBugemon(new BugemonInstance(def));
        assertTrue("Failed to add Bugemon to player team", addedBugemon);

        Team enemyTeam = new Team("enemyTeam");
        BugemonDefinition enemyDef = createDefinition("moussil", "Moussil", Type.Aqua);
        addedBugemon = enemyTeam.addBugemon(new BugemonInstance(enemyDef));
        assertTrue("Failed to add Bugemon to enemy team", addedBugemon);

        BattleService fakeService = new BattleService(createAttacks());
        controller = new BattleController(playerTeam, enemyTeam, fakeService);
    }

    @Test
    public void testPlayerAndEnemyNames() {
        BattleStateDTO state = controller.getCurrentBattleState();

        assertEquals("Florachu", state.player().name());
        assertEquals("Moussil", state.enemy().name());
    }

    @Test
    public void testGetTypeAndSprite() {
        BattleStateDTO state = controller.getCurrentBattleState();

        assertEquals(Type.Flora, state.player().type());
        assertEquals(Type.Aqua, state.enemy().type());
        assertTrue(state.player().spritePath().endsWith("florachu.png"));
        assertTrue(state.enemy().spritePath().endsWith("moussil.png"));
    }

    @Test
    public void testAttackReducesEnemyHp() {
        int enemyHpBefore = controller.getCurrentBattleState().enemy().hp();

        String log = controller.executePlayerAction(new AttackAction("fouet_liane"));

        int enemyHpAfter = controller.getCurrentBattleState().enemy().hp();

        assertNotNull(log);
        assertTrue(enemyHpAfter < enemyHpBefore);
    }

    @Test
    public void testEnemyAttackReducesPlayerHp() {
        int playerHpBefore = controller.getCurrentBattleState().player().hp();

        String log = controller.executePlayerAction(new EnemyAttackAction());

        int playerHpAfter = controller.getCurrentBattleState().player().hp();

        assertNotNull(log);
        assertTrue(playerHpAfter < playerHpBefore);
    }

    @Test
    public void testGetCurrentBattleStateContainsBattleInfo() {
        BattleStateDTO state = controller.getCurrentBattleState();

        assertNotNull(state);
        assertEquals("Florachu", state.player().name());
        assertEquals("Moussil", state.enemy().name());
        assertEquals(Type.Flora, state.player().type());
        assertEquals(Type.Aqua, state.enemy().type());
        assertNotNull(state.player().spritePath());
        assertNotNull(state.enemy().spritePath());
    }

    @Test
    public void testGetAttackSummariesReturnsPlayerAttackData() {
        List<AttackSummaryDTO> attacks = controller.getAttackSummaries(controller.getCurrentBattle().getPlayerActive());

        assertNotNull(attacks);
        assertFalse(attacks.isEmpty());
        assertEquals("Fouet-Liane", attacks.get(0).name());
        assertEquals(Type.Flora, attacks.get(0).type());
    }

    @Test
    public void testBattleDataExposesAttackInfo() {
        BattleStateDTO state = controller.getCurrentBattleState();
        List<String> effectiveness = controller.getAttackEffectivenessPreview();

        assertNotNull(state);
        assertNotNull(state.player().attacks());
        assertNotNull(effectiveness);
        assertEquals(1, state.player().attacks().size());
        assertEquals("Fouet-Liane", state.player().attacks().get(0).name());
        assertEquals(Type.Flora, state.player().attacks().get(0).type());
        assertEquals("C'est super efficace !", effectiveness.get(0));
    }

    @Test
    public void testPlayerActiveAttackDataMatchesBattleState() {
        BattleStateDTO state = controller.getCurrentBattleState();

        assertNotNull(state.player().attacks());
        assertFalse(state.player().attacks().isEmpty());
    }


    @Test
    public void testBattleStartsWithResetTemporaryState() throws Exception {
        Team testPlayerTeam = new Team("playerTeam");
        BugemonInstance first = new BugemonInstance(createDefinition("florachu", "Florachu", Type.Flora));
        BugemonInstance second = new BugemonInstance(createDefinition("moussil", "Moussil", Type.Aqua));
        first.applyTemporaryBonus(new Stats(0, 25, 0, 0));
        second.setParticipatedInBattle(true);

        assertTrue(testPlayerTeam.addBugemon(first));
        assertTrue(testPlayerTeam.addBugemon(second));

        Team testEnemyTeam = new Team("enemyTeam");
        assertTrue(testEnemyTeam.addBugemon(new BugemonInstance(createDefinition("enemy1", "Enemy1", Type.Flora))));
        assertTrue(testEnemyTeam.addBugemon(new BugemonInstance(createDefinition("enemy2", "Enemy2", Type.Aqua))));

        BattleController localController = new BattleController(testPlayerTeam, testEnemyTeam, new BattleService(createAttacks()));

        BugemonInstance battleFirst = localController.getCurrentBattle().getPlayerTeam().getBugemonById("florachu");
        BugemonInstance battleSecond = localController.getCurrentBattle().getPlayerTeam().getBugemonById("moussil");

        assertEquals(50, battleFirst.getEffectiveStats().getAttack());
        assertFalse(battleSecond.hasParticipatedInBattle());
    }

    private BugemonDefinition createDefinition(String id, String name, Type type) throws Exception {
        BugemonDefinition def = new BugemonDefinition();
        setField(def, "id", id);
        setField(def, "name", name);
        setField(def, "type", type);
        setField(def, "baseStats", new Stats(100, 50, 40, 30));
        setField(def, "attackIds", List.of("fouet_liane"));
        setField(def, "sprite", id + ".png");
        setField(def, "starter", false);
        return def;
    }

    private Map<String, Attack> createAttacks() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();

        Attack a = new Attack();
        setField(a, "id", "fouet_liane");
        setField(a, "name", "Fouet-Liane");
        setField(a, "type", Type.Flora);
        setField(a, "power", 30);
        setField(a, "description", "test attack");

        attacks.put("fouet_liane", a);
        return attacks;
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
