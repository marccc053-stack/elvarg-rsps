package com.elvarg.game.content.reloadworld;

import com.elvarg.game.content.PrayerHandler;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Skill;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * Reload's Wrath, the custom prayer of RELOAD.
 *
 * One prayer that boosts melee, ranged and magic at once. It is kept outside the normal
 * PrayerData list on purpose: that list is tied to quick prayers and to saved player files,
 * so adding to it would break existing saves. The combat formulas ask {@link #isActive}.
 */
public final class ReloadPrayer {

    /** Button and config ids, must match Widget.prayerBook() in the client. */
    public static final int BUTTON = 28010, CONFIG = 714;

    public static final int LEVEL = 80;
    public static final float DAMAGE = 1.30f, ACCURACY = 1.25f;

    /** One prayer point is drained every this many game ticks. */
    private static final int DRAIN_TICKS = 2;

    // Only touched from the game thread.
    private static final Set<Player> ACTIVE = Collections.newSetFromMap(new WeakHashMap<>());

    public static boolean isActive(Mobile character) {
        return character != null && character.isPlayer() && ACTIVE.contains(character.getAsPlayer());
    }

    public static boolean handleButton(Player player, int button) {
        if (button != BUTTON) {
            return false;
        }
        if (isActive(player)) {
            deactivate(player);
        } else {
            activate(player);
        }
        return true;
    }

    private static void activate(Player player) {
        if (player.getSkillManager().getMaxLevel(Skill.PRAYER) < LEVEL) {
            player.getPacketSender().sendConfig(CONFIG, 0);
            player.getPacketSender().sendMessage("Je hebt Prayer level " + LEVEL + " nodig voor Reload's Wrath.");
            return;
        }
        if (player.getSkillManager().getCurrentLevel(Skill.PRAYER) <= 0) {
            player.getPacketSender().sendConfig(CONFIG, 0);
            player.getPacketSender().sendMessage("Je hebt geen Prayer punten meer.");
            return;
        }
        // It replaces the normal attack, strength, ranged, magic and defence prayers.
        PrayerHandler.resetPrayers(player, PrayerHandler.STRENGTH_PRAYERS);
        PrayerHandler.resetPrayers(player, PrayerHandler.ATTACK_PRAYERS);
        PrayerHandler.resetPrayers(player, PrayerHandler.RANGED_PRAYERS);
        PrayerHandler.resetPrayers(player, PrayerHandler.MAGIC_PRAYERS);
        PrayerHandler.resetPrayers(player, PrayerHandler.DEFENCE_PRAYERS);

        ACTIVE.add(player);
        player.getPacketSender().sendConfig(CONFIG, 1);

        TaskManager.submit(new Task(DRAIN_TICKS) {
            @Override
            public void execute() {
                if (!isActive(player) || !player.isRegistered() || player.getHitpoints() <= 0) {
                    deactivate(player);
                    stop();
                    return;
                }
                player.getSkillManager().decreaseCurrentLevel(Skill.PRAYER, 1, 0);
                if (player.getSkillManager().getCurrentLevel(Skill.PRAYER) <= 0) {
                    player.getPacketSender().sendMessage("Je Prayer punten zijn op!");
                    deactivate(player);
                    stop();
                }
            }
        });
    }

    public static void deactivate(Mobile character) {
        if (!isActive(character)) {
            return;
        }
        Player player = character.getAsPlayer();
        ACTIVE.remove(player);
        player.getPacketSender().sendConfig(CONFIG, 0);
    }

    /** Called when a normal prayer is switched on: the stat prayers and Reload's Wrath exclude each other. */
    public static void onPrayerActivated(Mobile character, int prayerId) {
        if (!isActive(character)) {
            return;
        }
        if (contains(PrayerHandler.STRENGTH_PRAYERS, prayerId) || contains(PrayerHandler.ATTACK_PRAYERS, prayerId)
                || contains(PrayerHandler.RANGED_PRAYERS, prayerId) || contains(PrayerHandler.MAGIC_PRAYERS, prayerId)
                || contains(PrayerHandler.DEFENCE_PRAYERS, prayerId)) {
            deactivate(character);
        }
    }

    private static boolean contains(int[] prayers, int prayerId) {
        for (int prayer : prayers) {
            if (prayer == prayerId) {
                return true;
            }
        }
        return false;
    }

    private ReloadPrayer() {
    }
}
