#!/usr/bin/env python3
"""
Second asset step: the packs added after the first release.
Run automatically at the end of prepare_assets.py (or on its own:  python3 tools/prepare_new_assets.py).
It only ADDS files to src/main/resources/assets/ and merges its strips into manifest.json.

  * Dead (assets_src/dead)           final boss - 80x80 cells on a 12-column grid:
                                       row 0 idle (8) / row 1 attack (8) / row 2 hurt (2) / row 3 death (12)
                                     + Fire.png (3 crescent-wave frames, 16x28 each)
                                     + HUD_Boss.png, split into the bar frame and the red fill strip
  * Ramses (assets_src/ramses)       new regular enemy - 40x40 cells (idle 7 / track 4 / walk 8)
  * Platformer Slimes (32x32)        three slime models (green, blue, red). Every sheet has two identical
                                     rows, so only the top 32 px row is kept.
"""
import json, os
from PIL import Image

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SRC = os.path.join(ROOT, "assets_src")
OUT = os.path.join(ROOT, "src", "main", "resources", "assets")
for sub in ("dead", "ramses", "slime", "ui"):
    os.makedirs(os.path.join(OUT, sub), exist_ok=True)

MANIFEST_PATH = os.path.join(OUT, "manifest.json")
MANIFEST = {}
if os.path.exists(MANIFEST_PATH):
    with open(MANIFEST_PATH) as f:
        MANIFEST = json.load(f)

def load(*p):
    return Image.open(os.path.join(SRC, *p)).convert("RGBA")

def save(img, sub, name, frames=None):
    img.save(os.path.join(OUT, sub, name))
    if frames:
        MANIFEST[f"{sub}/{name}"] = frames

# ---------------------------------------------------------------- Dead
sheet = load("dead", "Dead_anin.png")
CELL = 80
def dead_row(row, n):
    return sheet.crop((0, row * CELL, n * CELL, (row + 1) * CELL))
save(dead_row(0, 8),  "dead", "idle.png",   8)
save(dead_row(1, 8),  "dead", "attack.png", 8)
save(dead_row(2, 2),  "dead", "hurt.png",   2)
save(dead_row(3, 12), "dead", "death.png",  12)
save(load("dead", "Fire.png"), "dead", "fire.png", 3)

hud = load("dead", "HUD_Boss.png")
save(hud.crop((0, 0, 80, 24)),  "ui", "boss_frame.png")    # gold frame + mask icon (hollow 74x2 slot at x=3,y=19)
save(hud.crop((3, 27, 77, 29)), "ui", "boss_fill.png")     # the red 74x2 fill strip

# ---------------------------------------------------------------- Ramses
for name, src, frames in (("idle", "Spr_Idle.png", 7), ("track", "Spr_Track.png", 4), ("walk", "Spr_Walk.png", 8)):
    save(load("ramses", src), "ramses", name + ".png", frames)

# ---------------------------------------------------------------- Slimes
SLIME_FRAMES = {"Idle": 7, "Walk": 7, "Jump": 12, "Dead": 5}
for color in ("Green", "Blue", "Red"):
    for anim, frames in SLIME_FRAMES.items():
        img = load("platformer_slimes", "32x32", anim, f"{anim}_{color}_32.png")
        top = img.crop((0, 0, frames * 32, 32))
        save(top, "slime", f"{color.lower()}_{anim.lower()}.png", frames)

with open(MANIFEST_PATH, "w") as f:
    json.dump(MANIFEST, f, indent=1, sort_keys=True)
print("manifest now has", len(MANIFEST), "entries")
