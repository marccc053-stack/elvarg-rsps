# RELOAD eiland

Eigen eiland van 256 x 256 tegels (4 x 4 mapregio's) op x 3840-4095, y 1664-1919, naast de bestaande wereld en de tier-zones.
Het sluit aan op het custom-systeem in `com.elvarg.game.content.custom` (tiers, scherven, mysteriekisten, kill counts).

![preview](preview.png)

## Wat er staat

| Plek | Inhoud | Commando |
|---|---|---|
| Dorp (midden) | plein met fontein, bank, winkel, drie huizen, marktkramen, altaar | `::zone home`, `::zone bank` |
| Bos (oost) | bomen t/m magic, meer met visplekken | `::zone forest`, `::zone lake`, `::zone magic_trees` |
| Mijn (noordoost) | koper t/m runite, oven, aambeelden, bankkist | `::zone mine`, `::zone smithy` |
| Vulkaan (noord) | demonen, hellhounds, fire giants, draken; drie wereldbazen | `::zone volcano`, `::zone vorstklauw`, `::zone webkoningin`, `::zone magmaroth` |
| Kust (zuid) | jungle, strand, rock crabs, trainingsarena | `::zone beach`, `::train` |
| Woestenij (west) | PvP-zone (telt als Wilderness level 20), chaos altar | `::zone pvp`, `::zone chaos_altar` |

`::zone` of `::eiland` zonder naam geeft de lijst.

## Wereldbazen

Los van de tier-bazen (`ReloadBoss`). Ze geven munten, scherven en 1 op 4 een mysteriekist, en tellen mee in de kill counts.

| Baas | Kracht | Gevecht |
|---|---|---|
| Vorstklauw | als tier 2 (4.000 hp) | zware melee, vorstgolf die je vastvriest, geneest zichzelf onder half leven |
| Webkoningin | als tier 3 (8.000 hp) | wisselt melee en magic, vergiftigt, web (vastzetten), zuigt prayer weg |
| Magmaroth | als tier 4 (15.000 hp) | bijt, vuuradem, elke vijfde aanval een uitbarsting op jouw tegel: wegstappen |

## Trainingsarena

Tien sparringreuzen (600 hp) en vijf sparringbruten (2.500 hp) die nauwelijks terugslaan en snel terugkomen. Ze geven botten, munten en af en toe een scherf.

## Prayer: Reload's Wrath

Level 80, eigen knop rechtsonder in het prayerboek. 30% meer schade met melee, ranged en magic, 25% meer accuracy en defence.
Vervangt Piety, Rigour en Augury en kost 1 prayerpunt per 1,2 seconde. Code: `ReloadPrayer`.

## Alleen eigen uitrusting (staat uit)

`ReloadRules.STANDARD_GEAR_DISABLED` staat op `false`. Op `true` vallen standaard wapens en harnassen niet meer bij monsters,
verdwijnen ze uit de shops, kun je ze niet meer aantrekken en geeft `::starter` het Gloed wapen in plaats van een whip.
Het staat uit omdat de tiers nu alleen melee-uitrusting hebben: er zouden geen bogen, staven, laarzen, handschoenen, capes of ringen overblijven
en zeven shops raken (bijna) leeg.

## Home naar het eiland

De home en de vier home-npc's volgen `GameConstants.DEFAULT_LOCATION`. Voor het dorp op het eiland:

```java
public static final Location DEFAULT_LOCATION = new Location(3968, 1787);
```

## Bestanden

- Map: `ElvargServer/data/clipping/maps/3700.dat` t/m `3731.dat` en 16 regio's in `map_index`; voor de client dezelfde bestanden in `ElvargClient/Cache/reload_maps/` (gelezen door `ResourceProvider`).
- Spawns van gewone npc's: `npc_spawns_reload_world.json`. Wereldbazen en sparring-npc's: `ReloadWorldContent` (server) en `CustomDefinitions` (client, id's 15120 t/m 15132).
- Coordinaten: `ReloadWorld.java` (gegenereerd).

## Opnieuw genereren

```
pip install numpy scipy pillow
python3 tools/reload-world/generate.py
```

Kustlijn, biomen, wegen, gebouwen, objecten en spawns staan in `generate.py`. Dezelfde `SEED` geeft exact dezelfde bestanden.

## Bekend

- Kust en lavaranden bestaan uit hele tegels en zijn hoekig van dichtbij.
- De wereldkaart (het kaart-icoon) kent dit gebied niet.
- Kleuren van de wereldbazen en sparring-npc's staan in `CustomDefinitions` en zijn nog niet in de client bekeken.
