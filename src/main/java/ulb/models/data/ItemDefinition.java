package ulb.models.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Définition d'un objet de combat ou d'inventaire.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemDefinition {

    @JsonProperty("id")
    private String id;

    @JsonProperty("nom")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("categorie")
    private String category;

    @JsonProperty("effet")
    private Effect effect;

    @JsonProperty("sprite")
    private String sprite;

    /**
     * Retourne l'identifiant technique de l'objet.
     *
     * @return identifiant.
     */
    public String getId() { return id; }

    /**
     * Retourne le nom affiché de l'objet.
     *
     * @return nom lisible.
     */
    public String getName() { return name; }

    /**
     * Retourne la description détaillée de l'objet.
     *
     * @return description.
     */
    public String getDescription() { return description; }

    /**
     * Retourne la catÃ©gorie de l'objet (ex: soin, boost).
     *
     * @return catÃ©gorie mÃ©tier.
     */
    public String getCategory() { return category; }

    /**
     * Retourne l'effet associé à l'objet.
     *
     * @return effet.
     */
    public Effect getEffect() { return effect; }

    /**
     * Retourne le sprite associé à l'objet.
     *
     * @return nom du sprite.
     */
    public String getSprite() { return sprite; }
}
