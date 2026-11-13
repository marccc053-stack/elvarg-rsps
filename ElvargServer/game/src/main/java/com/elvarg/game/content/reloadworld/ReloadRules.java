package com.elvarg.game.content.reloadworld;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.entity.impl.playerbot.PlayerBot;
import com.elvarg.game.model.EquipmentType;
import com.elvarg.game.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * The "only custom gear" rule of RELOAD. It is OFF for now: the custom tiers only have melee gear,
 * so switching it on today would leave no bows, staffs, boots, gloves, capes or rings in the game
 * and would empty most of the shops.
 *
 * While {@link #STANDARD_GEAR_DISABLED} is on, the weapons and armour of normal RuneScape
 * cannot be obtained or used: they are taken out of monster drops and shop stock, presets
 * do not load, and they cannot be equipped. Ammunition (arrows, bolts) is left alone until
 * custom ammunition exists, otherwise bows would be useless. Skilling tools stay as well.
 */
public final class ReloadRules {

    /** Set to true to make normal RuneScape weapons and armour unusable and unobtainable. */
    public static final boolean STANDARD_GEAR_DISABLED = false;

    /** Custom items start here, see CustomTier.ITEM_BASE. */
    private static final int FIRST_CUSTOM_ITEM = 30000;

    public static boolean isStandardGear(int itemId) {
        if (!STANDARD_GEAR_DISABLED || itemId >= FIRST_CUSTOM_ITEM) {
            return false;
        }
        ItemDefinition def = ItemDefinition.forId(itemId);
        if (def.isNoted()) {
            def = ItemDefinition.forId(def.unNote());
        }
        EquipmentType type = def.getEquipmentType();
        return type != null && type != EquipmentType.NONE && type != EquipmentType.ARROWS && !isTool(def.getName());
    }

    /** Skilling tools stay: without axes, pickaxes and harpoons the skills would stop working. */
    private static boolean isTool(String name) {
        if (name == null) {
            return false;
        }
        String lower = name.toLowerCase();
        return lower.contains("pickaxe") || lower.endsWith(" axe") || lower.contains("harpoon");
    }

    /** Removes standard gear from a list of drops. */
    public static List<Item> filterDrops(List<Item> drops) {
        if (STANDARD_GEAR_DISABLED) {
            drops.removeIf(item -> isStandardGear(item.getId()));
        }
        return drops;
    }

    /** Removes standard gear from the stock of a shop. */
    public static Item[] filterStock(Item[] stock) {
        if (!STANDARD_GEAR_DISABLED || stock == null) {
            return stock;
        }
        List<Item> kept = new ArrayList<>();
        for (Item item : stock) {
            if (item != null && !isStandardGear(item.getId())) {
                kept.add(item);
            }
        }
        return kept.toArray(new Item[0]);
    }

    /**
     * Presets hand out standard gear, so they are switched off for real players while the rule is on.
     *
     * @return true if the preset must not be loaded.
     */
    public static boolean blocksPreset(Player player) {
        if (!STANDARD_GEAR_DISABLED || player instanceof PlayerBot) {
            return false;
        }
        player.getPacketSender().sendMessage(player.isNewPlayer()
                ? "Welkom bij RELOAD. Typ ::starter voor je startpakket."
                : "Presets staan uit: RELOAD gebruikt alleen eigen uitrusting.");
        return true;
    }

    private ReloadRules() {
    }
}
