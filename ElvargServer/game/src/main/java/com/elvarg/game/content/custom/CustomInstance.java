package com.elvarg.game.content.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.elvarg.game.GameConstants;
import com.elvarg.game.World;
import com.elvarg.game.content.combat.hit.PendingHit;
import com.elvarg.game.content.combat.method.CombatMethod;
import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.npc.NPC;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.Location;
import com.elvarg.game.model.areas.impl.PrivateArea;
import com.elvarg.game.task.Task;
import com.elvarg.game.task.TaskManager;

/**
 * ::instance - a private arena with a grid of one tier's mobs (4x4, 6x6,
 * 8x8 or 10x10). Every hit lands on all mobs in the grid at once, and
 * the grid comes back each time it has been cleared.
 */
public final class CustomInstance {

    public static final int[] SIZES = {4, 6, 8, 10};

    /**
     * Tiles around the target that a hit also reaches. 0 means the whole
     * grid: every mob in the instance takes every hit at the same time.
     */
    public static final int AOE_RADIUS = 0;

    /** Tiles between two mobs in the grid. */
    private static final int SPACING = 2;

    private static final Location ARENA_CENTRE = new Location(2401, 5088);
    private static final Boundary BOUNDARY = new Boundary(2368, 2431, 5056, 5119);

    private CustomInstance() {
    }

    public static boolean isValidSize(int size) {
        for (int s : SIZES) {
            if (s == size) {
                return true;
            }
        }
        return false;
    }

    public static void start(Player player, CustomTier tier, int size) {
        if (!CustomContent.hasUnlocked(player, tier)) {
            player.getPacketSender().sendMessage("Je hebt tier " + tier.number() + " nog niet ontgrendeld.");
            return;
        }
        if (player.getArea() instanceof PrivateArea) {
            player.getPacketSender().sendMessage("Je zit al in een instance of raid. Typ ::leave om eruit te gaan.");
            return;
        }
        if (player.busy()) {
            player.getPacketSender().sendMessage("Dat kan nu niet.");
            return;
        }
        InstanceArea area = new InstanceArea(player, tier, size);
        // Stand just south of the grid.
        int half = (size - 1) * SPACING / 2;
        player.moveTo(new Location(ARENA_CENTRE.getX(), ARENA_CENTRE.getY() - half - 3));
        area.add(player);
        player.getPacketSender().sendMessage("@red@Instance " + size + "x" + size + ", tier " + tier.number() + " ("
                + tier.getDisplayName() + "). Elke klap raakt het hele veld tegelijk. ::leave om te stoppen.");
        area.spawnGrid();
    }

    /** Ends the instance the player is in, if any. */
    public static boolean leave(Player player) {
        if (player.getArea() instanceof InstanceArea) {
            ((InstanceArea) player.getArea()).end(true);
            return true;
        }
        return false;
    }

    /**
     * Called for every hit a player lands on an npc: inside an instance the
     * same damage also goes to the mobs around the target.
     */
    public static void splash(Player player, NPC target, CombatMethod method, int damage) {
        if (!(target.getPrivateArea() instanceof InstanceArea)) {
            return;
        }
        InstanceArea area = (InstanceArea) target.getPrivateArea();
        boolean ownerStrike = CustomItems.hasOwnerStrike(player);
        for (NPC other : area.mobs) {
            if (other == target || !other.isRegistered() || other.isDying() || other.getHitpoints() <= 0) {
                continue;
            }
            // The owner's blade clears the whole grid in one blow.
            if (!ownerStrike && AOE_RADIUS > 0
                    && !other.getLocation().isWithinDistance(target.getLocation(), AOE_RADIUS)) {
                continue;
            }
            int amount = ownerStrike ? other.getHitpoints() : damage;
            if (amount <= 0) {
                continue;
            }
            other.getCombat().getHitQueue().addPendingHit(PendingHit.create(player, other, method, amount, true));
        }
    }

    /**
     * The private arena of one instance.
     */
    public static final class InstanceArea extends PrivateArea {

        private final Player player;
        private final CustomTier tier;
        private final int size;
        private final List<NPC> mobs = new ArrayList<>();
        private int alive;
        private int rounds;
        private boolean ended;

        InstanceArea(Player player, CustomTier tier, int size) {
            super(Arrays.asList(BOUNDARY));
            this.player = player;
            this.tier = tier;
            this.size = size;
        }

        void spawnGrid() {
            final InstanceArea area = this;
            TaskManager.submit(new Task(4, player, false) {
                @Override
                protected void execute() {
                    stop();
                    if (area.ended || area.isDestroyed()) {
                        return;
                    }
                    mobs.clear();
                    int half = (size - 1) * SPACING / 2;
                    for (int column = 0; column < size; column++) {
                        for (int row = 0; row < size; row++) {
                            NPC npc = new NPC(tier.getInstanceMobId(), new Location(
                                    ARENA_CENTRE.getX() - half + column * SPACING,
                                    ARENA_CENTRE.getY() - half + row * SPACING));
                            npc.setOwner(player);
                            npc.getMovementCoordinator().setRadius(0);
                            add(npc);
                            World.getAddNPCQueue().add(npc);
                            mobs.add(npc);
                        }
                    }
                    alive = mobs.size();
                    rounds++;
                }
            });
        }

        @Override
        public void defeated(Player killer, Mobile character) {
            if (!character.isNpc() || ended) {
                return;
            }
            alive--;
            if (alive <= 0) {
                player.getPacketSender().sendMessage("Ronde " + rounds + " leeg. De volgende " + size + "x" + size
                        + " komt eraan.");
                spawnGrid();
            }
        }

        void end(boolean movePlayer) {
            if (ended) {
                return;
            }
            ended = true;
            for (NPC npc : mobs) {
                if (npc.isRegistered()) {
                    World.getRemoveNPCQueue().add(npc);
                }
            }
            mobs.clear();
            remove(player);
            if (movePlayer) {
                player.getCombat().reset();
                player.moveTo(GameConstants.DEFAULT_LOCATION.clone());
            }
            destroy();
        }

        @Override
        public void postLeave(Mobile mobile, boolean logout) {
            // Mobs leave when they die; only the player leaving ends the instance.
            if (mobile.isPlayer()) {
                if (logout) {
                    mobile.moveTo(GameConstants.DEFAULT_LOCATION.clone());
                }
                end(false);
            }
        }

        @Override
        public boolean canTeleport(Player player) {
            // Teleporting away is allowed: the instance closes once you are gone.
            return true;
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
            player.getPacketSender().sendMessage("@red@Je bent verslagen. De instance is gesloten, je spullen ben je niet kwijt.");
            end(true);
            return true;
        }

        @Override
        public boolean overridesNpcAggressionTolerance(Player player, int npcId) {
            return true;
        }
    }
}
