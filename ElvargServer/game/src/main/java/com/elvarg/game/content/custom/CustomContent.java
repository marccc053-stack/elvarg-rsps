package com.elvarg.game.content.custom;

import com.elvarg.game.World;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.definition.NpcDefinition;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.container.impl.Equipment;

/**
 * Registers the custom RELOAD content (items, npcs, spawns) on top of the
 * normal definitions and answers questions about it.
 */
public final class CustomContent {

    private static final int RING_OF_WEALTH = 2572;
    private static final int RING_OF_WEALTH_DROP_RATE = 5;

    /** Spawns must only be created once, also when definitions are reloaded. */
    private static boolean spawned;

    private CustomContent() {
    }

    /**
     * Called at the end of the item definition loader.
     */
    public static void registerItems() {
        for (CustomTier tier : CustomTier.values()) {
            for (int slot = 0; slot < CustomTier.SLOT_NAMES.length; slot++) {
                ItemDefinition base = ItemDefinition.forId(tier.getBaseItem(slot));
                int value = 1_000_000 * tier.number() * tier.number();
                ItemDefinition def = ItemDefinition.customCopy(base, tier.getItemId(slot), tier.getItemName(slot),
                        "Tier " + tier.number() + " uitrusting uit het rijk van " + tier.getDisplayName() + ".",
                        value, tier.getBonuses(slot));
                ItemDefinition.definitions.put(def.getId(), def);
            }
        }
        CustomItems.register();
    }

    /**
     * Called at the end of the npc definition loader.
     */
    public static synchronized void registerNpcs() {
        for (CustomTier tier : CustomTier.values()) {
            NpcDefinition mobBase = NpcDefinition.forId(tier.getMobBase());
            NpcDefinition mob = NpcDefinition.customCopy(mobBase, tier.getMobId(), tier.getMobName(),
                    tier.getCombatLevel(false), tier.getMobHitpoints(), tier.getMobMaxHit(), 12, false,
                    tier.getNpcStats(false));
            NpcDefinition.definitions.put(mob.getId(), mob);

            NpcDefinition bossBase = NpcDefinition.forId(tier.getBossBase());
            NpcDefinition boss = NpcDefinition.customCopy(bossBase, tier.getBossId(), tier.getBossName(),
                    tier.getCombatLevel(true), tier.getBossHitpoints(), tier.getBossMaxHit(), 60, false,
                    tier.getNpcStats(true));
            NpcDefinition.definitions.put(boss.getId(), boss);

            // Raid versions: aggressive, no respawn.
            NpcDefinition raidMob = NpcDefinition.customCopy(mobBase, tier.getRaidMobId(), tier.getMobName(),
                    tier.getCombatLevel(false), tier.getMobHitpoints(), tier.getMobMaxHit(), -1, true,
                    tier.getNpcStats(false));
            NpcDefinition.definitions.put(raidMob.getId(), raidMob);
            NpcDefinition raidBoss = NpcDefinition.customCopy(bossBase, tier.getRaidBossId(), tier.getBossName(),
                    tier.getCombatLevel(true), tier.getBossHitpoints(), tier.getBossMaxHit(), -1, true,
                    tier.getNpcStats(true));
            NpcDefinition.definitions.put(raidBoss.getId(), raidBoss);

            // Instance version of the mob.
            NpcDefinition instanceMob = NpcDefinition.customInstanceMob(mobBase, tier.getInstanceMobId(),
                    tier.getMobName(), tier.getCombatLevel(false), tier.getMobHitpoints(), tier.getMobMaxHit(),
                    tier.getNpcStats(false));
            NpcDefinition.definitions.put(instanceMob.getId(), instanceMob);
        }

        // Pets
        putPet(CustomItems.PET_BRIX_NPC, 114, "Brix");
        putPet(CustomItems.PET_GLOEDJE_NPC, 2005, "Gloedje");
        putPet(CustomItems.PET_MINI_RELOADER_NPC, 319, "Mini Reloader");
        putPet(CustomItems.PET_TWEELING_NPC, 497, "Tweeling");

        // Home npcs (same factory: one tile, cannot be attacked)
        putPet(15110, 2108, "Reload gids");
        putPet(15111, 637, "Scherven handelaar");
        putPet(15112, 2457, "Raid meester");
        putPet(15113, 3893, "Meestersmid");

        if (!spawned) {
            spawned = true;
            spawnNpcs();
        }
    }

    private static void putPet(int id, int baseId, String name) {
        NpcDefinition.definitions.put(id, NpcDefinition.customPet(NpcDefinition.forId(baseId), id, name));
    }

    private static void spawnNpcs() {
        for (CustomTier tier : CustomTier.values()) {
            Location centre = tier.getCentre();
            for (int[] offset : CustomTier.MOB_OFFSETS) {
                NPC mob = NPC.create(tier.getMobId(),
                        new Location(centre.getX() + offset[0], centre.getY() + offset[1], centre.getZ()));
                mob.getMovementCoordinator().setRadius(3);
                World.getAddNPCQueue().add(mob);
            }
            NPC boss = NPC.create(tier.getBossId(), tier.getBossLocation());
            boss.getMovementCoordinator().setRadius(2);
            World.getAddNPCQueue().add(boss);
        }

        // The four home npcs stand around the spot where you arrive.
        Location home = com.elvarg.game.GameConstants.DEFAULT_LOCATION;
        int[][] homeOffsets = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}};
        for (int i = 0; i < homeOffsets.length; i++) {
            NPC npc = NPC.create(15110 + i,
                    new Location(home.getX() + homeOffsets[i][0], home.getY() + homeOffsets[i][1], home.getZ()));
            npc.getMovementCoordinator().setRadius(0);
            World.getAddNPCQueue().add(npc);
        }
    }

    /**
     * @return the tier a custom npc belongs to, or null for a normal npc.
     */
    public static CustomTier tierForNpc(int npcId) {
        for (CustomTier tier : CustomTier.values()) {
            if (tier.getMobId() == npcId || tier.getBossId() == npcId) {
                return tier;
            }
        }
        return null;
    }

    public static boolean isBoss(int npcId) {
        CustomTier tier = tierForNpc(npcId);
        return tier != null && tier.getBossId() == npcId;
    }

    /**
     * @return the tier a raid npc belongs to, or null if it is not a raid npc.
     */
    public static CustomTier tierForRaidNpc(int npcId) {
        for (CustomTier tier : CustomTier.values()) {
            if (tier.getRaidMobId() == npcId || tier.getRaidBossId() == npcId) {
                return tier;
            }
        }
        return null;
    }

    /**
     * @return the tier an instance mob belongs to, or null if it is not one.
     */
    public static CustomTier tierForInstanceNpc(int npcId) {
        for (CustomTier tier : CustomTier.values()) {
            if (tier.getInstanceMobId() == npcId) {
                return tier;
            }
        }
        return null;
    }

    /**
     * @return the tier a custom gear item belongs to, or null.
     */
    public static CustomTier tierForItem(int itemId) {
        for (CustomTier tier : CustomTier.values()) {
            if (itemId >= tier.getItemId(0) && itemId < tier.getItemId(0) + CustomTier.SLOT_NAMES.length) {
                return tier;
            }
        }
        return null;
    }

    public static CustomTier highestUnlocked(Player player) {
        CustomTier highest = CustomTier.values()[0];
        for (CustomTier tier : CustomTier.values()) {
            if (hasUnlocked(player, tier)) {
                highest = tier;
            } else {
                break;
            }
        }
        return highest;
    }

    public static int getKills(Player player, int npcId) {
        Integer kills = player.getCustomKills().get(npcId);
        return kills == null ? 0 : kills;
    }

    /**
     * Has the player unlocked the zone of the given tier?
     */
    public static boolean hasUnlocked(Player player, CustomTier tier) {
        if (tier.ordinal() == 0) {
            return true;
        }
        CustomTier previous = CustomTier.values()[tier.ordinal() - 1];
        return getKills(player, previous.getMobId()) >= CustomTier.MOB_KILLS_FOR_NEXT_TIER
                && getKills(player, previous.getBossId()) >= CustomTier.BOSS_KILLS_FOR_NEXT_TIER;
    }

    /**
     * The player's total drop rate bonus in percent.
     */
    public static int getDropRateBonus(Player player) {
        Item[] worn = player.getEquipment().getItems();
        int bonus = 0;

        int amulet = worn[Equipment.AMULET_SLOT].getId();
        if (amulet == CustomItems.OWNER_AMULET) {
            bonus += CustomItems.OWNER_AMULET_DROP_RATE;
        }
        for (CustomTier tier : CustomTier.values()) {
            if (tier.getItemId(CustomTier.SLOT_AMULET) == amulet) {
                bonus += tier.getAmuletDropRate();
            }
            if (wearsFullSet(player, tier)) {
                bonus += getSetDropRate(tier);
            }
        }

        if (worn[Equipment.RING_SLOT].getId() == RING_OF_WEALTH) {
            bonus += RING_OF_WEALTH_DROP_RATE;
        }

        // Pet, luck potion, prestige and completed collection logs
        if (player.getCurrentPet() != null) {
            bonus += CustomItems.getPetDropRate(player.getCurrentPet().getId());
        }
        if (CustomProgress.getLuckMinutesLeft(player) > 0) {
            bonus += CustomItems.LUCK_POTION_DROP_RATE;
        }
        bonus += CustomProgress.getPrestige(player) * CustomProgress.PRESTIGE_DROP_RATE;
        bonus += CustomProgress.countCompletedTiers(player) * CustomProgress.LOG_TIER_DROP_RATE;
        return bonus;
    }

    /** Drop rate % for wearing weapon, helm, body, legs and shield of one tier. */
    public static int getSetDropRate(CustomTier tier) {
        return 5 * tier.number();
    }

    public static boolean wearsFullSet(Player player, CustomTier tier) {
        Item[] worn = player.getEquipment().getItems();
        return worn[Equipment.WEAPON_SLOT].getId() == tier.getItemId(CustomTier.SLOT_WEAPON)
                && worn[Equipment.HEAD_SLOT].getId() == tier.getItemId(CustomTier.SLOT_HELM)
                && worn[Equipment.BODY_SLOT].getId() == tier.getItemId(CustomTier.SLOT_BODY)
                && worn[Equipment.LEG_SLOT].getId() == tier.getItemId(CustomTier.SLOT_LEGS)
                && worn[Equipment.SHIELD_SLOT].getId() == tier.getItemId(CustomTier.SLOT_SHIELD);
    }
}
