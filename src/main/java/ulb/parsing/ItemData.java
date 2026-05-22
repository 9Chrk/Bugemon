package ulb.parsing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import ulb.models.data.ItemDefinition;

/**
 * Charge et expose les données d'objets depuis les ressources JSON.
 */
public class ItemData {

    private final Map<String, ItemDefinition> items;
    private final ObjectMapper mapper = new ObjectMapper();

    /**
     * Initialise le registre des objets à partir de `data/objets.json`.
     */
    public ItemData() {
        JsonDataLoader<ItemDefinition> loader = new JsonDataLoader<>();
        items = loader.load(
                "data/objets.json",
                "objets",
                ItemDefinition.class,
                ItemDefinition::getId
        );
    }

    /**
     * Retourne toutes les définitions d'objets chargées.
     *
     * @return map immuable des objets indexés par identifiant.
     */
    public Map<String, ItemDefinition> getAllItems() {
        return Map.copyOf(items);
    }

    /**
     * Charge l'inventaire de départ défini dans `data/objets.json`.
     *
     * @return inventaire de départ immuable (id d'objet -> quantité).
     */
    public Map<String, Integer> getStartingInventory() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("data/objets.json")) {
            if (is == null) {
                throw new IllegalArgumentException("Resource not found: data/objets.json");
            }

            Map<String, Object> jsonData = mapper.readValue(is, new TypeReference<>() {
            });
            Object rawInventory = jsonData.get("inventaire_depart");

            if (!(rawInventory instanceof Map<?, ?> rawMap)) {
                return Map.of();
            }

            Map<String, Integer> startingInventory = new HashMap<>();
            // Le parseur JSON renvoie des valeurs génériques : on les normalise ici en une seule passe.
            for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
                if (entry.getKey() instanceof String id && entry.getValue() instanceof Number quantity) {
                    startingInventory.put(id, quantity.intValue());
                }
            }
            return Map.copyOf(startingInventory);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load starting inventory.", e);
        }
    }
}
