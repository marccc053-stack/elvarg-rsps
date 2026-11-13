package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomItems;
import com.elvarg.game.content.custom.CustomProgress;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::collect - switches the auto collector on or off.
 */
public class CollectCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        boolean nowOff = CustomProgress.get(player, CustomProgress.KEY_COLLECT_OFF) == 0;
        CustomProgress.set(player, CustomProgress.KEY_COLLECT_OFF, nowOff ? 1 : 0);
        if (nowOff) {
            player.getPacketSender().sendMessage("Auto collector staat @red@uit@bla@. Buit valt weer op de grond.");
        } else if (CustomItems.hasCollector(player)) {
            player.getPacketSender().sendMessage("Auto collector staat @gre@aan@bla@. Buit komt naar je inventory, of je bank als die vol is.");
        } else {
            player.getPacketSender().sendMessage("Auto collector staat aan, maar je hebt nog geen Verzamelaar. Koop hem met ::shop.");
        }
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
