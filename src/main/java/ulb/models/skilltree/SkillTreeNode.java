package ulb.models.skilltree;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Représente un noeud de l'arbre de compétences.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillTreeNode {
    @JsonProperty("id")
    private String id;

    @JsonProperty("nom")
    private String name;

    @JsonProperty("description")
    private String description;

    @JsonProperty("cout")
    private int cost;

    @JsonProperty("max_niveau")
    private int maxLevel;

    @JsonProperty("effet")
    private SkillEffect effect;

    @JsonProperty("position")
    private Position position;

    @JsonProperty("prerequis")
    private List<String> prerequisites;

    /**
     * Position du noeud dans la grille de l'arbre de compétences.
     *
     * @param x coordonnée horizontale.
     * @param y coordonnée verticale.
     */
    public record Position(
            @JsonProperty("x") int x,
            @JsonProperty("y") int y) {
    }

    /**
     * Retourne l'identifiant technique du noeud.
     *
     * @return identifiant du noeud.
     */
    public String getId() {
        return id;
    }

    /**
     * Retourne le nom affiché du noeud.
     *
     * @return nom du noeud.
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne la description du noeud.
     *
     * @return description affichable.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Retourne le coût d'amélioration du noeud.
     *
     * @return coût en points de compétence.
     */
    public int getCost() {
        return cost;
    }

    /**
     * Retourne le niveau maximal du noeud.
     *
     * @return niveau maximal.
     */
    public int getMaxLevel() {
        return maxLevel;
    }

    /**
     * Retourne l'effet associé au noeud.
     *
     * @return effet de compétence.
     */
    public SkillEffect getEffect() {
        return effect;
    }

    /**
     * Retourne la position du noeud dans la vue.
     *
     * @return position du noeud.
     */
    public Position getPosition() {
        return position;
    }

    /**
     * Retourne les prérequis du noeud.
     *
     * @return liste immuable des identifiants prérequis.
     */
    public List<String> getPrerequisites() {
        return prerequisites == null ? List.of() : List.copyOf(prerequisites);
    }

    /**
     * Indique si le noeud est le point de départ de l'arbre.
     *
     * @return true si le noeud est le départ.
     */
    public boolean isStartNode() {
        return "start".equals(id);
    }
}
