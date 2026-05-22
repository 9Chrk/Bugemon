package ulb.models.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;


/**
 * Représente un Bugémon avec ses attributs, ses attaques et ses ressources d'affichage.
 */
public class BugemonDefinition {
    // Données
    @JsonProperty("id")
    private String id;
    @JsonProperty("nom")
    private String name;
    @JsonProperty("type")
    private Type type;
    @JsonProperty("stats")
    private Stats baseStats;
    @JsonProperty("attaques")
    private List<String> attackIds;
    @JsonProperty("sprite")
    private String sprite;
    @JsonProperty("starter")
    private boolean starter;

    // Constructeurs

    /**
     * Constructeur requis par Jackson pour la désérialisation.
     */
    public BugemonDefinition() {
    }

    // Méthodes

    /**
     * Indique si ce Bugémon fait partie des starters.
     *
     * @return true si le Bugémon est un starter.
     */
    public boolean isStarter() {
        return starter;
    }

    // ----------------- Accesseurs -----------------

    /**
     * Retourne l'identifiant technique du Bugémon.
     *
     * @return identifiant.
     */
    public String getId() {
        return id;
    }

    /**
     * Retourne le nom affiché du Bugémon.
     *
     * @return nom.
     */
    public String getName() {
        return name;
    }

    /**
     * Retourne le type du Bugémon.
     *
     * @return type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Retourne les statistiques de base du Bugémon.
     *
     * @return copie des statistiques de base.
     */
    public Stats getBaseStats() {
        return baseStats.copy();
    }

    /**
     * Retourne les identifiants des attaques connues par ce Bugémon.
     *
     * @return liste immuable des identifiants d'attaques.
     */
    public List<String> getAttackIds() {
        return List.copyOf(attackIds);
    }

    /**
     * Retourne le sprite associé au Bugémon.
     *
     * @return nom du sprite.
     */
    public String getSprite() {
        return sprite;
    }
}
