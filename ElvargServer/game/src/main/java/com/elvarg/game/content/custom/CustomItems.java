package com.elvarg.game.content.custom;

import java.util.concurrent.ThreadLocalRandom;

import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Item;

/**
 * The custom items that are not tier gear: the shard currency, mystery
 * boxes, the luck potion and the pet items.
 *
 * The client has matching entries in CustomDefinitions.
 */
public final class CustomItems {

    public static final int SHARD = 30100;
    public static final int MYSTERY_BOX = 30101;
    public static final int LUCK_POTION = 30102;
    /** Keep it in the inventory and drops come to you by themselves. */
    public static final int COLLECTOR = 30103;

    public static final int PET_BRIX_ITEM = 30110;
    public static final int PET_GLOEDJE_ITEM = 30111;
    public static final int PET_MINI_RELOADER_ITEM = 30112;
    /** The pet that counts every kill twice. */
    public static final int PET_TWEELING_ITEM = 30113;

    public static final int PET_BRIX_NPC = 15100;
    public static final int PET_GLOEDJE_NPC = 15101;
    public static final int PET_MINI_RELOADER_NPC = 15102;
    public static final int PET_TWEELING_NPC = 15103;

    public static final int[] PET_ITEMS = {PET_BRIX_ITEM, PET_GLOEDJE_ITEM, PET_MINI_RELOADER_ITEM,
            PET_TWEELING_ITEM};
    public static final String[] PET_NAMES = {"Brix", "Gloedje", "Mini Reloader", "Tweeling"};

    /** How many kills one kill counts for with the given pet following. */
    public static int getKillMultiplier(Player player) {
        return player.getCurrentPet() != null && player.getCurrentPet().getId() == PET_TWEELING_NPC ? 2 : 1;
    }

    // The owner set: only a player with owner rights can wear it.
    public static final int OWNER_WEAPON = 30200;
    public static final int OWNER_CROWN = 30201;
    public static final int OWNER_BODY = 30202;
    public static final int OWNER_LEGS = 30203;
    public static final int OWNER_SHIELD = 30204;
    public static final int OWNER_AMULET = 30205;
    public static final int[] OWNER_SET = {OWNER_WEAPON, OWNER_CROWN, OWNER_BODY, OWNER_LEGS, OWNER_SHIELD,
            OWNER_AMULET};
    public static final int OWNER_AMULET_DROP_RATE = 100;

    /** Minutes of extra drop rate from one luck potion. */
    public static final int LUCK_POTION_MINUTES = 30;
    public static final int LUCK_POTION_DROP_RATE = 25;

    private static final int COINS = 995;

    private CustomItems() {
    }

    public static void register() {
        put(SHARD, 13307, "Reload scherf", "De munt van RELOAD. Gebruik ::shop en ::upgrade.", 1, true);
        put(MYSTERY_BOX, 6199, "Mysteriekist", "Wat zou erin zitten?", 50, false);
        put(LUCK_POTION, 12695, "Geluksdrank",
                LUCK_POTION_MINUTES + " minuten lang " + LUCK_POTION_DROP_RATE + "% extra drop rate.", 30, false);
        put(COLLECTOR, 11941, "Verzamelaar", "Buit komt vanzelf naar je toe. Aan en uit met ::collect.", 500,
                false);
        put(PET_BRIX_ITEM, 13247, "Brix", "Een trouwe viervoeter. Geeft 10% drop rate.", 1, false);
        put(PET_GLOEDJE_ITEM, 13247, "Gloedje", "Een klein gloeiend demoontje. Geeft 5% drop rate.", 1, false);
        put(PET_MINI_RELOADER_ITEM, 12816, "Mini Reloader", "De Reloader in zakformaat. Geeft 15% drop rate.", 1,
                false);
        put(PET_TWEELING_ITEM, 13178, "Tweeling", "Telt elke kill dubbel zolang hij je volgt.", 1000, false);
        registerOwnerSet();
    }

    private static void registerOwnerSet() {
        // Bonuses: stab, slash, crush, magic, range attack; the same five for
        // defence; strength, ranged strength, magic damage, prayer.
        int[] weapon = new int[14];
        weapon[0] = weapon[1] = weapon[2] = 5000;
        weapon[10] = 5000;
        int[] armour = new int[14];
        for (int i = 5; i < 10; i++) {
            armour[i] = 5000;
        }
        armour[10] = 250;
        armour[13] = 50;
        int[] amulet = new int[14];
        for (int i = 0; i < 5; i++) {
            amulet[i] = 500;
        }
        amulet[10] = 500;
        amulet[13] = 50;

        putOwner(OWNER_WEAPON, 1305, "Eigenaars kling", weapon);
        putOwner(OWNER_CROWN, 1038, "Eigenaars kroon", armour);
        putOwner(OWNER_BODY, 11832, "Eigenaars harnas", armour);
        putOwner(OWNER_LEGS, 11834, "Eigenaars beenplaten", armour);
        putOwner(OWNER_SHIELD, 21895, "Eigenaars schild", armour);
        putOwner(OWNER_AMULET, 19553, "Eigenaars amulet", amulet);
    }

    private static void putOwner(int id, int baseId, String name, int[] bonuses) {
        ItemDefinition.definitions.put(id, ItemDefinition.customCopy(ItemDefinition.forId(baseId), id, name,
                "Alleen voor de eigenaar van RELOAD.", 1, bonuses));
    }

    public static boolean isOwnerItem(int itemId) {
        for (int id : OWNER_SET) {
            if (id == itemId) {
                return true;
            }
        }
        return false;
    }

    public static boolean isOwner(Player player) {
        return player.getRights() == com.elvarg.game.model.rights.PlayerRights.OWNER;
    }

    /**
     * One hit, one kill: an owner wielding the owner's blade.
     */
    public static boolean hasOwnerStrike(Player player) {
        return isOwner(player) && player.getEquipment().getItems()[
                com.elvarg.game.model.container.impl.Equipment.WEAPON_SLOT].getId() == OWNER_WEAPON;
    }

    private static void put(int id, int baseId, String name, String examine, int value, boolean stackable) {
        ItemDefinition base = ItemDefinition.forId(baseId);
        ItemDefinition.definitions.put(id, ItemDefinition.customMisc(base, id, name, examine, value, stackable));
    }

    /** Drop rate % a following pet gives. */
    public static int getPetDropRate(int petNpcId) {
        switch (petNpcId) {
            case PET_BRIX_NPC:
                return 10;
            case PET_GLOEDJE_NPC:
                return 5;
            case PET_MINI_RELOADER_NPC:
                return 15;
            default:
                return 0;
        }
    }

    /**
     * Handles the first click option ("Open", "Drink") of a custom item.
     *
     * @return true if the item was a custom item with an action.
     */
    public static boolean handleFirstClick(Player player, int itemId) {
        if (itemId == MYSTERY_BOX) {
            openMysteryBox(player);
            return true;
        }
        if (itemId == LUCK_POTION) {
            player.getInventory().delete(LUCK_POTION, 1);
            CustomProgress.addLuckMinutes(player, LUCK_POTION_MINUTES);
            player.getPacketSender().sendMessage("@gre@Je voelt je gelukkig: +" + LUCK_POTION_DROP_RATE
                    + "% drop rate, nog " + CustomProgress.getLuckMinutesLeft(player) + " minuten.");
            return true;
        }
        return false;
    }

    private static void openMysteryBox(Player player) {
        if (player.getInventory().getFreeSlots() < 1) {
            player.getPacketSender().sendMessage("Maak eerst een plek vrij in je inventory.");
            return;
        }
        player.getInventory().delete(MYSTERY_BOX, 1);
        CustomProgress.increment(player, CustomProgress.KEY_BOXES_OPENED);

        // The box scales with the highest tier the player has unlocked.
        CustomTier tier = CustomContent.highestUnlocked(player);
        ThreadLocalRandom random = ThreadLocalRandom.current();
        int roll = random.nextInt(1000);

        if (roll < 10) {
            // 1%: a pet
            int petItem = PET_ITEMS[random.nextInt(PET_ITEMS.length)];
            give(player, new Item(petItem, 1));
            CustomProgress.logItem(player, petItem);
            player.getPacketSender().sendMessage("@red@De kist beweegt... er zit een huisdier in: "
                    + ItemDefinition.forId(petItem).getName() + "!");
        } else if (roll < 110) {
            // 10%: a gear piece of your highest tier
            int slot = random.nextInt(CustomTier.SLOT_NAMES.length);
            give(player, new Item(tier.getItemId(slot), 1));
            CustomProgress.logItem(player, tier.getItemId(slot));
            player.getPacketSender().sendMessage("@red@Jackpot! De kist bevat: " + tier.getItemName(slot) + "!");
        } else if (roll < 260) {
            give(player, new Item(LUCK_POTION, 1));
            player.getPacketSender().sendMessage("De kist bevat een geluksdrank.");
        } else if (roll < 650) {
            int shards = (20 + random.nextInt(40)) * tier.number();
            give(player, new Item(SHARD, shards));
            player.getPacketSender().sendMessage("De kist bevat " + shards + " Reload scherven.");
        } else {
            int coins = (250_000 + random.nextInt(250_000)) * tier.number();
            give(player, new Item(COINS, coins));
            player.getPacketSender().sendMessage("De kist bevat " + coins + " coins.");
        }
    }

    /**
     * Does loot come to this player by itself? True with a Verzamelaar in
     * the inventory or the owner amulet on, unless switched off.
     */
    public static boolean hasCollector(Player player) {
        if (CustomProgress.get(player, CustomProgress.KEY_COLLECT_OFF) > 0) {
            return false;
        }
        return player.getInventory().contains(COLLECTOR)
                || player.getEquipment().getItems()[com.elvarg.game.model.container.impl.Equipment.AMULET_SLOT]
                        .getId() == OWNER_AMULET;
    }

    /**
     * Tries to collect a drop for the player: into the inventory, or into
     * the bank when the inventory has no room.
     *
     * @return true if the item was collected, false if it has to go on the ground.
     */
    public static boolean collect(Player player, Item item) {
        if (!hasCollector(player)) {
            return false;
        }
        boolean stackable = item.getDefinition().isStackable();
        boolean fitsInventory = stackable
                ? (player.getInventory().contains(item.getId()) || player.getInventory().getFreeSlots() >= 1)
                : player.getInventory().getFreeSlots() >= item.getAmount();
        if (fitsInventory) {
            player.getInventory().add(item);
            return true;
        }
        com.elvarg.game.model.container.impl.Bank bank = player.getBank(
                com.elvarg.game.model.container.impl.Bank.getTabForItem(player, item.getId()));
        if (bank != null && (bank.contains(item.getId()) || bank.getFreeSlots() >= 1)) {
            bank.add(item, false);
            player.getPacketSender().sendMessage("Inventory vol: " + item.getDefinition().getName()
                    + " is naar je bank gegaan.");
            return true;
        }
        return false;
    }

    /** Adds to the inventory, or drops/banks it when the inventory is full. */
    public static void give(Player player, Item item) {
        player.getInventory().forceAdd(player, item);
    }
}
