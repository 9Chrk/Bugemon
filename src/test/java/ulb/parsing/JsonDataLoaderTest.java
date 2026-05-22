package ulb.parsing;

import org.junit.Test;
import ulb.models.data.BugemonDefinition;
import ulb.models.data.Attack;
import java.util.Map;
import static org.junit.Assert.*;


public class JsonDataLoaderTest {

    @Test
    public void testLoadBugemons() {
        JsonDataLoader<BugemonDefinition> loader = new JsonDataLoader<>();

        Map<String, BugemonDefinition> bugemons =
                loader.load("data/bugemons.json",
                        "bugemons",
                        BugemonDefinition.class,
                        BugemonDefinition::getId);

        assertNotNull("Should return a map", bugemons);
        assertFalse("Map should not be empty", bugemons.isEmpty());
        assertNotNull("Should contain florachu", bugemons.get("florachu"));
    }

    @Test
    public void testLoadAttacks() {
        JsonDataLoader<Attack> loader = new JsonDataLoader<>();

        Map<String, Attack> attacks =
                loader.load("data/attaques.json",
                        "attaques",
                        Attack.class,
                        Attack::getId);

        assertNotNull("Should return a map", attacks);
        assertFalse("Map should not be empty", attacks.isEmpty());
        assertNotNull("Should contain fouet_liane", attacks.get("fouet_liane"));
    }

    @Test
    public void testLoadNonExistentResource() {
        JsonDataLoader<BugemonDefinition> loader = new JsonDataLoader<>();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> loader.load("data/nonexistent.json",
                        "bugemons",
                        BugemonDefinition.class,
                        BugemonDefinition::getId)
        );
        assertTrue("Exception message should mention the missing resource",
                exception.getMessage().contains("data/nonexistent.json"));
    }

    @Test
    public void testLoadWithWrongKey() {
        JsonDataLoader<BugemonDefinition> loader = new JsonDataLoader<>();

        Map<String, BugemonDefinition> result =
                loader.load("data/bugemons.json",
                        "wrong_key",
                        BugemonDefinition.class,
                        BugemonDefinition::getId);

        assertNotNull("Should return empty map when key not found", result);
        assertTrue("Should be empty when key not found", result.isEmpty());
    }
}
