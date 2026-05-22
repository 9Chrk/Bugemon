package ulb.models.game;

import java.util.ArrayList;
import java.util.List;

/**
 * Représente une équipe de Bugémons.
 * Contient une liste de `BugemonInstance` et un nom d'équipe.
 */
public class Team {
    public static final int MAX_TEAM_SIZE = 6;

    // Données
    private String name;
    private final List<BugemonInstance> team;

    // ----------------- Constructeurs -----------------

    /**
     * Construit une équipe vide avec un nom.
     *
     * @param name nom de l'équipe.
     */
    public Team(String name) {
        // Validation
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        this.name = name;
        this.team = new ArrayList<>();
    }

    /**
     * Construit une équipe avec un nom et une liste initiale.
     *
     * @param name nom de l'équipe.
     * @param team liste initiale de Bugémons.
     */
    public Team(String name, List<BugemonInstance> team) {
        // Validation
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Team name cannot be null or empty");
        }
        if (team == null) {
            throw new IllegalArgumentException("Team list cannot be null");
        }
        if (team.size() > MAX_TEAM_SIZE) {
            throw new IllegalArgumentException("Team cannot have more than " + MAX_TEAM_SIZE + " Bugemons");
        }
        this.name = name;
        this.team = new ArrayList<>(team);
    }

    /**
     * Construit une copie profonde d'une équipe existante.
     *
     * @param other équipe source.
     */
    public Team(Team other) {
        this.name = other.getName();
        this.team = new ArrayList<>(other.getTeam().stream() // Copie profonde des BugemonInstances.
                .map(BugemonInstance::new)
                .toList());
    }

    /**
     * Construit une équipe sans nom, utilisée comme structure vide technique.
     */
    public Team() {
        this.team = new ArrayList<>();
    }

    // ----------------- Méthodes -----------------

    // ----- Gestion d'équipe -----

    /**
     * Ajoute un Bugémon à l'équipe.
     *
     * @param bugemon Bugémon à ajouter.
     * @return true si l'ajout a réussi.
     */
    public boolean addBugemon(BugemonInstance bugemon) {
        if (bugemon == null || team.size() >= MAX_TEAM_SIZE || containsBugemon(bugemon)) {
            return false;
        }
        team.add(bugemon);
        return true;
    }

    /**
     * Retire un Bugémon de l'équipe.
     *
     * @param bugemon Bugémon à retirer.
     * @return true si le retrait a réussi.
     */
    public boolean removeBugemon(BugemonInstance bugemon) {
        return team.remove(bugemon);
    }

    /**
     * Remplace tous les membres de l'équipe par une nouvelle liste.
     *
     * @param newMembers nouvelle liste de Bugémons.
     */
    public void replaceAll(List<BugemonInstance> newMembers) {
        // Utilisé pour remplacer l'équipe active par un état reconstruit.
        this.team.clear();
        this.team.addAll(newMembers);
    }

    /**
     * Vérifie si l'équipe contient déjà un Bugémon de même identifiant.
     *
     * @param bugemon Bugémon à vérifier.
     * @return true si un Bugémon de même id existe déjà.
     */
    public boolean containsBugemon(BugemonInstance bugemon) {
        if (bugemon == null)
            return false;

        for (BugemonInstance b : team) {
            if (b.getId().equals(bugemon.getId()))
                return true;
        }
        return false;
    }

    // ----- Etat de l'équipe -----

    /**
     * Indique si l'équipe est vide.
     *
     * @return true si aucun Bugémon n'est présent.
     */
    public boolean isEmpty() {
        return team.isEmpty();
    }

    // ----- Accès aux Bugémons -----

    /**
     * Retourne le Bugémon à un index donné.
     *
     * @param index index dans l'équipe.
     * @return Bugémon correspondant.
     */
    public BugemonInstance getBugemonAt(int index) {
        return team.get(index);
    }

    /**
     * Retourne un Bugémon par identifiant.
     *
     * @param id identifiant recherché.
     * @return Bugémon trouvé, ou null.
     */
    public BugemonInstance getBugemonById(String id) {
        for (BugemonInstance b : team) {
            if (b.getId().equals(id))
                return b;
        }
        return null;
    }

    /**
     * Retourne le premier Bugémon de l'équipe.
     *
     * @return premier Bugémon, ou null si l'équipe est vide.
     */
    public BugemonInstance getFirstBugemon() {
        if (team.isEmpty())
            return null;
        return team.get(0);
    }

    /**
     * Retourne le premier Bugémon non K.O.
     *
     * @return premier Bugémon vivant, ou null.
     */
    public BugemonInstance getFirstAliveBugemon() {
        for (BugemonInstance b : team) {
            if (!b.isKo())
                return b;
        }
        return null;
    }

    /**
     * Retourne la liste des Bugémons vivants.
     *
     * @return liste immuable des Bugémons vivants.
     */
    public List<BugemonInstance> getAliveBugemons() {
        List<BugemonInstance> alive = new ArrayList<>();

        // On reconstruit la liste pour ne jamais exposer la collection interne.
        for (BugemonInstance b : team) {
            if (!b.isKo())
                alive.add(b);
        }
        return List.copyOf(alive);
    }

    /**
     * Retourne une copie non modifiable de la liste de l'équipe.
     *
     * @return copie de la liste des créatures.
     */
    public List<BugemonInstance> getBugemons() {
        return List.copyOf(this.team);
    }

    // ----- Soins et restauration d'équipe -----

    /**
     * Soigne tous les Bugémons non K.O. de l'équipe.
     *
     * @param amount quantité de soins.
     */
    public void healWholeTeam(int amount) {
        for (BugemonInstance b : team) {
            // Un Bugémon K.O. ne revient pas via un soin d'équipe standard.
            if (!b.isKo())
                b.heal(amount);
        }
    }

    /**
     * Réinitialise les bonus temporaires et les marqueurs de participation.
     */
    public void resetTeamTemporaryBonuses() {
        for (BugemonInstance b : team) {
            // Appelé entre deux combats pour repartir sans état de tour résiduel.
            b.resetTemporaryBonus();
            b.setParticipatedInBattle(false);
        }
    }

    /**
     * Réinitialise uniquement les modificateurs temporaires de combat.
     */
    public void resetCombatStatModifiers() {
        for (BugemonInstance b : team) {
            b.resetTemporaryBonus();
        }
    }

    /**
     * Efface les bonus actifs d'un tour sur chaque Bugémon.
     */
    public void clearActiveTurnBonuses() {
        for (BugemonInstance b : team) {
            b.clearActiveTurnBonus();
        }
    }

    // ----------------- Accesseurs -----------------

    /**
     * Retourne le nom de l'équipe.
     *
     * @return nom de l'équipe.
     */
    public String getName() {
        return name;
    }

    /**
     * Définit le nom de l'équipe.
     *
     * @param name nouveau nom.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Retourne une copie immuable de l'équipe.
     *
     * @return liste immuable des membres.
     */
    public List<BugemonInstance> getTeam() {
        return List.copyOf(team);
    }

    /**
     * Retourne la taille courante de l'équipe.
     *
     * @return nombre de membres.
     */
    public int getSize() {
        return team.size();
    }

    /**
     * Retourne le niveau du bugemon le plus élevé de l'équipe.
     * Utile la gestion de la difficulté.
     *
     * @return Le plus haut niveau.
     */
    public int getTopLevel() {
        int top = 0;

        // Parcours explicite pour éviter une valeur par défaut trompeuse en cas d'équipe vide.
        for (BugemonInstance bugemon : this.team) {
            int bugemonLevel = bugemon.getLevel();
            if (bugemonLevel > top) {
                top = bugemonLevel;
            }
        }
        return top;

    }

    /**
     * Retourne le niveau moyen des bugemon de l'équipe
     * Utile la gestion de la difficulté
     *
     * @return Le niveau moyen.
     */
    public int getMoyLevel() {
        int sum = 0;

        for (BugemonInstance bugemon : this.team) {
            sum += bugemon.getLevel();
        }
        return sum / team.size();
    }
}
