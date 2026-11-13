package com.elvarg.game.content.reloadworld;

import com.elvarg.game.World;
import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomItems;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.entity.impl.grounditem.ItemOnGroundManager;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;

import java.util.concurrent.ThreadLocalRandom;

/**
 * The custom npcs of the RELOAD island: three world bosses in the volcano arenas and the
 * sparring npcs of the training grounds. They sit next to the tier system (CustomTier) and
 * use the same building blocks: NpcDefinition.customCopy, shards, mystery boxes, kill counts.
 *
 * The client has the matching names and colours in CustomDefinitions (ids 15120 to 15132).
 */
public final class ReloadWorldContent {

    public static final int SPARRING_GIANT = 15120, SPARRING_BRUTE = 15121;
    public static final int VORSTKLAUW = 15130, WEBKONINGIN = 15131, MAGMAROTH = 15132;

    private static final int COINS = 995, BONES = 526, BIG_BONES = 532;

    /** A world boss drops a mystery box 1 in this many kills, before drop rate bonuses. */
    private static final int BOSS_BOX_CHANCE = 4;

    /** Called from CustomContent.registerNpcs(). */
    public static void registerNpcs() {
        // Sparring npcs: lots of health, almost no damage, quick respawn.
        int[] weak = {1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        int[] medium = {30, 30, 30, 1, 1, 0, 0, 0, 0, 0, 20, 20, 20, 20, 20, 0, 0, 0};
        put(NpcDefinition.customCopy(NpcDefinition.forId(2098), SPARRING_GIANT, "Sparringreus", 10, 600, 1, 5, false, weak));
        put(NpcDefinition.customCopy(NpcDefinition.forId(891), SPARRING_BRUTE, "Sparringbruut", 60, 2500, 4, 5, false, medium));

        // World bosses: as strong as the tier 2, 3 and 4 bosses, but each with its own fight.
        put(NpcDefinition.customCopy(NpcDefinition.forId(6609), VORSTKLAUW, "Vorstklauw", 400, 4000, 26, 60, true,
                CustomTier.VORST.getNpcStats(true)));
        put(NpcDefinition.customCopy(NpcDefinition.forId(6504), WEBKONINGIN, "Webkoningin", 600, 8000, 33, 60, true,
                CustomTier.GIF.getNpcStats(true)));
        put(NpcDefinition.customCopy(NpcDefinition.forId(239), MAGMAROTH, "Magmaroth", 800, 15000, 42, 60, true,
                CustomTier.SCHADUW.getNpcStats(true)));
    }

    private static void put(NpcDefinition def) {
        NpcDefinition.definitions.put(def.getId(), def);
    }

    /** Called once from CustomContent, after the definitions exist. */
    public static void spawnNpcs() {
        spawn(VORSTKLAUW, ReloadWorld.VORSTKLAUW_SPAWN, 3);
        spawn(WEBKONINGIN, ReloadWorld.WEBKONINGIN_SPAWN, 3);
        spawn(MAGMAROTH, ReloadWorld.MAGMAROTH_SPAWN, 2);
        for (int[] tile : ReloadWorld.TRAINING_GIANTS) {
            spawn(SPARRING_GIANT, new Location(tile[0], tile[1], 0), 2);
        }
        for (int[] tile : ReloadWorld.TRAINING_BRUTES) {
            spawn(SPARRING_BRUTE, new Location(tile[0], tile[1], 0), 2);
        }
    }

    private static void spawn(int id, Location location, int radius) {
        NPC npc = NPC.create(id, location);
        npc.getMovementCoordinator().setRadius(radius);
        World.getAddNPCQueue().add(npc);
    }

    public static boolean isWorldBoss(int npcId) {
        return npcId == VORSTKLAUW || npcId == WEBKONINGIN || npcId == MAGMAROTH;
    }

    /**
     * Loot and kill count, called from CustomDrops.handleKill().
     *
     * @return true if the npc was one of ours.
     */
    public static boolean handleKill(Player player, NPC npc) {
        int id = npc.getId();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (id == SPARRING_GIANT || id == SPARRING_BRUTE) {
            boolean brute = id == SPARRING_BRUTE;
            drop(player, npc, new Item(brute ? BIG_BONES : BONES, 1));
            drop(player, npc, new Item(COINS, (brute ? 1500 : 250) + random.nextInt(brute ? 1500 : 250)));
            if (brute && random.nextInt(4) == 0) {
                drop(player, npc, new Item(CustomItems.SHARD, 1));
            }
            return true;
        }
        if (!isWorldBoss(id)) {
            return false;
        }
        // 1, 2 or 3: how far up the ladder this boss sits.
        int rank = id - VORSTKLAUW + 1;

        int kills = CustomContent.getKills(player, id) + 1;
        player.getCustomKills().put(id, kills);
        player.getPacketSender().sendMessage("Je hebt nu " + kills + " kills op: " + npc.getDefinition().getName() + ".");

        int coins = 150_000 * rank * rank;
        drop(player, npc, new Item(COINS, coins + random.nextInt(coins)));
        drop(player, npc, new Item(BIG_BONES, 1));
        int shards = 40 * rank;
        drop(player, npc, new Item(CustomItems.SHARD, shards + random.nextInt(shards + 1)));

        double multiplier = 1.0 + CustomContent.getDropRateBonus(player) / 100.0;
        if (random.nextDouble() < Math.min(1.0, multiplier / BOSS_BOX_CHANCE)) {
            drop(player, npc, new Item(CustomItems.MYSTERY_BOX, 1));
            player.getPacketSender().sendMessage("@blu@Er valt een mysteriekist!");
        }
        return true;
    }

    private static void drop(Player player, NPC npc, Item item) {
        if (CustomItems.collect(player, item)) {
            return;
        }
        ItemOnGroundManager.register(player, item, npc.getLocation());
    }

    private ReloadWorldContent() {
    }
}
