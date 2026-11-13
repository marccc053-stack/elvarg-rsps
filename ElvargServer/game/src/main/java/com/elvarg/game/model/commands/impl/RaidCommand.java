package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomRaid;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::raid [tier] - starts a private raid of the given tier (default: highest unlocked).
 */
public class RaidCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        CustomTier tier = CustomContent.highestUnlocked(player);
        if (parts.length >= 2) {
            try {
                tier = CustomTier.forNumber(Integer.parseInt(parts[1]));
            } catch (NumberFormatException e) {
                tier = null;
            }
            if (tier == null) {
                player.getPacketSender().sendMessage("Gebruik: ::raid of ::raid 1 t/m ::raid "
                        + CustomTier.values().length + ".");
                return;
            }
        }
        CustomRaid.start(player, tier);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
