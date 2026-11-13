#!/usr/bin/env python3
"""
RELOAD world generator.

Builds a 256x256 tile custom world (4x4 map regions) for the Elvarg base:
home town in the middle, forest/skilling east, volcano/bosses north,
tropical coast/arena south, PvP wasteland west.

Run from anywhere:  python3 tools/reload-world/generate.py
Outputs:
  ElvargServer/data/clipping/maps/37xx.dat      (terrain + object files, gzip)
  ElvargServer/data/clipping/map_index          (16 regions appended)
  ElvargClient/Cache/reload_maps/37xx.gz + map_index
  ElvargServer/data/definitions/npc_spawns_reload_world.json
  tools/reload-world/preview.png, zones.json
Deterministic: same SEED gives byte-identical files.
"""
import gzip, json, math, os, struct, sys
import numpy as np
from scipy import ndimage
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from maplib import ROOT, region, INDEX, enc_terrain, enc_objects, dec_terrain, dec_objects
from locs import LOCS
from content import N as CUSTOM_NPC          # ids of the custom monsters and bosses

SEED = 20261113
RX0, RY0 = 60, 26                 # south-west region of the 4x4 block
BX, BY = RX0 * 64, RY0 * 64       # 3840, 1664
W = 256
FIRST_FILE = 3700                 # 3700..3731, range the client accepts for object files
C = 128                           # centre tile (local)
TOWN_H = 30

rng = np.random.default_rng(SEED)

def noise(scale, seed):
    r = np.random.default_rng(SEED + seed)
    n = W // scale + 3
    g = r.random((n, n))
    out = ndimage.zoom(g, scale, order=3)[scale:scale + W, scale:scale + W]
    out = (out - out.min()) / (out.max() - out.min())
    return out

X, Y = np.meshgrid(np.arange(W), np.arange(W), indexing='ij')   # arrays indexed [x][y]
DX, DY = X - C, Y - C
DIST = np.hypot(DX, DY)
ANG = np.arctan2(DY, DX)

# ---------------------------------------------------------------- land mask
n_big, n_med, n_small = noise(48, 1), noise(20, 2), noise(8, 3)
coast_r = 83 + 11 * (n_big - 0.5) * 2 + 6 * (n_med - 0.5) * 2 + 2 * (n_small - 0.5) * 2
coast_r = np.minimum(coast_r, 95)                     # keep >= 32 tiles of sea to the edge
land = DIST < coast_r

def disc(cx, cy, r):
    return np.hypot(X - cx, Y - cy) <= r

# ---------------------------------------------------------------- key spots
ARENA_A = (100, 186)      # Callisto
ARENA_B = (156, 186)      # Venenatis
CRATER = (128, 198)       # King Black Dragon, island in the crater lake
LAKE = (190, 110)
MINE = (192, 160)
SMITHY = (176, 156)
BEACH_ARENA = (128, 66)
CHAOS_ALTAR = (62, 128)

for (cx, cy), r in ((ARENA_A, 13), (ARENA_B, 13), (CRATER, 17), (MINE, 15), (BEACH_ARENA, 15), (CHAOS_ALTAR, 8)):
    land |= disc(cx, cy, r)
land &= (DIST < 96)

# lake
lake = (np.hypot(X - LAKE[0], Y - LAKE[1]) < 7.5 + 3 * (n_small - 0.5) * 2)
land_incl_lake = land.copy()

sea = ~land
dist_sea = ndimage.distance_transform_edt(land)       # tiles from the open sea

# ---------------------------------------------------------------- biomes
TOWN, MEADOW, FOREST, VOLCANO, TROPIC, WASTE = range(6)
wob = (n_med - 0.5) * 0.5
a = ANG + wob
biome = np.full((W, W), FOREST)
biome[(a > math.pi / 4) & (a <= 3 * math.pi / 4)] = VOLCANO
biome[(a < -math.pi / 4) & (a >= -3 * math.pi / 4)] = TROPIC
biome[(a > 3 * math.pi / 4) | (a < -3 * math.pi / 4)] = WASTE
biome[DIST < 40 + 6 * (n_small - 0.5)] = MEADOW
town_sq = (np.abs(DX) <= 28) & (np.abs(DY) <= 28)
biome[town_sq] = TOWN
biome[disc(*MINE, 15)] = FOREST
biome[disc(*CHAOS_ALTAR, 8)] = WASTE
# The PvP zone is the wasteland. The server gets it as a list of 4-row strips (see ReloadWorld.java).
PVP_STRIPS = []
_w = (biome == WASTE) & land
for y0 in range(0, W, 4):
    xs = np.nonzero(_w[:, y0:y0 + 4].any(axis=1))[0]
    if len(xs): PVP_STRIPS.append((int(xs.min()), int(xs.max()), y0, y0 + 3))
PVP_EDGE_X = int(np.nonzero(_w[:, C])[0].max())      # east edge of the wasteland on the west road

# ---------------------------------------------------------------- heights (float, in "height bytes", 1 unit = 8 client units)
rise = np.clip(dist_sea / 14.0, 0, 1)
rise = rise * rise * (3 - 2 * rise)
hf = 2 + 26 * rise + (n_med - 0.5) * 9 * rise + (n_small - 0.5) * 4 * rise
hf[biome == TROPIC] -= 6 * rise[biome == TROPIC]
cone_d = np.hypot(X - CRATER[0], Y - CRATER[1])
cone = np.clip(1 - cone_d / 40.0, 0, 1)
hf += 44 * cone * cone * (3 - 2 * cone) * (biome == VOLCANO)
hf[cone_d < 14] = hf[cone_d < 14] * 0 + (hf[cone_d < 14].mean() - 10)        # flat crater floor

def flatten(mask, value=None, blend=5):
    """Flatten heights inside mask, blending out over `blend` tiles."""
    global hf
    v = hf[mask].mean() if value is None else value
    d = ndimage.distance_transform_edt(~mask)
    wgt = np.clip(1 - d / blend, 0, 1)
    hf = hf * (1 - wgt) + v * wgt

flatten(disc(*ARENA_A, 10)); flatten(disc(*ARENA_B, 10))
flatten(disc(*BEACH_ARENA, 12)); flatten(disc(*MINE, 12), blend=4)
flatten(disc(*CHAOS_ALTAR, 6))
flatten(town_sq, TOWN_H, blend=10)
hf = ndimage.gaussian_filter(hf, 0.8)
flatten(town_sq, TOWN_H, blend=6)
hf[lake] = np.minimum(hf[lake], hf[lake].min())

# ---------------------------------------------------------------- world arrays (plane 0)
H = np.clip(np.rint(hf), 2, 250).astype(int)
UN = np.zeros((W, W), int); OV = np.zeros((W, W), int); FL = np.zeros((W, W), int)

# floor ids are 1-based ids from flo.dat, picked by sampling the real world map
G_LUSH, G_DRY, G_MID, DIRT, DIRT2 = 48, 50, 49, 63, 64
SAND, SAND_PALE, JUNGLE, JUNGLE2 = 68, 62, 66, 67
ASH, ASH2, ASH3, ROCK, WASTE1, WASTE2 = 72, 149, 150, 56, 146, 65
O_WATER, O_LAVA, O_STONE, O_PATH, O_PAVE, O_DARK = 6, 19, 10, 22, 171, 11

UN[:] = G_LUSH
UN[(n_small > 0.62)] = G_DRY
UN[(n_small < 0.3)] = G_MID
m = biome == VOLCANO
UN[m] = ASH; UN[m & (n_small > 0.6)] = ASH2; UN[m & (n_small < 0.3)] = ASH3
m = biome == TROPIC
UN[m] = JUNGLE; UN[m & (n_small > 0.6)] = JUNGLE2; UN[m & (n_small < 0.25)] = G_LUSH
UN[m & (dist_sea < 11 + 5 * n_med)] = SAND
m = biome == WASTE
UN[m] = WASTE1; UN[m & (n_small > 0.66)] = ASH; UN[m & (n_small < 0.28)] = WASTE2
UN[disc(*MINE, 12 + 0)] = ROCK
UN[disc(*MINE, 13) & (n_small > 0.55)] = DIRT
# beaches
beach = land & (dist_sea <= 3.2)
UN[beach & np.isin(biome, (FOREST, MEADOW, TOWN, TROPIC))] = SAND
UN[beach & np.isin(biome, (VOLCANO, WASTE))] = ROCK
UN[lake] = DIRT
ring = ndimage.binary_dilation(lake, iterations=2) & ~lake
UN[ring] = DIRT2

# sea + lake
water = sea | lake
OV[water] = O_WATER; FL[water] |= 1
H[sea] = 1
UN[sea] = SAND

# lava: crater ring with two causeways, plus scattered pools
lava = (biome == VOLCANO) & land & (noise(10, 7) > 0.74) & (dist_sea > 4) & (DIST > 44)
crater_ring = (cone_d >= 9.5) & (cone_d <= 13.5)
causeway = (np.abs(X - CRATER[0]) <= 1.5)
lava |= crater_ring & ~causeway
for c in (ARENA_A, ARENA_B):
    lava &= ~disc(*c, 12)
lava &= ~((np.abs(X - C) <= 4) & (Y > C) & (Y < CRATER[1] - 9))       # keep the north road clear
waste_lava = (biome == WASTE) & land & (noise(7, 9) > 0.86) & ~disc(*CHAOS_ALTAR, 9)
lava |= waste_lava
lava &= land & ~lake
OV[lava] = O_LAVA; FL[lava] |= 1

# ---------------------------------------------------------------- paths
path = np.zeros((W, W), bool)
def road(x0, y0, x1, y1, width=4, wiggle=2.0):
    n = int(max(abs(x1 - x0), abs(y1 - y0)))
    for i in range(n + 1):
        t = i / max(n, 1)
        px, py = x0 + (x1 - x0) * t, y0 + (y1 - y0) * t
        off = wiggle * math.sin(i / 6.5)
        if abs(x1 - x0) > abs(y1 - y0): py += off
        else: px += off
        xa, ya = int(round(px - width / 2)), int(round(py - width / 2))
        path[xa:xa + width, ya:ya + width] = True
road(C, C + 28, C, 176)                       # north, to the volcano
road(C, 176, CRATER[0], CRATER[1] - 14, 3, 0)
road(C, 172, ARENA_A[0] + 8, ARENA_A[1] - 4, 3, 1.5)
road(C, 172, ARENA_B[0] - 8, ARENA_B[1] - 4, 3, 1.5)
road(C + 28, C, 178, C)                       # east, into the forest
road(178, C, MINE[0] - 10, MINE[1] - 6, 3, 1.5)
road(178, C, LAKE[0] - 4, LAKE[1] + 10, 3, 1.5)
road(C, C - 28, C, BEACH_ARENA[1] + 12)       # south, to the beach arena
road(C - 28, C, PVP_EDGE_X + 1, C)            # west, to the PvP border
road(PVP_EDGE_X, C, CHAOS_ALTAR[0] + 5, C, 3, 1.5)
path &= land & ~lake
out_town = path & ~town_sq
OV[out_town & (biome != WASTE) & (biome != VOLCANO)] = O_PATH
OV[out_town & ((biome == WASTE) | (biome == VOLCANO))] = O_DARK
FL[out_town] &= ~1
lava &= ~path

# town: paved plaza, cross roads
plaza = (np.abs(DX) <= 10) & (np.abs(DY) <= 10)
cross = town_sq & ((np.abs(DX - 0.5) <= 2) | (np.abs(DY - 0.5) <= 2))
OV[cross] = O_STONE
OV[plaza] = O_PAVE
UN[town_sq & (UN != G_LUSH)] = G_LUSH
path |= cross | plaza
# footpaths from the buildings to the roads
lanes = np.zeros((W, W), bool)
lanes[131:141, 140:142] = True        # bank forecourt -> north road
lanes[109:111, 131:139] = True        # general store -> west road
lanes[110:112, 116:127] = True        # long house -> west road
lanes[146:148, 116:127] = True        # small house -> east road
lanes[122:124, 131:143] = True        # corner house -> west road
OV[lanes & (OV == 0)] = O_PATH
path |= lanes

# arenas and platforms
def platform(c, r, ov):
    m = disc(c[0], c[1], r)
    OV[m] = ov; FL[m] &= ~1
    return m
arena_mask = platform(ARENA_A, 9, O_DARK) | platform(ARENA_B, 9, O_DARK) | platform(CRATER, 8, O_DARK)
arena_mask |= disc(*BEACH_ARENA, 11)
UN[disc(*BEACH_ARENA, 11)] = SAND_PALE
UN[disc(*BEACH_ARENA, 12) & ~disc(*BEACH_ARENA, 11)] = DIRT
altar_pad = platform(CHAOS_ALTAR, 4, O_DARK)
smithy_pad = (np.abs(X - SMITHY[0]) <= 3) & (np.abs(Y - SMITHY[1]) <= 3)
OV[smithy_pad] = O_STONE
lava &= ~(arena_mask | altar_pad)

# ---------------------------------------------------------------- upper planes + objects
PL = {z: dict(H=np.full((W, W), -1), UN=np.zeros((W, W), int), OV=np.zeros((W, W), int),
              OS=np.zeros((W, W), int), FL=np.zeros((W, W), int)) for z in (1, 2, 3)}
OS0 = np.zeros((W, W), int)
objects = []                                   # (id, x, y, z, type, rot) in local world coords
occ = (FL & 1).astype(bool) | path | arena_mask | altar_pad | smithy_pad   # where scenery may NOT go
blocked = (FL & 1).astype(bool).copy()          # walk-blocking, for the reachability check

def size(oid, rot=0):
    d = LOCS[oid]; sx, sy = d['sx'], d['sy']
    return (sy, sx) if rot & 1 else (sx, sy)

def put(oid, x, y, rot=0, typ=10, force=False):
    sx, sy = size(oid, rot)
    if x < 1 or y < 1 or x + sx >= W - 1 or y + sy >= W - 1: return False
    if not force and (occ[x:x + sx, y:y + sy].any() or not land[x:x + sx, y:y + sy].all()): return False
    objects.append((oid, x, y, 0, typ, rot))
    occ[x:x + sx, y:y + sy] = True
    if LOCS[oid]['solid'] and typ in (10, 11): blocked[x:x + sx, y:y + sy] = True
    return True

def stamp(sx0, sy0, w, h, dx0, dy0):
    """Copy a rectangle of the real map (all planes, floors, walls, roofs, objects) into the world."""
    for x in range(w):
        for y in range(h):
            ax, ay = sx0 + x, sy0 + y
            T, _ = region(ax >> 6, ay >> 6)
            lx, ly = ax & 63, ay & 63
            tx, ty = dx0 + x, dy0 + y
            H[tx, ty] = TOWN_H
            if T['OV'][0][lx][ly]:
                OV[tx, ty] = T['OV'][0][lx][ly]; OS0[tx, ty] = T['OS'][0][lx][ly]
                if T['UN'][0][lx][ly]: UN[tx, ty] = T['UN'][0][lx][ly]
            else:
                OV[tx, ty] = 0
            FL[tx, ty] = T['FL'][0][lx][ly]
            for z in (1, 2, 3):
                P = PL[z]
                hh = T['H'][z][lx][ly]
                P['H'][tx, ty] = -1 if hh is None else hh
                for k in ('UN', 'OV', 'OS', 'FL'): P[k][tx, ty] = T[k][z][lx][ly]
    seen = set()
    for rx in range(sx0 >> 6, ((sx0 + w - 1) >> 6) + 1):
        for ry in range(sy0 >> 6, ((sy0 + h - 1) >> 6) + 1):
            _, O = region(rx, ry)
            for (oid, lx, ly, z, t, r) in O:
                ax, ay = rx * 64 + lx, ry * 64 + ly
                if sx0 <= ax < sx0 + w and sy0 <= ay < sy0 + h and t <= 22:
                    tx, ty = dx0 + ax - sx0, dy0 + ay - sy0
                    objects.append((oid, tx, ty, z, t, r))
                    if z == 0:
                        if t in (10, 11) and LOCS[oid]['solid']:
                            a, b = size(oid, r); blocked[tx:tx + a, ty:ty + b] = True
                        if t in (0, 1, 2, 3, 9): seen.add((tx, ty))
    occ[dx0:dx0 + w, dy0:dy0 + h] = True
    return seen

# Buildings lifted from Edgeville (bank, general store, three houses)
BANK_AT = (141, 134)
stamp(3089, 3486, 11, 15, *BANK_AT)            # bank
stamp(3075, 3506, 11, 9, 104, 139)             # general store
stamp(3090, 3506, 12, 9, 104, 107)             # long house
stamp(3104, 3493, 8, 8, 143, 108)              # small house
stamp(3106, 3509, 7, 9, 116, 143)              # corner house

# ---- town dressing
put(879, 127, 127, force=True)                                   # fountain, centre of the plaza
occ[124:132, 124:132] = True
for (x, y, r) in ((121, 127, 1), (133, 127, 3), (127, 121, 0), (127, 133, 2)):
    put(1104, x, y, r, force=True)                               # benches round the fountain
for sx_, sy_ in ((-1, -1), (-1, 1), (1, -1), (1, 1)):
    put(22738, C + sx_ * 11 - (sx_ < 0), C + sy_ * 11 - (sy_ < 0), force=True)   # lampposts on the plaza corners
STALLS = [(634, 119, 136), (629, 134, 136), (631, 119, 118), (633, 134, 118)]
for oid, x, y in STALLS: put(oid, x, y, force=True)
for i, d in enumerate(range(14, 28, 6)):
    for s in (-1, 1):
        put(22738, C + s * d, C + 3, force=True); put(22738, C + s * d, C - 2, force=True)
        put(22738, C + 3, C + s * d, force=True); put(22738, C - 2, C + s * d, force=True)
put(409, 132, 112, force=True)                                   # prayer altar
for (x, y) in ((C - 5, C + 27), (C + 4, C + 27), (C - 5, C - 28), (C + 4, C - 28), (C + 27, C + 4), (C + 27, C - 5), (C - 28, C + 4), (C - 28, C - 5)):
    put(575 if (x + y) % 2 else 579, x, y, force=True)           # statues at the four gates

def scatter(mask, ids, density, seed, margin=0):
    r = np.random.default_rng(SEED + seed)
    xs, ys = np.nonzero(mask)
    order = r.permutation(len(xs))
    n = int(len(xs) * density); done = 0
    for i in order:
        if done >= n: break
        oid = ids[int(r.integers(len(ids)))]
        x, y = int(xs[i]), int(ys[i])
        sx, sy = size(oid)
        if margin and occ[max(0, x - margin):x + sx + margin, max(0, y - margin):y + sy + margin].any(): continue
        if put(oid, x, y, int(r.integers(4)) if sx == sy else 0): done += 1
    return done

free = land & ~lake & ~lava
town_green = town_sq & ~path
scatter(town_green & (np.abs(DX) > 6) & (np.abs(DY) > 6), [1201, 1199, 1196, 1195, 1187, 1188], 0.05, 11)
scatter(town_green & ((np.abs(DX) > 22) | (np.abs(DY) > 22)), [1276, 1278], 0.02, 12, margin=2)
scatter(town_green, [1124, 1125], 0.012, 13, margin=1)

# ---- meadow: light trees, flowers, livestock area
m = free & (biome == MEADOW)
scatter(m, [1276, 1278], 0.012, 20, margin=2)
scatter(m, [1187, 1188, 1196, 1195, 1298, 1173, 1124], 0.035, 21)

# ---- forest: tree tiers get harder the further east (rare, big trees are planted first so they get room)
def plant(mask, oid, n, seed, margin=1):
    r = np.random.default_rng(SEED + seed)
    xs, ys = np.nonzero(mask); done = 0
    for i in r.permutation(len(xs)):
        if done >= n: break
        x, y = int(xs[i]), int(ys[i]); sx, sy = size(oid)
        if occ[max(0, x - margin):x + sx + margin, max(0, y - margin):y + sy + margin].any(): continue
        if put(oid, x, y, int(r.integers(4))): done += 1
    assert done >= n * 0.7, ('could not plant', oid, done, n)
    return done
m = free & (biome == FOREST) & ~disc(*MINE, 14) & ~beach
deep = m & (dist_sea > 7) & (DX > 60) & (np.abs(DY) < 30)
gx, gy = np.nonzero(deep)
GROVE = (int(gx.max()) - 4, int(gy[gx.argmax()]))
plant(m & disc(*GROVE, 6), 1761, 6, 33)                          # magic grove, far east
plant(m & (DX >= 60) & ~disc(*GROVE, 8), 1753, 10, 32, margin=2) # yews
willow_ring = ndimage.binary_dilation(lake, iterations=4) & ~ndimage.binary_dilation(lake, iterations=1) & free
plant(willow_ring, 1750, 6, 35, margin=0); plant(willow_ring, 1758, 5, 37, margin=0)
plant(m & (DX >= 46) & (DX < 76), 1759, 22, 31)                  # maples
plant(m & (DX >= 30) & (DX < 64), 1751, 24, 38)                  # oaks
scatter(m & (DX < 62), [1276, 1278], 0.03, 30, margin=1)         # ordinary trees near town
scatter(m & (DX >= 62), [1276, 1278], 0.012, 39, margin=1)
scatter(m, [1298, 1299, 1300, 1173, 1124, 1187, 1166, 19839], 0.04, 36)

# ---- mine: ore tiers in clusters, smithy on a stone pad
ORES = [(7453, 5, 'copper'), (7486, 5, 'tin'), (7455, 7, 'iron'), (7457, 3, 'silver'), (7456, 9, 'coal'),
        (7459, 5, 'mithril'), (7460, 4, 'adamantite'), (7461, 3, 'runite')]
r_ = np.random.default_rng(SEED + 40)
for i, (oid, n, _) in enumerate(ORES):
    ang = 2 * math.pi * i / len(ORES) + 0.3
    cx, cy = MINE[0] + 8 * math.cos(ang), MINE[1] + 8 * math.sin(ang)
    placed = 0; tries = 0
    while placed < n and tries < 200:
        tries += 1
        x, y = int(round(cx + r_.normal(0, 1.6))), int(round(cy + r_.normal(0, 1.6)))
        if occ[x - 1:x + 2, y - 1:y + 2].sum() > 2: continue
        if put(oid, x, y, int(r_.integers(4))): placed += 1
scatter(disc(*MINE, 14) & ~disc(*MINE, 11) & free, [10790, 10791], 0.03, 41, margin=1)
put(24009, SMITHY[0] - 3, SMITHY[1] + 1, 0, force=True)          # furnace (2x3)
put(2031, SMITHY[0] + 1, SMITHY[1] + 2, 0, force=True)           # anvils
put(2031, SMITHY[0] + 1, SMITHY[1] - 1, 0, force=True)
put(2693, SMITHY[0] - 2, SMITHY[1] - 3, 0, force=True)           # bank chest
put(362, SMITHY[0] + 3, SMITHY[1] - 3, force=True); put(357, SMITHY[0] + 3, SMITHY[1] + 3, force=True)

# ---- volcano
m = free & (biome == VOLCANO) & ~beach
scatter(m, [12576, 12575, 12576, 10790, 10791, 1286, 1289], 0.022, 50, margin=1)
scatter(m & (cone_d > 16), [14606, 14605, 668], 0.004, 51)
def ring_of(c, r, oid, n, phase=0.0):
    for i in range(n):
        a_ = 2 * math.pi * i / n + phase
        x, y = int(round(c[0] + r * math.cos(a_))), int(round(c[1] + r * math.sin(a_)))
        if path[x, y] or lava[x, y]: continue
        sx, sy = size(oid)
        if occ[x:x + sx, y:y + sy].any() and not arena_mask[x, y]: continue
        objects.append((oid, x, y, 0, 10, 0)); occ[x:x + sx, y:y + sy] = True; blocked[x:x + sx, y:y + sy] = True
ring_of(ARENA_A, 10, 724, 12, 0.26); ring_of(ARENA_B, 10, 724, 12, 0.26); ring_of(CRATER, 8, 724, 10, 0.31)
put(2693, C + 4, 170, force=True)                                # bank chest at the foot of the volcano

# ---- tropics
m = free & (biome == TROPIC)
sandm = m & (UN == SAND)
scatter(m & ~sandm, [1311, 1312, 2975, 1402, 1400, 1204, 4825], 0.04, 60, margin=1)
scatter(m & ~sandm, [1390, 1298, 1299, 2892, 4814], 0.05, 61)
scatter(sandm & (dist_sea > 4), [1311, 1312, 2975, 2975], 0.012, 62, margin=2)
ring_of(BEACH_ARENA, 12, 7016, 16, 0.2)
ring_of(BEACH_ARENA, 14, 724, 8, 0.4)
put(2693, C + 4, 82, force=True)

# ---- wasteland (PvP)
m = free & (biome == WASTE)
scatter(m, [1282, 1289, 1286, 1383, 1289, 1286], 0.022, 70, margin=1)
scatter(m, [668, 14606, 14605, 659, 736], 0.012, 71)
put(411, CHAOS_ALTAR[0] - 1, CHAOS_ALTAR[1] + 1, force=True)     # chaos altar
ring_of(CHAOS_ALTAR, 5, 724, 6, 0.5)
_wz = (biome == WASTE)
edge = land & ~_wz & ndimage.binary_dilation(_wz) & ~path        # border markers: torches and skull piles
ex, ey = np.nonzero(edge)
order = np.argsort(np.arctan2(ey - CHAOS_ALTAR[1], ex - CHAOS_ALTAR[0]))
last = None; k = 0
for i in order:
    p = (int(ex[i]), int(ey[i]))
    if last is None or abs(p[0] - last[0]) + abs(p[1] - last[1]) >= 5:
        if put(724 if k % 2 == 0 else 668, p[0], p[1]): last = p; k += 1
put(2693, PVP_EDGE_X + 6, C + 4, force=True)                     # bank chest just outside the border

# ---------------------------------------------------------------- npc spawns
spawns = []
def npc(nid, x, y, radius=0, desc='', facing='SOUTH'):
    spawns.append({"facing": facing, "radius": radius, "id": nid,
                   "position": {"x": BX + int(x), "y": BY + int(y), "z": 0}, "description": "RELOAD world: " + desc})
def npc_scatter(nid, mask, n, radius, desc, seed, gap=4, on_floor=False):
    r = np.random.default_rng(SEED + seed)
    xs, ys = np.nonzero(mask & ~blocked & (~occ | on_floor))      # on_floor: arenas and paths are fine too
    chosen = []
    start = len(spawns)
    for i in r.permutation(len(xs)):
        p = (int(xs[i]), int(ys[i]))
        if all(abs(p[0] - q[0]) + abs(p[1] - q[1]) >= gap for q in chosen):
            chosen.append(p); npc(nid, p[0], p[1], radius, desc)
            if len(chosen) >= n: break
    assert len(spawns) - start >= max(1, n // 2), ('too few spawns placed', desc, len(spawns) - start, n)
# fishing spots: water tiles that touch the shore
shore_lake = lake & ndimage.binary_dilation(~lake & land)
r_ = np.random.default_rng(SEED + 80)
xs, ys = np.nonzero(shore_lake); idx = r_.permutation(len(xs))[:5]
for i in idx: npc(1497, xs[i], ys[i], 0, 'fishing spot (lake)')
def coast_spots(sel, n, seed):
    shore = sea & ndimage.binary_dilation(land) & sel
    r = np.random.default_rng(SEED + seed); xs, ys = np.nonzero(shore)
    for i in r.permutation(len(xs))[:n]: npc(1498, xs[i], ys[i], 0, 'fishing spot (sea)')
coast_spots((DX > 60) & (np.abs(DY) < 30), 4, 81)
coast_spots((DY < -60) & (np.abs(DX) < 30), 4, 82)
vol = free & (biome == VOLCANO) & ~arena_mask & (cone_d > 15)
npc_scatter(CUSTOM_NPC['EMBER_DEMON'], vol & (DY < 66), 9, 3, 'ember demon', 83, 6)
npc_scatter(2025, vol & (DY >= 50), 5, 3, 'greater demon', 84, 7)
npc_scatter(135, vol, 4, 3, 'hellhound', 85, 8)
npc_scatter(2075, vol & (DY >= 44), 4, 3, 'fire giant', 86, 8)
npc_scatter(247, vol & (cone_d < 34), 2, 2, 'red dragon', 87, 12)
npc_scatter(252, vol & (cone_d < 30), 2, 2, 'black dragon', 88, 12)
npc(CUSTOM_NPC['FROSTCLAW'], ARENA_A[0] - 2, ARENA_A[1] - 2, 3, 'Frostclaw (west arena)')
npc(CUSTOM_NPC['VENOMQUEEN'], ARENA_B[0] - 2, ARENA_B[1] - 2, 3, 'Venomqueen (east arena)')
npc(CUSTOM_NPC['MAGMAROTH'], CRATER[0] - 2, CRATER[1] - 2, 2, 'Magmaroth (crater)')
# training grounds: the pillar arena on the south beach
_ring = disc(*BEACH_ARENA, 10) & ~disc(*BEACH_ARENA, 3) & ~blocked
npc_scatter(CUSTOM_NPC['SPARRING_GIANT'], _ring & (Y >= BEACH_ARENA[1]), 10, 2, 'sparring giant (training)', 97, 4, on_floor=True)
npc_scatter(CUSTOM_NPC['SPARRING_BRUTE'], _ring & (Y < BEACH_ARENA[1] - 2), 5, 2, 'sparring brute (training)', 98, 5, on_floor=True)
mead = free & (biome == MEADOW) & ~path
npc_scatter(2805, mead & (DX > 30), 5, 4, 'cow', 89)
npc_scatter(2692, mead & (DX > 30), 5, 3, 'chicken', 90)
npc_scatter(655, mead & (DY < -30), 5, 4, 'goblin', 91)
npc_scatter(2098, free & (biome == FOREST) & (DX > 55) & ~disc(*MINE, 16), 5, 4, 'hill giant', 92, 8)
npc_scatter(891, free & (biome == FOREST) & (DX > 70), 3, 3, 'moss giant', 93, 8)
npc_scatter(100, free & (biome == TROPIC) & (UN == SAND) & ~arena_mask, 8, 3, 'rock crab', 94, 5)
npc_scatter(70, free & (biome == WASTE) & ~altar_pad, 7, 4, 'skeleton', 95, 6)
npc_scatter(85, free & (biome == WASTE) & ~altar_pad, 4, 4, 'ghost', 96, 8)
npc(394, BANK_AT[0] + 8, BANK_AT[1] + 3, 0, 'banker', 'WEST')
npc(394, BANK_AT[0] + 8, BANK_AT[1] + 5, 0, 'banker', 'WEST')

# ---------------------------------------------------------------- sanity: everything reachable from home
HOME = (C, C - 5)
walk = land & ~blocked & ~lake & ~lava
lab, _ = ndimage.label(walk, structure=[[0, 1, 0], [1, 1, 1], [0, 1, 0]])
home_lab = lab[HOME]
assert home_lab != 0, 'home tile is blocked'
ZONES = {
    'home': HOME, 'bank': (BANK_AT[0] - 2, BANK_AT[1] + 4), 'forest': (176, C), 'lake': (LAKE[0] - 6, LAKE[1] + 11),
    'mine': (MINE[0] - 1, MINE[1]), 'smithy': (SMITHY[0], SMITHY[1]), 'magic_trees': (GROVE[0] - 7, GROVE[1]),
    'volcano': (C, 172), 'frostclaw': (ARENA_A[0] + 7, ARENA_A[1] - 6), 'venomqueen': (ARENA_B[0] - 7, ARENA_B[1] - 6),
    'magmaroth': (CRATER[0], CRATER[1] - 15), 'beach': (C, 84), 'train': (BEACH_ARENA[0], BEACH_ARENA[1] + 1),
    'pvp': (PVP_EDGE_X + 4, C), 'chaos_altar': (CHAOS_ALTAR[0] + 3, CHAOS_ALTAR[1] - 2),
}
def nearest_walkable(p):
    best = None
    for r in range(0, 8):
        for x in range(p[0] - r, p[0] + r + 1):
            for y in range(p[1] - r, p[1] + r + 1):
                if lab[x, y] == home_lab and (best is None or abs(x - p[0]) + abs(y - p[1]) < best[0]): best = (abs(x - p[0]) + abs(y - p[1]), (x, y))
        if best: return best[1]
    raise AssertionError('zone not reachable from home: %r' % (p,))
ZONES = {k: nearest_walkable(v) for k, v in ZONES.items()}
for c_, nm in ((ARENA_A, 'A'), (ARENA_B, 'B'), (CRATER, 'crater'), (CHAOS_ALTAR, 'altar'), (BEACH_ARENA, 'beach arena')):
    assert lab[nearest_walkable(c_)] == home_lab, nm
unreach = (walk & (lab != home_lab)).sum()

# ---------------------------------------------------------------- encode
def build_region(i, j):
    x0, y0 = i * 64, j * 64
    T = {k: [[[0] * 64 for _ in range(64)] for _ in range(4)] for k in ('OV', 'OS', 'FL', 'UN')}
    T['H'] = [[[None] * 64 for _ in range(64)] for _ in range(4)]
    for x in range(64):
        for y in range(64):
            gx, gy = x0 + x, y0 + y
            T['H'][0][x][y] = int(H[gx, gy]); T['UN'][0][x][y] = int(UN[gx, gy]); T['OV'][0][x][y] = int(OV[gx, gy])
            T['OS'][0][x][y] = int(OS0[gx, gy]); T['FL'][0][x][y] = int(FL[gx, gy])
            for z in (1, 2, 3):
                P = PL[z]
                if P['H'][gx, gy] >= 0: T['H'][z][x][y] = int(P['H'][gx, gy])
                for k in ('UN', 'OV', 'OS', 'FL'): T[k][z][x][y] = int(P[k][gx, gy])
    objs = [(o, x - x0, y - y0, z, t, r) for (o, x, y, z, t, r) in objects if x0 <= x < x0 + 64 and y0 <= y < y0 + 64]
    return T, objs

def gz(b): return gzip.compress(b, 9, mtime=0)

def main():
    srv = os.path.join(ROOT, 'ElvargServer', 'data', 'clipping')
    cli = os.path.join(ROOT, 'ElvargClient', 'Cache', 'reload_maps')
    os.makedirs(cli, exist_ok=True)
    entries = []; n_obj = 0
    for i in range(4):
        for j in range(4):
            k = i * 4 + j
            tf, of = FIRST_FILE + 2 * k, FIRST_FILE + 2 * k + 1
            T, objs = build_region(i, j)
            tb, ob = enc_terrain(T), enc_objects(objs)
            assert dec_terrain(tb) == T and sorted(dec_objects(ob)) == sorted(set(objs))
            n_obj += len(objs)
            for fid, data in ((tf, gz(tb)), (of, gz(ob))):
                open(os.path.join(srv, 'maps', '%d.dat' % fid), 'wb').write(data)
                open(os.path.join(cli, '%d.gz' % fid), 'wb').write(data)
            entries.append((((RX0 + i) << 8) | (RY0 + j), tf, of))
    # server map_index = original entries (as in the client cache) + ours
    mine = {e[0] for e in entries}
    base = [(r, t, o) for r, (t, o) in INDEX.items() if r not in mine]
    assert not any(t >= FIRST_FILE or o >= FIRST_FILE for _, t, o in base)
    allr = base + entries
    open(os.path.join(srv, 'map_index'), 'wb').write(struct.pack('>H', len(allr)) + b''.join(struct.pack('>HHH', *e) for e in allr))
    open(os.path.join(cli, 'map_index'), 'wb').write(struct.pack('>H', len(entries)) + b''.join(struct.pack('>HHH', *e) for e in entries))
    json.dump(spawns, open(os.path.join(ROOT, 'ElvargServer', 'data', 'definitions', 'npc_spawns_reload_world.json'), 'w'), indent=2)
    here = os.path.dirname(os.path.abspath(__file__))
    zones = {k: {"x": BX + v[0], "y": BY + v[1], "z": 0} for k, v in ZONES.items()}
    info = {"base": [BX, BY], "size": W, "regions": [e[0] for e in entries],
            "pvp_strips": [[BX + a, BX + b, BY + c_, BY + d_] for a, b, c_, d_ in PVP_STRIPS], "zones": zones}
    json.dump(info, open(os.path.join(here, 'zones.json'), 'w'), indent=2)
    # Java constants for the server
    jl = ['package com.elvarg.game.content.reloadworld;', '', 'import com.elvarg.game.model.Boundary;', 'import com.elvarg.game.model.Location;', '',
          'import java.util.Arrays;', 'import java.util.LinkedHashMap;', 'import java.util.List;', 'import java.util.Map;', '',
          '/**', ' * Coordinates of the custom RELOAD world. GENERATED by tools/reload-world/generate.py, do not edit by hand.', ' */',
          'public final class ReloadWorld {', '',
          '    /** South-west and north-east corner of the 4x4 region block. */',
          '    public static final int MIN_X = %d, MIN_Y = %d, MAX_X = %d, MAX_Y = %d;' % (BX, BY, BX + W - 1, BY + W - 1), '',
          '    /** Wilderness level used inside the PvP wasteland. */', '    public static final int PVP_LEVEL = 20;', '',
          '    public static final Location HOME = new Location(%d, %d, 0);' % (zones['home']['x'], zones['home']['y']), '',
          '    /** Named teleport spots, used by the ::zone command. */',
          '    public static final Map<String, Location> ZONES = new LinkedHashMap<>();', '', '    static {']
    for k, v in zones.items(): jl.append('        ZONES.put("%s", new Location(%d, %d, 0));' % (k.replace('_', ''), v['x'], v['y']))
    jl += ['    }', '', '    /** The PvP wasteland in the west, as horizontal strips. Added to the WildernessArea. */',
           '    public static final List<Boundary> PVP_BOUNDARIES = Arrays.asList(']
    jl.append(',\n'.join('            new Boundary(%d, %d, %d, %d)' % (BX + a, BX + b, BY + c_, BY + d_) for a, b, c_, d_ in PVP_STRIPS) + ');')
    jl += ['', '    public static boolean inside(int x, int y) {', '        return x >= MIN_X && x <= MAX_X && y >= MIN_Y && y <= MAX_Y;', '    }', '',
           '    private ReloadWorld() {', '    }', '}', '']
    jdir = os.path.join(ROOT, 'ElvargServer', 'game', 'src', 'main', 'java', 'com', 'elvarg', 'game', 'content', 'reloadworld')
    os.makedirs(jdir, exist_ok=True)
    open(os.path.join(jdir, 'ReloadWorld.java'), 'w').write('\n'.join(jl))
    # preview
    from render import render
    omap = {}
    for (o, x, y, z, t, r) in objects: omap.setdefault((x, y), []).append((o, t, r, z))
    def get(x, y): return (int(UN[x, y]), int(OV[x, y]), int(H[x, y]), omap.get((x, y), []))
    im = render(get, 0, 0, W, W, 4)
    from PIL import ImageDraw
    d = ImageDraw.Draw(im)
    for s in spawns:
        x, y = s['position']['x'] - BX, s['position']['y'] - BY
        d.rectangle([x * 4, (W - 1 - y) * 4, x * 4 + 3, (W - 1 - y) * 4 + 3], outline=(255, 255, 0))
    for k, v in ZONES.items():
        d.text((v[0] * 4 + 5, (W - 1 - v[1]) * 4 - 5), k, fill=(255, 255, 255))
    im.save(os.path.join(here, 'preview.png'))
    if os.environ.get('RELOAD_DEBUG'):
        for nm, (x0, y0, w, h) in {'town': (98, 98, 60, 60), 'volcano': (84, 160, 90, 60), 'east': (150, 90, 80, 90), 'south': (80, 30, 100, 70), 'west': (20, 90, 90, 80)}.items():
            render(get, x0, y0, w, h, 12).save('/tmp/dbg_%s.png' % nm)
    print('regions', len(entries), 'objects', n_obj, 'npc spawns', len(spawns), 'land tiles', int(land.sum()),
          'unreachable walkable tiles', int(unreach))
    for k, v in zones.items(): print('  %-12s %d %d' % (k, v['x'], v['y']))

if __name__ == '__main__':
    main()
