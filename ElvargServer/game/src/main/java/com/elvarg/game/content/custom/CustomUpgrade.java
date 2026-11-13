package com.elvarg.game.content.custom;

import java.util.concurrent.ThreadLocalRandom;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;

/**
 * ::upgrade - turns three identical gear pieces into the same piece of
 * the next tier. This is also a way into the last tier.
 */
public final class CustomUpgrade {

    private static final int PIECES_NEEDED = 3;
    private static final int SUCCESS_PERCENT = 60;

    private CustomUpgrade() {
    }

    /** Shards needed to upgrade a piece of the given tier. */
    public static int getCost(CustomTier tier) {
        return 25 * tier.number() * tier.number();
    }

    public static void upgrade(Player player) {
        // Find the highest tier piece the player holds three of.
        CustomTier[] tiers = CustomTier.values();
        for (int t = tiers.length - 2; t >= 0; t--) {
            CustomTier tier = tiers[t];
            for (int slot = 0; slot < CustomTier.SLOT_NAMES.length; slot++) {
                int itemId = tier.getItemId(slot);
                if (player.getInventory().getAmount(itemId) >= PIECES_NEEDED) {
                    attempt(player, tier, slot);
                    return;
                }
            }
        }
        player.getPacketSender().sendMessage("Upgraden: neem " + PIECES_NEEDED
                + " dezelfde tier-stukken mee in je inventory, plus Reload scherven.");
        player.getPacketSender().sendMessage("Kans van slagen " + SUCCESS_PERCENT
                + "%. Lukt het niet, dan krijg je 1 stuk terug.");
    }

    private static void attempt(Player player, CustomTier tier, int slot) {
        int cost = getCost(tier);
        if (player.getInventory().getAmount(CustomItems.SHARD) < cost) {
            player.getPacketSender().sendMessage("Je hebt " + cost + " Reload scherven nodig om "
                    + tier.getItemName(slot) + " te upgraden.");
            return;
        }
        CustomTier next = tier.next();
        player.getInventory().delete(CustomItems.SHARD, cost);
        player.getInventory().delete(tier.getItemId(slot), PIECES_NEEDED);

        if (ThreadLocalRandom.current().nextInt(100) < SUCCESS_PERCENT) {
            int result = next.getItemId(slot);
            CustomItems.give(player, new Item(result, 1));
            CustomProgress.logItem(player, result);
            player.getPacketSender().sendMessage("@gre@Gelukt! Je hebt nu: " + next.getItemName(slot) + ".");
        } else {
            CustomItems.give(player, new Item(tier.getItemId(slot), 1));
            player.getPacketSender().sendMessage("@red@De upgrade is mislukt. Je houdt 1 "
                    + tier.getItemName(slot) + " over.");
        }
    }
}
