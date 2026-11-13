package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.reloadworld.ReloadWorld;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;

/**
 * Teleports to a named spot on the RELOAD island.
 * ::zone lists the spots, ::zone mine teleports to the mine, ::train goes straight to the training grounds.
 */
public class ZoneCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        String zone = parts[0].equalsIgnoreCase("train") ? "train" : parts.length > 1 ? parts[1].toLowerCase() : null;
        if (zone == null) {
            player.getPacketSender().sendMessage("Eiland: " + String.join(", ", ReloadWorld.ZONES.keySet()));
            player.getPacketSender().sendMessage("Gebruik ::zone naam, bijvoorbeeld ::zone mine. ::train gaat naar de trainingsarena.");
            return;
        }
        Location target = ReloadWorld.ZONES.get(zone);
        if (target == null) {
            player.getPacketSender().sendMessage("Onbekende plek. Kies uit: " + String.join(", ", ReloadWorld.ZONES.keySet()));
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
