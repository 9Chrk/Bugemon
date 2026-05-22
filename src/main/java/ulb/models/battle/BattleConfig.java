package ulb.models.battle;

/**
 * Centralise les constantes utilisées par le module de combat.
 */
final class BattleConfig {

    // Messages retournés directement au joueur.
    // Utilisés par BattleService, SwitchAction, SurrenderAction et BattleKOHandler.
    static final String BATTLE_ALREADY_FINISHED_MESSAGE = "Le combat est déjà terminé.";
    static final String ITEM_NOT_OWNED_MESSAGE = "Vous ne possedez pas cet objet.";
    static final String ACTIVE_BUGEMON_ALREADY_FULL_HP_MESSAGE = "Le Bugemon actif a deja tous ses PV.";
    static final String TEAM_ALREADY_FULL_HP_MESSAGE = "L'équipe du joueur a déjà tous ses PV.";
    static final String ITEM_HAS_NO_USABLE_EFFECT_MESSAGE = "Cet objet n'a aucun effet utilisable maintenant.";
    static final String IMPOSSIBLE_SWITCH_MESSAGE = "Impossible de changer de Bugemon.";
    static final String SURRENDER_MESSAGE = "Combat abandonné.";
    static final String VICTORY_MESSAGE = "VICTOIRE !\n";
    static final String DEFEAT_MESSAGE = "DÉFAITE !\n";

    // Fragments utilisés pour construire les journaux de combat affichés dans la vue.
    // Utilisés par BattleAttackResolver, BattleEffectService, BattleKOHandler,
    // SwitchAction et ExperienceService.
    static final String USES_ATTACK_LOG = " utilise ";
    static final String ATTACK_SEPARATOR_LOG = " ! ";
    static final String LOSES_HP_LOG = " perd ";
    static final String HP_LOG = " PV.\n";
    static final String KO_LOG = " est KO.\n";
    static final String USES_ITEM_LOG = " utilise ";
    static final String ITEM_LOG_END = ".\n";
    static final String RECOVERS_HP_LOG = " récupère ";
    static final String TEAM_OF_LOG = "L'équipe de ";
    static final String NEXT_TURN_EFFECT_LOG = " subira un effet temporaire au prochain tour.\n";
    static final String STAT_GAIN_LOG = " gagne ";
    static final String STAT_LOSS_LOG = " perd ";
    static final String STAT_NAME_PREFIX_LOG = " en ";
    static final String PERMANENT_EFFECT_END_LOG = " jusqu'à la fin du combat.\n";
    static final String RESET_MALUS_LOG = " annule ses malus temporaires.\n";
    static final String ENTERS_BATTLE_LOG = " entre dans le combat.";
    static final String ENTERS_BATTLE_LINE_LOG = " entre dans le combat.\n";
    static final String LINE_BREAK = "\n";
    static final String BONUS_PREFIX = "+";
    static final String BONUS_SEPARATOR = ", ";
    static final String HEALTH_BONUS_LABEL = " HP max";
    static final String ATTACK_BONUS_LABEL = " Attaque";
    static final String DEFENSE_BONUS_LABEL = " Défense";
    static final String INITIATIVE_BONUS_LABEL = " Initiative";

    // Messages d'erreur utilisés par les validations des services et actions de combat.
    // Utilisés par BattleService, AttackAction, UseItemAction, SwitchAction,
    // SurrenderAction, Battle, BattleSelectionService, EnemyTeamFactory,
    // DamageCalculator et ExperienceService.
    static final String ATTACKS_MAP_NULL_ERROR = "Attacks map cannot be null.";
    static final String ITEMS_MAP_NULL_ERROR = "Items map cannot be null.";
    static final String RANDOM_NULL_ERROR = "Random cannot be null.";
    static final String DAMAGE_CALCULATOR_NULL_ERROR = "DamageCalculator cannot be null.";
    static final String ATTACK_ID_NULL_OR_EMPTY_ERROR = "Attack ID cannot be null or empty.";
    static final String ITEM_ID_NULL_OR_EMPTY_ERROR = "Item ID cannot be null or empty.";
    static final String INVENTORY_NULL_ERROR = "Inventory cannot be null.";
    static final String BATTLE_NULL_ERROR = "Battle cannot be null.";
    static final String BATTLE_SERVICE_NULL_ERROR = "Battle And Battle Service cannot be Null.";
    static final String BATTLE_AND_SERVICE_NULL_ERROR = "Battle and BattleService cannot be null.";
    static final String BUGEMON_NULL_ERROR = "Bugemon Cannot be Null.";
    static final String UNKNOWN_ATTACK_ERROR_PREFIX = "Unknown attack: ";
    static final String UNKNOWN_ITEM_ERROR_PREFIX = "Unknown item: ";
    static final String ACTIVE_BUGEMON_DOES_NOT_KNOW_ATTACK_PREFIX = "The active Bugemon does not know this attack: ";
    static final String PLAYER_TEAM_AND_DEFINITIONS_ERROR = "Player team and definitions must be provided";
    static final String DAMAGE_INPUT_NULL_ERROR = "The attacker or the defender or the attack cannot be null";
    static final String EXPERIENCE_INPUT_ERROR = "Floor number and enemy count must be positive.";
    static final String EXPERIENCE_DISTRIBUTION_ERROR = "Player team and total XP must be valid.";
    static final String BUGEMON_HAS_NO_ATTACKS_ERROR = "Bugemon has no attacks.";
    static final String UNKNOWN_ATTACK_ID_ERROR_PREFIX = "Unknown attack id: ";
    static final String TEAM_HAS_NO_AVAILABLE_BUGEMON_ERROR = "Team has no available Bugemon.";
    static final String TEAMS_NULL_ERROR = "Teams cannot be null.";
    static final String TEAMS_EMPTY_ERROR = "Teams must not be empty.";
    static final String TEAMS_SIZE_MISMATCH_ERROR = "Teams must have the same size.";

    // Valeurs textuelles venant des fichiers JSON d'attaques/objets pour identifier les effets.
    // Utilisées par BattleEffectService et BattleItemService.
    static final String EFFECT_TARGET_CASTER = "lanceur";
    static final String EFFECT_TARGET_TEAM = "equipe";
    static final String EFFECT_TARGET_OPPONENT = "adversaire";
    static final String EFFECT_DURATION_ONE_TURN = "1_tour";
    static final String EFFECT_DURATION_PERMANENT = "permanent";

    // Noms de statistiques attendus dans les effets de type stat_modifier.
    // Utilisés par BattleEffectService.
    static final String STAT_ATTACK = "attaque";
    static final String STAT_DEFENSE = "defense";
    static final String STAT_INITIATIVE = "initiative";
    static final String STAT_HEALTH = "pv";

    // Constantes de formule utilisées par DamageCalculator.
    static final double STAT_SCALE_BASE = 100.0;
    static final double CRITICAL_HIT_CHANCE = 0.10;
    static final double CRITICAL_HIT_MULTIPLIER = 1.5;
    static final double SUPER_EFFECTIVE_MULTIPLIER = 1.5;
    static final double NOT_VERY_EFFECTIVE_MULTIPLIER = 0.75;
    static final double NEUTRAL_TYPE_MULTIPLIER = 1.0;
    static final int MINIMUM_DAMAGE = 1;

    // Messages liés au multiplicateur de type calculé dans DamageCalculator.
    static final String SUPER_EFFECTIVE_MESSAGE = "C'est super efficace !";
    static final String NOT_VERY_EFFECTIVE_MESSAGE = "Ce n'est pas très efficace...";

    // Constantes de calcul d'expérience et de génération des bonus de montée de niveau.
    // Utilisées par ExperienceService.
    static final int XP_BASE_REWARD = 30;
    static final int BOSS_XP_MULTIPLIER = 2;
    static final int NORMAL_XP_MULTIPLIER = 1;
    static final int LEVEL_UP_BONUS_POINTS = 10;
    static final int LEVEL_UP_CHOICES_COUNT = 3;
    static final int BONUS_STAT_COUNT = 4;
    static final int HEALTH_BONUS_MULTIPLIER = 2;
    static final int INITIATIVE_BONUS_MULTIPLIER = 2;
    static final int MINIMUM_COMBINATION_STATS = 2;

    private BattleConfig() {
    }
}
