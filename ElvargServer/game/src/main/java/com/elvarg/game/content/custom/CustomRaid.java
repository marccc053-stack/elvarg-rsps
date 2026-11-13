package com.elvarg.game.content.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

import com.elvarg.game.GameConstants;
import com.elvarg.game.World;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

/**
 * ::raid - a private fight of three waves against a tier's npcs, in the
 * fight cave arena. The loot is paid out when the last wave is down.
 */
public final class CustomRaid {

    private static final Location ENTRANCE = new Location(2413, 5117);
    private static final Location ARENA_CENTRE = new Location(2401, 5088);
    private static final Boundary BOUNDARY = new Boundary(2368, 2431, 5056, 5119);

    private static final int[][] SPAWN_OFFSETS = {{-4, -3}, {4, -3}, {-4, 4}, {4, 4}, {0, 6}};

    /** Raid mobs per wave; the last wave also has the boss. */
    private static final int[] WAVE_MOBS = {3, 5, 2};

    private CustomRaid() {
    }

    public static void start(Player player, CustomTier tier) {
        if (!CustomContent.hasUnlocked(player, tier)) {
            player.getPacketSender().sendMessage("Je hebt tier " + tier.number() + " nog niet ontgrendeld.");
            return;
        }
        if (player.getArea() instanceof RaidArea) {
            player.getPacketSender().sendMessage("Je zit al in een raid.");
            return;
        }
        if (player.busy() || com.elvarg.game.content.combat.CombatFactory.isBeingAttacked(player)) {
            player.getPacketSender().sendMessage("Dat kan nu niet.");
            return;
        }
        RaidArea area = new RaidArea(player, tier);
        player.moveTo(ENTRANCE.clone());
        area.add(player);
        player.getPacketSender().sendMessage("@red@Raid tier " + tier.number() + " (" + tier.getDisplayName()
                + ") begint. Drie golven, de laatste met de boss. Loop de arena in!");
        area.spawnWave();
    }

    /**
     * The private instance of one raid.
     */
    public static final class RaidArea extends PrivateArea {

        private final Player player;
        private final CustomTier tier;
        private final List<NPC> spawned = new ArrayList<>();
        private int wave;
        private int alive;
        private boolean finished;

        RaidArea(Player player, CustomTier tier) {
            super(Arrays.asList(BOUNDARY));
            this.player = player;
            this.tier = tier;
        }

        void spawnWave() {
            final RaidArea area = this;
            TaskManager.submit(new Task(6, player, false) {
                @Override
                protected void execute() {
                    stop();
                    if (area.isDestroyed() || area.finished) {
                        return;
                    }
                    boolean lastWave = wave == WAVE_MOBS.length - 1;
                    player.getPacketSender().sendMessage("@red@Golf " + (wave + 1) + " van " + WAVE_MOBS.length
                            + (lastWave ? ": " + tier.getBossName() + "!" : "."));
                    for (int i = 0; i < WAVE_MOBS[wave]; i++) {
                        int[] offset = SPAWN_OFFSETS[i % SPAWN_OFFSETS.length];
                        spawn(tier.getRaidMobId(), offset[0], offset[1]);
                    }
                    if (lastWave) {
                        spawn(tier.getRaidBossId(), 0, -6);
                    }
                }
            });
        }

        private void spawn(int npcId, int offsetX, int offsetY) {
            final NPC npc = new NPC(npcId,
                    new Location(ARENA_CENTRE.getX() + offsetX, ARENA_CENTRE.getY() + offsetY));
            npc.setOwner(player);
            add(npc);
            World.getAddNPCQueue().add(npc);
            spawned.add(npc);
            alive++;

            // Let it come for the player as soon as it stands in the arena.
            TaskManager.submit(new Task(3, npc, false) {
                @Override
                protected void execute() {
                    stop();
                    if (!isDestroyed() && npc.isRegistered() && player.isRegistered()) {
                        npc.getCombat().attack(player);
                    }
                }
            });
        }

        @Override
        public void defeated(Player killer, Mobile character) {
            if (!character.isNpc() || finished) {
                return;
            }
            alive--;
            if (alive > 0) {
                return;
            }
            wave++;
            if (wave < WAVE_MOBS.length) {
                spawnWave();
                return;
            }
            finished = true;
            reward();
            leaveRaid();
        }

        private void reward() {
            int completed = CustomProgress.increment(player, CustomProgress.KEY_RAIDS_COMPLETED);
            ThreadLocalRandom random = ThreadLocalRandom.current();
            int shards = 50 * tier.number() * tier.number();
            CustomItems.give(player, new Item(CustomItems.SHARD, shards));
            CustomItems.give(player, new Item(CustomItems.MYSTERY_BOX, tier.number()));
            player.getPacketSender().sendMessage("@gre@Raid voltooid (" + completed + " in totaal)! Beloning: "
                    + shards + " Reload scherven en " + tier.number() + " mysteriekist(en).");

            double chance = 0.25 * (1.0 + CustomContent.getDropRateBonus(player) / 100.0);
            if (random.nextDouble() < Math.min(1.0, chance)) {
                int slot = random.nextInt(CustomTier.SLOT_NAMES.length);
                CustomItems.give(player, new Item(tier.getItemId(slot), 1));
                CustomProgress.logItem(player, tier.getItemId(slot));
                player.getPacketSender().sendMessage("@red@Raid buit: " + tier.getItemName(slot) + "!");
            }
        }

        private void leaveRaid() {
            remove(player);
            player.getCombat().reset();
            player.moveTo(GameConstants.DEFAULT_LOCATION.clone());
            cleanUp();
        }

        /** Removes whatever is still alive in the arena and closes the instance. */
        private void cleanUp() {
            finished = true;
            for (NPC npc : spawned) {
                if (npc.isRegistered()) {
                    World.getRemoveNPCQueue().add(npc);
                }
            }
            spawned.clear();
            destroy();
        }

        @Override
        public void postLeave(Mobile mobile, boolean logout) {
            // Npcs leave when they die; only the player leaving ends the raid.
            if (mobile.isPlayer()) {
                if (logout) {
                    mobile.moveTo(GameConstants.DEFAULT_LOCATION.clone());
                }
                remove(mobile);
                cleanUp();
            }
        }

        @Override
        public boolean canTeleport(Player player) {
            player.getPacketSender().sendMessage("Je kunt niet teleporteren uit een raid. Win, of ga ten onder.");
            return false;
        }

        @Override
        public boolean canTrade(Player player, Player target) {
            return false;
        }

        @Override
        public boolean isMulti(Mobile character) {
            return true;
        }

        @Override
        public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
            return false;
        }

        @Override
        public boolean handleDeath(Player player, Optional<Player> killer) {
            finished = true;
            player.getPacketSender().sendMessage("@red@Je bent verslagen in golf " + (wave + 1)
                    + ". De raid is voorbij, je spullen ben je niet kwijt.");
            leaveRaid();
            return true;
        }

        @Override
        public boolean overridesNpcAggressionTolerance(Player player, int npcId) {
            return true;
        }
    }
}
