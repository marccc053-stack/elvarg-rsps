package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomInstance;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::instance [tier] [size] - opens a private grid of mobs. ::leave closes it.
 */
public class InstanceCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (parts[0].equalsIgnoreCase("leave")) {
            if (!CustomInstance.leave(player)) {
                player.getPacketSender().sendMessage("Je zit niet in een instance.");
            }
            return;
        }
        CustomTier tier = CustomContent.highestUnlocked(player);
        int size = 4;
        try {
            if (parts.length >= 2) {
                tier = CustomTier.forNumber(Integer.parseInt(parts[1]));
            }
            if (parts.length >= 3) {
                String text = parts[2].toLowerCase();
                size = Integer.parseInt(text.contains("x") ? text.substring(0, text.indexOf('x')) : text);
            }
        } catch (NumberFormatException e) {
            tier = null;
        }
        if (tier == null || !CustomInstance.isValidSize(size)) {
            player.getPacketSender().sendMessage("Gebruik: ::instance <tier> <4, 6, 8 of 10>. Voorbeeld: ::instance 1 6");
            return;
        }
        CustomInstance.start(player, tier, size);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
