package com.elvarg.game.content.custom;

import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;

/**
 * The custom tiers of RELOAD. Every tier has its own zone, a mob, a boss
 * and a gear set. All balance numbers live here, so tuning the server
 * means editing this file only.
 *
 * Item ids:  30000 + (tier - 1) * 10 + slot   (see {@link #SLOT_NAMES})
 * Npc ids:   15000 + (tier - 1) * 10          (mob), +1 (boss),
 *            +2 (raid mob), +3 (raid boss), +4 (instance mob)
 *
 * The client has a matching table in com.runescape.cache.def.CustomDefinitions.
 */
public enum CustomTier {

    // name, zone centre, zone boundary, boss offset,
    // base item ids {weapon, helm, body, legs, shield, amulet},
    // weapon attack style index (0 stab, 1 slash, 2 crush),
    // mob base npc, boss base npc, mob name, boss name,
    // mob hp, mob max hit, boss hp, boss max hit, drop rate % on the amulet
    GLOED("Gloed", new Location(3257, 3276, 0), new Boundary(3240, 3268, 3254, 3300), 0, 14,
            new int[]{4587, 1163, 1127, 1079, 1201, 1704}, 1,
            2005, 2025, "Gloeddemon", "Heer van de Gloed",
            150, 10, 1500, 18, 5),

    VORST("Vorst", new Location(2707, 3718, 0), new Boundary(2690, 2730, 3705, 3735), 10, 4,
            new int[]{1305, 11335, 21892, 4087, 21895, 6585}, 1,
            2085, 5779, "Vorstreus", "IJsgraver",
            350, 14, 3500, 25, 10),

    GIF("Gif", new Location(2520, 3240, 0), new Boundary(2500, 2545, 3220, 3262), 12, 8,
            new int[]{13576, 4745, 4749, 4751, 12954, 19553}, 2,
            2075, 6610, "Gifreus", "Gifmoeder",
            750, 18, 7000, 32, 20),

    SCHADUW("Schaduw", new Location(3226, 3108, 0), new Boundary(3205, 3250, 3090, 3125), 10, -6,
            new int[]{24417, 22326, 22327, 22328, 11283, 19547}, 2,
            415, 2215, "Schaduwdemon", "Schaduwgeneraal",
            1500, 22, 14000, 40, 35),

    RELOAD("Reload", new Location(2984, 4378, 2), new Boundary(2970, 3000, 4362, 4398, 2), 4, 8,
            new int[]{22324, 26382, 26384, 26386, 12817, 24780}, 0,
            2048, 319, "Reload wachter", "De Reloader",
            3000, 28, 28000, 48, 50),

    // The secret last tier. Its gear is the best in the game.
    GOUD("Goud", new Location(1761, 5186, 0), new Boundary(1730, 1790, 5130, 5235), 8, 6,
            new int[]{23995, 10350, 10348, 10346, 10352, 12002}, 1,
            2098, 239, "Gouden reus", "De Gouden Draak",
            5000, 34, 50000, 55, 75);

    public static final int ITEM_BASE = 30000;
    public static final int NPC_BASE = 15000;

    public static final int SLOT_WEAPON = 0, SLOT_HELM = 1, SLOT_BODY = 2, SLOT_LEGS = 3, SLOT_SHIELD = 4,
            SLOT_AMULET = 5;

    public static final String[] SLOT_NAMES = {"wapen", "helm", "harnas", "beenplaten", "schild", "amulet"};

    /** Mob kills of the previous tier needed before the next zone opens. */
    public static final int MOB_KILLS_FOR_NEXT_TIER = 50;
    /** Boss kills of the previous tier needed before the next zone opens. */
    public static final int BOSS_KILLS_FOR_NEXT_TIER = 1;

    /** Offsets around the zone centre where the mobs spawn. */
    public static final int[][] MOB_OFFSETS = {{-4, -4}, {4, -4}, {-4, 4}, {4, 4}, {0, -7}, {0, 7}};

    private final String displayName;
    private final Location centre;
    private final Boundary boundary;
    private final int bossOffsetX, bossOffsetY;
    private final int[] baseItems;
    private final int weaponStyle;
    private final int mobBase, bossBase;
    private final String mobName, bossName;
    private final int mobHitpoints, mobMaxHit, bossHitpoints, bossMaxHit;
    private final int amuletDropRate;

    CustomTier(String displayName, Location centre, Boundary boundary, int bossOffsetX, int bossOffsetY,
               int[] baseItems, int weaponStyle, int mobBase, int bossBase, String mobName, String bossName,
               int mobHitpoints, int mobMaxHit, int bossHitpoints, int bossMaxHit, int amuletDropRate) {
        this.displayName = displayName;
        this.centre = centre;
        this.boundary = boundary;
        this.bossOffsetX = bossOffsetX;
        this.bossOffsetY = bossOffsetY;
        this.baseItems = baseItems;
        this.weaponStyle = weaponStyle;
        this.mobBase = mobBase;
        this.bossBase = bossBase;
        this.mobName = mobName;
        this.bossName = bossName;
        this.mobHitpoints = mobHitpoints;
        this.mobMaxHit = mobMaxHit;
        this.bossHitpoints = bossHitpoints;
        this.bossMaxHit = bossMaxHit;
        this.amuletDropRate = amuletDropRate;
    }

    /** 1 for the first tier, 5 for the last. */
    public int number() {
        return ordinal() + 1;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Location getCentre() {
        return centre;
    }

    public Boundary getBoundary() {
        return boundary;
    }

    public Location getBossLocation() {
        return new Location(centre.getX() + bossOffsetX, centre.getY() + bossOffsetY, centre.getZ());
    }

    public int getBaseItem(int slot) {
        return baseItems[slot];
    }

    public int getItemId(int slot) {
        return ITEM_BASE + ordinal() * 10 + slot;
    }

    public int getMobId() {
        return NPC_BASE + ordinal() * 10;
    }

    public int getBossId() {
        return NPC_BASE + ordinal() * 10 + 1;
    }

    /** Raid versions: aggressive and they do not respawn. */
    public int getRaidMobId() {
        return NPC_BASE + ordinal() * 10 + 2;
    }

    public int getRaidBossId() {
        return NPC_BASE + ordinal() * 10 + 3;
    }

    /** Instance version of the mob: one tile, aggressive, no respawn of its own. */
    public int getInstanceMobId() {
        return NPC_BASE + ordinal() * 10 + 4;
    }

    /** @return the next tier, or null for the last one. */
    public CustomTier next() {
        return ordinal() + 1 < values().length ? values()[ordinal() + 1] : null;
    }

    public static CustomTier last() {
        return values()[values().length - 1];
    }

    public int getMobBase() {
        return mobBase;
    }

    public int getBossBase() {
        return bossBase;
    }

    public String getMobName() {
        return mobName;
    }

    public String getBossName() {
        return bossName;
    }

    public int getMobHitpoints() {
        return mobHitpoints;
    }

    public int getMobMaxHit() {
        return mobMaxHit;
    }

    public int getBossHitpoints() {
        return bossHitpoints;
    }

    public int getBossMaxHit() {
        return bossMaxHit;
    }

    public int getAmuletDropRate() {
        return amuletDropRate;
    }

    public String getItemName(int slot) {
        return displayName + " " + SLOT_NAMES[slot];
    }

    /**
     * The 14 equipment bonuses for an item of this tier.
     * Order: stab, slash, crush, magic, range attack; stab, slash, crush,
     * magic, range defence; strength, ranged strength, magic damage, prayer.
     */
    public int[] getBonuses(int slot) {
        int[] attackPower = {150, 300, 500, 800, 1200, 1800};
        int[] helmDef = {60, 120, 200, 320, 500, 750};
        int[] bodyDef = {150, 300, 500, 800, 1200, 1800};
        int[] legsDef = {100, 200, 340, 540, 820, 1230};
        int[] shieldDef = {80, 160, 270, 430, 650, 980};
        int t = ordinal();
        int[] b = new int[14];
        switch (slot) {
            case SLOT_WEAPON:
                b[weaponStyle] = attackPower[t];
                b[10] = attackPower[t];
                break;
            case SLOT_HELM:
                fillDefence(b, helmDef[t]);
                b[10] = 5 * number();
                break;
            case SLOT_BODY:
                fillDefence(b, bodyDef[t]);
                b[10] = 10 * number();
                break;
            case SLOT_LEGS:
                fillDefence(b, legsDef[t]);
                b[10] = 8 * number();
                break;
            case SLOT_SHIELD:
                fillDefence(b, shieldDef[t]);
                b[10] = 5 * number();
                break;
            case SLOT_AMULET:
                b[0] = b[1] = b[2] = 20 * number();
                b[10] = 15 * number();
                b[13] = 3 * number();
                break;
        }
        return b;
    }

    private static void fillDefence(int[] bonuses, int amount) {
        bonuses[5] = amount;
        bonuses[6] = amount;
        bonuses[7] = amount;
        bonuses[8] = amount / 2;
        bonuses[9] = amount;
    }

    /**
     * Npc stats array as used by the combat formulas: attack, strength,
     * defence, ranged, magic levels, 5 unused, then stab/slash/crush/magic/
     * ranged defence bonuses.
     */
    public int[] getNpcStats(boolean boss) {
        int[] level = {70, 110, 160, 230, 320, 420};
        int[] defence = {40, 120, 260, 450, 700, 1000};
        int t = ordinal();
        int lvl = boss ? (int) (level[t] * 1.3) : level[t];
        int def = boss ? (int) (defence[t] * 1.3) : defence[t];
        return new int[]{lvl, lvl, lvl, 1, 1, 0, 0, 0, 0, 0, def, def, def, def, def, 0, 0, 0};
    }

    public int getCombatLevel(boolean boss) {
        return (boss ? 250 : 100) * number();
    }

    public static CustomTier forNumber(int number) {
        if (number < 1 || number > values().length) {
            return null;
        }
        return values()[number - 1];
    }
}
