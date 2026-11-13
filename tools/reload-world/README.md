# RELOAD wereld

Eigen wereld van 256 x 256 tegels (4 x 4 mapregio's) voor RELOAD RSPS, los van de bestaande RuneScape-kaart.
Ligt op x 3840-4095, y 1664-1919. De oude wereld blijft gewoon bestaan.

![preview](preview.png)

## Zones

| Zone | Wat staat er | Commando |
|---|---|---|
| Home (midden) | plein met fontein, bank, winkel, drie huizen, marktkramen, altaar | `::zone home`, `::zone bank` |
| Bos (oost) | bomen, eiken, esdoorns, taxussen, magic trees, meer met wilgen en visplekken | `::zone forest`, `::zone lake`, `::zone magictrees` |
| Mijn (noordoost) | koper t/m runite, oven, aambeelden, bankkist | `::zone mine`, `::zone smithy` |
| Vulkaan (noord) | ember demons, hellhounds, fire giants, draken; drie eigen bazen | `::zone volcano`, `::zone magmaroth`, `::zone venomqueen`, `::zone frostclaw` |
| Kust (zuid) | jungle, strand, rock crabs, zee-visplekken, trainingsarena met sparring giants en brutes | `::zone beach`, `::train` |
| Woestenij (west) | PvP-zone (telt als Wilderness level 20), chaos altar | `::zone pvp`, `::zone chaosaltar` |

`::zone` zonder naam geeft de lijst. Ontwikkelaars kunnen ook `::tele 3968 1787`.

## Custom content

Alles is een herkleurde kopie van een bestaand item of monster (de client kleurt het model om), dus er zijn geen nieuwe modellen nodig.
De lijst staat in `content.py`; `python3 tools/reload-world/content.py` schrijft daaruit de server- en clientbestanden.

**Route:** trainingsarena (`::train`) -> ember demons op de vulkaan -> de drie bazen.

| Set | Stukken | Komt van |
|---|---|---|
| Ember (oranje, niveau 40) | helm, platebody, platelegs, kiteshield, scimitar | sparring brutes, ember demons, bazen |
| Magma (rood, melee) | helm, chestplate, tassets, boots, whip, godsword | Magmaroth |
| Venom (groen, ranged) | helm, chestplate, chainskirt, bow | Venomqueen |
| Tidal (blauw, magic) | hood, robetop, robeskirt, staff | Frostclaw |
| Reload (paars) | cape, amulet | zeer zeldzaam bij alle drie de bazen |

| Baas | Plek | Gevecht |
|---|---|---|
| Magmaroth | krater | bijt, vuuradem, elke vijfde aanval een uitbarsting op jouw tegel en eromheen: wegstappen |
| Venomqueen | oostelijke arena | wisselt melee en magic, vergiftigt, web (vastzetten), zuigt prayer weg |
| Frostclaw | westelijke arena | zware melee, vorstgolf die je bevriest, geneest zichzelf onder half leven |

**Prayer: Reload's Wrath** (level 80, eigen knop rechtsonder in het prayerboek): 30% meer schade met melee, ranged en magic, 25% meer accuracy en defence. Vervangt Piety, Rigour en Augury en kost 1 prayerpunt per 1,2 seconde.

**Testen als owner:** `::reloadgear all` (of `ember`, `magma`, `venom`, `tidal`, `extra`) zet de items in je inventory.

Nieuwe bestanden: `items_reload.json`, `npc_defs_reload.json`, `npc_drops_reload.json` (server) en `ReloadCustoms.java` (client). Item-id's 30000+, NPC-id's 15000+.

## Home vast instellen

Nieuwe spelers, doodgaan en de home-teleport gebruiken `GameConstants.DEFAULT_LOCATION`. Zet die op:

```java
public static final Location DEFAULT_LOCATION = new Location(3968, 1787);
```

Dat is bewust niet automatisch gedaan, omdat de home lokaal al is aangepast.

## Hoe het werkt

- Server: 32 mapbestanden in `ElvargServer/data/clipping/maps/3700.dat` t/m `3731.dat`, 16 regio's toegevoegd aan `data/clipping/map_index`.
- Client: dezelfde bestanden los in `ElvargClient/Cache/reload_maps/`. `ResourceProvider` leest die map eerst; de cache zelf is niet aangepast.
- NPC's: `ElvargServer/data/definitions/npc_spawns_reload_world.json`, wordt na `npc_spawns.json` ingeladen.
- Coordinaten en PvP-grenzen: `ReloadWorld.java` (gegenereerd).

## Aanpassen en opnieuw genereren

```
pip install numpy scipy pillow
python3 tools/reload-world/generate.py
```

Alles staat in `generate.py`: kustlijn, biomen, wegen, gebouwen, objecten en spawns. Dezelfde `SEED` geeft exact dezelfde bestanden.
De gebouwen in het dorp zijn uit Edgeville gekopieerd (`stamp(...)`); met dezelfde functie is elk bestaand gebouw over te nemen.

## Bekend

- De kleuren van de custom items en bazen zijn nog niet in de client bekeken; tint en accent per set staan bovenin `content.py`.
- De sparring giants en brutes slaan nauwelijks terug en hebben veel levens: bedoeld om op te trainen.

- De kustlijn en lavaranden bestaan uit hele tegels, dus ze zijn hoekig van dichtbij.
- De wereldkaart (het kaart-icoon) kent dit gebied niet.
- Ladders in de gekopieerde huizen doen alleen iets als de server die ladder-id's afhandelt.
