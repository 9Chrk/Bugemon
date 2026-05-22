package ulb.models.data;

import org.junit.Test;

import static org.junit.Assert.*;

public class StatsTest {

    @Test
    public void testCopy() {
        Stats stats = new Stats(100, 50, 40, 30);
        Stats copy = stats.copy();

        assertNotSame(stats, copy);
        assertEquals(100, copy.getHealth());
        assertEquals(50, copy.getAttack());
        assertEquals(40, copy.getDefense());
        assertEquals(30, copy.getInitiative());
    }

    @Test
    public void testAdd() {
        Stats stats = new Stats(100, 50, 40, 30);
        Stats bonus = new Stats(10, 5, 3, 2);

        stats.add(bonus);

        assertEquals(110, stats.getHealth());
        assertEquals(55, stats.getAttack());
        assertEquals(43, stats.getDefense());
        assertEquals(32, stats.getInitiative());
    }

    @Test
    public void testReset() {
        Stats stats = new Stats(100, 50, 40, 30);

        stats.reset();

        assertEquals(0, stats.getHealth());
        assertEquals(0, stats.getAttack());
        assertEquals(0, stats.getDefense());
        assertEquals(0, stats.getInitiative());
    }

    @Test
    public void testApplyTo() {
        Stats base = new Stats(100, 50, 40, 30);
        Stats bonus = new Stats(10, 5, 3, 2);

        Stats result = bonus.applyTo(base);

        assertEquals(110, result.getHealth());
        assertEquals(55, result.getAttack());
        assertEquals(43, result.getDefense());
        assertEquals(32, result.getInitiative());

        // base ne doit pas être modifié
        assertEquals(100, base.getHealth());
        assertEquals(50, base.getAttack());
        assertEquals(40, base.getDefense());
        assertEquals(30, base.getInitiative());
    }
}