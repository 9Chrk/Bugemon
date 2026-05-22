package ulb.controller.service;

import ulb.controller.TeamManagerController;
import ulb.dto.RunSlotDTO;
import ulb.models.data.Difficulty;
import ulb.models.game.GameSlotData;
import ulb.models.game.PlayerProfile;

import java.util.ArrayList;
import java.util.List;

/**
 * Service fournissant les emplacements de partie sous forme DTO prêts pour
 * l'affichage.
 */
public class RunSlotService {
    private final TeamManagerController teamManagerController;

    /**
     * Construit le service à partir du contrôleur de profil joueur.
     *
     * @param teamManagerController contrôleur donnant accès au profil joueur.
     */
    public RunSlotService(TeamManagerController teamManagerController) {
        this.teamManagerController = teamManagerController;
    }

    /**
     * Retourne les emplacements de partie formatés pour l'affichage.
     *
     * @return liste des DTOs d'emplacements.
     */
    public List<RunSlotDTO> getRunSlots() {
        List<RunSlotDTO> slots = new ArrayList<>(PlayerProfile.RUN_SLOT_COUNT);
        for (int i = 0; i < PlayerProfile.RUN_SLOT_COUNT; i++) {
            GameSlotData slot = teamManagerController.getPlayerProfile().getRunSlot(i);
            slots.add(new RunSlotDTO(i, slot.isOccupied(), formatRunSlotDisplayText(i, slot)));
        }
        return slots;
    }

    private String formatRunSlotDisplayText(int slotIndex, GameSlotData slot) {
        if (!slot.isOccupied()) {
            return (slotIndex + 1) + ".  Libre";
        }

        String difficultyText = formatDifficulty(slot.getDifficulty());
        return (slotIndex + 1) + ".  " + slot.getRunName()
                + " - " + slot.getTeamName()
                + " - Etage " + slot.getTowerLevel()
                + " - " + difficultyText;
    }

    private String formatDifficulty(Difficulty difficulty) {
        Difficulty resolvedDifficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        return switch (resolvedDifficulty) {
            case EASY -> "Facile";
            case NORMAL -> "Normal";
            case HARD -> "Difficile";
        };
    }
}

