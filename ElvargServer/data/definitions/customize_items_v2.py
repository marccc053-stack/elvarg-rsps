#!/usr/bin/env python3
"""
customize_items.py - past custom stats toe op items.json

Alleen bonuses worden aangepast. Namen en examines blijven staan,
want die leest de client uit de cache en niet van de server.

Gebruik:
    python customize_items.py items.json

Maakt items.json.bak als backup (alleen als die nog niet bestaat).

Bonuses-array (14 posities):
  0  stab attack        7  crush defence
  1  slash attack       8  magic defence
  2  crush attack       9  range defence
  3  magic attack      10  strength
  4  range attack      11  ranged strength
  5  stab defence      12  magic damage
  6  slash defence     13  prayer
"""

import json
import shutil
import sys
import os

# ---------------------------------------------------------------
# item_id: {bonus_index: waarde}
# Alleen de posities die je noemt worden overschreven.
# ---------------------------------------------------------------

CUSTOM = {
    # ---- Tier 1: starter ----
    4587: {1: 75, 10: 70},                    # dragon scimitar
    1305: {0: 70, 1: 75, 10: 72},             # dragon longsword

    # ---- Tier 2: mid ----
    4151: {1: 110, 10: 105},                  # abyssal whip
    13263: {2: 115, 10: 112},                 # abyssal bludgeon
    13576: {2: 105, 10: 100},                 # dragon warhammer

    # ---- Tier 3: endgame ----
    11802: {1: 165, 2: 100, 10: 160},         # armadyl godsword
    13652: {0: 145, 1: 145, 10: 90},          # dragon claws
    21003: {2: 175, 10: 170},                 # elder maul
    22324: {0: 150, 10: 100},                 # ghrazi rapier
    11838: {1: 140, 2: 132, 10: 132},         # saradomin sword

    # ---- Ranged ----
    11785: {4: 120},                          # armadyl crossbow

    # ---- Magic ----
    11791: {3: 40, 12: 20},                   # staff of the dead
}

BONUS_NAMES = [
    "stab att", "slash att", "crush att", "magic att", "range att",
    "stab def", "slash def", "crush def", "magic def", "range def",
    "strength", "ranged str", "magic dmg", "prayer",
]


def main():
    if len(sys.argv) < 2:
        print("Gebruik: python customize_items.py <pad naar items.json>")
        sys.exit(1)

    path = sys.argv[1]
    if not os.path.isfile(path):
        print(f"Bestand niet gevonden: {path}")
        sys.exit(1)

    backup = path + ".bak"
    if not os.path.exists(backup):
        shutil.copy2(path, backup)
        print(f"Backup gemaakt: {backup}")
    else:
        print(f"Backup bestaat al, niet overschreven: {backup}")

    with open(path, "r", encoding="utf-8") as f:
        items = json.load(f)

    index = {i["id"]: i for i in items if "id" in i}

    changed = 0
    missing = []

    for item_id, spec in CUSTOM.items():
        item = index.get(item_id)
        if item is None:
            missing.append(item_id)
            continue

        bonuses = item.get("bonuses")
        if not isinstance(bonuses, list) or len(bonuses) < 14:
            bonuses = [0.0] * 14

        old = {p: bonuses[p] for p in spec}
        for pos, value in spec.items():
            bonuses[pos] = float(value)
        item["bonuses"] = bonuses

        changed += 1
        parts = [f"{BONUS_NAMES[p]} {old[p]:g}->{v}" for p, v in sorted(spec.items())]
        print(f"  {item_id:>6}  {item.get('name', '?'):<28}  " + ", ".join(parts))

    with open(path, "w", encoding="utf-8") as f:
        json.dump(items, f, indent=2, ensure_ascii=False)

    print()
    print(f"{changed} items aangepast, weggeschreven naar {path}")
    if missing:
        print(f"Niet gevonden in items.json: {missing}")
    print("Herstart de server om de nieuwe stats te laden.")


if __name__ == "__main__":
    main()
