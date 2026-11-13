package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomItems;
import com.elvarg.game.content.custom.CustomProgress;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::log - shows which custom items the player has obtained.
 */
public class CollectionLogCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.getPacketSender().sendMessage("@blu@Collectie - elke complete tier geeft +"
                + CustomProgress.LOG_TIER_DROP_RATE + "% drop rate");
        for (CustomTier tier : CustomTier.values()) {
            StringBuilder missing = new StringBuilder();
            for (int slot = 0; slot < CustomTier.SLOT_NAMES.length; slot++) {
                if (!CustomProgress.hasLogged(player, tier.getItemId(slot))) {
                    missing.append(missing.length() == 0 ? "" : ", ").append(CustomTier.SLOT_NAMES[slot]);
                }
            }
            boolean complete = missing.length() == 0;
            player.getPacketSender().sendMessage((complete ? "@gre@" : "@bla@") + "Tier " + tier.number() + " "
                    + tier.getDisplayName() + ": " + CustomProgress.countLogged(player, tier) + "/"
                    + CustomTier.SLOT_NAMES.length + (complete ? " compleet" : " - mist: " + missing));
        }
        StringBuilder pets = new StringBuilder();
        for (int i = 0; i < CustomItems.PET_ITEMS.length; i++) {
            if (CustomProgress.hasLogged(player, CustomItems.PET_ITEMS[i])) {
                pets.append(pets.length() == 0 ? "" : ", ").append(CustomItems.PET_NAMES[i]);
            }
        }
        player.getPacketSender().sendMessage("Huisdieren: " + (pets.length() == 0 ? "nog geen" : pets.toString()));
        player.getPacketSender().sendMessage("Raids voltooid: "
                + CustomProgress.get(player, CustomProgress.KEY_RAIDS_COMPLETED) + ", kisten geopend: "
                + CustomProgress.get(player, CustomProgress.KEY_BOXES_OPENED) + ", prestige: "
                + CustomProgress.getPrestige(player));
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
