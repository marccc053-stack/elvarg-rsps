package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.reloadworld.ReloadWorld;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;

/**
 * Teleports to a named spot in the custom RELOAD world.
 * ::zone lists the spots, ::zone mine teleports to the mine.
 */
public class ZoneCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        if (parts.length < 2) {
            player.getPacketSender().sendMessage("Zones: " + String.join(", ", ReloadWorld.ZONES.keySet()));
            player.getPacketSender().sendMessage("Use ::zone name, for example ::zone home");
            return;
        }
        Location target = ReloadWorld.ZONES.get(parts[1].toLowerCase());
        if (target == null) {
            player.getPacketSender().sendMessage("Unknown zone. Zones: " + String.join(", ", ReloadWorld.ZONES.keySet()));
            return;
        }
        if (TeleportHandler.checkReqs(player, target)) {
            TeleportHandler.teleport(player, target.clone(), TeleportType.NORMAL, false);
        }
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
