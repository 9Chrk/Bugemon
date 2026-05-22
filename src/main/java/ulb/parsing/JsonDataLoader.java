package ulb.parsing;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.io.File;
import java.io.FileInputStream;


/**
 * Chargeur JSON générique permettant de lire des entités depuis un fichier de ressources.
 *
 * @param <T> type d'entité à charger.
 */
public class JsonDataLoader<T> {
    // Données
    private final ObjectMapper mapper = new ObjectMapper();


    // ----------------- Méthodes -----------------

    /**
     * Charge une collection d'entités depuis une clé JSON et la transforme en map indexée par identifiant.
     *
     * @param resourcePath chemin de la ressource JSON.
     * @param jsonKey clé contenant la liste des entités dans le JSON.
     * @param entityClass classe cible de désérialisation.
     * @param idExtractor fonction qui extrait l'identifiant de chaque entité.
     * @return map des entités indexées par identifiant.
     */
    public Map<String, T> load(String resourcePath, String jsonKey, Class<T> entityClass, Function<T, String> idExtractor) {
        Map<String, T> entityMap = new HashMap<>();
        InputStream is = getClass().getClassLoader().getResourceAsStream(resourcePath);
        try {
            if (is == null) {
                // Fallback utilisé pour les fichiers locaux comme custom.json.
                File file = new File(resourcePath);
                if (file.exists()) {
                    is = new FileInputStream(file);
                }else{
                    throw new IllegalArgumentException("Resource not found: " + resourcePath);
                }
            }

            Map<String, Object> jsonData = mapper.readValue(is, new TypeReference<>() {
            });
            List<T> rawList = (List<T>) jsonData.get(jsonKey);

            if (rawList != null) {
                for (Object rawItem : rawList) {
                    // Jackson reconvertit chaque entrée vers la classe métier demandée.
                    T entity = mapper.convertValue(rawItem, entityClass);
                    String id = idExtractor.apply(entity);

                    if (entityMap.containsKey(id)) {
                        // Les doublons sont bloqués tôt pour éviter un écrasement silencieux.
                        throw new IllegalStateException("Duplicate ID found: " + id + " in resource: " + resourcePath);
                    }

                    entityMap.put(id, entity);
                }
            }
            return entityMap;

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load JSON resource: " + resourcePath, e);
        }
    }
}
