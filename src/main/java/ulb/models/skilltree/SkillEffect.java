package ulb.models.skilltree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Représente l'effet d'un noeud de l'arbre de compétences.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillEffect {
    @JsonProperty("type")
    private String type;

    @JsonProperty("stat")
    private String stat;

    @JsonProperty("valeur")
    private Double value;

    @JsonProperty("valeur_pourcent")
    private Integer percentageValue;

    @JsonProperty("quantite")
    private Integer quantity;

    @JsonProperty("categorie")
    private String category;

    @JsonProperty("type_cible")
    private String targetType;

    /**
     * Retourne le type d'effet de compétence.
     *
     * @return type d'effet.
     */
    public String getType() {
        return type;
    }

    /**
     * Retourne la statistique ciblée par l'effet.
     *
     * @return nom de la statistique, ou null si l'effet n'en utilise pas.
     */
    public String getStat() {
        return stat;
    }

    /**
     * Retourne la valeur numérique de l'effet avec une valeur de secours.
     *
     * @param defaultValue valeur utilisée si aucune valeur n'est définie.
     * @return valeur de l'effet.
     */
    public double getValueOrDefault(double defaultValue) {
        return value == null ? defaultValue : value;
    }

    /**
     * Retourne la valeur en pourcentage avec une valeur de secours.
     *
     * @param defaultValue valeur utilisée si aucun pourcentage n'est défini.
     * @return pourcentage de l'effet.
     */
    public int getPercentageValueOrDefault(int defaultValue) {
        return percentageValue == null ? defaultValue : percentageValue;
    }

    /**
     * Retourne la quantité configurée avec une valeur de secours.
     *
     * @param defaultValue valeur utilisée si aucune quantité n'est définie.
     * @return quantité de l'effet.
     */
    public int getQuantityOrDefault(int defaultValue) {
        return quantity == null ? defaultValue : quantity;
    }

    /**
     * Retourne la catégorie d'objet associée à l'effet.
     *
     * @return catégorie, ou null si l'effet n'en utilise pas.
     */
    public String getCategory() {
        return category;
    }

    /**
     * Retourne le type ciblé par un bonus élémentaire.
     *
     * @return type cible sous forme textuelle.
     */
    public String getTargetType() {
        return targetType;
    }
}
