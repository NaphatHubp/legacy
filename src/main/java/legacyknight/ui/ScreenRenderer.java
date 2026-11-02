package legacyknight.ui;

import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import legacyknight.core.Game;
import legacyknight.level.Level;
import legacyknight.story.Scene;
import legacyknight.story.Story;

/**
 * วาดหน้าจอที่ไม่ใช่ฉากเล่น: เมนูหลัก, วิธีเล่น, แผนที่เลือกด่าน, เนื้อเรื่อง, ชนะ, แพ้ และหยุดเกม
 * ทุกหน้าใช้ชุดเครื่องมือกลางใน Gui (กรอบหินกรอบทอง ฟอนต์พิกเซล) ให้หน้าตาเหมือนกันทั้งเกม
 */
final class ScreenRenderer {

    private final NightfieldRenderer night;

    ScreenRenderer(NightfieldRenderer night) {
        this.night = night;
    }

    // =====================================================================
    //  เมนูหลัก
    // =====================================================================
    void drawMenu(Graphics2D g, Game game) {
        double time = game.getTime();
        int mouseX = game.getInput().mouseX, mouseY = game.getInput().mouseY;
        Level scenery = game.getLevels()[0];

        // ฉากทุ่งกลางคืนเลื่อนช้าๆ เป็นฉากหลัง
        night.drawBackground(g, scenery, time * 25, time);
        night.drawFloor(g, scenery, 476);
        Gui.dim(g, 70);
        Gui.vignette(g, 0.85f);

        // ชื่อเกม: มงกุฎ + ตัวอักษรทองขอบดำ
        int bob = (int) Math.round(Math.sin(time * 2) * 3);
        Gui.sprite(g, Sprites.CROWN, SCREEN_W / 2 - 27, 36 + bob, 6, true);
        Gui.heading(g, "THE LEGACY", 48, Gui.BONE, Gui.GOLD_D, SCREEN_W / 2.0, 150 + bob);
        Gui.heading(g, "KNIGHT", 72, Gui.GOLD_L, Gui.GOLD_D, SCREEN_W / 2.0, 232 + bob);
        Gui.text(g, "a tale of steel against Death", Gui.body(26), Gui.BONE_D, SCREEN_W / 2.0, 266, 0);

        for (int i = 0; i < UiLayout.MENU_BUTTONS.length; i++) {
            Rectangle r = UiLayout.MENU_BUTTONS[i];
            Gui.button(g, r, UiLayout.MENU_LABELS[i], r.contains(mouseX, mouseY), i == 0, time);
        }

        // ตัวละครประดับมุมจอ: แมวอัศวิน สไลม์เขียว Ramses และ Dead
        CatRenderer.drawIdle(g, 150, 482, 1, time, 4);
        SlimeRenderer.drawPortrait(g, 250, 482, false, time, "green", 3);
        RamsesRenderer.drawPortrait(g, 730, 482, false, time, 3);
        ReaperRenderer.drawPortrait(g, 840, 482, false, time, 3);
    }

    // =====================================================================
    //  วิธีเล่น: ซ้าย = ปุ่มควบคุม + เป้าหมาย / ขวา = สมุดบันทึกมอนสเตอร์
    // =====================================================================
    void drawTutorial(Graphics2D g, Game game) {
        double time = game.getTime();
        night.drawBackground(g, game.getLevels()[0], time * 10, time);
        Gui.dim(g, 150);
        Gui.vignette(g, 0.8f);

        Gui.panel(g, 36, 20, SCREEN_W - 72, SCREEN_H - 40, 0.96f);
        Gui.heading(g, "HOW TO PLAY", 24, Gui.GOLD_L, Gui.GOLD_D, SCREEN_W / 2.0, 70);
        Gui.divider(g, SCREEN_W / 2.0, 88, 520);

        // ----- ซ้าย: ปุ่ม -----
        int lx = 68;
        Gui.text(g, "CONTROLS", Gui.body(28), Gui.GOLD_L, lx, 128, -1);
        String[][] keys = { { "W", "Jump" }, { "A / D", "Move left / right" }, { "S", "Crouch" },
                { "L-CLICK", "Sword attack" }, { "R-CLICK", "Hold to raise shield" } };
        for (int i = 0; i < keys.length; i++) {
            int y = 142 + i * 36;
            keycap(g, lx, y, 104, 28, keys[i][0]);
            Gui.text(g, keys[i][1], Gui.body(24), Gui.BONE, lx + 120, y + 21, -1);
        }

        Gui.text(g, "GOAL", Gui.body(28), Gui.GOLD_L, lx, 350, -1);
        String[] goals = {
                "Slay every foe, take the key, then step through the door.",
                "Orange/red shots can be blocked or slashed. Dark ones cannot - dodge! Pillars show where they land.",
                "Kills give EXP.  P = pause, Esc = leave." };
        int y = 372;
        for (String goal : goals) {
            Gui.sprite(g, Sprites.POINTER, lx, y - 14, 2, false);
            y = Gui.paragraph(g, goal, Gui.body(21), Gui.BONE_D, lx + 20, y, 340, 20) + 4;
        }

        // ----- เส้นกั้นกลาง -----
        g.setColor(Gui.GOLD_D);
        g.fillRect(SCREEN_W / 2 - 6, 110, 2, SCREEN_H - 170);

        // ----- ขวา: มอนสเตอร์ -----
        int rx = SCREEN_W / 2 + 14;
        Gui.text(g, "FOES OF NIGHTFIELD", Gui.body(28), Gui.GOLD_L, rx, 128, -1);
        String[] names = { "Hopper", "Spitter", "Brute", "Ramses", "Demon King", "Dead" };
        String[] descs = { "Leaps at you. Dodge the landing.",
                "Spits goo. Block it or close in.",
                "Charges after a flash. Hit when tired.",
                "Armored. Strike when he tires.",
                "Mini boss. Mutates at half HP.",
                "Final boss. Vanishes and slashes." };
        for (int i = 0; i < names.length; i++) {
            int top = 140 + i * 58;
            portraitBox(g, rx, top, 64, 52);
            drawFoe(g, i, rx + 32, top + 48, time);
            Gui.text(g, names[i], Gui.body(25), i >= 4 ? Gui.BLOOD : Gui.BONE, rx + 78, top + 20, -1);
            Gui.paragraph(g, descs[i], Gui.body(20), Gui.BONE_D, rx + 78, top + 40, 340, 18);
        }

        Gui.text(g, "Click anywhere or press Esc to go back", Gui.body(22),
                Gui.withAlpha(Gui.BONE_D, (int) (150 + 100 * Math.sin(time * 4))), SCREEN_W / 2.0, SCREEN_H - 30, 0);
    }

    private static void keycap(Graphics2D g, int x, int y, int w, int h, String label) {
        g.setColor(Gui.INK);
        g.fillRect(x, y, w, h);
        g.setColor(Gui.STONE_L);
        g.fillRect(x + 2, y + 2, w - 4, h - 6);
        g.setColor(Gui.STONE);
        g.fillRect(x + 2, y + h - 6, w - 4, 4);
        Gui.text(g, label, Gui.body(24), Gui.BONE, x + w / 2.0, y + 20, 0);
    }

    private static void portraitBox(Graphics2D g, int x, int y, int w, int h) {
        g.setColor(Gui.INK);
        g.fillRect(x - 2, y - 2, w + 4, h + 4);
        g.setColor(Gui.GOLD_D);
        g.fillRect(x - 1, y - 1, w + 2, h + 2);
        g.setColor(Gui.NIGHT);
        g.fillRect(x, y, w, h);
    }

    private static void drawFoe(Graphics2D g, int index, double cx, double foot, double time) {
        java.awt.Shape clip = g.getClip();
        g.clipRect((int) cx - 32, (int) foot - 48, 64, 52);
        switch (index) {
            case 0: SlimeRenderer.drawPortrait(g, cx, foot, true, time, "green", 2); break;
            case 1: SlimeRenderer.drawPortrait(g, cx, foot, true, time, "blue", 2); break;
            case 2: SlimeRenderer.drawPortrait(g, cx, foot, true, time, "red", 2); break;
            case 3: RamsesRenderer.drawPortrait(g, cx, foot + 6, true, time, 2); break;
            case 4: DemonRenderer.drawPortrait(g, cx, foot - 2, false, time, "", 1); break;
            default: ReaperRenderer.drawPortrait(g, cx, foot + 8, false, time, 1); break;
        }
        g.setClip(clip);
    }

    // =====================================================================
    //  แผนที่เลือกด่าน
    // =====================================================================
    void drawMap(Graphics2D g, Game game) {
        double time = game.getTime();
        int mouseX = game.getInput().mouseX, mouseY = game.getInput().mouseY;
        Level[] levels = game.getLevels();
        int unlocked = game.getUnlocked();

        night.drawBackground(g, levels[0], time * 8, time);
        Gui.dim(g, 160);
        Gui.vignette(g, 0.9f);

        Gui.heading(g, "KINGDOM OF NIGHTFIELD", 24, Gui.GOLD_L, Gui.GOLD_D, SCREEN_W / 2.0, 62);
        Gui.divider(g, SCREEN_W / 2.0, 80, 560);
        Gui.text(g, "Clear a stage to unlock the next. Click an open stage to ride out.", Gui.body(24),
                Gui.BONE_D, SCREEN_W / 2.0, 106, 0);

        drawMapPaths(g, unlocked, time);

        for (int i = 0; i < levels.length; i++) {
            Rectangle r = UiLayout.MAP_NODES[i];
            Level lv = levels[i];
            boolean open = i <= unlocked;
            boolean cleared = game.isCleared(i);
            boolean hover = open && r.contains(mouseX, mouseY);

            Gui.panel(g, r.x, r.y, r.width, r.height, open ? 0.96f : 0.8f);
            if (!open) {
                g.setColor(new Color(6, 5, 14, 120));
                g.fillRect(r.x + 6, r.y + 6, r.width - 12, r.height - 12);
            }
            if (hover) {
                g.setColor(Gui.GOLD_L);
                g.drawRect(r.x - 3, r.y - 3, r.width + 5, r.height + 5);
                g.setColor(Gui.withAlpha(Gui.GOLD_L, 40));
                g.fillRect(r.x + 6, r.y + 6, r.width - 12, r.height - 12);
            }

            Color nameColor = open ? Gui.BONE : new Color(110, 108, 124);
            Gui.text(g, lv.code, Gui.title(24), open ? Gui.GOLD_L : new Color(100, 96, 110), r.getCenterX(), r.y + 46, 0);
            Gui.text(g, lv.name, Gui.body(27), nameColor, r.getCenterX(), r.y + 76, 0);

            // ป้ายสถานะ: ไอคอน + ข้อความ
            int by = r.y + 106;
            String label;
            Color color;
            Sprites.Sprite icon;
            if (!open) {
                label = "LOCKED"; color = new Color(120, 118, 134); icon = Sprites.LOCK;
            } else if (cleared) {
                label = "CLEARED"; color = Gui.MOSS; icon = Sprites.CHECK;
            } else if (i == 3) {
                label = "FINAL BOSS"; color = Gui.BLOOD; icon = Sprites.SKULL;
            } else if (i == 2) {
                label = "MINI BOSS"; color = Gui.BLOOD; icon = Sprites.SKULL;
            } else {
                label = "MINIONS"; color = Gui.BONE_D; icon = Sprites.SWORD;
            }
            int textW = g.getFontMetrics(Gui.body(22)).stringWidth(label);
            int iconScale = 2;
            int total = icon.width * iconScale + 8 + textW;
            int sx = (int) (r.getCenterX() - total / 2.0);
            Gui.sprite(g, icon, sx, by - 14, iconScale, open);
            Gui.text(g, label, Gui.body(22), color, sx + icon.width * iconScale + 8, by + 2, -1);
        }

        // แถบค่าสถานะอัศวิน
        Gui.panel(g, 190, SCREEN_H - 76, 580, 52, 0.94f);
        int sy = SCREEN_H - 43;
        Gui.sprite(g, Sprites.CROWN, 206, sy - 17, 2, false);
        Gui.text(g, "KNIGHT  LV " + game.getStats().getLevel(), Gui.body(25), Gui.GOLD_L, 232, sy, -1);
        Gui.sprite(g, Sprites.HEART, 400, sy - 16, 2, true);
        Gui.text(g, "" + game.getStats().getMaxHp(), Gui.body(25), Gui.BONE, 422, sy, -1);
        Gui.sprite(g, Sprites.SWORD, 490, sy - 20, 2, true);
        Gui.text(g, "" + game.getStats().getAttack(), Gui.body(25), Gui.BONE, 512, sy, -1);
        Gui.sprite(g, Sprites.GEM, 570, sy - 15, 2, true);
        Gui.text(g, game.getStats().getExp() + "/" + game.getStats().expNeeded(), Gui.body(25), Gui.BONE, 592, sy, -1);

        Rectangle back = UiLayout.BACK_BUTTON;
        Gui.button(g, back, "BACK", back.contains(mouseX, mouseY), false, time);
    }

    /** เส้นทางประแบบจุดทองระหว่างด่าน (ด่านที่เปิดแล้วเรืองทอง ที่ยังล็อกเป็นสีหม่น) */
    private void drawMapPaths(Graphics2D g, int unlocked, double time) {
        for (int i = 0; i < UiLayout.MAP_NODES.length - 1; i++) {
            Rectangle a = UiLayout.MAP_NODES[i], b = UiLayout.MAP_NODES[i + 1];
            boolean lit = i < unlocked;
            double x1, y1, x2, y2;
            if (a.y == b.y) {
                boolean rightward = b.x > a.x;
                x1 = rightward ? a.x + a.width + 8 : a.x - 8;
                x2 = rightward ? b.x - 8 : b.x + b.width + 8;
                y1 = y2 = a.getCenterY();
            } else {
                x1 = x2 = a.getCenterX();
                y1 = a.y + a.height + 8;
                y2 = b.y - 8;
            }
            double len = Math.hypot(x2 - x1, y2 - y1);
            int dots = (int) (len / 16);
            for (int d = 0; d <= dots; d++) {
                double t = d / (double) dots;
                int px = (int) Math.round(x1 + (x2 - x1) * t) - 3;
                int py = (int) Math.round(y1 + (y2 - y1) * t) - 3;
                double glow = lit ? 0.5 + 0.5 * Math.sin(time * 5 - d * 0.7) : 0;
                g.setColor(Gui.INK);
                g.fillRect(px - 1, py - 1, 8, 8);
                g.setColor(lit ? (glow > 0.6 ? Gui.GOLD_L : Gui.GOLD) : Gui.STONE_L);
                g.fillRect(px, py, 6, 6);
            }
        }
    }

    // =====================================================================
    //  ชนะ / แพ้ / หยุด (วาดทับฉากเล่นอีกที)
    // =====================================================================
    void drawWin(Graphics2D g, Game game) {
        double time = game.getTime();
        int index = game.getLevelIndex();
        Level[] levels = game.getLevels();

        Gui.dim(g, 150);
        Gui.vignette(g, 0.9f);
        Gui.panel(g, 150, 70, SCREEN_W - 300, 400, 0.96f);

        int bob = (int) Math.round(Math.sin(time * 3) * 3);
        Gui.sprite(g, Sprites.CROWN, SCREEN_W / 2 - 27, 100 + bob, 6, true);
        Gui.heading(g, "VICTORY", 48, Gui.GOLD_L, Gui.GOLD_D, SCREEN_W / 2.0, 214);
        Gui.text(g, levels[index].code + "  " + levels[index].name + "  cleared", Gui.body(30), Gui.MOSS,
                SCREEN_W / 2.0, 258, 0);
        if (index < levels.length - 1) {
            Gui.text(g, levels[index + 1].code + " unlocked", Gui.body(24), Gui.BONE, SCREEN_W / 2.0, 286, 0);
        }
        Gui.divider(g, SCREEN_W / 2.0, 304, 420);

        // ข้อความต่อเนื่องของเนื้อเรื่อง
        int y = 340;
        for (String line : Gui.wrap(g, Story.outro(index), Gui.body(25), 600)) {
            Gui.text(g, line, Gui.body(25), Gui.BONE_D, SCREEN_W / 2.0, y, 0);
            y += 26;
        }
        Gui.text(g, index == levels.length - 1 ? "Click or press Enter to continue"
                        : "Click or press Enter to return to the map", Gui.body(22),
                Gui.withAlpha(Gui.GOLD_L, (int) (150 + 100 * Math.sin(time * 4))), SCREEN_W / 2.0, 452, 0);
    }

    void drawLose(Graphics2D g, double time) {
        g.setColor(new Color(30, 0, 8, 175));
        g.fillRect(0, 0, SCREEN_W, SCREEN_H);
        Gui.vignette(g, 1f);
        int bob = (int) Math.round(Math.sin(time * 2.5) * 3);
        Gui.sprite(g, Sprites.SKULL, SCREEN_W / 2 - 36, 150 + bob, 8, true);
        Gui.heading(g, "YOU HAVE FALLEN", 32, Gui.BLOOD, Gui.BLOOD_D, SCREEN_W / 2.0, 290);
        Gui.divider(g, SCREEN_W / 2.0, 312, 480);
        Gui.text(g, "Click or press R to rise again", Gui.body(30), Gui.BONE, SCREEN_W / 2.0, 352, 0);
        Gui.text(g, "Esc = back to map", Gui.body(24), Gui.BONE_D, SCREEN_W / 2.0, 384, 0);
    }

    // =====================================================================
    //  หน้าเนื้อเรื่อง: ฉากทุ่งกลางคืน ตัวละคร กล่องบทพูดแบบพิมพ์ดีด
    // =====================================================================
    void drawStory(Graphics2D g, Game game) {
        double time = game.getTime();
        Scene scene = game.getScene();
        int chapter = game.getStoryLevel();
        Level scenery = game.getLevels()[chapter];

        night.drawBackground(g, scenery, time * 12, time);
        night.drawFloor(g, scenery, 268);

        // ตัวละคร: แมวอยู่ซ้ายเสมอ ฝั่งขวาเป็นศัตรูของบทนั้น
        CatRenderer.drawIdle(g, 170, 276, 1, time, 5);
        boolean prologue = scene.title.equals("The Legacy Knight");
        boolean epilogue = scene.title.equals("The Legacy Continues");
        if (prologue || chapter == 3 && !epilogue) {
            ReaperRenderer.drawPortrait(g, 780, 276, false, time, 3);
        } else if (!epilogue && chapter == 2) {
            DemonRenderer.drawPortrait(g, 780, 262, false, time, "", 2);
        } else if (!epilogue && chapter == 1) {
            RamsesRenderer.drawPortrait(g, 770, 276, false, time, 4);
        } else if (!epilogue) {
            SlimeRenderer.drawPortrait(g, 760, 276, false, time, "blue", 4);
        }
        Gui.vignette(g, 0.7f);

        // แผ่นป้ายชื่อบท
        Gui.heading(g, scene.title.toUpperCase(), scene.title.length() > 24 ? 16 : 24, Gui.GOLD_L, Gui.GOLD_D,
                SCREEN_W / 2.0, 66);
        Gui.divider(g, SCREEN_W / 2.0, 84, 520);

        // กล่องบทพูด
        int bx = 50, by = 312, bw = SCREEN_W - 100, bh = 200;
        Gui.panel(g, bx, by, bw, bh, 0.95f);

        // พิมพ์ทีละตัวอักษร (นับรวมตัวขึ้นบรรทัดใหม่ของย่อหน้า)
        java.awt.Font f = Gui.body(27);
        int remaining = game.getStoryChars();
        int y = by + 46;
        for (String paragraph : scene.paragraphs) {
            for (String line : Gui.wrap(g, paragraph, f, bw - 72)) {
                if (remaining <= 0) break;
                String shown = line.length() <= remaining ? line : line.substring(0, remaining);
                Gui.text(g, shown, f, Gui.BONE, bx + 34, y, -1);
                remaining -= line.length() + 1;
                y += 28;
            }
            remaining -= 1;       // ขึ้นย่อหน้าใหม่
            y += 8;
        }

        // คำใบ้มุมล่างขวาของกล่อง
        boolean done = game.isStoryTextComplete();
        String hint = done ? "Click / Enter to continue   (Esc = map)" : "Click / Enter to show all text";
        Gui.text(g, hint, Gui.body(21),
                Gui.withAlpha(Gui.GOLD_L, done ? (int) (150 + 100 * Math.sin(time * 4)) : 150),
                bx + bw - 30, by + bh - 20, 1);
    }

    void drawPause(Graphics2D g, double time) {
        Gui.dim(g, 160);
        Gui.panel(g, SCREEN_W / 2 - 170, 190, 340, 170, 0.96f);
        Gui.heading(g, "PAUSED", 32, Gui.GOLD_L, Gui.GOLD_D, SCREEN_W / 2.0, 262);
        Gui.divider(g, SCREEN_W / 2.0, 284, 240);
        Gui.text(g, "Press P to resume", Gui.body(26), Gui.BONE, SCREEN_W / 2.0, 322, 0);
        Gui.text(g, "Esc = back to map", Gui.body(22), Gui.BONE_D, SCREEN_W / 2.0, 346, 0);
    }
}
