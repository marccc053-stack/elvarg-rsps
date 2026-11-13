package com.elvarg.game.content.custom;

import java.util.Map;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;

/**
 * Everything the custom content remembers about a player. It is all kept
 * in the player's customKills map, which is saved with the character:
 * positive keys are kills per npc id, the negative keys below are progress.
 */
public final class CustomProgress {

    public static final int KEY_PRESTIGE = -1;
    /** Minute (since 1970) at which the luck potion runs out. */
    public static final int KEY_LUCK_UNTIL = -2;
    public static final int KEY_STARTER_CLAIMED = -3;
    public static final int KEY_RAIDS_COMPLETED = -4;
    public static final int KEY_BOXES_OPENED = -5;
    /** 1 when the player switched the auto collector off. */
    public static final int KEY_COLLECT_OFF = -6;
    /** Collection log entries are stored at -(LOG_OFFSET + item id). */
    private static final int LOG_OFFSET = 100000;

    public static final int MAX_PRESTIGE = 10;
    public static final int PRESTIGE_DROP_RATE = 10;
    public static final int PRESTIGE_DAMAGE = 5;
    /** Kills of the last boss needed to prestige. */
    public static final int PRESTIGE_BOSS_KILLS = 10;
    /** Drop rate % for every tier whose collection log is complete. */
    public static final int LOG_TIER_DROP_RATE = 3;

    private CustomProgress() {
    }

    public static int get(Player player, int key) {
        Integer value = player.getCustomKills().get(key);
        return value == null ? 0 : value;
    }

    public static void set(Player player, int key, int value) {
        player.getCustomKills().put(key, value);
    }

    public static int increment(Player player, int key) {
        int value = get(player, key) + 1;
        set(player, key, value);
        return value;
    }

    // ---- prestige -------------------------------------------------------

    public static int getPrestige(Player player) {
        return get(player, KEY_PRESTIGE);
    }

    /**
     * Raises a player's max hit by their prestige damage bonus.
     */
    public static int boostDamage(Player player, int maxHit) {
        int prestige = getPrestige(player);
        if (prestige <= 0) {
            return maxHit;
        }
        return maxHit + maxHit * prestige * PRESTIGE_DAMAGE / 100;
    }

    /**
     * Attempts to prestige: resets the tier progress in return for a
     * permanent bonus.
     */
    public static void prestige(Player player) {
        int prestige = getPrestige(player);
        if (prestige >= MAX_PRESTIGE) {
            player.getPacketSender().sendMessage("Je hebt het hoogste prestige al bereikt.");
            return;
        }
        CustomTier last = CustomTier.last();
        int bossKills = CustomContent.getKills(player, last.getBossId());
        if (bossKills < PRESTIGE_BOSS_KILLS) {
            player.getPacketSender().sendMessage("Voor prestige heb je " + PRESTIGE_BOSS_KILLS + " kills op "
                    + last.getBossName() + " nodig. Je hebt er " + bossKills + ".");
            return;
        }
        // Reset the kill counts (positive keys) so the tiers lock again.
        Map<Integer, Integer> map = player.getCustomKills();
        map.keySet().removeIf(key -> key > 0);
        set(player, KEY_PRESTIGE, prestige + 1);
        player.getPacketSender().sendMessage("@red@Prestige " + (prestige + 1) + "! Permanent +"
                + (prestige + 1) * PRESTIGE_DROP_RATE + "% drop rate en +" + (prestige + 1) * PRESTIGE_DAMAGE
                + "% schade. De tiers zijn weer op slot, je uitrusting houd je.");
    }

    // ---- luck potion ----------------------------------------------------

    private static int nowMinutes() {
        return (int) (System.currentTimeMillis() / 60000L);
    }

    public static int getLuckMinutesLeft(Player player) {
        return Math.max(0, get(player, KEY_LUCK_UNTIL) - nowMinutes());
    }

    public static void addLuckMinutes(Player player, int minutes) {
        set(player, KEY_LUCK_UNTIL, nowMinutes() + getLuckMinutesLeft(player) + minutes);
    }

    // ---- collection log -------------------------------------------------

    public static boolean hasLogged(Player player, int itemId) {
        return get(player, -(LOG_OFFSET + itemId)) > 0;
    }

    /**
     * Records that the player obtained a custom item. Completing a tier's
     * set for the first time gives a reward.
     */
    public static void logItem(Player player, int itemId) {
        if (hasLogged(player, itemId)) {
            return;
        }
        set(player, -(LOG_OFFSET + itemId), 1);
        player.getPacketSender().sendMessage("@blu@Nieuw in je collectie! Bekijk hem met ::log.");

        for (CustomTier tier : CustomTier.values()) {
            for (int slot = 0; slot < CustomTier.SLOT_NAMES.length; slot++) {
                if (tier.getItemId(slot) == itemId && hasCompleted(player, tier)) {
                    CustomItems.give(player, new Item(CustomItems.MYSTERY_BOX, tier.number()));
                    player.getPacketSender().sendMessage("@gre@Collectie van tier " + tier.number() + " ("
                            + tier.getDisplayName() + ") compleet! Beloning: " + tier.number()
                            + " mysteriekist(en) en permanent +" + LOG_TIER_DROP_RATE + "% drop rate.");
                    return;
                }
            }
        }
    }

    public static int countLogged(Player player, CustomTier tier) {
        int count = 0;
        for (int slot = 0; slot < CustomTier.SLOT_NAMES.length; slot++) {
            if (hasLogged(player, tier.getItemId(slot))) {
                count++;
            }
        }
        return count;
    }

    public static boolean hasCompleted(Player player, CustomTier tier) {
        return countLogged(player, tier) == CustomTier.SLOT_NAMES.length;
    }

    public static int countCompletedTiers(Player player) {
        int count = 0;
        for (CustomTier tier : CustomTier.values()) {
            if (hasCompleted(player, tier)) {
                count++;
            }
        }
        return count;
    }
}
