package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::tiers - overview of all custom tiers and the player's progress.
 */
public class TiersCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        player.getPacketSender().sendMessage("@blu@RELOAD tiers - teleporteer met ::t1 t/m ::t" + CustomTier.values().length);
        for (CustomTier tier : CustomTier.values()) {
            boolean unlocked = CustomContent.hasUnlocked(player, tier);
            player.getPacketSender().sendMessage((unlocked ? "@gre@" : "@red@") + "Tier " + tier.number() + " "
                    + tier.getDisplayName() + "@bla@ - " + tier.getMobName() + ": "
                    + CustomContent.getKills(player, tier.getMobId()) + " kills, " + tier.getBossName() + ": "
                    + CustomContent.getKills(player, tier.getBossId()) + " kills");
        }
        player.getPacketSender().sendMessage("Volgende tier: " + CustomTier.MOB_KILLS_FOR_NEXT_TIER
                + " mob kills en " + CustomTier.BOSS_KILLS_FOR_NEXT_TIER + " boss kill in de vorige tier.");
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
