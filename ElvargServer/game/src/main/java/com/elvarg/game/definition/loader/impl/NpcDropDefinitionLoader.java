package com.elvarg.game.definition.loader.impl;

import com.elvarg.game.GameConstants;
import com.elvarg.game.definition.NpcDropDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;

public class NpcDropDefinitionLoader extends DefinitionLoader {

    @Override
    public void load() throws Throwable {
    	NpcDropDefinition.definitions.clear();
        FileReader reader = new FileReader(file());
        NpcDropDefinition[] defs = new Gson().fromJson(reader, NpcDropDefinition[].class);
        for (NpcDropDefinition def : defs) {
            for (int npcId : def.getNpcIds()) {
                NpcDropDefinition.definitions.put(npcId, def);
            }
        }
        reader.close();

        // Drops of the custom RELOAD NPCs live in their own file (written by tools/reload-world/content.py)
        File extra = new File(GameConstants.DEFINITIONS_DIRECTORY + "npc_drops_reload.json");
        if (extra.exists()) {
            try (FileReader extraReader = new FileReader(extra)) {
                for (NpcDropDefinition def : new Gson().fromJson(extraReader, NpcDropDefinition[].class)) {
                    for (int npcId : def.getNpcIds()) {
                        NpcDropDefinition.definitions.put(npcId, def);
                    }
                }
            }
        }
    }

    @Override
    public String file() {
        return GameConstants.DEFINITIONS_DIRECTORY + "npc_drops.json";
    }
}
