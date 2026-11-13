package com.runescape.cache.def;

import java.util.HashMap;
import java.util.Map;

/**
 * RELOAD custom items and npcs. They do not exist in the cache: each one
 * borrows the models of an existing item or npc and gets its own name and
 * colour tint.
 *
 * Keep this table in sync with the server:
 * com.elvarg.game.content.custom.CustomTier
 *
 * Item ids:  30000 + (tier - 1) * 10 + slot, 30100+ for the other items
 * Npc ids:   15000 + (tier - 1) * 10 (mob), +1 (boss), +2 (raid mob),
 *            +3 (raid boss), 15100+ for the pets
 */
public final class CustomDefinitions {

    public static final class CustomItem {
        public final int baseId;
        public final String name;
        /** -1 keeps the colours of the base item. */
        public final int hue, saturation;
        /** First right-click option, or null to keep the base item's options. */
        public final String action;
        /** RELOAD's own models (see CustomModels), -1 to use the base item's. */
        public int inventoryModel = -1, wornModel = -1;
        /** Icon settings that go with the own inventory model. */
        public int zoom, rotationY, rotationX, translateX, translateYZ;

        CustomItem ownModels(int inventoryModel, int wornModel, int zoom, int rotationY, int rotationX,
                             int translateX, int translateYZ) {
            this.inventoryModel = inventoryModel;
            this.wornModel = wornModel;
            this.zoom = zoom;
            this.rotationY = rotationY;
            this.rotationX = rotationX;
            this.translateX = translateX;
            this.translateYZ = translateYZ;
            return this;
        }

        CustomItem(int baseId, String name, int hue, int saturation, String action) {
            this.baseId = baseId;
            this.name = name;
            this.hue = hue;
            this.saturation = saturation;
            this.action = action;
        }
    }

    public static final class CustomNpc {
        public final int baseId;
        public final String name;
        public final int combatLevel;
        public final int hue, saturation;
        /** 128 is the normal size. */
        public final int scale;
        /** Pets can only be picked up. */
        public final boolean pet;
        /** First option of a friendly npc ("Talk-to"), or null. */
        public String talkAction;
        /** Instance mobs stand on one tile, whatever the base npc's size. */
        public boolean oneTile;

        CustomNpc oneTile() {
            this.oneTile = true;
            return this;
        }

        CustomNpc(int baseId, String name, int combatLevel, int hue, int saturation, int scale, boolean pet) {
            this.baseId = baseId;
            this.name = name;
            this.combatLevel = combatLevel;
            this.hue = hue;
            this.saturation = saturation;
            this.scale = scale;
            this.pet = pet;
        }

        CustomNpc talk(String action) {
            this.talkAction = action;
            return this;
        }
    }

    private static final int ITEM_BASE = 30000;
    private static final int NPC_BASE = 15000;

    private static final String[] SLOT_NAMES = {"wapen", "helm", "harnas", "beenplaten", "schild", "amulet"};

    private static final Map<Integer, CustomItem> ITEMS = new HashMap<Integer, CustomItem>();
    private static final Map<Integer, CustomNpc> NPCS = new HashMap<Integer, CustomNpc>();

    static {
        // tier, name, hue (0-63), saturation (0-7), base items {weapon, helm, body, legs, shield, amulet},
        // mob base npc, mob name, boss base npc, boss name, boss scale
        tier(1, "Gloed", 5, 5, new int[]{4587, 1163, 1127, 1079, 1201, 1704},
                2005, "Gloeddemon", 2025, "Heer van de Gloed", 150);
        tier(2, "Vorst", 33, 6, new int[]{1305, 11335, 21892, 4087, 21895, 6585},
                2085, "Vorstreus", 5779, "IJsgraver", 150);
        tier(3, "Gif", 21, 7, new int[]{13576, 4745, 4749, 4751, 12954, 19553},
                2075, "Gifreus", 6610, "Gifmoeder", 128);
        tier(4, "Schaduw", 50, 6, new int[]{24417, 22326, 22327, 22328, 11283, 19547},
                415, "Schaduwdemon", 2215, "Schaduwgeneraal", 140);
        tier(5, "Reload", 0, 7, new int[]{22324, 26382, 26384, 26386, 12817, 24780},
                2048, "Reload wachter", 319, "De Reloader", 128);
        tier(6, "Goud", 9, 7, new int[]{23995, 10350, 10348, 10346, 10352, 12002},
                2098, "Gouden reus", 239, "De Gouden Draak", 128);

        // Other items: id, base item, name, hue, saturation, first option
        ITEMS.put(30100, new CustomItem(13307, "Reload scherf", 0, 7, null));
        ITEMS.put(30101, new CustomItem(6199, "Mysteriekist", 50, 6, "Open"));
        ITEMS.put(30102, new CustomItem(12695, "Geluksdrank", 9, 7, "Drink"));
        ITEMS.put(30103, new CustomItem(11941, "Verzamelaar", 9, 7, null));
        ITEMS.put(30110, new CustomItem(13247, "Brix", -1, 0, null));
        ITEMS.put(30111, new CustomItem(13247, "Gloedje", 5, 5, null));
        ITEMS.put(30112, new CustomItem(12816, "Mini Reloader", 0, 7, null));
        ITEMS.put(30113, new CustomItem(13178, "Tweeling", 9, 7, null));

        // The owner set. Weapon, shield and crown are RELOAD's own models;
        // body, legs and amulet are existing ones in black and white.
        ITEMS.put(30200, new CustomItem(1305, "Eigenaars kling", -1, 0, null)
                .ownModels(70061, 70060, 2100, 456, 12, 0, 6));
        ITEMS.put(30201, new CustomItem(1038, "Eigenaars kroon", -1, 0, null)
                .ownModels(70065, 70064, 620, 76, 1852, 1, 1));
        ITEMS.put(30202, new CustomItem(11832, "Eigenaars harnas", 0, 0, null));
        ITEMS.put(30203, new CustomItem(11834, "Eigenaars beenplaten", 0, 0, null));
        ITEMS.put(30204, new CustomItem(21895, "Eigenaars schild", -1, 0, null)
                .ownModels(70063, 70062, 1700, 344, 1104, -6, -14));
        ITEMS.put(30205, new CustomItem(19553, "Eigenaars amulet", 9, 7, null));

        // Pets: id, base npc, name, hue, saturation, scale
        NPCS.put(15100, new CustomNpc(114, "Brix", 0, -1, 0, 128, true));
        NPCS.put(15101, new CustomNpc(2005, "Gloedje", 0, 5, 5, 50, true));
        NPCS.put(15102, new CustomNpc(319, "Mini Reloader", 0, 0, 7, 28, true));
        NPCS.put(15103, new CustomNpc(497, "Tweeling", 0, 9, 7, 45, true));

        // Home npcs: id, base npc, name
        NPCS.put(15110, new CustomNpc(2108, "Reload gids", 0, -1, 0, 128, false).talk("Praat"));
        NPCS.put(15111, new CustomNpc(637, "Scherven handelaar", 0, -1, 0, 128, false).talk("Handel"));
        NPCS.put(15112, new CustomNpc(2457, "Raid meester", 0, -1, 0, 128, false).talk("Praat"));
        NPCS.put(15113, new CustomNpc(3893, "Meestersmid", 0, -1, 0, 128, false).talk("Upgrade"));
    }

    private static void tier(int number, String name, int hue, int saturation, int[] baseItems, int mobBase,
                             String mobName, int bossBase, String bossName, int bossScale) {
        int offset = (number - 1) * 10;
        for (int slot = 0; slot < baseItems.length; slot++) {
            ITEMS.put(ITEM_BASE + offset + slot,
                    new CustomItem(baseItems[slot], name + " " + SLOT_NAMES[slot], hue, saturation, null));
        }
        // Weapon (slot 0) and shield (slot 4) have RELOAD's own models.
        int models = 70000 + offset;
        ITEMS.get(ITEM_BASE + offset).ownModels(models + 1, models, 1900, 456, 12, 0, 6);
        ITEMS.get(ITEM_BASE + offset + 4).ownModels(models + 3, models + 2, 1700, 344, 1104, -6, -14);
        CustomNpc mob = new CustomNpc(mobBase, mobName, 100 * number, hue, saturation, 128, false);
        CustomNpc boss = new CustomNpc(bossBase, bossName, 250 * number, hue, saturation, bossScale, false);
        NPCS.put(NPC_BASE + offset, mob);
        NPCS.put(NPC_BASE + offset + 1, boss);
        // Raid versions look the same.
        NPCS.put(NPC_BASE + offset + 2, mob);
        NPCS.put(NPC_BASE + offset + 3, boss);
        // Instance version: smaller, so a whole grid fits.
        NPCS.put(NPC_BASE + offset + 4,
                new CustomNpc(mobBase, mobName, 100 * number, hue, saturation, 80, false).oneTile());
    }

    private CustomDefinitions() {
    }

    public static CustomItem item(int id) {
        return ITEMS.get(id);
    }

    public static CustomNpc npc(int id) {
        return NPCS.get(id);
    }
}
