#!/usr/bin/env python3
"""
customize_items.py - past custom namen, examines en stats toe op items.json

Gebruik:
    python customize_items.py items.json

Maakt items.json.bak als backup en schrijft items.json opnieuw weg.

Bonuses-array (14 posities):
  0  stab attack        7  crush defence
  1  slash attack       8  magic defence
  2  crush attack       9  range defence
  3  magic attack      10  strength
  4  range attack      11  ranged strength
  5  stab defence      12  magic damage
  6  slash defence     13  prayer

Alleen de velden die je hieronder opgeeft worden aangepast.
Alles wat je weglaat blijft ongemoeid.
"""

import json
import shutil
import sys
import os

# ---------------------------------------------------------------
# Hier pas je aan. Formaat:
#   item_id: {"name": ..., "examine": ..., "bonuses": {index: waarde}}
# ---------------------------------------------------------------

CUSTOM = {
    # ---- Tier 1: starter ----
    4587: {
        "name": "Reload Scimitar",
        "examine": "Het eerste wapen van elke Reload-speler.",
        "bonuses": {1: 75, 10: 70},
    },
    1305: {
        "name": "Reload Langzwaard",
        "examine": "Langer bereik, tragere klap.",
        "bonuses": {0: 70, 1: 75, 10: 72},
    },

    # ---- Tier 2: mid ----
    4151: {
        "name": "Reload Zweep",
        "examine": "Uit de diepte gehaald en opnieuw gesmeed.",
        "bonuses": {1: 110, 10: 105},
    },
    13263: {
        "name": "Reload Knots",
        "examine": "Verplettert alles wat te dicht bij komt.",
        "bonuses": {2: 115, 10: 112},
    },
    13576: {
        "name": "Reload Hamer",
        "examine": "Breekt harnas als papier.",
        "bonuses": {2: 105, 10: 100},
    },

    # ---- Tier 3: endgame ----
    11802: {
        "name": "Reload Godsword",
        "examine": "Gesmeed voor wie de rest al verslagen heeft.",
        "bonuses": {1: 165, 2: 100, 10: 160},
    },
    13652: {
        "name": "Reload Klauwen",
        "examine": "Vier sneden in een oogwenk.",
        "bonuses": {0: 145, 1: 145, 10: 90},
    },
    21003: {
        "name": "Reload Moker",
        "examine": "Traag, maar niemand staat daarna nog rechtop.",
        "bonuses": {2: 175, 10: 170},
    },
    22324: {
        "name": "Reload Rapier",
        "examine": "Precisie boven kracht.",
        "bonuses": {0: 150, 10: 100},
    },
    11838: {
        "name": "Reload Kruisvaarderszwaard",
        "examine": "Geneest de drager bij elke rake klap.",
        "bonuses": {1: 140, 2: 132, 10: 132},
    },

    # ---- Ranged ----
    11785: {
        "name": "Reload Kruisboog",
        "examine": "Doorboort op grote afstand.",
        "bonuses": {4: 120},
    },

    # ---- Magic ----
    11791: {
        "name": "Reload Staf",
        "examine": "Bundelt de magie van de hele server.",
        "bonuses": {3: 40, 12: 20},
    },
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

    index = {}
    for item in items:
        if "id" in item:
            index[item["id"]] = item

    changed = 0
    missing = []

    for item_id, spec in CUSTOM.items():
        item = index.get(item_id)
        if item is None:
            missing.append(item_id)
            continue

        old_name = item.get("name", "?")

        if "name" in spec:
            item["name"] = spec["name"]
        if "examine" in spec:
            item["examine"] = spec["examine"]

        if "bonuses" in spec:
            bonuses = item.get("bonuses")
            if not isinstance(bonuses, list) or len(bonuses) < 14:
                bonuses = [0.0] * 14
            for pos, value in spec["bonuses"].items():
                bonuses[pos] = float(value)
            item["bonuses"] = bonuses

        changed += 1
        detail = ""
        if "bonuses" in spec:
            parts = [f"{BONUS_NAMES[p]}={v}" for p, v in sorted(spec["bonuses"].items())]
            detail = "  (" + ", ".join(parts) + ")"
        print(f"  {item_id:>6}  {old_name}  ->  {item.get('name')}{detail}")

    with open(path, "w", encoding="utf-8") as f:
        json.dump(items, f, indent=2, ensure_ascii=False)

    print()
    print(f"{changed} items aangepast, weggeschreven naar {path}")
    if missing:
        print(f"Niet gevonden in items.json: {missing}")
    print("Herstart de server, of gebruik ::reloaditems als dat commando werkt.")


if __name__ == "__main__":
    main()
