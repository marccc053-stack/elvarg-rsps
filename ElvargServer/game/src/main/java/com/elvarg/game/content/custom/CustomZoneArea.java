package com.elvarg.game.content.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.elvarg.game.entity.impl.Mobile;
import com.elvarg.game.entity.impl.player.Player;
import com.elvarg.game.model.Boundary;
import com.elvarg.game.model.areas.Area;

/**
 * The custom tier zones: multi combat, and no items are lost on death.
 */
public class CustomZoneArea extends Area {

    public CustomZoneArea() {
        super(boundaries());
    }

    private static List<Boundary> boundaries() {
        List<Boundary> list = new ArrayList<>();
        for (CustomTier tier : CustomTier.values()) {
            list.add(tier.getBoundary());
        }
        return list;
    }

    @Override
    public String getName() {
        return "RELOAD tier zone";
    }

    @Override
    public boolean isMulti(Mobile character) {
        return true;
    }

    @Override
    public boolean dropItemsOnDeath(Player player, Optional<Player> killer) {
        return false;
    }
}
