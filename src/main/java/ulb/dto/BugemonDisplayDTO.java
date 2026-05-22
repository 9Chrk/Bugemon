package ulb.dto;

/**
 * DTO représentant les informations de base d'un Bugémon pour affichage dans la UI.
 * Isolé le modèle de la vue lors des opérations de swap d'équipe.
 */
public record BugemonDisplayDTO(String name, int level, int index) {
    /**
     * Construit le DTO avec les informations d'affichage.
     *
     * @param name  nom du Bugémon.
     * @param level niveau du Bugémon.
     * @param index position dans l'équipe.
     */
    public BugemonDisplayDTO {
    }

    /**
     * Retourne le nom du Bugémon.
     *
     * @return nom.
     */
    @Override
    public String name() {
        return name;
    }

    /**
     * Retourne l'index du Bugémon dans l'équipe.
     *
     * @return index.
     */
    @Override
    public int index() {
        return index;
    }

    /**
     * Retourne le texte d'affichage pour le Bugémon.
     *
     * @return texte formaté.
     */
    public String getDisplayText() {
        return name + " (Niv." + level + ")";
    }
}

