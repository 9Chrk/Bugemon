package ulb.models.game;

import org.junit.Before;
import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import ulb.parsing.BugemonData;
import static org.junit.Assert.*;


public class BugemonInstanceTest {

    private BugemonData bugemonData;
    private BugemonDefinition florachu;
    private BugemonInstance instance;

    @Before
    public void setUp() {
        bugemonData = new BugemonData();
        florachu = bugemonData.getBugemon("florachu");
        instance = new BugemonInstance(florachu);
    }

    // --- Construction ---

    @Test
    public void testDefaultConstruction() {
        assertEquals(1, instance.getLevel());
        assertEquals(0, instance.getXp());
        assertFalse(instance.isKo());
        assertFalse(instance.hasParticipatedInBattle());
    }

    @Test
    public void testConstructionWithLevelAndXp() {
        BugemonInstance custom = new BugemonInstance(florachu, 5, 30);
        assertEquals(5, custom.getLevel());
        assertEquals(30, custom.getXp());
    }

    @Test
    public void testSpeciesReference() {
        assertSame(florachu, instance.getSpecies());
        assertEquals("Florachu", instance.getName());
    }

    @Test
    public void testInitialHpEqualsBaseHealth() {
        int expectedHp = florachu.getBaseStats().getHealth();
        assertEquals(expectedHp, instance.getCurrentHp());
    }

    // --- Dégâts et soins ---

    @Test
    public void testTakeDamage() {
        int initialHp = instance.getCurrentHp();
        instance.takeDamage(10);
        assertEquals(initialHp - 10, instance.getCurrentHp());
    }

    @Test
    public void testTakeDamageDoesNotGoBelowZero() {
        instance.takeDamage(99999);
        assertEquals(0, instance.getCurrentHp());
    }

    @Test
    public void testIsKoAfterLethalDamage() {
        instance.takeDamage(99999);
        assertTrue(instance.isKo());
    }

    @Test
    public void testHeal() {
        instance.takeDamage(20);
        int hpAfterDamage = instance.getCurrentHp();
        instance.heal(10);
        assertEquals(hpAfterDamage + 10, instance.getCurrentHp());
    }

    @Test
    public void testHealDoesNotExceedMax() {
        instance.takeDamage(5);
        instance.heal(99999);
        assertEquals(instance.getEffectiveStats().getHealth(), instance.getCurrentHp());
    }

    @Test
    public void testRestoreFullHp() {
        instance.takeDamage(30);
        instance.restoreFullHp();
        assertEquals(instance.getEffectiveStats().getHealth(), instance.getCurrentHp());
    }

    // --- Modificateurs de statistiques ---

    @Test
    public void testPermanentBonus() {
        Stats bonus = new Stats(10, 5, 3, 2);
        instance.applyPermanentBonus(bonus);
        Stats effective = instance.getEffectiveStats();

        assertEquals(florachu.getBaseStats().getHealth() + 10, effective.getHealth());
        assertEquals(florachu.getBaseStats().getAttack() + 5, effective.getAttack());
    }

    @Test
    public void testTemporaryBonus() {
        Stats bonus = new Stats(0, 10, 0, 0);
        instance.applyTemporaryBonus(bonus);
        Stats effective = instance.getEffectiveStats();

        assertEquals(florachu.getBaseStats().getAttack() + 10, effective.getAttack());
    }

    @Test
    public void testResetTemporaryBonus() {
        Stats bonus = new Stats(0, 10, 0, 0);
        instance.applyTemporaryBonus(bonus);
        instance.resetTemporaryBonus();
        Stats effective = instance.getEffectiveStats();

        assertEquals(florachu.getBaseStats().getAttack(), effective.getAttack());
    }

    @Test
    public void testPermanentBonusPersistsAfterTemporaryReset() {
        instance.applyPermanentBonus(new Stats(0, 5, 0, 0));
        instance.applyTemporaryBonus(new Stats(0, 10, 0, 0));
        instance.resetTemporaryBonus();

        assertEquals(florachu.getBaseStats().getAttack() + 5,
                instance.getEffectiveStats().getAttack());
    }

    @Test
    public void testRunHpBonusIncreasesMaxHp() {
        int baseMaxHp = instance.getEffectiveStats().getHealth();
        instance.setRunBonus(new Stats(25, 0, 0, 0));

        assertEquals(baseMaxHp + 25, instance.getEffectiveStats().getHealth());
    }

    @Test
    public void testRunHpBonusDoesNotIncreaseCurrentHpWithoutHeal() {
        instance.takeDamage(20);
        int hpBeforeRunBonus = instance.getCurrentHp();

        instance.setRunBonus(new Stats(25, 0, 0, 0));

        assertEquals(hpBeforeRunBonus, instance.getCurrentHp());
        assertEquals(florachu.getBaseStats().getHealth() + 25, instance.getEffectiveStats().getHealth());
    }

    @Test
    public void testCopyConstructorKeepsRunBonusAndHpConsistency() {
        instance.setRunBonus(new Stats(45, 0, 0, 0));
        instance.restoreFullHp();

        BugemonInstance copy = new BugemonInstance(instance);

        assertEquals(instance.getEffectiveStats().getHealth(), copy.getEffectiveStats().getHealth());
        assertEquals(instance.getCurrentHp(), copy.getCurrentHp());
        assertTrue(copy.getCurrentHp() <= copy.getEffectiveStats().getHealth());
    }

    // --- Attaques ---

    @Test
    public void testLearnedAttacksMatchSpecies() {
        assertNotNull(instance.getLearnedAttackIds());
        assertTrue(instance.getLearnedAttackIds().contains("fouet_liane"));
    }

    @Test
    public void testLearnNewAttack() {
        instance.learnAttack("new_attack");
        assertTrue(instance.getLearnedAttackIds().contains("new_attack"));
    }

    @Test
    public void testLearnDuplicateAttackIgnored() {
        int sizeBefore = instance.getLearnedAttackIds().size();
        instance.learnAttack("fouet_liane");
        assertEquals(sizeBefore, instance.getLearnedAttackIds().size());
    }

    @Test
    public void testForgetAttack() {
        instance.learnAttack("temp_attack");
        instance.forgetAttack("temp_attack");
        assertFalse(instance.getLearnedAttackIds().contains("temp_attack"));
    }

    // --- XP et niveaux ---

    @Test
    public void testGainXpNoLevelUp() {
        // Le niveau 1 demande 50 XP pour monter de niveau.
        int levels = instance.gainXp(10);
        assertEquals(0, levels);
        assertEquals(10, instance.getXp());
        assertEquals(1, instance.getLevel());
    }

    @Test
    public void testGainXpTriggersLevelUp() {
        // 50 XP sont nécessaires au niveau 1.
        int levels = instance.gainXp(50);
        assertEquals(1, levels);
        assertEquals(2, instance.getLevel());
    }

    @Test
    public void testGainXpMultipleLevelUps() {
        // Niveau 1->2 = 50, niveau 2->3 = 150 ; total = 200.
        int levels = instance.gainXp(200);
        assertTrue("Should gain at least 2 levels", levels == 2);
        assertTrue(instance.getLevel() == 3);
    }

    @Test
    public void testLevelUpRestoresHp() {
        instance.takeDamage(20);
        instance.gainXp(50);
        assertEquals(instance.getEffectiveStats().getHealth(), instance.getCurrentHp());
    }

    // --- Participation au combat ---

    @Test
    public void testBattleParticipationFlag() {
        assertFalse(instance.hasParticipatedInBattle());
        instance.setParticipatedInBattle(true);
        assertTrue(instance.hasParticipatedInBattle());
    }
}
