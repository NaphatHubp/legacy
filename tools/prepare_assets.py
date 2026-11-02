#!/usr/bin/env python3
"""
Turns the raw art packs in assets_src/ into game-ready sprite strips in
src/main/resources/assets/ .

Run it only when you change the source art:   python3 tools/prepare_assets.py
(needs: pip install pillow numpy)

What it does
  * Meow Knight : the packs ship as vertical strips with uneven spacing. We cut
                  each frame out, line every frame up on the cat's feet and body
                  centre, and write one horizontal strip per animation.
  * Block anim  : the pack has no block animation, so we build one by drawing a
                  pixel-art shield (raise / hold / hit) over the cat frames.
  * Flying Demon: split into strips, plus a guard animation (shield) and
                  purple "mutated" copies of everything for the boss's 2nd form.
  * Tileset     : cut into individual 8x8 tiles + decoration pieces.
"""
import colorsys, json, os
import numpy as np
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "assets_src")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets")
for sub in ("cat", "demon", "tiles"):
    os.makedirs(os.path.join(OUT, sub), exist_ok=True)

MANIFEST = {}   # file -> frame count (read by the Java side)

# ----------------------------------------------------------------------
# helpers
# ----------------------------------------------------------------------
def load(path):
    return Image.open(path).convert("RGBA")

def hue_shift(img, degrees, sat=1.0, val=1.0):
    """Rotate the hue of every pixel (keeps alpha)."""
    a = np.array(img).astype(np.float32) / 255.0
    rgb, alpha = a[..., :3], a[..., 3:]
    flat = rgb.reshape(-1, 3)
    out = np.empty_like(flat)
    for i, (r, g, b) in enumerate(flat):
        h, s, v = colorsys.rgb_to_hsv(r, g, b)
        h = (h + degrees / 360.0) % 1.0
        out[i] = colorsys.hsv_to_rgb(h, min(1, s * sat), min(1, v * val))
    res = np.concatenate([out.reshape(rgb.shape), alpha], axis=2)
    return Image.fromarray((res * 255 + 0.5).astype(np.uint8), "RGBA")

def split_rows(img, n):
    """Cut a vertical strip into n frames at its n-1 biggest empty gaps."""
    a = np.array(img)[..., 3] > 0
    rows = a.any(axis=1)
    gaps, start = [], None
    for y, filled in enumerate(rows):
        if not filled and start is None: start = y
        if filled and start is not None:
            if start > 0: gaps.append((y - start, start, y))
            start = None
    gaps = sorted(gaps, reverse=True)[: n - 1]
    cuts = sorted((s + e) // 2 for _, s, e in gaps)
    bounds = [0] + cuts + [img.height]
    return [img.crop((0, bounds[i], img.width, bounds[i + 1])) for i in range(n)]

BODY = {(0xdf,0xed,0xed),(0xc6,0xdb,0xde),(0x9b,0xaa,0xb3),(0x8a,0x98,0xa0),
        (0xa6,0xaf,0xb4),(0x72,0x87,0x94),(0x6e,0x7a,0x80),(0xe6,0xa3,0xa3),(0x4e,0x4d,0x5c)}

def body_anchor(frame):
    """(centre_x, feet_y) of the cat's own body, ignoring sword effects."""
    a = np.array(frame)
    ys, xs = [], []
    for y in range(a.shape[0]):
        for x in range(a.shape[1]):
            if a[y, x, 3] > 0 and tuple(a[y, x, :3]) in BODY:
                ys.append(y); xs.append(x)
    if not xs:
        return frame.width / 2, frame.height - 1
    return (min(xs) + max(xs) + 1) / 2, max(ys) + 1

def strip(frames, cw, ch):
    s = Image.new("RGBA", (cw * len(frames), ch), (0, 0, 0, 0))
    for i, f in enumerate(frames):
        s.alpha_composite(f, (i * cw, 0))
    return s

def save_strip(img, subdir, name, frames):
    img.save(os.path.join(OUT, subdir, name))
    MANIFEST[f"{subdir}/{name}"] = frames

# ----------------------------------------------------------------------
# 1) Cat
# ----------------------------------------------------------------------
CAT = os.path.join(SRC, "meow_knight")
CAT_ANIMS = {            # name : (source file, frame count)
    "idle":   ("Meow-Knight_Idle.png", 6),
    "run":    ("Meow-Knight_Run.png", 8),
    "jump":   ("Meow-Knight_Jump.png", 12),
    "hurt":   ("Meow-Knight_Take_Damage.png", 3),
    "dodge":  ("Meow-Knight_Dodge.png", 8),
    "death":  ("Meow-Knight_Death.png", 6),
    "slash1": ("Meow-Knight_Attack_2.png", 4),
    "slash2": ("Meow-Knight_Attack_3.png", 4),
}
raw = {}
for name, (fn, n) in CAT_ANIMS.items():
    frames = split_rows(load(os.path.join(CAT, fn)), n)
    raw[name] = [(f, *body_anchor(f)) for f in frames]

# uniform cell: cat centre in the middle, feet near the bottom
left = right = up = down = 0
for frs in raw.values():
    for f, cx, fy in frs:
        bb = f.getbbox()
        if not bb: continue
        left = max(left, cx - bb[0]); right = max(right, bb[2] - cx)
        up = max(up, fy - bb[1]);     down = max(down, bb[3] - fy)
half = int(max(left, right)) + 2
CW = half * 2
FEET = int(up) + 2          # feet row inside the cell
CH = FEET + int(down) + 3
print(f"cat cell = {CW}x{CH}, feet at row {FEET}")

def place(f, cx, fy):
    cell = Image.new("RGBA", (CW, CH), (0, 0, 0, 0))
    cell.alpha_composite(f, (int(round(half - cx)), int(round(FEET - fy))))
    return cell

cat_cells = {n: [place(*t) for t in frs] for n, frs in raw.items()}

# ---- pixel shield used by the cat (and, scaled up, by the boss) ----
SHIELD_ART = [
    "..DDDDDD..",
    ".DLLLLLLD.",
    "DLLBBWBBLD",
    "DLBBBWBBBD",
    "DLWWWWWWLD",
    "DLBBBWBBBD",
    "DBBBBWBBBD",
    ".DBBBWBBD.",
    ".DBBBWBBD.",
    "..DBBWBD..",
    "...DBWD...",
    "....DD....",
]
PAL = {"D": (0x2a,0x2f,0x5c,255), "B": (0x4f,0x7f,0xb3,255),
       "L": (0x99,0xc2,0xdb,255), "W": (0xdf,0xed,0xed,255)}
def make_shield(glow=0, flash=False):
    h, w = len(SHIELD_ART), len(SHIELD_ART[0])
    im = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y, row in enumerate(SHIELD_ART):
        for x, ch in enumerate(row):
            if ch in PAL:
                c = PAL[ch]
                if flash and ch in "BL": c = (255, 255, 255, 255)
                elif glow and ch == "W": c = (255, 255, 255, 255) if glow > 1 else c
                im.putpixel((x, y), c)
    return im

def spark(cell, x, y, color=(255, 255, 255, 255)):
    for dx, dy in ((0,0),(1,0),(-1,0),(0,1),(0,-1)):
        if 0 <= x+dx < cell.width and 0 <= y+dy < cell.height:
            cell.putpixel((x+dx, y+dy), color)

idle = cat_cells["idle"]
shield = make_shield()
sx0 = half + 4                       # shield sits just in front of the cat
def with_shield(base, dx=0, dy=0, shield_img=shield, nudge=0):
    cell = Image.new("RGBA", (CW, CH), (0, 0, 0, 0))
    cell.alpha_composite(base, (-nudge, 0))
    cell.alpha_composite(shield_img, (sx0 + dx, FEET - shield_img.height - 1 + dy))
    return cell

# raise: shield swings up from low (3 frames), hold: 4 frames, hit: 3 frames
block_raise = [with_shield(idle[0], dx=-2, dy=9), with_shield(idle[1], dx=-1, dy=4), with_shield(idle[2], dx=0, dy=1)]
block_hold  = [with_shield(idle[i % 6], dy=(0, -1, 0, 1)[i]) for i in range(4)]
hit = []
for i, (dx, flash, nudge) in enumerate(((1, True, 1), (2, False, 2), (0, False, 1))):
    c = with_shield(idle[3 + i], dx=dx, shield_img=make_shield(flash=flash), nudge=nudge)
    if i == 0:
        for sx, sy in ((sx0 + 11, FEET - 12), (sx0 + 12, FEET - 7), (sx0 + 10, FEET - 16)):
            spark(c, sx, sy, (255, 245, 180, 255))
    if i == 1:
        for sx, sy in ((sx0 + 13, FEET - 14), (sx0 + 13, FEET - 5)):
            c.putpixel((sx, sy), (255, 245, 180, 255))
    hit.append(c)

for name, frs in (("block_raise", block_raise), ("block_hold", block_hold), ("block_hit", hit)):
    cat_cells[name] = frs

for name, frs in cat_cells.items():
    save_strip(strip(frs, CW, CH), "cat", f"{name}.png", len(frs))

# ----------------------------------------------------------------------
# 2) Flying demon (boss + mini boss)
# ----------------------------------------------------------------------
DEM = os.path.join(SRC, "flying_demon", "Sprites")
CELL_W, CELL_H = 81, 71
def demon_frames(name):
    im = load(os.path.join(DEM, "with_outline", f"{name}.png"))
    n = im.width // CELL_W
    return [im.crop((i * CELL_W, 0, (i + 1) * CELL_W, CELL_H)) for i in range(n)]

# the demon is drawn on a slightly bigger canvas so the guard shield fits
DW, DH = 120, 78
DEM_BASE_X = (DW - CELL_W) // 2
def dcell(frame, extra=None, extra_xy=(0, 0)):
    c = Image.new("RGBA", (DW, DH), (0, 0, 0, 0))
    c.alpha_composite(frame, (DEM_BASE_X, DH - CELL_H))
    if extra is not None:
        c.alpha_composite(extra, extra_xy)
    return c

# guard animation = the SAME shield art as the cat's block, scaled up
gx = DW // 2 - 36                       # in front of the demon (he faces left)
def gshield(scale_k, flash=False, glow=0):
    s = make_shield(flash=flash, glow=glow)
    s = s.resize((s.width * 3, s.height * 3), Image.NEAREST)
    if scale_k != 1:
        s = s.resize((int(s.width * scale_k), int(s.height * scale_k)), Image.NEAREST)
    return s
def guard_cell(base, k, dy=0, flash=False, glow=0):
    s = gshield(k, flash, glow)
    return dcell(base, s, (gx - s.width // 2 - 6, DH - s.height - 4 + dy))

def build_demon_set(suffix, tint):
    """Writes every demon strip for one colour variant. The shield is added AFTER
    tinting so it stays blue in every variant."""
    t = (lambda f: f) if tint is None else tint
    fr = {n: [t(f) for f in demon_frames(n.upper())] for n in ("idle", "flying", "attack", "hurt", "death")}
    sets = {
        "idle":   [dcell(f) for f in fr["idle"]],
        "fly":    [dcell(f) for f in fr["flying"]],
        "attack": [dcell(f) for f in fr["attack"]],
        "hurt":   [dcell(f) for f in fr["hurt"]],
        "death":  [dcell(f) for f in fr["death"]],
    }
    i = fr["idle"]
    sets["guard_raise"] = [guard_cell(i[0], 0.55, 10), guard_cell(i[1], 0.8, 4), guard_cell(i[2], 1.0, 0)]
    sets["guard_hold"]  = [guard_cell(i[k], 1.0, (0, -1, 0, 1)[k]) for k in range(4)]
    sets["guard_hit"]   = [guard_cell(i[0], 1.0, 0, flash=True), guard_cell(i[1], 1.0, 0), guard_cell(i[2], 1.0, 0)]
    for name, frs in sets.items():
        save_strip(strip(frs, DW, DH), "demon", f"{name}{suffix}.png", len(frs))

build_demon_set("", None)                                              # boss, first form
build_demon_set("_mutated", lambda f: hue_shift(f, 255, 1.05, 1.0))   # boss, second form
build_demon_set("_captain", lambda f: hue_shift(f, 170, 0.9, 1.0))    # mini bosses

proj = load(os.path.join(DEM, "projectile.png"))
proj.save(os.path.join(OUT, "demon", "fireball.png")); MANIFEST["demon/fireball.png"] = 1
hue_shift(proj, 255).save(os.path.join(OUT, "demon", "fireball_dark.png")); MANIFEST["demon/fireball_dark.png"] = 1

# ----------------------------------------------------------------------
# 3) Night-field tileset
# ----------------------------------------------------------------------
TS = load(os.path.join(SRC, "peaceful_nightfield", "tileset.png"))
def crop(name, x, y, w, h):
    TS.crop((x, y, x + w, y + h)).save(os.path.join(OUT, "tiles", name))
# ground (x,y are pixel coordinates in tileset.png, every tile is 8x8)
crop("grass_tl.png", 8, 8, 8, 8);  crop("grass_tm.png", 16, 8, 8, 8);  crop("grass_tr.png", 24, 8, 8, 8)
crop("grass_l.png", 8, 16, 8, 8);  crop("grass_r.png", 24, 16, 8, 8)
for i, (x, y) in enumerate(((8,48),(16,48),(24,48),(8,56),(16,56),(24,56))):
    crop(f"dirt_{i}.png", x, y, 8, 8)
# wooden bridge platform
crop("plank_l.png", 72, 24, 8, 8); crop("plank_m.png", 80, 24, 8, 8)
crop("plank_post.png", 88, 24, 8, 8); crop("plank_r.png", 96, 24, 8, 8)
# decoration
crop("tree_canopy.png", 40, 40, 24, 24)
crop("tree_trunk.png", 64, 48, 8, 16); crop("tree_root.png", 72, 56, 8, 8)
for i, x in enumerate((72, 80, 88, 96)):
    crop(f"flower_{i}.png", x, 8, 8, 8)
# background strip: night sky + mountains (tileable)
crop("sky_mountains.png", 112, 32, 64, 32)
crop("star_0.png", 128, 32, 8, 8); crop("star_1.png", 152, 40, 8, 8)

# sprite geometry the Java side needs
MANIFEST["_cat_feet_row"] = FEET        # row inside a cat cell where the feet touch the ground
MANIFEST["_demon_cell_w"] = DW
MANIFEST["_demon_cell_h"] = DH

with open(os.path.join(OUT, "manifest.json"), "w") as f:
    json.dump(MANIFEST, f, indent=1, sort_keys=True)
print("wrote", len(MANIFEST), "strips ->", OUT)

# The packs added later (Dead, Ramses, Platformer Slimes) have their own step.
# It merges its strips into the manifest.json written just above.
import subprocess, sys
subprocess.check_call([sys.executable, os.path.join(ROOT, "tools", "prepare_new_assets.py")])
