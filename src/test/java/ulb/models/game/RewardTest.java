package ulb.models.game;

import org.junit.Before;
import org.junit.Test;
import ulb.models.data.Attack;
import ulb.models.data.Type;
import ulb.parsing.AttackData;
import ulb.parsing.BugemonData;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Tests des règles métier des récompenses (notamment enseignement d'attaque par type).
 */
public class RewardTest {

    private BugemonData bugemonData;
    private AttackData attackData;

    @Before
    public void setUp() {
        bugemonData = new BugemonData();
        attackData = new AttackData();
    }

    @Test
    public void attackReward_canTeachTo_sameTypeAsSpecies_returnsTrue() {
        BugemonInstance flora = new BugemonInstance(bugemonData.getBugemon("florachu"));
        Attack floraAttack = attackData.getAttack("fouet_liane");
        Reward.AttackReward reward = new Reward.AttackReward(floraAttack);

        assertTrue(reward.canTeachTo(flora));
    }

    @Test
    public void attackReward_canTeachTo_differentTypeFromSpecies_returnsFalse() {
        BugemonInstance flora = new BugemonInstance(bugemonData.getBugemon("florachu"));
        Attack pyroAttack = attackData.getAttack("explosion_ardente");
        Reward.AttackReward reward = new Reward.AttackReward(pyroAttack);

        assertFalse(reward.canTeachTo(flora));
    }

    @Test
    public void attackReward_canTeachTo_pyroSpeciesWithPyroAttack_returnsTrue() {
        BugemonInstance pyro = new BugemonInstance(bugemonData.getBugemon("exceflam"));
        Attack pyroAttack = attackData.getAttack("explosion_ardente");
        Reward.AttackReward reward = new Reward.AttackReward(pyroAttack);

        assertTrue(reward.canTeachTo(pyro));
    }

    @Test
    public void attackReward_canTeachTo_pyroSpeciesWithFloraAttack_returnsFalse() {
        BugemonInstance pyro = new BugemonInstance(bugemonData.getBugemon("exceflam"));
        Attack floraAttack = attackData.getAttack("fouet_liane");
        Reward.AttackReward reward = new Reward.AttackReward(floraAttack);

        assertFalse(reward.canTeachTo(pyro));
    }

    @Test
    public void attackReward_canTeachTo_coversAllTypes_whenAttackMatchesSpecies() {
        assertTrue(match("florachu", "fouet_liane", Type.Flora));
        assertTrue(match("exceflam", "explosion_ardente", Type.Pyro));
        assertTrue(match("commitide", "vague_rapide", Type.Aqua));
        assertTrue(match("rockachu", "jet_de_pierres", Type.Litho));
    }

    private boolean match(String bugemonId, String attackId, Type expectedSharedType) {
        BugemonInstance b = new BugemonInstance(bugemonData.getBugemon(bugemonId));
        Attack a = attackData.getAttack(attackId);
        assertTrue("test data: species type", b.getSpecies().getType() == expectedSharedType);
        assertTrue("test data: attack type", a.getType() == expectedSharedType);
        return new Reward.AttackReward(a).canTeachTo(b);
    }
}
