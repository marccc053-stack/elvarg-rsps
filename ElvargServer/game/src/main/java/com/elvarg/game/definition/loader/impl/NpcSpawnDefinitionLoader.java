package com.elvarg.game.definition.loader.impl;

import java.io.File;
import java.io.FileReader;

import com.elvarg.game.GameConstants;
import com.elvarg.game.World;
import com.elvarg.game.definition.NpcSpawnDefinition;
import com.elvarg.game.definition.loader.DefinitionLoader;
import com.elvarg.game.entity.impl.npc.NPC;
import com.google.gson.Gson;

public class NpcSpawnDefinitionLoader extends DefinitionLoader {

    @Override
    public void load() throws Throwable {
        FileReader reader = new FileReader(file());
        NpcSpawnDefinition[] defs = new Gson().fromJson(reader, NpcSpawnDefinition[].class);
        for (NpcSpawnDefinition def : defs) {
            NPC npc = NPC.create(def.getId(), def.getPosition());
            npc.getMovementCoordinator().setRadius(def.getRadius());
            npc.setFace(def.getFacing());
            World.getAddNPCQueue().add(npc);
        }
        reader.close();

        // Spawns of the RELOAD island live in their own file (written by tools/reload-world/generate.py)
        File extra = new File(GameConstants.DEFINITIONS_DIRECTORY + "npc_spawns_reload_world.json");
        if (extra.exists()) {
            try (FileReader extraReader = new FileReader(extra)) {
                for (NpcSpawnDefinition def : new Gson().fromJson(extraReader, NpcSpawnDefinition[].class)) {
                    NPC npc = NPC.create(def.getId(), def.getPosition());
                    npc.getMovementCoordinator().setRadius(def.getRadius());
                    npc.setFace(def.getFacing());
                    World.getAddNPCQueue().add(npc);
                }
            }
        }
    }

    @Override
    public String file() {
        return GameConstants.DEFINITIONS_DIRECTORY + "npc_spawns.json";
    }

}
