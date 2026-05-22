package ulb.controller.service;

import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;

import ulb.models.game.*;
import ulb.models.battle.ExperienceService;
import ulb.models.skilltree.SkillTreeBonuses;

/**
 * Service de gestion des choix et remontées de niveau.
 */
public class LevelUpChoiceHandler {
    private final Deque<PendingLevelUpChoice> levelUpQueue = new ArrayDeque<>();
    private final ExperienceService experienceService = new ExperienceService();

    /**
     * Ajoute des choix de montée de niveau à la file.
     *
     * @param bugemon      Bugémon ayant gagné des niveaux.
     * @param levelsGained nombre de niveaux gagnés.
     * @param skillBonuses bonus modifiant les choix proposés.
     * @param summary      journal textuel à compléter.
     */
    public void queueLevelUpChoices(BugemonInstance bugemon, int levelsGained,
            SkillTreeBonuses skillBonuses, StringBuilder summary) {
        for (int i = 0; i < levelsGained; i++) {
            // Reconstitue chaque niveau atteint si plusieurs niveaux sont gagnés d'un coup.
            int levelReached = bugemon.getLevel() - levelsGained + i + 1;
            levelUpQueue.add(new PendingLevelUpChoice(bugemon, levelReached,
                    experienceService.generateLevelUpChoices(skillBonuses.getLevelUpChoiceCount())));
            summary.append(bugemon.getName()).append(" reached level ").append(levelReached).append("!\n");
        }
    }

    /**
     * Applique le choix de montée de niveau en attente et le retire de la file.
     *
     * @param choiceIndex index du choix sélectionné.
     * @return ligne de journal décrivant le bonus appliqué.
     */
    public String applyPendingLevelUpChoice(int choiceIndex) {
        PendingLevelUpChoice pending = levelUpQueue.pollFirst();
        if (pending == null)
            return "";

        // La file impose de résoudre les montées de niveau dans l'ordre.
        LevelUpBonus bonus = pending.choices().get(choiceIndex);
        bonus.applyTo(pending.bugemon());
        pending.bugemon().restoreFullHp();

        return String.format("%s received: %s", pending.bugemon().getName(), bonus.description());
    }

    /**
     * Indique s'il reste des choix de montée de niveau à résoudre.
     *
     * @return true si au moins un choix est en attente.
     */
    public boolean hasPendingChoices() {
        return !levelUpQueue.isEmpty();
    }

    /**
     * Retourne les descriptions du premier ensemble de choix.
     *
     * @return descriptions affichables, ou liste vide si aucun choix n'attend.
     */
    public java.util.List<String> getCurrentChoiceDescriptions() {
        // La vue ne voit que le premier paquet de choix en attente.
        return levelUpQueue.isEmpty() ? java.util.List.of()
                : levelUpQueue.peekFirst().choices().stream().map(LevelUpBonus::description).toList();
    }

    /**
     * Retourne le titre de la fenêtre de montée de niveau active.
     *
     * @return titre affichable, ou chaîne vide si aucun choix n'attend.
     */
    public String getCurrentChoiceTitle() {
        if (levelUpQueue.isEmpty()) {
            return "";
        }

        PendingLevelUpChoice pending = levelUpQueue.peekFirst();
        return pending.bugemon().getName() + " atteint le niveau " + pending.reachedLevel();
    }

    /**
     * Réinitialise la file des choix.
     */
    public void clear() {
        levelUpQueue.clear();
    }
}





