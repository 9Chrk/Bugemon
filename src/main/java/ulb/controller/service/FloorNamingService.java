package ulb.controller.service;

/**
 * Service de nommage des étages de la tour.
 */
public class FloorNamingService {

    /**
     * Retourne le nom lisible d'un étage.
     *
     * @param floor numéro de l'étage.
     * @return nom affichable de l'étage.
     */
    public String getFloorName(int floor) {
        return switch (floor) {
            case 2 -> "Accueil";
            case 3 -> "Cafétéria";
            case 4 -> "Centre de Soin";
            case 5 -> "Open Space";
            case 6 -> "Laboratoire";
            case 7 -> "Bureau du Professeur";
            case 8 -> "Entrepôt";
            case 9 -> "Labo Secret";
            case 10 -> "Salle de Surveillance";
            default -> "Inconnu";
        };
    }
}

