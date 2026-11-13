package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Projectile;
import com.elvarg.util.Misc;

/**
 * Vorstklauw, world boss in the west arena of the RELOAD island.
 *
 * Mostly heavy melee. One attack in four is a frost wave that freezes the player in place
 * when it lands. Below half health it sometimes roars and heals itself.
 */
public class VorstklauwCombatMethod extends CombatMethod {

    private static final Animation MELEE_ANIMATION = new Animation(4925);
    private static final Projectile FROST_WAVE = new Projectile(395, 31, 43, 40, 60);
    private static final Graphic FROZEN = new Graphic(369, GraphicHeight.LOW);
    private static final int ROAR_HEAL = 60;

    private CombatType attackType = CombatType.MELEE;

    @Override
    public void start(Mobile character, Mobile target) {
        character.performAnimation(MELEE_ANIMATION);
        if (attackType == CombatType.MAGIC) {
            Projectile.sendProjectile(character, target, FROST_WAVE);
        }
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[] { new PendingHit(character, target, this, attackType == CombatType.MAGIC ? 2 : 1) };
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        if (attackType != CombatType.MAGIC || !hit.isAccurate() || hit.getTarget() == null) {
            return;
        }
        CombatFactory.freeze(hit.getTarget(), 10);
        hit.getTarget().performGraphic(FROZEN);
        if (hit.getTarget().isPlayer()) {
            hit.getTarget().getAsPlayer().getPacketSender().sendMessage("@blu@De adem van Vorstklauw vriest je vast!");
        }
    }

    @Override
    public void finished(Mobile character, Mobile target) {
        attackType = Misc.getRandom(3) == 0 ? CombatType.MAGIC : CombatType.MELEE;

        if (character.isNpc() && character.getHitpoints() > 0
                && character.getHitpoints() < character.getAsNpc().getDefinition().getHitpoints() / 2
                && Misc.getRandom(7) == 0) {
            character.heal(ROAR_HEAL);
            character.forceChat("GRRROOOAAAR!");
        }
    }

    @Override
    public int attackDistance(Mobile character) {
        return attackType == CombatType.MAGIC ? 7 : 2;
    }

    @Override
    public CombatType type() {
        return attackType;
    }
}
