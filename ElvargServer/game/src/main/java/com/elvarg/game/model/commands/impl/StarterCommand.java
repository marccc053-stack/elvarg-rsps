package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomItems;
import com.elvarg.game.content.custom.CustomProgress;
import com.elvarg.game.model.Item;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::starter - a one time kit to get going in tier 1.
 */
public class StarterCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (CustomProgress.get(player, CustomProgress.KEY_STARTER_CLAIMED) > 0) {
            player.getPacketSender().sendMessage("Je hebt je startpakket al gehad.");
            return;
        }
        CustomProgress.set(player, CustomProgress.KEY_STARTER_CLAIMED, 1);
        CustomItems.give(player, new Item(4151, 1)); // abyssal whip
        CustomItems.give(player, new Item(386, 100)); // noted sharks
        CustomItems.give(player, new Item(CustomItems.SHARD, 50));
        CustomItems.give(player, new Item(CustomItems.MYSTERY_BOX, 2));
        CustomItems.give(player, new Item(CustomItems.LUCK_POTION, 1));
        player.getPacketSender().sendMessage("@gre@Startpakket ontvangen. Typ ::help voor alle commands, ::t1 om te beginnen.");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
