package ulb.models.game;

import java.util.HashMap;
import java.util.Map;
import ulb.parsing.ItemData;

/**
 * Représente l'inventaire courant du joueur.
 */
public class Inventory {

    private final Map<String, Integer> items;

    /**
     * Construit un inventaire avec les quantités de départ définies dans les données.
     */
    public Inventory() {
        this(new ItemData().getStartingInventory());
    }

    /**
     * Construit un inventaire à partir d'un stock initial.
     *
     * @param initialItems map id -> quantité initiale.
     */
    public Inventory(Map<String, Integer> initialItems) {
        this.items = new HashMap<>();
        if (initialItems == null) {
            return;
        }

        // Passe par addItem pour valider et fusionner les quantités dès le chargement.
        for (Map.Entry<String, Integer> entry : initialItems.entrySet()) {
            addItem(entry.getKey(), entry.getValue());
        }
    }

    /**
     * Ajoute une quantité d'objet à l'inventaire.
     *
     * @param itemId identifiant de l'objet.
     * @param quantity quantité à ajouter.
     */
    public void addItem(String itemId, int quantity) {
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException("Item ID cannot be null or blank.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive.");
        }
        items.put(itemId, items.getOrDefault(itemId, 0) + quantity);
    }

    /**
     * Vérifie si l'inventaire contient au moins un exemplaire d'un objet.
     *
     * @param itemId identifiant de l'objet.
     * @return true si l'objet est disponible.
     */
    public boolean hasItem(String itemId) {
        return items.getOrDefault(itemId, 0) <= 0;
    }

    /**
     * Consomme une unité d'objet.
     *
     * @param itemId identifiant de l'objet.
     */
    public void useItem(String itemId) {
        if (hasItem(itemId)) {
            return;
        }

        items.put(itemId, items.get(itemId) - 1);

        // Nettoie l'entrée quand la quantité atteint zéro.
        if (items.get(itemId) <= 0) {
            items.remove(itemId);
        }

    }

    /**
     * Retourne la quantité disponible pour un objet.
     *
     * @param itemId identifiant de l'objet.
     * @return quantité disponible.
     */
    public int getQuantity(String itemId) {
        return items.getOrDefault(itemId, 0);
    }

    /**
     * Réinitialise l'inventaire avec les objets de départ.
     */
    public void resetToStartingInventory() {
        items.clear();
        // Recharge le stock de départ depuis les données pour rester aligné avec le JSON.
        items.putAll(new ItemData().getStartingInventory());
    }

    /**
     * Retourne un instantané immuable de l'inventaire.
     *
     * @return map id -> quantité.
     */
    public Map<String, Integer> getItems() {
        return Map.copyOf(items);
    }
}
