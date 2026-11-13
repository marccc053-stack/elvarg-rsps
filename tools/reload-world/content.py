#!/usr/bin/env python3
"""
RELOAD custom content: gear, monsters, bosses.

Everything is a recoloured ("tinted") copy of an existing item or NPC, so no new models are needed.
One table here feeds both sides:

  server  ElvargServer/data/definitions/items_reload.json, npc_defs_reload.json, npc_drops_reload.json
          .../game/content/reloadworld/ReloadIds.java
  client  ElvargClient/.../cache/def/ReloadCustoms.java

Run:  python3 tools/reload-world/content.py      (generate.py imports the ids for the spawns)
Hue is 0-63 (0 red, 5 orange, 10 yellow, 21 green, 32 cyan, 43 blue, 53 purple), saturation 0-7.
"""
import copy, json, os

ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
DEFS = os.path.join(ROOT, 'ElvargServer', 'data', 'definitions')

# bonus order: stab, slash, crush, magic, range | def stab, slash, crush, magic, range | str, ranged str, magic dmg, prayer
EMBER, MAGMA, VENOM, TIDAL, RELOAD = (5, 6, 9), (0, 7, 6), (21, 6, 15), (36, 6, 44), (53, 6, 8)   # (hue, sat, accent hue)

# id, constant, name, base item, multiplier on positive bonuses, extra bonuses {index: add}, tint, examine
ITEMS = [
    (30000, 'EMBER_HELM', 'Ember helm', 1163, 1.5, {}, EMBER, 'Still warm from the forge.'),
    (30001, 'EMBER_PLATEBODY', 'Ember platebody', 1127, 1.5, {}, EMBER, 'Still warm from the forge.'),
    (30002, 'EMBER_PLATELEGS', 'Ember platelegs', 1079, 1.5, {}, EMBER, 'Still warm from the forge.'),
    (30003, 'EMBER_KITESHIELD', 'Ember kiteshield', 1201, 1.5, {}, EMBER, 'Still warm from the forge.'),
    (30004, 'EMBER_SCIMITAR', 'Ember scimitar', 1333, 1.5, {}, EMBER, 'The edge glows faintly.'),

    (30010, 'MAGMA_HELM', 'Magma helm', 10828, 1.35, {10: 3}, MAGMA, 'Forged in the crater of Magmaroth.'),
    (30011, 'MAGMA_CHESTPLATE', 'Magma chestplate', 11832, 1.35, {10: 2}, MAGMA, 'Forged in the crater of Magmaroth.'),
    (30012, 'MAGMA_TASSETS', 'Magma tassets', 11834, 1.35, {10: 2}, MAGMA, 'Forged in the crater of Magmaroth.'),
    (30013, 'MAGMA_BOOTS', 'Magma boots', 11836, 1.35, {10: 4}, MAGMA, 'Forged in the crater of Magmaroth.'),
    (30014, 'MAGMA_WHIP', 'Magma whip', 4151, 1.35, {}, MAGMA, 'It cracks like cooling rock.'),
    (30015, 'MAGMA_GODSWORD', 'Magma godsword', 11802, 1.3, {}, MAGMA, 'Too hot to hold for long.'),

    (30020, 'VENOM_HELM', 'Venom helm', 11826, 1.4, {11: 2}, VENOM, 'Spun from the silk of the Venomqueen.'),
    (30021, 'VENOM_CHESTPLATE', 'Venom chestplate', 11828, 1.4, {11: 4}, VENOM, 'Spun from the silk of the Venomqueen.'),
    (30022, 'VENOM_CHAINSKIRT', 'Venom chainskirt', 11830, 1.4, {11: 3}, VENOM, 'Spun from the silk of the Venomqueen.'),
    (30023, 'VENOM_BOW', 'Venom bow', 861, 1.8, {11: 20}, VENOM, 'Strung with spider silk.'),

    (30030, 'TIDAL_HOOD', 'Tidal hood', 4708, 1.4, {12: 3}, TIDAL, 'Cold to the touch.'),
    (30031, 'TIDAL_ROBETOP', 'Tidal robetop', 4712, 1.4, {12: 4}, TIDAL, 'Cold to the touch.'),
    (30032, 'TIDAL_ROBESKIRT', 'Tidal robeskirt', 4714, 1.4, {12: 3}, TIDAL, 'Cold to the touch.'),
    (30033, 'TIDAL_STAFF', 'Tidal staff', 11791, 1.3, {12: 5}, TIDAL, 'Frost gathers around the tip.'),

    (30040, 'RELOAD_CAPE', 'Reload cape', 6568, 2.0, {10: 6, 11: 4, 12: 3, 13: 4}, RELOAD, 'Proof that all three bosses fell.'),
    (30041, 'RELOAD_AMULET', 'Reload amulet', 6585, 1.5, {}, RELOAD, 'It hums with stored power.'),
]

# id, constant, name, base npc, tint, scale (128 = normal), overrides for the server definition, client size override
NPCS = [
    (15000, 'MAGMAROTH', 'Magmaroth', 239, MAGMA, 140,
     dict(hitpoints=900, maxHit=45, combatLevel=525, respawn=40, statMult=1.25, aggressive=True), None),
    (15001, 'VENOMQUEEN', 'Venomqueen', 6504, VENOM, 128,
     dict(hitpoints=800, maxHit=50, combatLevel=510, respawn=40, statMult=1.15, aggressive=True, walkRadius=3), None),
    (15002, 'FROSTCLAW', 'Frostclaw', 6609, TIDAL, 128,
     dict(hitpoints=850, maxHit=55, combatLevel=515, respawn=40, statMult=1.15, aggressive=True, walkRadius=3), 4),
    (15003, 'SPARRING_GIANT', 'Sparring giant', 2098, EMBER, 110,
     dict(hitpoints=600, maxHit=1, combatLevel=10, respawn=5, statMult=0.2, aggressive=False, retreats=False), None),
    (15004, 'SPARRING_BRUTE', 'Sparring brute', 891, RELOAD, 135,
     dict(hitpoints=2500, maxHit=4, combatLevel=60, respawn=5, statMult=0.5, aggressive=False, retreats=False), None),
    (15005, 'EMBER_DEMON', 'Ember demon', 2005, EMBER, 128,
     dict(hitpoints=180, maxHit=14, combatLevel=120, respawn=20, statMult=1.3, aggressive=True), None),
]

I = {c: i for i, c, *_ in ITEMS}
N = {c: i for i, c, *_ in NPCS}
D = lambda item, lo=1, hi=1: {"itemId": item, "minAmount": lo, "maxAmount": hi}
COINS, BONES, BIG_BONES, DRAGON_BONES, SHARK, SUPER_RESTORE, PRAYER_POT = 995, 526, 532, 536, 385, 3024, 2434
EMBER_SET = [D(I[c]) for c in ('EMBER_HELM', 'EMBER_PLATEBODY', 'EMBER_PLATELEGS', 'EMBER_KITESHIELD', 'EMBER_SCIMITAR')]
JEWEL = [D(I['RELOAD_AMULET']), D(I['RELOAD_CAPE'])]
DROPS = [
    {"npcIds": [N['SPARRING_GIANT']], "rdtChance": 0, "alwaysDrops": [D(BONES)],
     "commonDrops": [D(COINS, 50, 400)], "veryRareDrops": EMBER_SET},
    {"npcIds": [N['SPARRING_BRUTE']], "rdtChance": 0, "alwaysDrops": [D(BIG_BONES)],
     "commonDrops": [D(COINS, 300, 1500)], "uncommonDrops": [D(SHARK, 1, 2)], "rareDrops": EMBER_SET},
    {"npcIds": [N['EMBER_DEMON']], "rdtChance": 64, "alwaysDrops": [D(BIG_BONES)],
     "commonDrops": [D(COINS, 1000, 5000), D(SHARK, 1, 3)], "uncommonDrops": [D(PRAYER_POT), D(565, 20, 60), D(560, 20, 60)],
     "rareDrops": EMBER_SET},
    {"npcIds": [N['MAGMAROTH']], "rdtChance": 32, "alwaysDrops": [D(DRAGON_BONES, 2, 2), D(COINS, 20000, 60000)],
     "commonDrops": [D(SHARK, 3, 6), D(SUPER_RESTORE, 1, 2)], "uncommonDrops": EMBER_SET,
     "rareDrops": [D(I[c]) for c in ('MAGMA_HELM', 'MAGMA_CHESTPLATE', 'MAGMA_TASSETS', 'MAGMA_BOOTS', 'MAGMA_WHIP')],
     "veryRareDrops": [D(I['MAGMA_GODSWORD'])] + JEWEL},
    {"npcIds": [N['VENOMQUEEN']], "rdtChance": 32, "alwaysDrops": [D(BIG_BONES, 2, 2), D(COINS, 20000, 60000)],
     "commonDrops": [D(SHARK, 3, 6), D(892, 50, 150)], "uncommonDrops": EMBER_SET + [D(11212, 20, 60)],
     "rareDrops": [D(I[c]) for c in ('VENOM_HELM', 'VENOM_CHESTPLATE', 'VENOM_CHAINSKIRT')],
     "veryRareDrops": [D(I['VENOM_BOW'])] + JEWEL},
    {"npcIds": [N['FROSTCLAW']], "rdtChance": 32, "alwaysDrops": [D(BIG_BONES, 2, 2), D(COINS, 20000, 60000)],
     "commonDrops": [D(SHARK, 3, 6), D(565, 50, 150), D(560, 50, 150)], "uncommonDrops": EMBER_SET + [D(SUPER_RESTORE, 1, 2)],
     "rareDrops": [D(I[c]) for c in ('TIDAL_HOOD', 'TIDAL_ROBETOP', 'TIDAL_ROBESKIRT')],
     "veryRareDrops": [D(I['TIDAL_STAFF'])] + JEWEL},
]


def main():
    items = {x['id']: x for x in json.load(open(os.path.join(DEFS, 'items.json')))}
    npcs = {x['id']: x for x in json.load(open(os.path.join(DEFS, 'npc_defs.json')))}
    assert max(items) < 30000 and max(npcs) < 15000

    out = []
    for iid, const, name, base, mult, extra, tint, examine in ITEMS:
        d = copy.deepcopy(items[base])
        d.update(id=iid, name=name, examine=examine, noteId=-1, noted=False, tradeable=True, dropable=True, sellable=True)
        b = [round(v * mult) if v > 0 else v for v in d['bonuses']]
        for k, v in extra.items(): b[k] += v
        d['bonuses'] = [float(v) for v in b]
        d['value'] = d['dropValue'] = int(max(d.get('value', 0), 100000) * mult)
        d.pop('bloodMoneyValue', None)
        out.append(d)
    json.dump(out, open(os.path.join(DEFS, 'items_reload.json'), 'w'), indent=1)

    out = []
    for nid, const, name, base, tint, scale, ov, csize in NPCS:
        d = copy.deepcopy(npcs[base]); ov = dict(ov)
        m = ov.pop('statMult')
        d['stats'] = [int(round(s * m)) if s > 1 else s for s in d['stats']]
        d.update(id=nid, name=name, attackable=True, **ov)
        out.append(d)
    json.dump(out, open(os.path.join(DEFS, 'npc_defs_reload.json'), 'w'), indent=1)
    json.dump(DROPS, open(os.path.join(DEFS, 'npc_drops_reload.json'), 'w'), indent=1)

    j = ['package com.elvarg.game.content.reloadworld;', '',
         '/**', ' * Ids of the custom RELOAD items and NPCs. GENERATED by tools/reload-world/content.py, do not edit by hand.', ' */',
         'public final class ReloadIds {', '']
    j += ['    public static final int %s = %d;' % (c, i) for i, c, *_ in ITEMS] + ['']
    j += ['    public static final int %s = %d;' % (c, i) for i, c, *_ in NPCS]
    j += ['', '    private ReloadIds() {', '    }', '}', '']
    open(os.path.join(ROOT, 'ElvargServer/game/src/main/java/com/elvarg/game/content/reloadworld/ReloadIds.java'), 'w').write('\n'.join(j))

    c = ['package com.runescape.cache.def;', '',
         '/**', ' * Custom RELOAD items and NPCs: tinted copies of existing ones.',
         ' * GENERATED by tools/reload-world/content.py, do not edit by hand.', ' */',
         'public final class ReloadCustoms {', '',
         '    /** {id, base id, hue, saturation, accent hue} */',
         '    private static final int[][] ITEMS = {']
    c += ['        {%d, %d, %d, %d, %d},' % (i, b, t[0], t[1], t[2]) for i, _, _, b, _, _, t, _ in ITEMS]
    c += ['    };', '    private static final String[] ITEM_NAMES = {'] + ['        "%s",' % n for _, _, n, *_ in ITEMS]
    c += ['    };', '', '    /** {id, base id, hue, saturation, accent hue, scale, combat level, size or -1} */',
          '    private static final int[][] NPCS = {']
    c += ['        {%d, %d, %d, %d, %d, %d, %d, %d},' % (i, b, t[0], t[1], t[2], s, ov['combatLevel'], -1 if cs is None else cs)
          for i, _, _, b, t, s, ov, cs in NPCS]
    c += ['    };', '    private static final String[] NPC_NAMES = {'] + ['        "%s",' % n for _, _, n, *_ in NPCS]
    c += ['''    };

    private static int find(int[][] table, int id) {
        for (int i = 0; i < table.length; i++)
            if (table[i][0] == id)
                return i;
        return -1;
    }

    public static boolean isItem(int id) {
        return find(ITEMS, id) != -1;
    }

    /** The cache item a custom item is copied from, or the id itself for normal items. */
    public static int baseItem(int id) {
        int i = find(ITEMS, id);
        return i == -1 ? id : ITEMS[i][1];
    }

    public static void applyItem(ItemDefinition def) {
        int i = find(ITEMS, def.id);
        if (i == -1)
            return;
        def.name = ITEM_NAMES[i];
        def.noted_item_id = -1;
        def.unnoted_item_id = -1;
        def.tintHue = ITEMS[i][2];
        def.tintSaturation = ITEMS[i][3];
        def.tintAccentHue = ITEMS[i][4];
    }

    public static boolean isNpc(int id) {
        return find(NPCS, id) != -1;
    }

    public static int baseNpc(int id) {
        int i = find(NPCS, id);
        return i == -1 ? id : NPCS[i][1];
    }

    public static void applyNpc(NpcDefinition def) {
        int i = find(NPCS, (int) def.interfaceType);
        if (i == -1)
            return;
        def.name = NPC_NAMES[i];
        def.tintHue = NPCS[i][2];
        def.tintSaturation = NPCS[i][3];
        def.tintAccentHue = NPCS[i][4];
        def.scaleXZ = def.scaleXZ * NPCS[i][5] / 128;
        def.scaleY = def.scaleY * NPCS[i][5] / 128;
        def.combatLevel = NPCS[i][6];
        if (NPCS[i][7] != -1)
            def.size = NPCS[i][7];
    }

    private ReloadCustoms() {
    }
}
''']
    open(os.path.join(ROOT, 'ElvargClient/src/main/java/com/runescape/cache/def/ReloadCustoms.java'), 'w').write('\n'.join(c))
    print('items', len(ITEMS), 'npcs', len(NPCS), 'drop tables', len(DROPS))


if __name__ == '__main__':
    main()
