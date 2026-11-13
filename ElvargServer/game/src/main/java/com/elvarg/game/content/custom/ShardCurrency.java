package com.elvarg.game.content.custom;

import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.container.shop.currency.ShopCurrency;

/**
 * Shop currency: Reload scherven from the inventory.
 */
public class ShardCurrency implements ShopCurrency {

    @Override
    public String getName() {
        return "Reload scherven";
    }

    @Override
    public int getAmountForPlayer(Player player) {
        return player.getInventory().getAmount(CustomItems.SHARD);
    }

    @Override
    public void decrementForPlayer(Player player, int amount) {
        player.getInventory().delete(CustomItems.SHARD, amount);
    }

    @Override
    public void incrementForPlayer(Player player, int amount) {
        player.getInventory().add(CustomItems.SHARD, amount);
    }
}
