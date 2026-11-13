package com.elvarg.game.model.commands.impl;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.commands.Command;
import com.elvarg.game.model.rights.PlayerRights;

import static com.elvarg.game.content.reloadworld.ReloadIds.*;

/**
 * Test command for the custom RELOAD gear: ::reloadgear ember | magma | venom | tidal | all.
 * Needs free inventory space; owner and developer only.
 */
public class ReloadGearCommand implements Command {

    private static final int[] EMBER = {EMBER_HELM, EMBER_PLATEBODY, EMBER_PLATELEGS, EMBER_KITESHIELD, EMBER_SCIMITAR};
    private static final int[] MAGMA = {MAGMA_HELM, MAGMA_CHESTPLATE, MAGMA_TASSETS, MAGMA_BOOTS, MAGMA_WHIP, MAGMA_GODSWORD};
    private static final int[] VENOM = {VENOM_HELM, VENOM_CHESTPLATE, VENOM_CHAINSKIRT, VENOM_BOW};
    private static final int[] TIDAL = {TIDAL_HOOD, TIDAL_ROBETOP, TIDAL_ROBESKIRT, TIDAL_STAFF};
    private static final int[] EXTRA = {RELOAD_CAPE, RELOAD_AMULET};

    @Override
    public void execute(Player player, String command, String[] parts) {
        String set = parts.length > 1 ? parts[1].toLowerCase() : "all";
        boolean all = set.equals("all");
        boolean given = false;
        if (all || set.equals("ember")) given |= give(player, EMBER);
        if (all || set.equals("magma")) given |= give(player, MAGMA);
        if (all || set.equals("venom")) given |= give(player, VENOM);
        if (all || set.equals("tidal")) given |= give(player, TIDAL);
        if (all || set.equals("extra")) given |= give(player, EXTRA);
        if (!given) {
            player.getPacketSender().sendMessage("Use ::reloadgear ember, magma, venom, tidal, extra or all");
        }
    }

    private static boolean give(Player player, int[] items) {
        for (int id : items) {
            if (ItemDefinition.forId(id) == null || ItemDefinition.forId(id).getId() != id) {
                player.getPacketSender().sendMessage("Item " + id + " is missing, is items_reload.json loaded?");
                continue;
            }
            player.getInventory().add(new Item(id, 1));
        }
        return true;
    }

    @Override
    public boolean canUse(Player player) {
        PlayerRights rights = player.getRights();
        return rights == PlayerRights.OWNER || rights == PlayerRights.DEVELOPER;
    }
}
