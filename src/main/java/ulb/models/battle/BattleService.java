package ulb.models.battle;

import ulb.models.data.Attack;
import ulb.models.data.ItemDefinition;
import ulb.models.game.BugemonInstance;
import ulb.models.game.Inventory;
import ulb.models.game.Team;

import java.util.Map;
import java.util.Random;


/**
 * Implémente les règles centrales du combat et la résolution des tours.
 */
public class BattleService {
    // Données
    private final Map<String, Attack> attacksById;
    private final Map<String, ItemDefinition> itemsById;
    private final DamageCalculator damageCalculator;
    private final BattleSelectionService selectionService;
    private final BattleItemService itemService;
    private final BattleAttackResolver attackResolver;

    // Valeurs exposées pour l'affichage des dégâts dans BattleView.

    private int damagePlayerTaken;
    private int damageEnemyTaken;

    // ----------------- Constructeurs -----------------

    /**
     * Construit le service avec les attaques et des dépendances par défaut.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     */
    public BattleService(Map<String, Attack> attacksById) {
        this(attacksById, Map.of(), new Random(), new DamageCalculator());
    }

    // Permet d'injecter un Random seedé pour des tests déterministes.

    /**
     * Construit le service avec un générateur aléatoire fourni.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     * @param random source aléatoire.
     */
    public BattleService(Map<String, Attack> attacksById, Random random) {
        this(attacksById, Map.of(), random, new DamageCalculator());
    }

    /**
     * Construit le service avec attaques et objets, puis dépendances par défaut.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     * @param itemsById objets disponibles indexés par identifiant.
     */
    public BattleService(Map<String, Attack> attacksById, Map<String, ItemDefinition> itemsById) {
        this(attacksById, itemsById, new Random(), new DamageCalculator());
    }

    /**
     * Construit le service avec attaques, objets et source aléatoire fournis.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     * @param itemsById objets disponibles indexés par identifiant.
     * @param random source aléatoire.
     */
    public BattleService(Map<String, Attack> attacksById,
            Map<String, ItemDefinition> itemsById,
            Random random) {
        this(attacksById, itemsById, random, new DamageCalculator());
    }

    /**
     * Construit le service avec toutes les dépendances injectées.
     *
     * @param attacksById attaques disponibles indexées par identifiant.
     * @param itemsById objets disponibles indexés par identifiant.
     * @param random source aléatoire.
     * @param damageCalculator calculateur de dégâts.
     */
    public BattleService(Map<String, Attack> attacksById,
            Map<String, ItemDefinition> itemsById,
            Random random,
            DamageCalculator damageCalculator) {
        if (attacksById == null) {
            throw new IllegalArgumentException(BattleConfig.ATTACKS_MAP_NULL_ERROR);
        }
        if (itemsById == null) {
            throw new IllegalArgumentException(BattleConfig.ITEMS_MAP_NULL_ERROR);
        }
        if (random == null) {
            throw new IllegalArgumentException(BattleConfig.RANDOM_NULL_ERROR);
        }
        if (damageCalculator == null) {
            throw new IllegalArgumentException(BattleConfig.DAMAGE_CALCULATOR_NULL_ERROR);
        }
        // Copies immuables : le combat ne dépend pas des registres modifiés ailleurs.
        this.attacksById = Map.copyOf(attacksById);
        this.itemsById = Map.copyOf(itemsById);
        this.damageCalculator = damageCalculator;
        this.selectionService = new BattleSelectionService(this.attacksById, random);
        BattleEffectService effectService = new BattleEffectService();
        this.itemService = new BattleItemService(
                effectService,
                BattleConfig.ITEM_HAS_NO_USABLE_EFFECT_MESSAGE,
                BattleConfig.ACTIVE_BUGEMON_ALREADY_FULL_HP_MESSAGE,
                BattleConfig.TEAM_ALREADY_FULL_HP_MESSAGE);
        BattleKOHandler koHandler = new BattleKOHandler(this.selectionService);
        this.attackResolver = new BattleAttackResolver(damageCalculator, effectService, koHandler);
    }

    // ----------------- API publique -----------------



    /**
     * Exécute une attaque du joueur.
     *
     * @param battle combat courant.
     * @param attackId identifiant de l'attaque.
     * @return message de combat décrivant l'action.
     */
    public String playerAttack(Battle battle, String attackId) {
        validateBattle(battle);

        if (attackId == null || attackId.isEmpty()) {
            throw new IllegalArgumentException(BattleConfig.ATTACK_ID_NULL_OR_EMPTY_ERROR);
        }
        if (battle.isFinished()) {
            return BattleConfig.BATTLE_ALREADY_FINISHED_MESSAGE;
        }

        BugemonInstance attacker = battle.getPlayerActive();
        BugemonInstance defender = battle.getEnemyActive();

        if (!attacker.getLearnedAttackIds().contains(attackId)) {
            throw new IllegalArgumentException(
                    BattleConfig.ACTIVE_BUGEMON_DOES_NOT_KNOW_ATTACK_PREFIX + attackId);
        }

        Attack attack = attacksById.get(attackId);
        if (attack == null) {
            throw new IllegalArgumentException(BattleConfig.UNKNOWN_ATTACK_ERROR_PREFIX + attackId);
        }

        return applyAttack(battle, attacker, defender, attack, true);
    }

    /**
     * Tente de changer le Bugémon actif du joueur.
     *
     * @param battle combat courant.
     * @param newActive Bugémon ciblé pour le changement.
     * @return true si le changement a réussi.
     */
    public boolean switchPlayerBugemon(Battle battle, BugemonInstance newActive) {
        if (battle == null || newActive == null) {
            return false;
        }

        Team playerTeam = battle.getPlayerTeam();
        // On récupère l'instance interne au combat, pas l'objet reçu depuis la vue.
        BugemonInstance teamMember = playerTeam.getBugemonById(newActive.getId());

        if (teamMember == null) {
            return false;
        }
        if (teamMember.isKo()) {
            return false;
        }
        if (teamMember == battle.getPlayerActive()) {
            return false;
        }

        battle.setPlayerActive(teamMember);
        return true;
    }

    /**
     * Fait abandonner le joueur et termine immédiatement le combat.
     *
     * @param battle combat courant.
     */
    public void surrender(Battle battle) {
        validateBattle(battle);
        battle.endBattle();
        battle.getPlayerTeam().resetCombatStatModifiers();
        battle.getEnemyTeam().resetCombatStatModifiers();
    }

    /**
     * Débute un tour et active les bonus temporaires en attente.
     *
     * @param battle combat courant.
     */
    public void startTurn(Battle battle) {
        validateBattle(battle);
        battle.getPlayerActive().activateQueuedTurnBonus();
        battle.getEnemyActive().activateQueuedTurnBonus();
    }

    /**
     * Termine un tour et nettoie les bonus temporaires d'un tour.
     *
     * @param battle combat courant.
     */
    public void endTurn(Battle battle) {
        validateBattle(battle);
        battle.getPlayerTeam().clearActiveTurnBonuses();
        battle.getEnemyTeam().clearActiveTurnBonuses();

        if (battle.isFinished()) {
            // Les bonus temporaires ne doivent jamais survivre à la fin du combat.
            battle.getPlayerTeam().resetCombatStatModifiers();
            battle.getEnemyTeam().resetCombatStatModifiers();
        }
    }

    /**
     * Utilise un objet depuis l'inventaire du joueur.
     *
     * @param battle combat courant.
     * @param inventory inventaire du joueur.
     * @param itemId identifiant de l'objet.
     * @return message décrivant l'effet appliqué.
     */
    public String useItem(Battle battle, Inventory inventory, String itemId) {
        validateBattle(battle);
        if (inventory == null) {
            throw new IllegalArgumentException(BattleConfig.INVENTORY_NULL_ERROR);
        }
        if (itemId == null || itemId.isBlank()) {
            throw new IllegalArgumentException(BattleConfig.ITEM_ID_NULL_OR_EMPTY_ERROR);
        }
        if (battle.isFinished()) {
            return BattleConfig.BATTLE_ALREADY_FINISHED_MESSAGE;
        }
        if (inventory.hasItem(itemId)) {
            return BattleConfig.ITEM_NOT_OWNED_MESSAGE;
        }

        ItemDefinition item = itemsById.get(itemId);
        if (item == null) {
            throw new IllegalArgumentException(BattleConfig.UNKNOWN_ITEM_ERROR_PREFIX + itemId);
        }

        String validationMessage = itemService.validateItemUsage(battle, item);
        if (validationMessage != null) {
            return validationMessage;
        }

        // On applique d'abord l'effet pour éviter de consommer l'objet si l'effet échoue.
        String effectLog = itemService.applyItemEffect(
                battle,
                battle.getPlayerActive(),
                battle.getEnemyActive(),
                item);

        if (effectLog == null) {
            return BattleConfig.ITEM_HAS_NO_USABLE_EFFECT_MESSAGE;
        }

        inventory.useItem(itemId);
        return effectLog;
    }

    // ----------------- Traitements internes de tour -----------------

    /**
     * Exécute une attaque automatique de l'adversaire.
     *
     * @param battle combat courant.
     * @return message décrivant l'attaque ennemie.
     */
    public String enemyAutoAttack(Battle battle) {
        BugemonInstance attacker = battle.getEnemyActive();
        BugemonInstance defender = battle.getPlayerActive();
        Attack attack = selectionService.getRandomAttack(attacker);
        return applyAttack(battle, attacker, defender, attack, false);
    }

    /**
     * Applique une attaque et construit le journal de combat associé.
     *
     * @param battle combat courant.
     * @param attacker Bugémon attaquant.
     * @param defender Bugémon défenseur.
     * @param attack attaque utilisée.
     * @param attackerIsPlayer true si l'attaquant appartient à l'équipe du joueur.
     * @return message de combat détaillé.
     */
    private String applyAttack(Battle battle,
            BugemonInstance attacker,
            BugemonInstance defender,
            Attack attack,
            boolean attackerIsPlayer) {

        BattleAttackResolver.AttackResolutionResult result =
                attackResolver.resolveAttack(battle, attacker, defender, attack, attackerIsPlayer);

        // Une seule zone clignote à la fois dans la vue de combat.
        if (result.wasAttackerPlayer()) {
            damageEnemyTaken = result.damageInflicted();
            damagePlayerTaken = 0;
        } else {
            damagePlayerTaken = result.damageInflicted();
            damageEnemyTaken = 0;
        }

        return result.combatLog();
    }

    /**
     * Valide qu'un combat est bien disponible.
     *
     * @param battle combat à vérifier.
     */
    private void validateBattle(Battle battle) {
        if (battle == null) {
            throw new IllegalArgumentException(BattleConfig.BATTLE_NULL_ERROR);
        }
    }


    /**
     * Retourne les attaques disponibles.
     *
     * @return map immuable des attaques indexées par identifiant.
     */
    public Map<String, Attack> getAttacksById() {
        return attacksById;
    }

    /**
     * Retourne les objets disponibles.
     *
     * @return map immuable des objets indexés par identifiant.
     */
    public Map<String, ItemDefinition> getItemsById() {
        return itemsById;
    }

    /**
     * Retourne les dégâts reçus récemment par le joueur.
     *
     * @return valeur de dégâts côté joueur.
     */
    public int getDamageTakenPlayer() {
        return damagePlayerTaken;
    }

    /**
     * Retourne les dégâts reçus récemment par l'ennemi.
     *
     * @return valeur de dégâts côté ennemi.
     */
    public int getDamageTakenEnemy() {
        return damageEnemyTaken;
    }

    /**
     * Retourne le calculateur de dégâts utilisé par le service.
     *
     * @return calculateur de dégâts.
     */
    public DamageCalculator getDamageCalculator() {
        return damageCalculator;
    }
}
