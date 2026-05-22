package ulb.parsing;

import java.util.Map;
import ulb.models.data.BugemonDefinition;
import java.io.File;


/**
 * Charge et expose les données des Bugémons depuis les ressources JSON.
 */
public class BugemonData {
    // Données
    private Map<String, BugemonDefinition> bugemons;
    private final JsonDataLoader<BugemonDefinition> loader;

    // Constructeurs

    /**
     * Initialise le registre des Bugémons à partir de `data/bugemons.json`.
     */
    public BugemonData() {
        this.loader = new JsonDataLoader<>();
        loadData();
    }

    private void loadData() {
        bugemons = loader.load("data/bugemons.json", "bugemons", BugemonDefinition.class, BugemonDefinition::getId);
        // Les créations locales complètent le catalogue de base.
        loadCustomBugemons();
    }

    /**
     * Charge les Bugémons personnalisés depuis le fichier local s'il existe.
     */
    public void loadCustomBugemons() {
        File customFile = new File("custom.json");
        if (customFile.exists()) {
            // Les ids personnalisés remplaceraient volontairement ceux déjà présents.
            Map<String, BugemonDefinition> customBugemons = loader.load("custom.json", "bugemons", BugemonDefinition.class, BugemonDefinition::getId);
            bugemons.putAll(customBugemons);
        }
    }

    // ----------------- Accesseurs -----------------

    /**
     * Retourne un Bugémon par son identifiant.
     *
     * @param id identifiant technique du Bugémon.
     * @return définition correspondante, ou null si elle n'existe pas.
     */
    public BugemonDefinition getBugemon(String id) {
        return bugemons.get(id);
    }

    /**
     * Retourne toutes les définitions de Bugémons chargées.
     *
     * @return map des Bugémons indexés par identifiant.
     */
    public Map<String, BugemonDefinition> getAllBugemons() {
        return bugemons;
    }
}
