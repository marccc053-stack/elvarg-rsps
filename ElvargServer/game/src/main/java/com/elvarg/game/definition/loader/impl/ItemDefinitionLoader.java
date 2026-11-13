package com.elvarg.game.definition.loader.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.ItemDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;

public class ItemDefinitionLoader extends DefinitionLoader {

    @Override
    public void load() throws Throwable {
    	ItemDefinition.definitions.clear();
        FileReader reader = new FileReader(file());
        ItemDefinition[] defs = new Gson().fromJson(reader, ItemDefinition[].class);
        for (ItemDefinition def : defs) {
            ItemDefinition.definitions.put(def.getId(), def);
        }
        reader.close();

        // Custom RELOAD items live in their own file (written by tools/reload-world/content.py)
        File extra = new File(GameConstants.DEFINITIONS_DIRECTORY + "items_reload.json");
        if (extra.exists()) {
            try (FileReader extraReader = new FileReader(extra)) {
                for (ItemDefinition def : new Gson().fromJson(extraReader, ItemDefinition[].class)) {
                    ItemDefinition.definitions.put(def.getId(), def);
                }
            }
        }
    }

    @Override
    public String file() {
        return GameConstants.DEFINITIONS_DIRECTORY + "items.json";
    }
}
