package ulb.models.skilltree;

import org.junit.Test;

import static org.junit.Assert.*;

public class SkillTreeProgressTest {

    @Test
    public void testStartNodeIsAlwaysActiveAndFree() {
        SkillTreeProgress progress = new SkillTreeProgress();

        assertEquals(1, progress.getCurrentLevel("start"));
        assertTrue(progress.isActive("start"));
        assertTrue(progress.isAvailable("start"));
        assertFalse(progress.canAllocatePoint("start"));
        assertFalse(progress.canRemovePoint("start"));
    }

    @Test
    public void testAllocateThenRemoveParentCascadesDependentRefundsPoints() {
        SkillTreeProgress progress = new SkillTreeProgress(3, java.util.Map.of());

        assertTrue(progress.allocatePoint("hp_1"));
        assertTrue(progress.allocatePoint("initiative_1"));
        assertEquals(1, progress.getAvailablePoints());

        assertTrue(progress.removePoint("hp_1"));

        assertEquals(3, progress.getAvailablePoints());
        assertEquals(0, progress.getCurrentLevel("hp_1"));
        assertEquals(0, progress.getCurrentLevel("initiative_1"));
    }

    @Test
    public void testAlternativePrerequisiteKeepsNodeActive() {
        SkillTreeProgress progress = new SkillTreeProgress(8, java.util.Map.of());

        assertTrue(progress.allocatePoint("hp_1"));
        assertTrue(progress.allocatePoint("initiative_1"));
        assertTrue(progress.allocatePoint("defense_1"));
        assertTrue(progress.allocatePoint("regen_combat"));
        assertTrue(progress.allocatePoint("hp_2"));

        assertTrue(progress.removePoint("initiative_1"));

        assertEquals(1, progress.getCurrentLevel("hp_2"));
        assertTrue(progress.isActive("hp_2"));
        assertTrue(progress.isActive("regen_combat"));
    }

    @Test
    public void testComputeBonusesAggregatesAllocatedLevels() {
        SkillTreeProgress progress = new SkillTreeProgress(5, java.util.Map.of());

        assertTrue(progress.allocatePoint("attaque_1"));
        assertTrue(progress.allocatePoint("xp_boost"));

        SkillTreeBonuses bonuses = progress.computeBonuses();

        assertEquals(3, bonuses.getTeamStatsBonus().getAttack());
        assertEquals(1.2, bonuses.getXpMultiplier(), 0.0001);
        assertEquals(3, bonuses.getLevelUpChoiceCount());
    }

    @Test
    public void testComputeBonusesIncludesHpBonus() {
        SkillTreeProgress progress = new SkillTreeProgress(1, java.util.Map.of());

        assertTrue(progress.allocatePoint("hp_1"));

        SkillTreeBonuses bonuses = progress.computeBonuses();

        assertEquals(10, bonuses.getTeamStatsBonus().getHealth());
    }
}
