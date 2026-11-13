package com.elvarg.game.entity.impl.npc.impl;

import java.util.ArrayList;
import java.util.List;

import com.elvarg.game.World;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.content.custom.ReloadBossCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

/**
 * The RELOAD tier bosses, in the open world (ids ending in 1) and in
 * raids (ids ending in 3). See CustomTier for the id scheme.
 */
@Ids({15001, 15003, 15011, 15013, 15021, 15023, 15031, 15033, 15041, 15043, 15051, 15053})
public class ReloadBoss extends NPC {

    private static final CombatMethod COMBAT_METHOD = new ReloadBossCombatMethod();

    private int attacks;
    private boolean enraged;

    public ReloadBoss(int id, Location position) {
        super(id, position);
    }

    @Override
    public CombatMethod getCombatMethod() {
        return COMBAT_METHOD;
    }

    public CustomTier getTier() {
        CustomTier tier = CustomContent.tierForNpc(getId());
        return tier != null ? tier : CustomContent.tierForRaidNpc(getId());
    }

    /** Counts an attack and returns the new total. */
    public int nextAttack() {
        return ++attacks;
    }

    public boolean isEnraged() {
        // A boss that is back at full health (respawned) starts calm again.
        if (enraged && getHitpoints() >= getDefinition().getHitpoints()) {
            enraged = false;
            attacks = 0;
        }
        return enraged;
    }

    public void setEnraged(boolean enraged) {
        this.enraged = enraged;
    }

    /** Players within the given distance that can see this boss. */
    public List<Player> playersInRange(int distance) {
        List<Player> result = new ArrayList<>();
        for (Player player : World.getPlayers()) {
            if (player != null && player.getPrivateArea() == getPrivateArea()
                    && player.getLocation().getZ() == getLocation().getZ()
                    && player.getLocation().isWithinDistance(getLocation(), distance + size())) {
                result.add(player);
            }
        }
        return result;
    }
}
