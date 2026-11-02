# ⚔️ The Legacy Knight

A 2D action platformer written in plain **Java Swing** - no external libraries.
You play a sword-wielding cat knight, the last bearer of the Legacy Sword, fighting through four
night-field stages to face Dead, the Reaper.

**Goal of every stage:** defeat all enemies → grab the key → walk into the door.

## Story

Each stage opens with a short story page, and clearing it shows the next beat of the tale. There is a
prologue when you press START for the first time and an epilogue after the final boss.
All story text lives in one file: [`story/Story.java`](src/main/java/legacyknight/story/Story.java).

## Controls

| Input | Action |
|-------|--------|
| `A` / `D` (or arrows) | Move |
| `W` / `↑` / `Space` | Jump |
| `S` / `↓` | Crouch |
| Left click | Sword slash (two alternating swings, aimed toward the mouse) |
| Hold right click | Raise the shield - blocks 80% of damage from the front (has its own raise / hold / impact animation) |
| `P` | Pause |
| `Esc` | Leave stage / go back |
| `Enter` / click | Advance story pages |

**Tips**
- Killing a slime heals a little; every kill gives EXP, and levelling up refills your HP.
- Orange / red projectiles can be **blocked** with the shield or slashed away. **Dark red / purple ones cannot be blocked** - a pillar with a `!` on the ground appears about a second before they land.

## UI theme

All screens share one dark-castle look: stone panels with gold frames (the gold matches Dead's boss bar), pixel fonts (Press Start 2P for titles, VT323 for text) and pixel icons. The shared toolkit is `ui/Gui.java` (colours, fonts, panels, buttons, bars) and `ui/Sprites.java` (heart, gem, shield, sword, skull, key...). Change a colour or font there and every screen follows.

## Regular monsters

| Monster | Looks | Ability | How to beat it |
|---|---|---|---|
| **Hopper** | Green slime | Crouches, then leaps at you (even across small gaps) | Sidestep the landing, slash while it recovers |
| **Spitter** | Blue slime | Backs away and spits goo from range (blockable) | Close the distance fast, or block and advance |
| **Brute** | Big red slime (80 HP) | Flashes and shows `!`, then charges very fast; rests afterwards | Dodge the charge, hit it while it rests |
| **Ramses** | Crystal-shelled turtle | Stares you down (Track pose), then rushes. Armour halves your damage - **except while he is tired after a rush** (white glow) | Dodge the rush, then hit the glowing Ramses for full damage |

## Mini boss: the Demon King (MP3)

The old final boss is now the mini boss of MP3 (220 HP).

| | Form I (HP above 50%) | Form II - mutated (HP at or below 50%) |
|---|---|---|
| Look | Red demon | Bigger, purple, glowing aura |
| Attacks | Aimed fireball -> 3-way fan -> fast fireball | Ring of 12 fireballs -> wide 5-way fan -> **sky rain** (telegraphed, unblockable) -> 7-fireball sweep |
| Guard | Raises a shield for ~2 s every ~6 s (takes only 15% damage), then counter-fires | Guards more often but shorter (~1.5 s) |

At 50% HP the game gives a **warning**: red screen pulse, a `WARNING!` banner and a 3-second countdown. He cannot be hurt and stops attacking during it, then he transforms and knocks you back.

## Final boss: Dead the Reaper (MP4)

| | Form I (HP above 50%) | Form II - enraged (HP at or below 50%, purple) |
|---|---|---|
| Moves | Floats in and out to keep his distance | Faster |
| Attacks | Scythe wave (aimed) -> 3-way fan -> two fast red waves | Ring of 10 waves -> 5-way fan -> **soul rain** (telegraphed, unblockable) -> **teleport slash** -> 7-way fan |
| Close range | If you get too close he winds up a **scythe slash** (22 damage, blockable) - back off or raise your shield | same |

Every attack starts with the same wind-up pose (raised scythe) for 0.6 s. In form II he vanishes, a pillar marks where he will reappear, and he slashes right after. At 50% HP the WARNING countdown appears (3 s, invulnerable) before he awakens.

## Running the game

You need **JDK 8 or newer** (`javac -version`).

**macOS / Linux**
```bash
./scripts/run.sh
```

**Windows**
```bat
scripts\run.bat
```

**Maven** (not tested here - the scripts above are the verified path)
```bash
mvn package
java -jar target/the-legacy-knight-1.0.0.jar
```

**By hand**
```bash
mkdir out
javac -encoding UTF-8 -d out $(find src/main/java -name '*.java')
cp -r src/main/resources/. out/
java -cp out legacyknight.Main
```

> **ภาษาไทย (สรุป):** ต้องมี JDK 8 ขึ้นไป แล้วรัน `./scripts/run.sh` (Mac/Linux) หรือ `scripts\run.bat` (Windows)
> คอมเมนต์ในโค้ดเป็นภาษาไทยทั้งหมด จึงต้องคอมไพล์ด้วย `-encoding UTF-8` (สคริปต์ใส่ให้แล้ว)

## Project layout

```
the-legacy-knight/
├── assets_src/                  Original art packs, untouched (see CREDITS.md)
├── tools/prepare_assets.py      Turns assets_src/ into game-ready sprite strips
├── scripts/                     run.sh / run.bat
└── src/main/
    ├── resources/assets/        Generated sprite strips + tiles + manifest.json (committed so you can run without Python)
    └── java/legacyknight/
        ├── Main.java            Opens the window
        ├── core/                Game rules and screens
        │   ├── Constants.java       Every tunable number (gravity, speeds, scales, mutation time...)
        │   ├── Game.java            Which screen we are on, unlocked stages, story flow
        │   ├── GameState.java       MENU / TUTORIAL / STORY / MAP / PLAY / WIN / LOSE
        │   ├── PlayerStats.java     Level, EXP, max HP, attack (kept between stages)
        │   └── InputHandler.java    Keyboard and mouse
        ├── story/               Story.java (all the text) and Scene.java
        ├── level/               Level, Spawn, Theme (colour theme per stage), LevelFactory (the 4 stages)
        ├── entity/              Entity, Player, Enemy, EnemyType, Arrow (fireball), Telegraph, Particle
        ├── world/               What happens while playing
        │   ├── GameWorld.java       Everything in the current stage + damage / win / lose rules
        │   ├── PlayerController.java  Walk, jump, crouch, slash, block
        │   ├── EnemyAI.java         Hopper / Spitter / Brute / Ramses
        │   ├── DemonKingAI.java     Mini boss Demon King: guard, mutation warning, both forms' patterns
        │   ├── ReaperAI.java        Final boss Dead: scythe waves, soul rain, teleport slash, rage form
        │   └── Banner.java
        ├── assets/              Assets.java (image loader + recolouring), Animation.java
        └── ui/                  All drawing
            ├── CatRenderer.java       Picks the cat animation from the player's state
            ├── DemonRenderer.java     Picks the demon animation (guard, windup, mutation flicker...)
            ├── NightfieldRenderer.java  Sky, mountains, grass terrain, bridges, trees, flowers
            ├── WorldRenderer.java, HudRenderer.java, ScreenRenderer.java, Renderer.java, GamePanel.java
            ├── Gui.java, Sprites.java   Shared UI kit (panels, buttons, fonts, pixel icons)
            └── SlimeRenderer.java, RamsesRenderer.java, ReaperRenderer.java   Slimes, Ramses and Dead
```

`core` and `world` contain the rules and never draw anything; `ui` only reads state and draws it.

## Customising

| I want to... | Edit |
|--------------|------|
| Change jump height, speeds, sword reach, mutation warning length, sprite scales | `core/Constants.java` |
| Edit or add stages, change a stage's colours | `level/LevelFactory.java` (`new Theme(groundHue, skyHue, brightness)`) |
| Rewrite the story | `story/Story.java` |
| Change the Demon King (`world/DemonKingAI.java`), Dead (`world/ReaperAI.java`) or the regular monsters (`world/EnemyAI.java`) | attacks, timing, HP is in `level/LevelFactory.java` / `level/Level.java` |
| Change how the animations are chosen | `ui/CatRenderer.java`, `ui/DemonRenderer.java`, `ui/ReaperRenderer.java` |
| Replace or add art | Edit `assets_src/`, then `python3 tools/prepare_assets.py` (needs `pillow` and `numpy`) |

Adding a seventh stage: add it in `LevelFactory`, raise `LEVEL_COUNT`, add a chapter in `Story`,
and add a node rectangle in `ui/UiLayout.java`.

## Credits and licence

Code: [MIT](LICENSE). Art: third-party packs - see [CREDITS.md](CREDITS.md).
**Two of the three packs came without a licence file, so check their terms before publishing this repo publicly.**
