package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomItems;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.commands.Command;

/**
 * ::ownergear - gives the owner set. Owners only.
 */
public class OwnerGearCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        // The owner amulet already collects loot; the Verzamelaar is for when you take it off.
        if (player.getInventory().getAmount(CustomItems.COLLECTOR) == 0) {
            CustomItems.give(player, new Item(CustomItems.COLLECTOR, 1));
        }
        if (player.getInventory().getAmount(CustomItems.PET_TWEELING_ITEM) == 0
                && (player.getCurrentPet() == null
                || player.getCurrentPet().getId() != CustomItems.PET_TWEELING_NPC)) {
            CustomItems.give(player, new Item(CustomItems.PET_TWEELING_ITEM, 1));
        }
        for (int id : CustomItems.OWNER_SET) {
            if (player.getInventory().getAmount(id) == 0) {
                CustomItems.give(player, new Item(id, 1));
            }
        }
        player.getPacketSender().sendMessage("@red@Je eigenaars set ligt klaar. Met de kling in de hand is elke klap raak.");
    }

    @Override
    public boolean canUse(Player player) {
        return CustomItems.isOwner(player);
    }
}
