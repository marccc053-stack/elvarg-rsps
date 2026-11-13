package com.elvarg.game.content.combat.method.impl.npcs;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.content.combat.CombatType;
import com.elvarg.game.content.combat.hit.HitDamage;
import com.elvarg.game.content.combat.hit.HitMask;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Animation;
import com.elvarg.game.model.Graphic;
import com.elvarg.game.model.GraphicHeight;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.Projectile;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;
import com.elvarg.util.Misc;
import com.elvarg.util.timers.TimerKey;

import java.util.ArrayList;
import java.util.List;

/**
 * Magmaroth, boss of the volcano crater in the RELOAD world.
 *
 * Bites in melee range, breathes fire from a distance, and every fifth attack makes the
 * crater erupt: lava lands on the tile the player stands on and a few tiles around it,
 * three ticks later. Stepping away avoids it.
 */
public class MagmarothCombatMethod extends CombatMethod {

    private static final Animation BITE = new Animation(91);
    private static final Animation BREATH = new Animation(84);
    private static final Projectile FIRE = new Projectile(393, 31, 43, 40, 55);
    private static final Projectile LAVA = new Projectile(551, 60, 0, 30, 90);
    private static final Graphic LAVA_LANDING = new Graphic(157, GraphicHeight.MIDDLE);
    private static final int ERUPTION_MAX_HIT = 45;

    private CombatType attackType = CombatType.MAGIC;
    private boolean erupting;
    private int attacks;

    @Override
    public void start(Mobile character, Mobile target) {
        erupting = ++attacks % 5 == 0 && target.isPlayer();
        if (erupting) {
            erupt(character, target);
        } else if (attackType == CombatType.MAGIC) {
            character.performAnimation(BREATH);
            Projectile.sendProjectile(character, target, FIRE);
        } else {
            character.performAnimation(BITE);
        }
    }

    private void erupt(Mobile character, Mobile target) {
        character.performAnimation(BREATH);
        character.forceChat("The crater erupts!");

        Location centre = target.getLocation();
        List<Location> tiles = new ArrayList<>();
        tiles.add(new Location(centre.getX(), centre.getY()));
        for (int i = 0; i < 5; i++) {
            tiles.add(new Location(centre.getX() - 2 + Misc.getRandom(4), centre.getY() - 2 + Misc.getRandom(4)));
        }
        for (Location tile : tiles) {
            Projectile.sendProjectile(character, tile, LAVA);
        }
        TaskManager.submit(new Task(4) {
            @Override
            public void execute() {
                stop();
                if (!character.isRegistered()) {
                    return;
                }
                for (Player player : character.getAsNpc().getPlayersWithinDistance(14)) {
                    for (Location tile : tiles) {
                        player.getPacketSender().sendGlobalGraphic(LAVA_LANDING, tile);
                    }
                    for (Location tile : tiles) {
                        if (player.getLocation().getX() == tile.getX() && player.getLocation().getY() == tile.getY()) {
                            player.getCombat().getHitQueue()
                                    .addPendingDamage(new HitDamage(15 + Misc.getRandom(ERUPTION_MAX_HIT - 15), HitMask.RED));
                            player.getPacketSender().sendMessage("You are caught in the eruption!");
                            break;
                        }
                    }
                }
            }
        });
        character.getTimers().register(TimerKey.COMBAT_ATTACK, 5);
    }

    @Override
    public PendingHit[] hits(Mobile character, Mobile target) {
        if (erupting) {
            return null;
        }
        PendingHit hit = new PendingHit(character, target, this, attackType == CombatType.MAGIC ? 2 : 1);
        if (attackType == CombatType.MAGIC && target.isPlayer()
                && !PrayerHandler.isActivated(target, PrayerHandler.PROTECT_FROM_MAGIC)) {
            // The fire always burns a little when it is not prayed against.
            hit.getHits()[0].incrementDamage(8);
        }
        return new PendingHit[] { hit };
    }

    @Override
    public void finished(Mobile character, Mobile target) {
        boolean close = character.getLocation().getDistance(target.getLocation()) <= 3;
        attackType = close && Misc.getRandom(2) != 0 ? CombatType.MELEE : CombatType.MAGIC;
    }

    @Override
    public int attackSpeed(Mobile character) {
        return 5;
    }

    @Override
    public int attackDistance(Mobile character) {
        return 8;
    }

    @Override
    public CombatType type() {
        return attackType;
    }
}
