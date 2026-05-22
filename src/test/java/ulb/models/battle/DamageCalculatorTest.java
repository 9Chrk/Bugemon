package ulb.models.battle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ulb.models.data.Attack;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.models.data.Type;
import ulb.models.game.BugemonInstance;

import java.lang.reflect.Field;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class DamageCalculatorTest {
    private DamageCalculator damageCalculator;

    @Before
    public void disableCriticalHits() {
        damageCalculator = new DamageCalculator();
        damageCalculator.disableCriticalHits();
    }

    @After
    public void resetCriticalHits() {
        damageCalculator.enableCriticalHits();
    }

    @Test
    public void calculateDamage_neutralWithNoStats_returnsBasePower() throws Exception {
        Attack attack = new Attack();
        setField(attack, "power", 40);
        setField(attack, "type", Type.Flora);

        BugemonInstance attacker =
                new BugemonInstance(createDefinition("a", "Attacker", Type.Flora, 0, 0, 30));
        BugemonInstance defender =
                new BugemonInstance(createDefinition("d", "Defender", Type.Pyro, 0, 0, 30));

        int damage = damageCalculator.calculateDamage(attacker, defender, attack);

        assertEquals(40, damage);
    }

    @Test
    public void calculateDamage_appliesSuperEffectiveMultiplier() throws Exception {
        Attack attack = new Attack();
        setField(attack, "power", 40);
        setField(attack, "type", Type.Flora);

        BugemonInstance attacker =
                new BugemonInstance(createDefinition("a", "Attacker", Type.Flora, 0, 0, 30));
        BugemonInstance defender =
                new BugemonInstance(createDefinition("d", "Defender", Type.Aqua, 0, 0, 30));

        int damage = damageCalculator.calculateDamage(attacker, defender, attack);

        assertEquals(60, damage); // 40 * 1.5
    }

    @Test
    public void calculateDamage_appliesNotEffectiveMultiplier() throws Exception {
        Attack attack = new Attack();
        setField(attack, "power", 40);
        setField(attack, "type", Type.Flora);

        BugemonInstance attacker =
                new BugemonInstance(createDefinition("a", "Attacker", Type.Flora, 0, 0, 30));
        BugemonInstance defender =
                new BugemonInstance(createDefinition("d", "Defender", Type.Litho, 0, 0, 30));

        int damage = damageCalculator.calculateDamage(attacker, defender, attack);

        assertEquals(30, damage); // 40 * 0.75
    }

    @Test
    public void calculateDamage_usesAttackAndDefenseScaling() throws Exception {
        Attack attack = new Attack();
        setField(attack, "power", 40);
        setField(attack, "type", Type.Flora);

        BugemonInstance attacker =
                new BugemonInstance(createDefinition("a", "Attacker", Type.Flora, 50, 0, 30));
        BugemonInstance defender =
                new BugemonInstance(createDefinition("d", "Defender", Type.Pyro, 0, 40, 30));

        int damage = damageCalculator.calculateDamage(attacker, defender, attack);

        // 40 * 1.5 * (100/140) ≈ 42.857 -> 43
        assertEquals(43, damage);
    }

    @Test
    public void calculateDamage_minimumDamageIsOne() throws Exception {
        Attack attack = new Attack();
        setField(attack, "power", 1);
        setField(attack, "type", Type.Flora);

        BugemonInstance attacker =
                new BugemonInstance(createDefinition("a", "Attacker", Type.Flora, -99, 0, 30));
        BugemonInstance defender =
                new BugemonInstance(createDefinition("d", "Defender", Type.Aqua, 0, 10000, 30));

        int damage = damageCalculator.calculateDamage(attacker, defender, attack);

        assertEquals(1, damage);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateDamage_nullAttacker_throwsException() throws Exception {
        Attack attack = new Attack();
        setField(attack, "power", 40);
        setField(attack, "type", Type.Flora);

        BugemonInstance defender =
                new BugemonInstance(createDefinition("d", "Defender", Type.Aqua, 0, 0, 30));

        damageCalculator.calculateDamage(null, defender, attack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateDamage_nullDefender_throwsException() throws Exception {
        Attack attack = new Attack();
        setField(attack, "power", 40);
        setField(attack, "type", Type.Flora);

        BugemonInstance attacker =
                new BugemonInstance(createDefinition("a", "Attacker", Type.Flora, 0, 0, 30));

        damageCalculator.calculateDamage(attacker, null, attack);
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateDamage_nullAttack_throwsException() throws Exception {
        BugemonInstance attacker =
                new BugemonInstance(createDefinition("a", "Attacker", Type.Flora, 0, 0, 30));
        BugemonInstance defender =
                new BugemonInstance(createDefinition("d", "Defender", Type.Aqua, 0, 0, 30));

        damageCalculator.calculateDamage(attacker, defender, null);
    }

    @Test
    public void computeTypeMultiplier_returnsExpectedValues() {
        assertEquals(1.5, damageCalculator.computeTypeMultiplier(Type.Flora, Type.Aqua), 0.0001);
        assertEquals(0.75, damageCalculator.computeTypeMultiplier(Type.Flora, Type.Litho), 0.0001);
        assertEquals(1.0, damageCalculator.computeTypeMultiplier(Type.Flora, Type.Pyro), 0.0001);
        assertEquals(1.0, damageCalculator.computeTypeMultiplier(null, Type.Pyro), 0.0001);
        assertEquals(1.0, damageCalculator.computeTypeMultiplier(Type.Flora, null), 0.0001);
    }

    @Test
    public void getEffectivenessMessage_returnsExpectedMessages() {
        assertEquals("C'est super efficace !", damageCalculator.getEffectivenessMessage(1.5));
        assertEquals("Ce n'est pas très efficace...", damageCalculator.getEffectivenessMessage(0.75));
        assertEquals("", damageCalculator.getEffectivenessMessage(1.0));
    }

    private BugemonDefinition createDefinition(String id,
                                               String name,
                                               Type type,
                                               int attack,
                                               int defense,
                                               int initiative) throws Exception {
        BugemonDefinition definition = new BugemonDefinition();

        setField(definition, "id", id);
        setField(definition, "name", name);
        setField(definition, "type", type);
        setField(definition, "baseStats", new Stats(100, attack, defense, initiative));
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
