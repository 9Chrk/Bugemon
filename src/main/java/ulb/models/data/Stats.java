package ulb.models.data;

import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * Représente les statistiques d'un Bugémon.
 */
public class Stats {
    // Données
    @JsonProperty("pv")
    private int health;
    @JsonProperty("attaque")
    private int attack;
    @JsonProperty("defense")
    private int defense;
    @JsonProperty("initiative")
    private int initiative;

    // ----------------- Constructeurs -----------------

    /**
     * Construit un ensemble de statistiques initialisées à zéro.
     */
    public Stats() {
        this(0, 0, 0, 0);
    }

    /**
     * Construit un ensemble de statistiques à partir de valeurs explicites.
     *
     * @param health points de vie.
     * @param attack attaque.
     * @param defense défense.
     * @param initiative initiative.
     */
    public Stats(int health, int attack, int defense, int initiative) {
        this.health     = health;
        this.attack     = attack;
        this.defense    = defense;
        this.initiative = initiative;
    }

    /**
     * Construit une copie des statistiques fournies.
     *
     * @param others statistiques à copier.
     */
    public Stats(Stats others) {
        this(others.health, others.attack, others.defense, others.initiative);
    }

    // ----------------- Méthodes -----------------

    /**
     * Retourne une copie de ces statistiques.
     *
     * @return nouvelle instance de statistiques.
     */
    public Stats copy() {
        return new Stats(this);
    }

    /**
     * Ajoute les statistiques fournies à celles-ci.
     *
     * @param other statistiques à additionner.
     */
    public void add(Stats other) {
        this.health     += other.health;
        this.attack     += other.attack;
        this.defense    += other.defense;
        this.initiative += other.initiative;
    }

    /**
     * Soustrait les statistiques fournies de celles-ci.
     *
     * @param other statistiques à soustraire.
     */
    public void subtract(Stats other) {
        this.health     -= other.health;
        this.attack     -= other.attack;
        this.defense    -= other.defense;
        this.initiative -= other.initiative;
    }

    /**
     * Réinitialise toutes les statistiques à zéro.
     */
    public void reset() {
        this.health     = 0;
        this.attack     = 0;
        this.defense    = 0;
        this.initiative = 0;
    }

    /**
     * Applique ces statistiques en bonus à une base donnée.
     *
     * @param base statistiques de base.
     * @return nouvelle instance résultante.
     */
    public Stats applyTo(Stats base) {
        Stats result = base.copy();
        result.add(this);
        return result;
    }

    // ----------------- Accesseurs -----------------

    /**
     * Retourne les points de vie.
     *
     * @return points de vie.
     */
    public int getHealth() {
        return health;
    }

    /**
     * Retourne l'attaque.
     *
     * @return attaque.
     */
    public int getAttack() {
        return attack;
    }

    /**
     * Retourne la défense.
     *
     * @return défense.
     */
    public int getDefense() {
        return defense;
    }

    /**
     * Retourne l'initiative.
     *
     * @return initiative.
     */
    public int getInitiative() {
        return initiative;
    }
}
