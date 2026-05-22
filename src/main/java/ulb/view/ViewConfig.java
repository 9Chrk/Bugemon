package ulb.view;

/**
 * Centralise les chemins des ressources graphiques et CSS utilisées par les vues.
 */
public final class ViewConfig {
    public static final String BUGEMON_SPRITE_PATH = "/assets/bugemons/png/";
    public static final String CSS_STYLE_PATH = "/css/style.css";
    public static final String ATTACK_ICON_BUTTON_VIEW = "/images/fight-icons/attack-icon.png";
    public static final String ITEM_ICON_BUTTON_VIEW = "/images/fight-icons/item-icon.png";
    public static final String SWITCH_ICON_BUTTON_VIEW = "/images/fight-icons/switch-icon.png";
    public static final String SURRENDER_ICON_BUTTON_VIEW = "/images/fight-icons/surrender-icon.png";
    public static final String BACK_ICON_BUTTON_VIEW = "/images/fight-icons/back-icon.png";

    public static final String COMBAT_ICON_FLOOR_VIEW = "/images/floormap/combat.png";
    public static final String REWARD_ICON_FLOOR_VIEW = "/images/floormap/reward.png";
    public static final String BOSS_ICON_FLOOR_VIEW = "/images/floormap/boss.png";

    public static final String FLOOR_BG_PATH_FORMAT = "/images/floors/floor%d_bg.png";

    /**
     * Empêche l'instanciation de cette classe de constantes.
     */
    private ViewConfig() {
    }
}
