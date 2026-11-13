package com.elvarg.game.content.custom;

import com.elvarg.game.content.combat.CombatFactory;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.impl.ReloadBoss;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Skill;
import com.elvarg.game.task.impl.CombatPoisonEffect.PoisonType;

/**
 * Combat of the tier bosses. Each boss fights with normal melee attacks
 * and uses its own special every few attacks. Below a quarter of its
 * health a boss becomes enraged and attacks faster.
 */
public class ReloadBossCombatMethod extends CombatMethod {

    /** A special every this many attacks. */
    private static final int SPECIAL_EVERY = 5;

    @Override
    public CombatType type() {
        return CombatType.MELEE;
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        return new PendingHit[]{new PendingHit(character, target, this)};
    }

    @Override
    public void start(Mobile character, Mobile target) {
        int animation = character.getAttackAnim();
        if (animation != -1) {
            character.performAnimation(new Animation(animation));
        }
    }

    @Override
    public int attackDistance(Mobile character) {
        return 2;
    }

    @Override
    public int attackSpeed(Mobile character) {
        int speed = character.getBaseAttackSpeed();
        if (character instanceof ReloadBoss && ((ReloadBoss) character).isEnraged()) {
            speed = Math.max(2, speed - 2);
        }
        return speed;
    }

    @Override
    public void finished(Mobile character, Mobile target) {
        if (!(character instanceof ReloadBoss) || target == null || !target.isPlayer()) {
            return;
        }
        ReloadBoss boss = (ReloadBoss) character;
        Player player = target.getAsPlayer();
        CustomTier tier = boss.getTier();
        if (tier == null) {
            return;
        }

        // Enrage once, below a quarter of the health.
        if (!boss.isEnraged() && boss.getHitpoints() * 4 < boss.getDefinition().getHitpoints()) {
            boss.setEnraged(true);
            boss.forceChat("Nu wordt het menens!");
            player.getPacketSender().sendMessage("@red@" + tier.getBossName() + " is woedend en valt sneller aan!");
        }

        if (boss.nextAttack() % SPECIAL_EVERY != 0) {
            return;
        }
        special(boss, player, tier);
    }

    private void special(ReloadBoss boss, Player player, CustomTier tier) {
        switch (tier) {
            case GLOED:
                // Fire burst: a fixed extra hit on everyone standing close.
                boss.forceChat("Brand!");
                for (Player near : boss.playersInRange(3)) {
                    near.getCombat().getHitQueue().addPendingDamage(
                            new com.elvarg.game.content.combat.hit.HitDamage(tier.getBossMaxHit() / 2,
                                    com.elvarg.game.content.combat.hit.HitMask.RED));
                    near.getPacketSender().sendMessage("De gloed schroeit je.");
                }
                break;
            case VORST:
                // Freeze: the player cannot move for a few seconds.
                boss.forceChat("Bevries!");
                CombatFactory.freeze(player, 8);
                player.getPacketSender().sendMessage("@blu@Je bent vastgevroren!");
                break;
            case GIF:
                // Venom.
                boss.forceChat("Proef mijn gif!");
                CombatFactory.poisonEntity(player, PoisonType.SUPER);
                break;
            case SCHADUW:
                // Shadow drain: takes prayer points.
                boss.forceChat("De schaduw neemt je kracht.");
                int prayer = player.getSkillManager().getCurrentLevel(Skill.PRAYER);
                player.getSkillManager().setCurrentLevel(Skill.PRAYER, Math.max(0, prayer - 15));
                player.getPacketSender().sendMessage("@red@Je prayer wordt weggezogen!");
                break;
            case RELOAD:
                // Reload: heals itself.
                boss.forceChat("Reload!");
                boss.heal(boss.getDefinition().getHitpoints() / 25);
                player.getPacketSender().sendMessage("@red@" + tier.getBossName() + " herlaadt en geneest.");
                break;
            case GOUD:
                // Golden roar: stuns briefly and heals a little.
                boss.forceChat("Alles wat blinkt is van mij!");
                CombatFactory.stun(player, 2, false);
                boss.heal(boss.getDefinition().getHitpoints() / 50);
                break;
        }
    }
}
