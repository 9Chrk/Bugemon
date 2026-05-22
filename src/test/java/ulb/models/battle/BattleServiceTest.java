package ulb.models.battle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ulb.models.data.Attack;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Effect;
import ulb.models.data.HealingEffect;
import ulb.models.data.ItemDefinition;
import ulb.models.data.ResetMalusEffect;
import ulb.models.data.Stats;
import ulb.models.data.StatModifierEffect;
import ulb.models.data.Type;
import ulb.models.data.UnknownEffect;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Inventory;
import ulb.models.game.Team;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class BattleServiceTest {

    private Battle battleOneVsOne;
    private Battle battleTwoVsTwo;
    private BattleService battleService;
    private DamageCalculator damageCalculator;

    private BugemonInstance playerBugemon1;
    private BugemonInstance playerBugemon2;
    private BugemonInstance enemyBugemon1;
    private BugemonInstance enemyBugemon2;

    @Before
    public void setUp() throws Exception {
        damageCalculator = new DamageCalculator();
        damageCalculator.disableCriticalHits();

        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("fouet_liane", createAttack("fouet_liane", "Fouet Liane", 30, Type.Flora));
        attacks.put("pollen_sournois", createAttack("pollen_sournois", "Pollen Sournois", 20, Type.Flora));
        attacks.put("racines_vives", createAttack("racines_vives", "Racines Vives", 10, Type.Flora));
        attacks.put("attaque_pyro", createAttack("attaque_pyro", "Attaque Pyro", 40, Type.Pyro));
        attacks.put("attaque_aqua", createAttack("attaque_aqua", "Attaque Aqua", 40, Type.Aqua));

        battleService = new BattleService(attacks, Map.of(), new java.util.Random(), damageCalculator);

        // Combat 1 contre 1.
        Team playerTeam1 = new Team("player1", new ArrayList<>());
        Team enemyTeam1 = new Team("enemy1", new ArrayList<>());

        playerBugemon1 = new BugemonInstance(createDefinition(
                "florachu", "Florachu", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives")
        ));
        enemyBugemon1 = new BugemonInstance(createDefinition(
                "moussil", "Moussil", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives")
        ));

        playerTeam1.addBugemon(playerBugemon1);
        enemyTeam1.addBugemon(enemyBugemon1);

        battleOneVsOne = new Battle(playerTeam1, enemyTeam1);

        // Combat 2 contre 2.
        Team playerTeam2 = new Team("player2", new ArrayList<>());
        Team enemyTeam2 = new Team("enemy2", new ArrayList<>());

        playerBugemon1 = new BugemonInstance(createDefinition(
                "florachu", "Florachu", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives")
        ));
        playerBugemon2 = new BugemonInstance(createDefinition(
                "loopine", "Loopine", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives")
        ));
        enemyBugemon1 = new BugemonInstance(createDefinition(
                "moussil", "Moussil", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives")
        ));
        enemyBugemon2 = new BugemonInstance(createDefinition(
                "lichenox", "Lichenox", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives")
        ));

        playerTeam2.addBugemon(playerBugemon1);
        playerTeam2.addBugemon(playerBugemon2);
        enemyTeam2.addBugemon(enemyBugemon1);
        enemyTeam2.addBugemon(enemyBugemon2);

        battleTwoVsTwo = new Battle(playerTeam2, enemyTeam2);
    }

    @After
    public void resetCriticalHits() {
        damageCalculator.enableCriticalHits();
    }

    @Test
    public void testPlayerAttackReducesEnemyHp() {
        int initialHp = battleOneVsOne.getEnemyActive().getCurrentHp();

        String log = battleService.playerAttack(battleOneVsOne, "fouet_liane");

        assertNotNull(log);
        assertTrue(log.contains("Fouet Liane"));
        assertTrue(battleOneVsOne.getEnemyActive().getCurrentHp() < initialHp);
    }

    @Test
    public void testPlayerAttackCanFinishBattle() {
        battleService.playerAttack(battleOneVsOne, "fouet_liane");
        battleService.playerAttack(battleOneVsOne, "fouet_liane");
        battleService.playerAttack(battleOneVsOne, "fouet_liane");
        String log = battleService.playerAttack(battleOneVsOne, "fouet_liane");

        assertTrue(battleOneVsOne.isFinished());
        assertTrue(log.contains("KO"));
        assertTrue(log.contains("VICTOIRE"));
    }

    @Test
    public void testSwitchPlayerBugemonWorks() {
        boolean switched = battleService.switchPlayerBugemon(battleTwoVsTwo, playerBugemon2);

        assertTrue(switched);
        assertEquals(playerBugemon2.getId(), battleTwoVsTwo.getPlayerActive().getId());
        assertNotSame(playerBugemon2, battleTwoVsTwo.getPlayerActive());
    }

    @Test
    public void testSwitchPlayerBugemonFailsWithNull() {
        boolean switched = battleService.switchPlayerBugemon(battleTwoVsTwo, null);

        assertFalse(switched);
    }

    @Test
    public void testSwitchPlayerBugemonFailsIfBugemonNotInTeam() throws Exception {
        BugemonInstance outsider = new BugemonInstance(createDefinition(
                "outsider", "Outsider", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane", "pollen_sournois", "racines_vives")
        ));

        boolean switched = battleService.switchPlayerBugemon(battleTwoVsTwo, outsider);

        assertFalse(switched);
    }

    @Test
    public void testSwitchPlayerBugemonFailsIfBugemonIsKo() {
        battleTwoVsTwo.getPlayerTeam().getBugemonById(playerBugemon2.getId()).takeDamage(1000);

        boolean switched = battleService.switchPlayerBugemon(battleTwoVsTwo, playerBugemon2);

        assertFalse(switched);
    }

    @Test
    public void testSurrenderEndsBattle() {
        battleService.surrender(battleOneVsOne);

        assertTrue(battleOneVsOne.isFinished());
    }

    @Test
    public void testEnemyAutoAttackReturnsLog() {
        String log = battleService.enemyAutoAttack(battleOneVsOne);

        assertNotNull(log);
        assertFalse(log.isEmpty());
    }

    @Test
    public void testPlayerAttackUsesPlayerAsAttacker() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("fouet_liane", createAttack("fouet_liane", "Fouet Liane", 30, Type.Flora));

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "player_fast", "PlayerFast", Type.Flora, 100, 50, 40, 100,
                Arrays.asList("fouet_liane")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "enemy_slow", "EnemySlow", Type.Flora, 100, 50, 40, 10,
                Arrays.asList("fouet_liane")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());

        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);

        String log = service.playerAttack(battle, "fouet_liane");

        assertTrue(log.startsWith("PlayerFast utilise Fouet Liane !"));
    }

    @Test
    public void testEnemyAutoAttackUsesEnemyAsAttacker() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("fouet_liane", createAttack("fouet_liane", "Fouet Liane", 30, Type.Flora));

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "player_slow", "PlayerSlow", Type.Flora, 100, 50, 40, 10,
                Arrays.asList("fouet_liane")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "enemy_fast", "EnemyFast", Type.Flora, 100, 50, 40, 100,
                Arrays.asList("fouet_liane")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());

        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);

        String log = service.enemyAutoAttack(battle);

        assertTrue(log.startsWith("EnemyFast utilise Fouet Liane !"));
        assertTrue(log.contains("utilise"));
    }

    @Test
    public void testPlayerAttackAddsSuperEffectiveMessage() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("fouet_liane", createAttack("fouet_liane", "Fouet Liane", 30, Type.Flora));

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "florachu", "Florachu", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "aqua_mon", "AquaMon", Type.Aqua, 100, 50, 40, 30,
                Arrays.asList("fouet_liane")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());

        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);

        String log = service.playerAttack(battle, "fouet_liane");

        assertTrue(log.contains("C'est super efficace !"));
    }

    @Test
    public void testPlayerAttackAddsNotVeryEffectiveMessage() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("fouet_liane", createAttack("fouet_liane", "Fouet Liane", 30, Type.Flora));

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "florachu", "Florachu", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "litho_mon", "LithoMon", Type.Litho, 100, 50, 40, 30,
                Arrays.asList("fouet_liane")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());

        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);

        String log = service.playerAttack(battle, "fouet_liane");

        assertTrue(log.contains("Ce n'est pas très efficace..."));
    }

    @Test
    public void testEnemyAutoAttackCanKnockOutTargetAndFinishBattle() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("attaque_pyro", createAttack("attaque_pyro", "Attaque Pyro", 500, Type.Pyro));

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "player_fast", "PlayerFast", Type.Pyro, 100, 50, 40, 100,
                Arrays.asList("attaque_pyro")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "enemy_slow", "EnemySlow", Type.Litho, 50, 50, 40, 10,
                Arrays.asList("attaque_pyro")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());

        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);

        String log = service.enemyAutoAttack(battle);

        assertTrue(log.contains("PlayerFast est KO."));
        assertFalse(log.contains("PlayerFast utilise"));
        assertTrue(battle.isFinished());
    }

    @Test
    public void testPlayerAttackAppliesPermanentStatModifier() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put(
                "fouet_liane",
                createAttack(
                        "fouet_liane",
                        "Fouet Liane",
                        30,
                        Type.Flora,
                        java.util.List.of(
                                createEffect("stat_modifier", "adversaire", "defense", -5, "permanent", null)
                        )
                )
        );

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "florachu", "Florachu", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "moussil", "Moussil", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("fouet_liane")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());
        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);

        service.playerAttack(battle, "fouet_liane");

        assertEquals(35, battle.getEnemyActive().getEffectiveStats().getDefense());
    }

    @Test
    public void testPlayerAttackAppliesHealingEffectToAttacker() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put(
                "pluie_calme",
                createAttack(
                        "pluie_calme",
                        "Pluie Calme",
                        20,
                        Type.Aqua,
                        java.util.List.of(
                                createEffect("soin", "lanceur", null, null, null, 15)
                        )
                )
        );

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "aqua_mon", "AquaMon", Type.Aqua, 100, 50, 40, 30,
                Arrays.asList("pluie_calme")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "enemy", "Enemy", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("pluie_calme")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());
        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);
        battle.getPlayerActive().takeDamage(20);

        service.playerAttack(battle, "pluie_calme");

        assertEquals(95, battle.getPlayerActive().getCurrentHp());
    }

    @Test
    public void testPlayerAttackQueuesOneTurnEffectForNextTurnOnly() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put(
                "pollen_sournois",
                createAttack(
                        "pollen_sournois",
                        "Pollen Sournois",
                        20,
                        Type.Flora,
                        java.util.List.of(
                                createEffect("stat_modifier", "adversaire", "initiative", -10, "1_tour", null)
                        )
                )
        );

        BattleService service = new BattleService(attacks);

        BugemonInstance player = new BugemonInstance(createDefinition(
                "florachu", "Florachu", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("pollen_sournois")
        ));
        BugemonInstance enemy = new BugemonInstance(createDefinition(
                "moussil", "Moussil", Type.Flora, 100, 50, 40, 30,
                Arrays.asList("pollen_sournois")
        ));

        Team playerTeam = new Team("player", new ArrayList<>());
        Team enemyTeam = new Team("enemy", new ArrayList<>());
        playerTeam.addBugemon(player);
        enemyTeam.addBugemon(enemy);

        Battle battle = new Battle(playerTeam, enemyTeam);

        service.playerAttack(battle, "pollen_sournois");
        assertEquals(30, battle.getEnemyActive().getEffectiveStats().getInitiative());

        service.startTurn(battle);
        assertEquals(20, battle.getEnemyActive().getEffectiveStats().getInitiative());

        service.endTurn(battle);
        assertEquals(30, battle.getEnemyActive().getEffectiveStats().getInitiative());
    }

    @Test
    public void testUseHealingItemConsumesItemAndHealsActiveBugemon() throws Exception {
        ItemDefinition healingItem = createItemDefinition(
                "baie_test",
                "Baie Test",
                createEffect("soin", "lanceur", null, null, null, 20)
        );
        BattleService service = new BattleService(createTestAttacks(), Map.of("baie_test", healingItem));
        Inventory inventory = new Inventory(Map.of("baie_test", 1));

        battleOneVsOne.getPlayerActive().takeDamage(30);

        String log = service.useItem(battleOneVsOne, inventory, "baie_test");

        assertTrue(log.contains("Baie Test"));
        assertEquals(90, battleOneVsOne.getPlayerActive().getCurrentHp());
        assertEquals(0, inventory.getQuantity("baie_test"));
    }

    @Test
    public void testUseHealingItemFailsWhenActiveBugemonIsFullHp() throws Exception {
        ItemDefinition healingItem = createItemDefinition(
                "baie_test",
                "Baie Test",
                createEffect("soin", "lanceur", null, null, null, 20)
        );
        BattleService service = new BattleService(createTestAttacks(), Map.of("baie_test", healingItem));
        Inventory inventory = new Inventory(Map.of("baie_test", 1));

        String log = service.useItem(battleOneVsOne, inventory, "baie_test");

        assertEquals("Le Bugemon actif a deja tous ses PV.", log);
        assertEquals(1, inventory.getQuantity("baie_test"));
        assertEquals(100, battleOneVsOne.getPlayerActive().getCurrentHp());
    }

    @Test
    public void testUseItemWithoutUsableEffectDoesNotConsumeItem() throws Exception {
        ItemDefinition uselessItem = createItemDefinition(
                "objet_vide",
                "Objet Vide",
                null
        );
        BattleService service = new BattleService(createTestAttacks(), Map.of("objet_vide", uselessItem));
        Inventory inventory = new Inventory(Map.of("objet_vide", 1));

        String log = service.useItem(battleOneVsOne, inventory, "objet_vide");

        assertEquals("Cet objet n'a aucun effet utilisable maintenant.", log);
        assertEquals(1, inventory.getQuantity("objet_vide"));
    }

    @Test
    public void testUseItemReturnsConsistentMessageWhenItemIsMissing() throws Exception {
        ItemDefinition healingItem = createItemDefinition(
                "baie_test",
                "Baie Test",
                createEffect("soin", "lanceur", null, null, null, 20)
        );
        BattleService service = new BattleService(createTestAttacks(), Map.of("baie_test", healingItem));
        Inventory inventory = new Inventory(Map.of());

        String log = service.useItem(battleOneVsOne, inventory, "baie_test");

        assertEquals("Vous ne possedez pas cet objet.", log);
    }

    private BugemonDefinition createDefinition(String id,
                                               String name,
                                               Type type,
                                               int health,
                                               int attack,
                                               int defense,
                                               int initiative,
                                               java.util.List<String> attackIds) throws Exception {
        BugemonDefinition definition = new BugemonDefinition();

        setField(definition, "id", id);
        setField(definition, "name", name);
        setField(definition, "type", type);
        setField(definition, "baseStats", new Stats(health, attack, defense, initiative));
        setField(definition, "attackIds", attackIds);
        setField(definition, "sprite", id + ".png");
        setField(definition, "starter", false);

        return definition;
    }

    private Attack createAttack(String id, String name, int power, Type type) throws Exception {
        return createAttack(id, name, power, type, new ArrayList<>());
    }

    private Attack createAttack(String id,
                                String name,
                                int power,
                                Type type,
                                java.util.List<Effect> effects) throws Exception {
        Attack attack = new Attack();

        setField(attack, "id", id);
        setField(attack, "name", name);
        setField(attack, "type", type);
        setField(attack, "description", "test");
        setField(attack, "power", power);
        setField(attack, "effects", effects);

        return attack;
    }

    private ItemDefinition createItemDefinition(String id,
                                                String name,
                                                Effect effect) throws Exception {
        ItemDefinition item = new ItemDefinition();

        setField(item, "id", id);
        setField(item, "name", name);
        setField(item, "description", "test item");
        setField(item, "effect", effect);
        setField(item, "sprite", id + ".png");

        return item;
    }

    private Map<String, Attack> createTestAttacks() throws Exception {
        Map<String, Attack> attacks = new HashMap<>();
        attacks.put("fouet_liane", createAttack("fouet_liane", "Fouet Liane", 30, Type.Flora));
        return attacks;
    }

    private Effect createEffect(String type,
                                String target,
                                String stat,
                                Integer modifier,
                                String duration,
                                Integer value) throws Exception {
        Effect effect = createEffectByType(type);

        setField(effect, "type", type);
        setField(effect, "target", target);
        setField(effect, "stat", stat);
        setField(effect, "modifier", modifier);
        setField(effect, "duration", duration);
        setField(effect, "value", value);

        return effect;
    }

    private Effect createEffectByType(String type) {
        if ("soin".equals(type)) {
            return new HealingEffect();
        }
        if ("stat_modifier".equals(type)) {
            return new StatModifierEffect();
        }
        if ("reset_malus".equals(type)) {
            return new ResetMalusEffect();
        }
        return new UnknownEffect();
    }

    private void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = findField(target.getClass(), fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Field findField(Class<?> type, String fieldName) throws NoSuchFieldException {
        Class<?> currentType = type;
        while (currentType != null) {
            try {
                return currentType.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                currentType = currentType.getSuperclass();
            }
        }
        throw new NoSuchFieldException(fieldName);
    }
}
