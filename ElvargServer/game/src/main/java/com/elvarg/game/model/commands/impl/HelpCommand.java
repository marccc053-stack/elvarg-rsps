package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::help - lists the RELOAD commands.
 */
public class HelpCommand implements Command {

    @Override
    public void execute(Player player, String command, String[] parts) {
        String[] lines = {
                "@blu@RELOAD commands",
                "::tiers - voortgang per tier    ::t1 t/m ::t" + CustomTier.values().length + " - naar een tier zone",
                "::raid [tier] - golven en een boss in je eigen arena",
                "::instance <tier> <4/6/8/10> - eigen veld vol monsters, elke klap raakt ze allemaal. ::leave stopt",
                "::upgrade - 3 dezelfde stukken worden 1 stuk van de volgende tier",
                "::shop - winkel voor Reload scherven    ::dr - je drop rate",
                "::log - je collectie    ::prestige - opnieuw beginnen met een permanente bonus",
                "::starter - eenmalig startpakket    ::home - naar huis",
                "::collect - auto collector aan of uit (Verzamelaar uit ::shop)"
        };
        for (String line : lines) {
            player.getPacketSender().sendMessage(line);
        }
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
