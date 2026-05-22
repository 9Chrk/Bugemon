package ulb.models.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


/**
 * Représente une attaque qu'un Bugémon peut utiliser.
 */
public class Attack {
    // Données
    @JsonProperty("id")
    private String id;
    @JsonProperty("type")
    private Type type;
    @JsonProperty("nom")
    private String name;
    @JsonProperty("description")
    private String description;
    @JsonProperty("puissance")
    private int power;
    @JsonProperty("effets")
    private List<Effect> effects;

    // Constructeurs

    /**
     * Constructeur requis par Jackson pour la désérialisation.
     */
    public Attack() {
    }

    // ----------------- Accesseurs -----------------

    /**
     * Retourne l'identifiant technique de l'attaque.
     *
     * @return identifiant.
     */
    public String getId() {
        return id;
    }

    /**
     * Retourne le type de l'attaque.
     *
     * @return type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Retourne le nom affiché de l'attaque.
     *
     * @return nom.
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne la description de l'attaque.
     *
     * @return description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retourne la puissance de base de l'attaque.
     *
     * @return puissance.
     */
    public int getPower() {
        return power;
    }

    /**
     * Retourne la liste immuable des effets de l'attaque.
     *
     * @return effets associés, ou liste vide si aucun effet n'est défini.
     */
    public List<Effect> getEffects() {
        if (effects == null) {
            return List.of();
        }
        return List.copyOf(effects);
    }
}
