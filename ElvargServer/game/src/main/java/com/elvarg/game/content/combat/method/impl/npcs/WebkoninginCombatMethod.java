package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.model.Skill;
import com.elvarg.game.task.impl.CombatPoisonEffect.PoisonType;
import com.elvarg.util.Misc;

/**
 * Webkoningin, world boss in the east arena of the RELOAD island.
 *
 * Switches between melee and magic after every attack. Her hits poison, sometimes
 * wrap the player in a web (cannot move for a few seconds) and sometimes drain prayer.
 */
public class WebkoninginCombatMethod extends CombatMethod {

    private static final Animation MELEE_ANIMATION = new Animation(5319);
    private static final Animation MAGIC_ANIMATION = new Animation(5322);
    private static final Projectile SPIT = new Projectile(165, 31, 43, 40, 55);
    private static final Graphic DRAIN = new Graphic(172, GraphicHeight.MIDDLE);

    private CombatType attackType = CombatType.MELEE;

    @Override
    public void start(Mobile character, Mobile target) {
        if (attackType == CombatType.MAGIC) {
            character.performAnimation(MAGIC_ANIMATION);
            Projectile.sendProjectile(character, target, SPIT);
        } else {
            character.performAnimation(MELEE_ANIMATION);
        }
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[] { new PendingHit(character, target, this, attackType == CombatType.MAGIC ? 2 : 1) };
    }

    @Override
    public void finished(Mobile character, Mobile target) {
        attackType = attackType == CombatType.MAGIC ? CombatType.MELEE : CombatType.MAGIC;
    }

    @Override
    public void handleAfterHitEffects(PendingHit hit) {
        if (!hit.isAccurate() || hit.getTarget() == null || !hit.getTarget().isPlayer()) {
            return;
        }
        Player player = hit.getTarget().getAsPlayer();
        int roll = Misc.getRandom(100);
        if (roll < 30) {
            CombatFactory.poisonEntity(player, PoisonType.SUPER);
        } else if (roll < 42) {
            CombatFactory.freeze(player, 8);
            player.getPacketSender().sendMessage("@red@De Webkoningin spint je in haar web!");
        } else if (roll < 57) {
            player.performGraphic(DRAIN);
            player.getSkillManager().decreaseCurrentLevel(Skill.PRAYER, 5 + hit.getTotalDamage() / 4, 0);
            player.getPacketSender().sendMessage("@red@De Webkoningin zuigt je prayer weg!");
        }
    }

    @Override
    public int attackDistance(Mobile character) {
        return 5;
    }

    @Override
    public CombatType type() {
        return attackType;
    }
}
