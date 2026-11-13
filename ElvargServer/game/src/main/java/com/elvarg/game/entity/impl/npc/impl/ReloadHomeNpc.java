package com.elvarg.game.entity.impl.npc.impl;

import com.elvarg.game.content.custom.CustomContent;
import com.elvarg.game.content.custom.CustomRaid;
import com.elvarg.game.content.custom.CustomTier;
import com.elvarg.game.content.custom.CustomUpgrade;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.npc.NPCInteraction;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Ids;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.commands.impl.ShardShopCommand;
import com.elvarg.game.model.dialogues.builders.DynamicDialogueBuilder;
import com.elvarg.game.model.dialogues.entries.impl.NpcDialogue;
import com.elvarg.game.model.dialogues.entries.impl.OptionDialogue;
import com.elvarg.game.model.teleportation.TeleportHandler;
import com.elvarg.game.model.teleportation.TeleportType;

/**
 * The four RELOAD npcs at home: the guide (tier teleports), the shard
 * trader (shop), the raid master and the master smith (upgrades).
 */
@Ids({15110, 15111, 15112, 15113})
public class ReloadHomeNpc extends NPC implements NPCInteraction {

    public static final int GUIDE = 15110;
    public static final int TRADER = 15111;
    public static final int RAID_MASTER = 15112;
    public static final int SMITH = 15113;

    public ReloadHomeNpc(int id, Location position) {
        super(id, position);
    }

    @Override
    public void firstOptionClick(Player player, NPC npc) {
        switch (npc.getId()) {
            case GUIDE:
                player.getDialogueManager().start(new GuideDialogue(0));
                break;
            case TRADER:
                new ShardShopCommand().execute(player, "shop", new String[]{"shop"});
                break;
            case RAID_MASTER:
                player.getDialogueManager().start(new RaidDialogue());
                break;
            case SMITH:
                CustomUpgrade.upgrade(player);
                break;
        }
    }

    @Override
    public void secondOptionClick(Player player, NPC npc) {
        firstOptionClick(player, npc);
    }

    @Override
    public void thirdOptionClick(Player player, NPC npc) {
    }

    @Override
    public void forthOptionClick(Player player, NPC npc) {
    }

    @Override
    public void useItemOnNpc(Player player, NPC npc, int itemId, int slot) {
    }

    private static void teleport(Player player, CustomTier tier) {
        player.getPacketSender().sendInterfaceRemoval();
        if (!CustomContent.hasUnlocked(player, tier)) {
            player.getPacketSender().sendMessage("Tier " + tier.number() + " is nog op slot. Typ ::tiers voor je voortgang.");
            return;
        }
        if (TeleportHandler.checkReqs(player, tier.getCentre())) {
            TeleportHandler.teleport(player, tier.getCentre(), TeleportType.NORMAL, false);
        }
    }

    /** Tier teleports, three per page. */
    private static final class GuideDialogue extends DynamicDialogueBuilder {

        private final int page;

        GuideDialogue(int page) {
            this.page = page;
        }

        @Override
        public void build(Player player) {
            final CustomTier[] tiers = CustomTier.values();
            final int first = page * 3;
            add(new NpcDialogue(0, GUIDE, "Waar wil je heen? Hoe verder, hoe sterker de vijand."));
            add(new OptionDialogue(1, (option) -> {
                switch (option) {
                    case FIRST_OPTION:
                        teleport(player, tiers[first]);
                        break;
                    case SECOND_OPTION:
                        teleport(player, tiers[first + 1]);
                        break;
                    case THIRD_OPTION:
                        teleport(player, tiers[first + 2]);
                        break;
                    case FOURTH_OPTION:
                        player.getDialogueManager().start(new GuideDialogue(page == 0 ? 1 : 0));
                        break;
                    default:
                        player.getPacketSender().sendInterfaceRemoval();
                        break;
                }
            }, label(player, tiers[first]), label(player, tiers[first + 1]), label(player, tiers[first + 2]),
                    page == 0 ? "Verdere tiers..." : "Eerdere tiers...", "Laat maar"));
        }

        private static String label(Player player, CustomTier tier) {
            return "Tier " + tier.number() + ": " + tier.getDisplayName()
                    + (CustomContent.hasUnlocked(player, tier) ? "" : " (op slot)");
        }
    }

    private static final class RaidDialogue extends DynamicDialogueBuilder {

        @Override
        public void build(Player player) {
            final CustomTier tier = CustomContent.highestUnlocked(player);
            add(new NpcDialogue(0, RAID_MASTER, "Drie golven, daarna de boss. Durf je een raid van tier "
                    + tier.number() + " aan?"));
            add(new OptionDialogue(1, (option) -> {
                player.getPacketSender().sendInterfaceRemoval();
                if (option == com.elvarg.game.model.dialogues.DialogueOption.FIRST_OPTION) {
                    CustomRaid.start(player, tier);
                }
            }, "Ja, begin de raid", "Nee, nog niet"));
        }
    }
}
