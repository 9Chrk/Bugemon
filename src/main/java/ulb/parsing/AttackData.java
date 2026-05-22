package ulb.parsing;

import java.util.Map;
import ulb.models.data.Attack;


/**
 * Charge et expose les données d'attaques depuis les ressources JSON.
 */
public class AttackData {
    // Données
    private final Map<String, Attack> attacks;

    // Constructeurs

    /**
     * Initialise le registre des attaques à partir de `data/attaques.json`.
     */
    public AttackData() {
        JsonDataLoader<Attack> loader = new JsonDataLoader<>();
        // Les attaques restent indexées par id pour les références des Bugémons.
        attacks = loader.load("data/attaques.json", "attaques", Attack.class, Attack::getId);
    }


    // ----------------- Accesseurs -----------------

    /**
     * Retourne une attaque par son identifiant.
     *
     * @param id identifiant technique de l'attaque.
     * @return attaque correspondante, ou null si elle n'existe pas.
     */
    public Attack getAttack(String id) {
        return attacks.get(id);
    }

    /**
     * Retourne toutes les attaques chargées.
     *
     * @return map des attaques indexées par identifiant.
     */
    public Map<String, Attack> getAllAttacks() {
        return attacks;
    }
}
