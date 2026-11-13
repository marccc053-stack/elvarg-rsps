package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomUpgrade;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::upgrade - combines three identical tier pieces into the next tier.
 */
public class UpgradeCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        CustomUpgrade.upgrade(player);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
