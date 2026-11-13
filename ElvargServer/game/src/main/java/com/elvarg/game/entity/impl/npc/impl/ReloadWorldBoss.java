package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.MagmarothCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.VorstklauwCombatMethod;
import com.elvarg.game.content.combat.method.impl.npcs.WebkoninginCombatMethod;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;

import static com.elvarg.game.content.reloadworld.ReloadWorldContent.*;

/**
 * The three world bosses of the RELOAD island (the tier bosses are ReloadBoss).
 * Every boss gets its own combat script instance, because the scripts remember
 * what the next attack will be.
 */
@Ids({VORSTKLAUW, WEBKONINGIN, MAGMAROTH})
public class ReloadWorldBoss extends NPC {

    private final CombatMethod combatMethod;

    public ReloadWorldBoss(int id, Location position) {
        super(id, position);
        switch (id) {
            case MAGMAROTH:
                combatMethod = new MagmarothCombatMethod();
                break;
            case WEBKONINGIN:
                combatMethod = new WebkoninginCombatMethod();
                break;
            default:
                combatMethod = new VorstklauwCombatMethod();
                break;
        }
    }

    @Override
    public CombatMethod getCombatMethod() {
        return combatMethod;
    }
}
