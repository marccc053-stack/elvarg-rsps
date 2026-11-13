package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomProgress;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::prestige - resets the tier progress for a permanent bonus.
 */
public class PrestigeCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (parts.length < 2 || !parts[1].equalsIgnoreCase("ja")) {
            int prestige = CustomProgress.getPrestige(player);
            player.getPacketSender().sendMessage("Prestige " + prestige + " van " + CustomProgress.MAX_PRESTIGE
                    + ". Elk niveau: +" + CustomProgress.PRESTIGE_DROP_RATE + "% drop rate en +"
                    + CustomProgress.PRESTIGE_DAMAGE + "% schade, permanent.");
            player.getPacketSender().sendMessage("Nodig: " + CustomProgress.PRESTIGE_BOSS_KILLS + " kills op "
                    + CustomTier.last().getBossName() + ". Je tiers gaan weer op slot, je spullen houd je.");
            player.getPacketSender().sendMessage("Typ ::prestige ja om het te doen.");
            return;
        }
        CustomProgress.prestige(player);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
