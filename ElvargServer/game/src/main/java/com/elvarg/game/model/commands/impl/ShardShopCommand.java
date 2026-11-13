package com.elvarg.game.model.commands.impl;

import com.elvarg.game.content.custom.CustomItems;
import com.elvarg.game.content.custom.ShardCurrency;
import com.elvarg.game.model.Item;
import com.elvarg.game.model.container.shop.Shop;
import com.elvarg.game.model.container.shop.ShopManager;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.commands.Command;

/**
 * ::shop - opens the shop that sells for Reload scherven.
 */
public class ShardShopCommand implements Command {

    private static final int SHOP_ID = 777;

    @Override
    public void execute(Player player, String command, String[] parts) {
        Shop shop = ShopManager.shops.get(SHOP_ID);
        if (shop == null) {
            shop = new Shop(SHOP_ID, "Scherven winkel", new Item[]{
                    new Item(CustomItems.MYSTERY_BOX, 100000),
                    new Item(CustomItems.LUCK_POTION, 100000),
                    new Item(CustomItems.COLLECTOR, 100000),
                    new Item(CustomItems.PET_TWEELING_ITEM, 100000)}, new ShardCurrency());
            ShopManager.shops.put(SHOP_ID, shop);
        }
        ShopManager.open(player, SHOP_ID);
    }

    @Override
    public boolean canUse(Player player) {
        return true;
    }
}
