package com.elvarg.game.definition;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an npc's definition.
 * Holds its information, such as
 * name and combat level.
 *
 * @author Professor Oak
 */
public class NpcDefinition {

    /**
     * The map containing all our {@link ItemDefinition}s.
     */
    public static final Map<Integer, NpcDefinition> definitions = new HashMap<Integer, NpcDefinition>();

    /**
     * The default {@link ItemDefinition} that will be used.
     */
    private static final NpcDefinition DEFAULT = new NpcDefinition();

    /**
     * A fallback set of stats for NPCs.
     */
    private static final int[] DEFAULT_STATS = new int[] { 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

    private int id;
    private String name;
    private String examine;
    private int size;
    private int walkRadius;
    private boolean attackable;
    private boolean retreats;
    private boolean aggressive;
    private boolean aggressiveTolerance = true;
    private boolean poisonous;
    private boolean fightsBack = true;
    private int respawn;
    private int maxHit;
    private int hitpoints = 10;
    private int attackSpeed;
    private int attackAnim;
    private int defenceAnim;
    private int deathAnim;
    private int combatLevel;
    private int[] stats;
    private int slayerLevel;
    private int combatFollowDistance;

    /**
     * Creates a custom definition based on an existing npc. Size and
     * animations come from the base; combat values are replaced.
     */
    public static NpcDefinition customCopy(NpcDefinition base, int id, String name, int combatLevel, int hitpoints,
                                           int maxHit, int respawn, boolean aggressive, int[] stats) {
        NpcDefinition def = new NpcDefinition();
        def.id = id;
        def.name = name;
        def.examine = name + ".";
        def.size = base.size;
        def.walkRadius = base.walkRadius;
        def.attackable = true;
        def.retreats = false;
        def.aggressive = aggressive;
        def.aggressiveTolerance = base.aggressiveTolerance;
        def.poisonous = false;
        def.fightsBack = true;
        def.respawn = respawn;
        def.maxHit = maxHit;
        def.hitpoints = hitpoints;
        def.attackSpeed = base.attackSpeed;
        def.attackAnim = base.attackAnim;
        def.defenceAnim = base.defenceAnim;
        def.deathAnim = base.deathAnim;
        def.combatLevel = combatLevel;
        def.stats = stats;
        def.slayerLevel = 1;
        def.combatFollowDistance = base.combatFollowDistance;
        return def;
    }

    /**
     * Creates the definition of an instance mob: like a normal custom npc,
     * but one tile big so a whole grid of them fits in the arena.
     */
    public static NpcDefinition customInstanceMob(NpcDefinition base, int id, String name, int combatLevel,
                                                  int hitpoints, int maxHit, int[] stats) {
        NpcDefinition def = customCopy(base, id, name, combatLevel, hitpoints, maxHit, -1, true, stats);
        def.size = 1;
        return def;
    }

    /**
     * Creates the definition of a custom pet: one tile, cannot be attacked.
     */
    public static NpcDefinition customPet(NpcDefinition base, int id, String name) {
        NpcDefinition def = customCopy(base, id, name, 0, 10, 0, -1, false, DEFAULT_STATS);
        def.size = 1;
        def.attackable = false;
        def.fightsBack = false;
        return def;
    }

    /**
     * Attempts to get the {@link ItemDefinition} for the
     * given item.
     *
     * @param item
     * @return
     */
    public static NpcDefinition forId(int item) {
        return definitions.getOrDefault(item, DEFAULT);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExamine() {
        return examine;
    }

    public int getSize() {
        return size;
    }

    public int getWalkRadius() {
        return walkRadius;
    }

    public boolean isAttackable() {
        return attackable;
    }

    public boolean doesRetreat() {
        return retreats;
    }

    public boolean isAggressive() {
        return aggressive;
    }

    /**
     * Whether this NPC gain tolerance towards players after being around them for a given length of time.
     *
     * @return {boolean}
     */
    public boolean buildsAggressionTolerance() {
        return aggressiveTolerance;
    }

    public boolean isPoisonous() {
        return poisonous;
    }

    public boolean doesFightBack() {
        return fightsBack;
    }

    public int getRespawn() {
        return respawn;
    }

    public int getMaxHit() {
        return maxHit;
    }

    public int getHitpoints() {
        return hitpoints;
    }

    public void setMaxHitpoints(int hitpoints) {
        this.hitpoints = hitpoints;
    }

    public int getAttackSpeed() {
        return attackSpeed;
    }

    public int getAttackAnim() {
        return attackAnim;
    }

    public int getDefenceAnim() {
        return defenceAnim;
    }

    public int getDeathAnim() {
        return deathAnim;
    }

    public int getCombatLevel() {
        return combatLevel;
    }

    public int[] getStats() {
        if (stats == null) {
            return DEFAULT_STATS;
        }

        return stats;
    }

    public int getSlayerLevel() {
        return slayerLevel;
    }

    public int getCombatFollowDistance() {
        return combatFollowDistance;
    }
}
