package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.FrostclawCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.MagmarothCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.VenomqueenCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.game.content.reloadworld.ReloadIds.*;

/**
 * The three custom bosses of the RELOAD world. Every boss gets its own combat script instance,
 * because the scripts remember what the next attack will be.
 */
@Ids({MAGMAROTH, VENOMQUEEN, FROSTCLAW})
public class ReloadBoss extends NPC {

    private final CombatMethod combatMethod;

    public ReloadBoss(int id, Location position) {
        super(id, position);
        switch (id) {
            case MAGMAROTH:
                combatMethod = new MagmarothCombatMethod();
                break;
            case VENOMQUEEN:
                combatMethod = new VenomqueenCombatMethod();
                break;
            default:
                combatMethod = new FrostclawCombatMethod();
                break;
        }
    }

    @Override
    public CombatMethod getCombatMethod() {
        return combatMethod;
    }
}
