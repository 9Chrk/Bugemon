package ulb.parsing;

import org.junit.Before;
import org.junit.Test;
import ulb.models.data.Attack;
import ulb.models.data.Type;
import java.util.Map;
import static org.junit.Assert.*;


public class AttackDataTest {

    private AttackData attackData;

    @Before
    public void setUp() {
        attackData = new AttackData();
    }

    @Test
    public void testLoadAttacks_NotEmpty() {
        Map<String, Attack> attacks = attackData.getAllAttacks();
        assertNotNull("Attacks map should not be null", attacks);
        assertFalse("Attacks map should not be empty", attacks.isEmpty());
    }

    @Test
    public void testGetAttack_ExistingId() {
        Attack fouetLiane = attackData.getAttack("fouet_liane");
        assertNotNull("Fouet-Liane should exist", fouetLiane);
        assertEquals("Fouet-Liane", fouetLiane.getName());
        assertEquals("fouet_liane", fouetLiane.getId());
    }

    @Test
    public void testGetAttack_NonExistingId() {
        Attack unknown = attackData.getAttack("attaque_inconnue");
        assertNull("Unknown attack should return null", unknown);
    }

    @Test
    public void testAttackHasCorrectProperties() {
        Attack fouetLiane = attackData.getAttack("fouet_liane");
        assertNotNull(fouetLiane);
        assertEquals(40, fouetLiane.getPower());
        assertEquals(Type.Flora, fouetLiane.getType());
        assertNotNull("Description should not be null", fouetLiane.getDescription());
        assertFalse("Description should not be empty", fouetLiane.getDescription().isEmpty());
    }

    @Test
    public void testAttackHasEffects() {
        Attack fouetLiane = attackData.getAttack("fouet_liane");
        assertNotNull(fouetLiane);
        assertNotNull("Effects list should not be null", fouetLiane.getEffects());
        assertFalse("Effects list should not be empty for fouet_liane", fouetLiane.getEffects().isEmpty());
    }

    @Test
    public void testAttackWithoutEffects() {
        Attack feuilleTranchante = attackData.getAttack("feuille_tranchante");
        assertNotNull(feuilleTranchante);
        assertNotNull("Effects list should not be null", feuilleTranchante.getEffects());
        assertTrue("Effects list should be empty for feuille_tranchante", feuilleTranchante.getEffects().isEmpty());
    }

    @Test
    public void testMultipleAttacksLoaded() {
        Map<String, Attack> attacks = attackData.getAllAttacks();
        assertNotNull(attackData.getAttack("fouet_liane"));       // Flora
        assertNotNull(attackData.getAttack("explosion_ardente")); // Pyro
        assertNotNull(attackData.getAttack("vague_rapide"));      // Aqua
        assertNotNull(attackData.getAttack("jet_de_pierres"));    // Litho
        assertTrue("Should have multiple attacks loaded", attacks.size() >= 4);
    }
}
