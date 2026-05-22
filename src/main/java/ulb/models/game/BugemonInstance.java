package ulb.models.game;

import ulb.models.data.BugemonDefinition;
import ulb.models.data.Stats;
import java.util.ArrayList;
import java.util.List;

/**
 * Instance mutable d'un Bugémon utilisée en équipe et en combat.
 *
 * Cette classe gère les PV courants, le niveau, l'XP, les bonus permanents
 * et temporaires, ainsi que les attaques apprises.
 */
public class BugemonInstance {
    // Données
    private final BugemonDefinition species;
    private final List<String> learnedAttackIds;
    // Etat courant
    private int level;
    private int xp;
    private int currentHp;
    private boolean hasParticipatedInBattle; // Utilisé pour l'attribution d'XP en fin de combat.
    // Bonus de statistiques
    private final Stats permanentBonus;
    private transient Stats runBonus;
    private final Stats temporaryBonus;
    private final Stats queuedTurnBonus;
    private final Stats activeTurnBonus;
    // Constantes
    private static final int BASE_XP_REQUIREMENT = 50;

    // ----------------- Constructeurs -----------------

    /**
     * Construit un Bugémon niveau 1 avec 0 XP.
     *
     * @param species définition d'espèce.
     */
    public BugemonInstance(BugemonDefinition species) {
        this(species, 1, 0);
    }

    /**
     * Construit un Bugémon avec niveau et XP initiaux.
     *
     * @param species définition d'espèce.
     * @param level   niveau initial.
     * @param xp      XP initiale.
     */
    public BugemonInstance(BugemonDefinition species, int level, int xp) {
        // Validation
        if (species == null)
            throw new IllegalArgumentException("Species cannot be null");
        if (level < 1)
            throw new IllegalArgumentException("Level must be at least 1");
        if (xp < 0)
            throw new IllegalArgumentException("XP cannot be negative");

        // Données
        this.species = species;
        this.learnedAttackIds = new ArrayList<>(species.getAttackIds());

        // Bonus de statistiques
        this.permanentBonus = new Stats();
        this.runBonus = new Stats();
        this.temporaryBonus = new Stats();
        this.queuedTurnBonus = new Stats();
        this.activeTurnBonus = new Stats();

        // Etat courant
        this.level = level;
        this.xp = xp;
        this.currentHp = getEffectiveStats().getHealth();
        this.hasParticipatedInBattle = false;
    }

    /**
     * Construit une copie profonde d'une instance existante.
     *
     * @param other instance source.
     */
    public BugemonInstance(BugemonInstance other) {
        // Validation
        if (other == null)
            throw new IllegalArgumentException("Other BugemonInstance cannot be null");

        // Données
        this.species = other.getSpecies();
        // Copie de liste pour que l'oubli/apprentissage reste isolé entre instances.
        this.learnedAttackIds = new ArrayList<>(other.getLearnedAttackIds());

        // Bonus de statistiques
        this.permanentBonus = new Stats(other.permanentBonus);
        this.runBonus = new Stats(other.ensureRunBonusInitialized());
        this.temporaryBonus = new Stats(other.temporaryBonus);
        this.queuedTurnBonus = new Stats(other.queuedTurnBonus);
        this.activeTurnBonus = new Stats(other.activeTurnBonus);

        // Etat courant
        this.level = other.getLevel();
        this.xp = other.getXp();
        this.currentHp = other.getCurrentHp();
        if (this.currentHp > getEffectiveStats().getHealth()) {
            // Un changement de bonus peut rendre les PV sauvegardés trop hauts.
            this.currentHp = getEffectiveStats().getHealth();
        }
        this.hasParticipatedInBattle = other.hasParticipatedInBattle();
    }

    // ----------------- Méthodes -----------------

    // ----- Dégâts et soins -----

    /**
     * Applique des dégâts au Bugémon.
     *
     * @param amount valeur des dégâts.
     */
    public void takeDamage(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Damage cannot be negative");
        currentHp = Math.max(0, currentHp - amount);
    }

    /**
     * Soigne le Bugémon sans dépasser ses PV maximums.
     *
     * @param amount valeur de soin.
     */
    public void heal(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("Heal amount cannot be negative");
        int maxHp = getEffectiveStats().getHealth();
        currentHp = Math.min(maxHp, currentHp + amount);
    }

    /**
     * Restaure complètement les PV du Bugémon.
     */
    public void restoreFullHp() {
        currentHp = getEffectiveStats().getHealth();
    }

    /**
     * Indique si le Bugémon est K.O.
     *
     * @return true si ses PV sont à 0 ou moins.
     */
    public boolean isKo() {
        return currentHp <= 0;
    }

    // ----- Modificateurs de statistiques -----

    /**
     * Applique un bonus permanent de statistiques.
     *
     * @param bonus bonus à appliquer.
     */
    public void applyPermanentBonus(Stats bonus) {
        if (bonus == null)
            throw new IllegalArgumentException("Bonus cannot be null");
        permanentBonus.add(bonus);
    }

    /**
     * Applique un bonus temporaire de statistiques.
     *
     * @param bonus bonus à appliquer.
     */
    public void applyTemporaryBonus(Stats bonus) {
        if (bonus == null)
            throw new IllegalArgumentException("Bonus cannot be null");
        temporaryBonus.add(bonus);
    }

    /**
     * Met en attente un bonus à activer au prochain tour.
     *
     * @param bonus bonus à mettre en file.
     */
    public void queueNextTurnBonus(Stats bonus) {
        if (bonus == null)
            throw new IllegalArgumentException("Bonus cannot be null");
        queuedTurnBonus.add(bonus);
    }

    /**
     * Active les bonus en attente pour le tour courant.
     */
    public void activateQueuedTurnBonus() {
        activeTurnBonus.reset();
        // Le bonus préparé devient actif uniquement pour ce tour.
        activeTurnBonus.add(queuedTurnBonus);
        queuedTurnBonus.reset();
    }

    /**
     * Efface les bonus actifs pour ce tour.
     */
    public void clearActiveTurnBonus() {
        activeTurnBonus.reset();
    }

    /**
     * Réinitialise tous les bonus temporaires.
     */
    public void resetTemporaryBonus() {
        temporaryBonus.reset();
        queuedTurnBonus.reset();
        activeTurnBonus.reset();
    }

    /**
     * Supprime uniquement la contribution négative des bonus temporaires.
     */
    public void clearNegativeTemporaryBonuses() {
        // Les boosts positifs restent, seuls les malus sont neutralisés.
        removeNegativeContribution(temporaryBonus);
        removeNegativeContribution(queuedTurnBonus);
        removeNegativeContribution(activeTurnBonus);
    }

    /**
     * DÃ©finit le bonus de run (persistant sur toute la partie) appliquÃ© aux stats.
     *
     * @param bonus bonus de run Ã  appliquer.
     */
    public void setRunBonus(Stats bonus) {
        if (bonus == null) {
            throw new IllegalArgumentException("Bonus cannot be null");
        }
        ensureRunBonusInitialized();
        runBonus.reset();
        runBonus.add(bonus);
        if (currentHp > getEffectiveStats().getHealth()) {
            currentHp = getEffectiveStats().getHealth();
        }
    }

    /**
     * Supprime le bonus de run courant.
     */
    public void clearRunBonus() {
        ensureRunBonusInitialized();
        runBonus.reset();
        if (currentHp > getEffectiveStats().getHealth()) {
            currentHp = getEffectiveStats().getHealth();
        }
    }

    /**
     * Calcule les statistiques effectives (base mise à l'échelle par le niveau
     * + permanent + temporaire + actif tour).
     *
     * @return statistiques effectives.
     */
    public Stats getEffectiveStats() {
        Stats scaledStats = scaleStatsByLevel(species.getBaseStats(), this.level);

        // Ordre volontaire : niveau, permanents, bonus de run, temporaires, bonus du tour.
        Stats withPermanent = permanentBonus.applyTo(scaledStats);
        Stats withRunBonus = ensureRunBonusInitialized().applyTo(withPermanent);
        Stats withTemporary = temporaryBonus.applyTo(withRunBonus);
        return activeTurnBonus.applyTo(withTemporary);
    }

    /**
     * Calcule les stats de base ajustées selon le niveau.
     * Ici, chaque niveau au-dessus du niveau 1 augmente les stats de 10%.
     */
    private Stats scaleStatsByLevel(Stats base, int level) {
        // Calcul du facteur (ex: niveau 1 = 1.0, niveau 5 = 1.4)
        double factor = 1.0 + (level - 1) * 0.1;

        return new Stats(
                (int) (base.getHealth() * factor),
                (int) (base.getAttack() * factor),
                (int) (base.getDefense() * factor),
                (int) (base.getInitiative() * factor));
    }

    // ----- Attaques -----

    /**
     * Retourne les identifiants des attaques apprises.
     *
     * @return liste immuable des attaques.
     */
    public List<String> getLearnedAttackIds() {
        return List.copyOf(learnedAttackIds);
    }

    /**
     * Apprend une attaque si elle n'est pas déjà connue.
     *
     * @param attackId identifiant de l'attaque.
     */
    public void learnAttack(String attackId) {
        if (!learnedAttackIds.contains(attackId)) {
            learnedAttackIds.add(attackId);
        }
    }

    /**
     * Oublie une attaque.
     *
     * @param attackId identifiant de l'attaque.
     */
    public void forgetAttack(String attackId) {
        learnedAttackIds.remove(attackId);
    }

    // ----- XP et niveaux -----

    /**
     * Définit le marqueur de participation au combat.
     *
     * @param participated true si le Bugémon a participé.
     */
    public void setParticipatedInBattle(boolean participated) {
        this.hasParticipatedInBattle = participated;
    }

    /**
     * Ajoute de l'XP et applique les montées de niveau éventuelles.
     *
     * @param amount quantité d'XP gagnée.
     * @return nombre de niveaux gagnés.
     */
    public int gainXp(int amount) {
        if (amount < 0)
            throw new IllegalArgumentException("XP gain cannot be negative");
        xp += amount;
        return this.updateLevel();
    }

    private void levelUp() {
        level++;
    }

    private int xpRequiredForNextLevel() {
        return BASE_XP_REQUIREMENT + BASE_XP_REQUIREMENT * (level - 1);
    }

    private boolean canLevelUp() {
        return xp >= xpRequiredForNextLevel();
    }

    private int updateLevel() {
        int levelsGained = 0;

        while (canLevelUp()) {
            // L'XP excédentaire est conservée pour le niveau suivant.
            xp -= xpRequiredForNextLevel();
            levelUp();
            levelsGained++;
        }
        if (levelsGained > 0)
            // Monter de niveau remet le Bugémon en état de continuer la run.
            restoreFullHp();
        return levelsGained;
    }

    // --- Accesseurs ---

    /**
     * Retourne la définition d'espèce.
     *
     * @return espèce de base.
     */
    public BugemonDefinition getSpecies() {
        return species;
    }

    /**
     * Retourne le nom du Bugémon.
     *
     * @return nom affiché.
     */
    public String getName() {
        return species.getName();
    }

    /**
     * Retourne les PV courants.
     *
     * @return PV actuels.
     */
    public int getCurrentHp() {
        return currentHp;
    }

    /**
     * Retourne le niveau courant.
     *
     * @return niveau.
     */
    public int getLevel() {
        return level;
    }

    /**
     * Modifie le niveau du bugemon.
     * Utile principalement lors de la création d'un bugemon ennemi.
     * Met à jour les PV au maximum correspondant au nouveau niveau.
     */
    public void setLevel(int level) {
        this.level = level;
        this.restoreFullHp();
        this.xp = 0;
    }

    /**
     * Retourne l'XP courante.
     *
     * @return XP.
     */
    public int getXp() {
        return xp;
    }

    /**
     * Indique si le Bugémon a participé au combat.
     *
     * @return true si participation enregistrée.
     */
    public boolean hasParticipatedInBattle() {
        return hasParticipatedInBattle;
    }

    /**
     * Retourne l'identifiant technique du Bugémon.
     *
     * @return identifiant.
     */
    public String getId() {
        return species.getId();
    }

    /**
     * Copie tout l'état mutable depuis une autre instance.
     *
     * @param other instance source.
     */
    public void copyStateFrom(BugemonInstance other) {
        if (other == null) {
            throw new IllegalArgumentException("Other BugemonInstance cannot be null");
        }

        this.learnedAttackIds.clear();
        this.learnedAttackIds.addAll(other.getLearnedAttackIds());

        // Les objets Stats sont mutables : on copie leur contenu au lieu de les partager.
        this.permanentBonus.reset();
        this.permanentBonus.add(other.permanentBonus);
        this.ensureRunBonusInitialized().reset();
        this.runBonus.add(other.ensureRunBonusInitialized());

        this.temporaryBonus.reset();
        this.temporaryBonus.add(other.temporaryBonus);

        this.queuedTurnBonus.reset();
        this.queuedTurnBonus.add(other.queuedTurnBonus);

        this.activeTurnBonus.reset();
        this.activeTurnBonus.add(other.activeTurnBonus);

        this.level = other.level;
        this.xp = other.xp;
        this.currentHp = other.currentHp;
        // La participation au combat est aussi un état utile pour l'XP.
        this.hasParticipatedInBattle = other.hasParticipatedInBattle;
    }

    private Stats ensureRunBonusInitialized() {
        if (runBonus == null) {
            runBonus = new Stats();
        }
        return runBonus;
    }

    private void removeNegativeContribution(Stats stats) {
        // On retire uniquement la partie négative de chaque statistique
        // afin de conserver les bonus positifs déjà présents.
        Stats negativePart = new Stats(
                Math.min(0, stats.getHealth()),
                Math.min(0, stats.getAttack()),
                Math.min(0, stats.getDefense()),
                Math.min(0, stats.getInitiative()));
        stats.subtract(negativePart);
    }

}
