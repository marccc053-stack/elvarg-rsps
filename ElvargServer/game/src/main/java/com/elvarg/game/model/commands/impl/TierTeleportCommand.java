package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;

/**
 * ::t1 .. ::t5 - teleports to a custom tier zone if it is unlocked.
 */
public class TierTeleportCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        int number;
        try {
            number = Integer.parseInt(parts[0].substring(1));
        } catch (NumberFormatException e) {
            return;
        }
        CustomTier tier = CustomTier.forNumber(number);
        if (tier == null) {
            return;
        }
        if (!CustomContent.hasUnlocked(player, tier)) {
            CustomTier previous = CustomTier.forNumber(number - 1);
            player.getPacketSender().sendMessage("Tier " + number + " is nog op slot. Nodig in tier "
                    + previous.number() + ": " + CustomTier.MOB_KILLS_FOR_NEXT_TIER + " kills op "
                    + previous.getMobName() + " (" + CustomContent.getKills(player, previous.getMobId())
                    + ") en " + CustomTier.BOSS_KILLS_FOR_NEXT_TIER + " kill op " + previous.getBossName() + " ("
                    + CustomContent.getKills(player, previous.getBossId()) + ").");
            return;
        }
        if (!TeleportHandler.checkReqs(player, tier.getCentre())) {
            return;
        }
        TeleportHandler.teleport(player, tier.getCentre(), TeleportType.NORMAL, false);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
