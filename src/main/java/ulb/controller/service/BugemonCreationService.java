package ulb.controller.service;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.Normalizer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import ulb.controller.TeamManagerController;

/**
 * Service de création et gestion des Bugémons personnalisés.
 */
public class BugemonCreationService {
    private final TeamManagerController teamManagerController;

    /**
     * Construit le service de création avec le contrôleur d'équipe à rafraîchir.
     *
     * @param teamManagerController contrôleur gérant les Bugémons disponibles.
     */
    public BugemonCreationService(TeamManagerController teamManagerController) {
        this.teamManagerController = teamManagerController;
    }

    /**
     * Sauvegarde un Bugémon personnalisé avec projection d'image et mise à jour du fichier JSON.
     *
     * @param name      nom du Bugémon.
     * @param type      type choisi.
     * @param pv        points de vie.
     * @param atk       attaque.
     * @param def       défense.
     * @param ini       initiative.
     * @param moves     attaques sélectionnées.
     * @param spriteURL chemin de l'image source.
     * @throws IOException si l'image ou le fichier JSON ne peut pas être écrit.
     */
    public void saveCustomBugemon(String name, String type, int pv, int atk, int def, int ini,
            List<String> moves, String spriteURL) throws IOException {
        // Le sprite est copié avant l'écriture JSON pour éviter une définition orpheline.
        String spriteName = copySpriteToProject(spriteURL);

        String bugemonId = name.toLowerCase().trim();
        // Les attaques affichées par nom sont converties en identifiants de données.
        List<String> normalizedMoves = moves.stream()
                .map(this::normalizeAttackName)
                .toList();

        updateCustomBugemonsFile(name, bugemonId, type, pv, atk, def, ini, normalizedMoves, spriteName);

        teamManagerController.refreshAvailableBugemons();
    }

    /**
     * Valide les champs minimaux de création d'un Bugémon personnalisé.
     *
     * @param name          nom saisi.
     * @param imageFile     image choisie.
     * @param selectedMoves attaques sélectionnées.
     * @return true si les champs obligatoires sont présents.
     */
    public boolean isBugemonCreationValid(String name, File imageFile, List<String> selectedMoves) {
        return name != null && !name.trim().isEmpty()
                && imageFile != null
                && selectedMoves != null && selectedMoves.size() == 3;
    }

    /**
     * Copie l'image sélectionnée vers le répertoire images du projet.
     */
    private String copySpriteToProject(String spriteURL) throws IOException {
        java.nio.file.Path source = Paths.get(spriteURL);
        String fileName = source.getFileName().toString();
        java.nio.file.Path imagesDir = Paths.get(System.getProperty("user.dir"), "images");
        if (!Files.exists(imagesDir)) {
            // Le dossier peut ne pas exister sur une installation fraîche.
            Files.createDirectories(imagesDir);
        }
        java.nio.file.Path target = imagesDir.resolve(fileName);
        // Remplace l'image si l'utilisateur réimporte volontairement le même fichier.
        Files.copy(source, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        return fileName;
    }

    /**
     * Normalise les noms d'attaques (minuscules, sans accents, underscores).
     */
    private String normalizeAttackName(String attackName) {
        return Normalizer.normalize(attackName, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[\\s-]+", "_");
    }

    /**
     * Lit le fichier JSON existant, ajoute le nouveau Bugémon et réécrit le fichier.
     */
    private void updateCustomBugemonsFile(String name, String id, String type, int pv, int atk,
            int def, int ini, List<String> moves, String spriteName) throws IOException {
        File file = new File("custom.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        Map<String, List<CustomBugemonsData>> wrapper;
        if (file.exists()) {
            // On conserve les Bugémons personnalisés déjà présents.
            try (Reader reader = new FileReader(file)) {
                java.lang.reflect.Type typeToken = new TypeToken<Map<String, List<CustomBugemonsData>>>() {
                }.getType();
                wrapper = gson.fromJson(reader, typeToken);
            }
        } else {
            wrapper = new HashMap<>();
        }

        if (wrapper == null) {
            wrapper = new HashMap<>();
        }
        // Le fichier garde une racine "bugemons" identique aux ressources de base.
        List<CustomBugemonsData> bugemons = wrapper.computeIfAbsent("bugemons", k -> new ArrayList<>());

        var allExistingBugemons = teamManagerController.getAllBugemonsSummaries();

        // Un nom ou identifiant déjà utilisé rendrait le registre ambigu.
        boolean nameExists = allExistingBugemons.stream()
                .anyMatch(b -> b.name().equalsIgnoreCase(name) || b.id().equalsIgnoreCase(id));
        if (nameExists) {
            throw new IOException("Un Bugémon avec ce nom existe déjà dans le jeu.");
        }

        boolean imageExists = allExistingBugemons.stream()
                .anyMatch(b -> b.spritePath().endsWith(spriteName));
        if (imageExists) {
            throw new IOException("Cette image est déjà utilisée par un autre Bugémon dans le jeu.");
        }

        // Objet miroir du JSON attendu par le chargeur générique.
        CustomBugemonsData newData = new CustomBugemonsData();
        newData.id = id;
        newData.nom = name;
        newData.type = type;
        newData.attaques = moves;
        newData.sprite = "../../../../../images/" + spriteName;
        newData.starter = true;

        newData.stats = new CustomBugemonsStats();
        newData.stats.pv = pv;
        newData.stats.attaque = atk;
        newData.stats.defense = def;
        newData.stats.initiative = ini;

        bugemons.add(newData);

        try (Writer writer = new FileWriter(file)) {
            gson.toJson(wrapper, writer);
        }
    }

    // Classes internes correspondant à la structure JSON.
    @SuppressWarnings("all")
    private static class CustomBugemonsStats {
        int pv;
        int attaque;
        int defense;
        int initiative;
    }

    @SuppressWarnings("all")
    private static class CustomBugemonsData {
        String id;
        String nom;
        String type;
        CustomBugemonsStats stats;
        List<String> attaques;
        String sprite;
        Boolean starter;
    }
}

