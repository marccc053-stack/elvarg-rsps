package com.elvarg.game.content.custom;

import java.util.concurrent.ThreadLocalRandom;

import com.elvarg.game.content.PetHandler;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;

/**
 * Kill counting and loot for the custom tier npcs.
 */
public final class CustomDrops {

    private static final int COINS = 995;
    private static final int BIG_BONES = 532;

    /** Base chance (1 in x) per gear piece. */
    private static final int MOB_GEAR_CHANCE = 120;
    private static final int BOSS_GEAR_CHANCE = 12;
    private static final int MOB_AMULET_CHANCE = 400;
    private static final int BOSS_AMULET_CHANCE = 30;
    private static final int MOB_BOX_CHANCE = 60;
    private static final int BOSS_BOX_CHANCE = 5;
    private static final int BOSS_PET_CHANCE = 150;

    private CustomDrops() {
    }

    /**
     * Handles the death of an npc for its killer.
     *
     * @return true if the npc was a custom npc and its loot has been handled.
     */
    public static boolean handleKill(Player player, NPC npc) {
        // Raid npcs give no loot of their own: the raid pays out at the end.
        if (CustomContent.tierForRaidNpc(npc.getId()) != null) {
            return true;
        }
        CustomTier tier = CustomContent.tierForNpc(npc.getId());
        // An instance mob counts and pays out as the normal mob of its tier.
        boolean instance = false;
        if (tier == null) {
            tier = CustomContent.tierForInstanceNpc(npc.getId());
            instance = tier != null;
        }
        if (tier == null) {
            return false;
        }
        boolean boss = CustomContent.isBoss(npc.getId());

        // Kill count
        int countedId = instance ? tier.getMobId() : npc.getId();
        CustomTier next = tier.next();
        boolean nextWasLocked = next != null && !CustomContent.hasUnlocked(player, next);
        int before = CustomContent.getKills(player, countedId);
        int kills = before + CustomItems.getKillMultiplier(player);
        player.getCustomKills().put(countedId, kills);
        int step = instance ? 100 : 10;
        if (boss || kills / step != before / step) {
            player.getPacketSender().sendMessage(
                    "Je hebt nu " + kills + " kills op: " + npc.getDefinition().getName() + ".");
        }
        if (nextWasLocked && CustomContent.hasUnlocked(player, next)) {
            player.getPacketSender().sendMessage("@gre@Tier " + next.number() + " (" + next.getDisplayName()
                    + ") is ontgrendeld! Gebruik ::t" + next.number() + " om erheen te gaan.");
        }

        // Loot that always drops
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int coinBase = 2500 * tier.number() * tier.number() * (boss ? 20 : 1);
        drop(player, npc, new Item(COINS, coinBase + random.nextInt(coinBase)));
        if (!instance) {
            drop(player, npc, new Item(BIG_BONES, 1));
        }
        int shards = tier.number() * (boss ? 25 : 2);
        drop(player, npc, new Item(CustomItems.SHARD, shards + random.nextInt(shards + 1)));

        double multiplier = 1.0 + CustomContent.getDropRateBonus(player) / 100.0;

        // Gear, one independent roll per piece
        for (int slot = 0; slot < CustomTier.SLOT_NAMES.length; slot++) {
            int oneIn;
            if (slot == CustomTier.SLOT_AMULET) {
                oneIn = boss ? BOSS_AMULET_CHANCE : MOB_AMULET_CHANCE;
            } else {
                oneIn = boss ? BOSS_GEAR_CHANCE : MOB_GEAR_CHANCE;
            }
            if (roll(random, oneIn, multiplier)) {
                drop(player, npc, new Item(tier.getItemId(slot), 1));
                CustomProgress.logItem(player, tier.getItemId(slot));
                player.getPacketSender().sendMessage(
                        "@red@Zeldzame drop: " + tier.getItemName(slot) + " (1 op " + oneIn + ")!");
            }
        }

        // Mystery box
        if (roll(random, boss ? BOSS_BOX_CHANCE : MOB_BOX_CHANCE, multiplier)) {
            drop(player, npc, new Item(CustomItems.MYSTERY_BOX, 1));
            player.getPacketSender().sendMessage("@blu@Er valt een mysteriekist!");
        }

        // Pets, from bosses only: two tiers share one pet.
        if (boss && roll(random, BOSS_PET_CHANCE, multiplier)) {
            int petIndex = Math.min(tier.ordinal() / 2, CustomItems.PET_ITEMS.length - 1);
            int petItem = CustomItems.PET_ITEMS[petIndex];
            CustomProgress.logItem(player, petItem);
            player.getPacketSender().sendMessage(
                    "@red@Huisdier drop: " + CustomItems.PET_NAMES[petIndex] + " (1 op " + BOSS_PET_CHANCE + ")!");
            PetHandler.drop(player, petItem, true);
        }
        return true;
    }

    private static boolean roll(ThreadLocalRandom random, int oneIn, double multiplier) {
        return random.nextDouble() < Math.min(1.0, multiplier / oneIn);
    }

    private static void drop(Player player, NPC npc, Item item) {
        if (CustomItems.collect(player, item)) {
            return;
        }
        ItemOnGroundManager.register(player, item, npc.getLocation());
    }
}
