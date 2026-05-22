package ulb.models.battle;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import ulb.models.data.Stats;
import ulb.models.game.BugemonInstance;
import ulb.models.game.LevelUpBonus;
import ulb.models.game.Team;

/**
 * Gère le calcul de l'XP de combat, sa répartition et la génération des bonus de montée de niveau.
 */
public class ExperienceService {
    private final Random random;

    /**
     * Construit le service avec une source aléatoire par défaut.
     */
    public ExperienceService() {
        this(new Random());
    }

    /**
     * Construit le service avec une source aléatoire fournie.
     *
     * @param random source aléatoire utilisée pour les choix de bonus.
     */
    public ExperienceService(Random random) {
        this.random = random == null ? new Random() : random;
    }

    /**
     * Calcule l'XP totale gagnée après une victoire.
     *
     * @param floorNumber numéro d'étage.
     * @param bossBattle true si le combat est un boss.
     * @param enemyCount nombre d'adversaires.
     * @return XP totale gagnée.
     */
    public int calculateVictoryXp(int floorNumber, boolean bossBattle, int enemyCount) {
        if (floorNumber < 1 || enemyCount < 1) {
            throw new IllegalArgumentException(BattleConfig.EXPERIENCE_INPUT_ERROR);
        }

        int typeMultiplier = bossBattle ? BattleConfig.BOSS_XP_MULTIPLIER : BattleConfig.NORMAL_XP_MULTIPLIER;
        return BattleConfig.XP_BASE_REWARD * floorNumber * typeMultiplier * enemyCount;
    }

    /**
     * Répartit l'XP de victoire entre les Bugémons ayant participé.
     *
     * @param playerTeam équipe du joueur.
     * @param totalXp XP totale à distribuer.
     * @return détail de la répartition effectuée.
     */
    public ExperienceResolution distributeVictoryXp(Team playerTeam, int totalXp) {
        if (playerTeam == null || totalXp < 0) {
            throw new IllegalArgumentException(BattleConfig.EXPERIENCE_DISTRIBUTION_ERROR);
        }

        List<BugemonInstance> participants = playerTeam.getBugemons().stream()
                .filter(BugemonInstance::hasParticipatedInBattle)
                .toList();

        if (participants.isEmpty() || totalXp == 0) {
            return new ExperienceResolution(totalXp, List.of());
        }

        int baseShare = totalXp / participants.size();
        int remainder = totalXp % participants.size();
        List<ExperienceGain> gains = new ArrayList<>();

        for (int i = 0; i < participants.size(); i++) {
            BugemonInstance participant = participants.get(i);
            // Le reste est distribué sur les premiers participants pour ne perdre aucun point.
            int xpShare = baseShare + (i < remainder ? 1 : 0);
            int levelsGained = participant.gainXp(xpShare);
            gains.add(new ExperienceGain(participant, xpShare, levelsGained));
        }

        return new ExperienceResolution(totalXp, gains);
    }

    /**
     * Génère trois propositions de bonus de montée de niveau.
     *
     * @return liste immuable de bonus possibles.
     */
    public List<LevelUpBonus> generateLevelUpChoices() {
        return generateLevelUpChoices(BattleConfig.LEVEL_UP_CHOICES_COUNT);
    }

    /**
     * GÃ©nÃ¨re un nombre demandÃ© de propositions de bonus de montÃ©e de niveau.
     *
     * @param choiceCount nombre de choix Ã  produire.
     * @return liste immuable de bonus possibles.
     */
    public List<LevelUpBonus> generateLevelUpChoices(int choiceCount) {
        if (choiceCount < 1) {
            throw new IllegalArgumentException("Choice count must be positive.");
        }

        List<LevelUpBonus> result = new ArrayList<>();
        Set<String> usedDescriptions = new LinkedHashSet<>();

        while (result.size() < choiceCount) {
            LevelUpBonus candidate;

            if (result.isEmpty()) {
                // Le premier choix est toujours un bonus simple et lisible.
                candidate = getFixedBonus(random.nextInt(BattleConfig.BONUS_STAT_COUNT));
            } else {
                candidate = random.nextBoolean()
                        ? getFixedBonus(random.nextInt(BattleConfig.BONUS_STAT_COUNT))
                        : generateCombinationBonus();
            }

            if (usedDescriptions.add(candidate.description())) {
                // Évite d'afficher deux options identiques dans le même niveau.
                result.add(candidate);
            }
        }

        return List.copyOf(result);
    }

    private LevelUpBonus getFixedBonus(int index) {
        return switch (index) {
            case 0 -> buildBonus(BattleConfig.LEVEL_UP_BONUS_POINTS, 0, 0, 0);
            case 1 -> buildBonus(0, BattleConfig.LEVEL_UP_BONUS_POINTS, 0, 0);
            case 2 -> buildBonus(0, 0, BattleConfig.LEVEL_UP_BONUS_POINTS, 0);
            default -> buildBonus(0, 0, 0, BattleConfig.LEVEL_UP_BONUS_POINTS);
        };
    }

    private LevelUpBonus generateCombinationBonus() {
        int hpPoints = 0;
        int attackPoints = 0;
        int defensePoints = 0;
        int initiativePoints = 0;

        while (hpPoints + attackPoints + defensePoints + initiativePoints < BattleConfig.LEVEL_UP_BONUS_POINTS) {
            int remaining = BattleConfig.LEVEL_UP_BONUS_POINTS
                    - (hpPoints + attackPoints + defensePoints + initiativePoints);
            int delta = 1 + random.nextInt(remaining);

            switch (random.nextInt(BattleConfig.BONUS_STAT_COUNT)) {
                case 0 -> hpPoints += delta;
                case 1 -> attackPoints += delta;
                case 2 -> defensePoints += delta;
                default -> initiativePoints += delta;
            }
        }

        int nonZero = 0;
        if (hpPoints > 0) nonZero++;
        if (attackPoints > 0) nonZero++;
        if (defensePoints > 0) nonZero++;
        if (initiativePoints > 0) nonZero++;

        if (nonZero < BattleConfig.MINIMUM_COMBINATION_STATS) {
            // Une combinaison doit réellement répartir les points sur plusieurs stats.
            return generateCombinationBonus();
        }

        return buildBonus(hpPoints, attackPoints, defensePoints, initiativePoints);
    }

    private LevelUpBonus buildBonus(int hpPoints, int attackPoints, int defensePoints, int initiativePoints) {
        Stats bonus = new Stats(
                hpPoints * BattleConfig.HEALTH_BONUS_MULTIPLIER,
                attackPoints,
                defensePoints,
                initiativePoints * BattleConfig.INITIATIVE_BONUS_MULTIPLIER
        );

        List<String> parts = new ArrayList<>();
        if (bonus.getHealth() > 0) {
            parts.add(BattleConfig.BONUS_PREFIX + bonus.getHealth() + BattleConfig.HEALTH_BONUS_LABEL);
        }
        if (bonus.getAttack() > 0) {
            parts.add(BattleConfig.BONUS_PREFIX + bonus.getAttack() + BattleConfig.ATTACK_BONUS_LABEL);
        }
        if (bonus.getDefense() > 0) {
            parts.add(BattleConfig.BONUS_PREFIX + bonus.getDefense() + BattleConfig.DEFENSE_BONUS_LABEL);
        }
        if (bonus.getInitiative() > 0) {
            parts.add(BattleConfig.BONUS_PREFIX + bonus.getInitiative() + BattleConfig.INITIATIVE_BONUS_LABEL);
        }

        return new LevelUpBonus(bonus, String.join(BattleConfig.BONUS_SEPARATOR, parts));
    }
}
