package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomProgress;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::dr - shows the player's drop rate bonus.
 */
public class DropRateCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.getPacketSender().sendMessage(
                "Je drop rate bonus is @blu@" + CustomContent.getDropRateBonus(player) + "%@bla@.");
        for (CustomTier tier : CustomTier.values()) {
            if (CustomContent.wearsFullSet(player, tier)) {
                player.getPacketSender().sendMessage("Volledige " + tier.getDisplayName() + " set: +"
                        + CustomContent.getSetDropRate(tier) + "%.");
            }
        }
        player.getPacketSender().sendMessage("Bronnen: tier amulet, volledige tier set, ring of wealth, huisdier,");
        player.getPacketSender().sendMessage("geluksdrank (" + CustomProgress.getLuckMinutesLeft(player)
                + " min over), prestige en complete collecties.");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
