package com.elvarg.game.model.commands.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::home - teleports home.
 */
public class HomeCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (TeleportHandler.checkReqs(player, GameConstants.DEFAULT_LOCATION)) {
            TeleportHandler.teleport(player, GameConstants.DEFAULT_LOCATION, TeleportType.NORMAL, false);
        }
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
