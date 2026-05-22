package ulb.models.data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


/**
 * Classe mère des effets générés par une attaque ou un objet.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true,
        defaultImpl = UnknownEffect.class
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = HealingEffect.class, name = "soin"),
        @JsonSubTypes.Type(value = StatModifierEffect.class, name = "stat_modifier"),
        @JsonSubTypes.Type(value = ResetMalusEffect.class, name = "reset_malus")
})
public abstract class Effect {
    // Données
    @JsonProperty("type")
    private String type;
    @JsonProperty("cible")
    private String target;
    @JsonProperty("stat")
    private String stat;
    @JsonProperty("modificateur")
    private Integer modifier;
    @JsonProperty("duree")
    private String duration;
    @JsonProperty("valeur")
    private Integer value;

    // Constructeurs

    /**
     * Constructeur requis par Jackson pour la désérialisation.
     */
    public Effect() {
    }

    /**
     * Accepte un visiteur d'effet.
     *
     * @param visitor visiteur chargé d'appliquer l'effet.
     */
    public abstract void accept(EffectVisitor visitor);

    // ----------------- Accesseurs -----------------

    /**
     * Retourne le type d'effet.
     *
     * @return type.
     */
    public String getType() {
        return type;
    }

    /**
     * Retourne la cible de l'effet.
     *
     * @return cible.
     */
    public String getTarget() {
        return target;
    }

    /**
     * Retourne la statistique visée par l'effet.
     *
     * @return statistique.
     */
    public String getStat() {
        return stat;
    }

    /**
     * Retourne le modificateur associé à l'effet.
     *
     * @return modificateur.
     */
    public Integer getModifier() {
        return modifier;
    }

    /**
     * Retourne la durée de l'effet.
     *
     * @return durée.
     */
    public String getDuration() {
        return duration;
    }

    /**
     * Retourne la valeur numérique associée à l'effet.
     *
     * @return valeur.
     */
    public Integer getValue() {
        return value;
    }
}
