package legacyknight.ui;

import static legacyknight.core.Constants.ATTACK_COOLDOWN;
import static legacyknight.core.Constants.MUTATION_WARNING_TIME;
import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import legacyknight.assets.Assets;
import legacyknight.core.Game;
import legacyknight.core.GameState;
import legacyknight.core.PlayerStats;
import legacyknight.entity.Enemy;
import legacyknight.entity.Player;
import legacyknight.world.Banner;
import legacyknight.world.GameWorld;

/**
 * วาดข้อมูลที่ลอยอยู่บนจอตอนเล่น (HUD):
 * แถบเลือด แถบ EXP ภารกิจมุมขวาบน แถบเลือดบอส ไอคอนโล่/ดาบด้านล่าง และข้อความ Banner
 */
final class HudRenderer {

    private HudRenderer() { }

    static void draw(Graphics2D g, Game game) {
        GameWorld world = game.getWorld();
        Player p = world.player;
        PlayerStats stats = game.getStats();

        drawHealthAndExp(g, p, stats);
        drawObjective(g, world);
        drawBossBar(g, world);
        drawMutationWarning(g, world, game.getTime());
        drawActionIcons(g, p);
        drawBanner(g, world.banner, game.getState());
    }

    // ---------- เลือด (หัวใจ + แถบแดง) และ EXP (อัญมณี + แถบเขียว) ----------
    private static void drawHealthAndExp(Graphics2D g, Player p, PlayerStats stats) {
        Gui.panel(g, 10, 10, 330, 84, 0.9f);
        Gui.sprite(g, Sprites.HEART, 26, 27, 3, true);
        double hpFrac = p.hp / (double) stats.maxHp;
        Gui.bar(g, 62, 28, 200, 14, hpFrac, hpFrac < 0.3 ? new Color(255, 90, 80) : new Color(226, 84, 84),
                new Color(138, 30, 48), 10);
        Gui.text(g, p.hp + "/" + stats.maxHp, Gui.body(22), Gui.BONE, 276, 42, -1);

        Gui.sprite(g, Sprites.GEM, 26, 56, 3, true);
        Gui.bar(g, 62, 60, 200, 8, stats.exp / (double) stats.expNeeded(), new Color(130, 224, 140),
                new Color(48, 130, 76), 0);
        Gui.text(g, "LV " + stats.level, Gui.body(22), Gui.GOLD_L, 276, 70, -1);
        Gui.text(g, "ATK " + stats.attack, Gui.body(20), Gui.BONE_D, 62, 88, -1);
    }

    // ---------- ภารกิจ (มุมขวาบน) ----------
    private static void drawObjective(Graphics2D g, GameWorld world) {
        int x = SCREEN_W - 260;
        Gui.panel(g, x, 10, 250, 84, 0.9f);
        Gui.text(g, world.level.code + "  " + world.level.name, Gui.body(24), Gui.GOLD_L, x + 18, 36, -1);
        Gui.sprite(g, Sprites.SKULL, x + 18, 46, 2, true);
        Gui.text(g, "FOES  " + world.aliveCount(), Gui.body(23), Gui.BONE, x + 46, 62, -1);
        Gui.sprite(g, Sprites.KEY, x + 18, 72, 2, world.hasKey);
        Gui.text(g, world.hasKey ? "KEY  GOT IT!" : "KEY  NOT YET", Gui.body(23),
                world.hasKey ? Gui.GOLD_L : Gui.BONE_D, x + 46, 86, -1);
    }

    // ---------- แถบเลือดบอส (โผล่เมื่อมินิบอส/บอสอยู่ใกล้) ----------
    private static void drawBossBar(Graphics2D g, GameWorld world) {
        Enemy boss = null;
        for (Enemy e : world.enemies) {
            if (e.alive && e.type.isBoss() && Math.abs(e.x - world.player.x) < 900) {
                boss = e;
                break;
            }
        }
        if (boss == null) return;

        if (boss.type.isReaper()) {
            drawReaperBar(g, boss);
            return;
        }
        int w = 360, x = SCREEN_W / 2 - w / 2 + 40, y = 118;
        Color top = boss.mutated ? new Color(210, 130, 255) : new Color(240, 90, 80);
        Color bottom = boss.mutated ? new Color(96, 36, 160) : new Color(140, 26, 40);
        Gui.sprite(g, Sprites.SKULL, x - 34, y - 6, 3, true);
        Gui.bar(g, x, y, w, 16, boss.hp / (double) boss.maxHp, top, bottom, 0);

        // ขีดกลางแถบ = จุดที่เลือดเหลือครึ่งเดียวแล้วจะกลายพันธุ์
        g.setColor(new Color(255, 255, 255, 200));
        g.fillRect(x + w / 2 - 1, y - 6, 2, 28);

        String label = boss.mutated ? "DEMON KING  MUTATED" : "DEMON KING";
        Gui.text(g, label, Gui.body(24), boss.mutated ? new Color(220, 160, 255) : Gui.BONE, x, y - 10, -1);
        if (boss.guardTimer > 0) {
            Gui.text(g, "GUARDING", Gui.body(24), new Color(150, 200, 255), x + w, y - 10, 1);
        }
    }

    // ---------- แถบเลือด Dead: ใช้ภาพกรอบทอง + หน้ากากจากชุด HUD_Boss (ช่องใน 74x2 พิกเซล ขยาย 4 เท่า) ----------
    private static void drawReaperBar(Graphics2D g, Enemy boss) {
        final int s = 4;
        BufferedImage frame = Assets.image("ui/boss_frame.png");
        BufferedImage fill = Assets.image("ui/boss_fill.png");
        int fx = SCREEN_W / 2 + 40 - frame.getWidth() * s / 2, fy = 52;
        int ix = fx + 3 * s, iy = fy + 19 * s, iw = fill.getWidth() * s, ih = fill.getHeight() * s;
        double frac = Math.max(0, boss.hp / (double) boss.maxHp);

        g.setColor(new Color(10, 8, 20, 220));
        g.fillRect(ix, iy, iw, ih);
        if (boss.mutated) {
            g.setColor(new Color(220, 70, 190));
            g.fillRect(ix, iy, (int) (iw * frac), ih);
        } else {
            int fw = (int) (iw * frac);
            int srcW = (int) Math.max(1, Math.round(fill.getWidth() * frac));
            g.drawImage(fill, ix, iy, ix + fw, iy + ih, 0, 0, srcW, fill.getHeight(), null);
        }
        g.drawImage(frame, fx, fy, frame.getWidth() * s, frame.getHeight() * s, null);

        // ขีดกลางแถบ = จุดที่เลือดเหลือครึ่งเดียวแล้ว Dead จะเปลี่ยนร่าง
        g.setColor(new Color(255, 255, 255, 200));
        g.fillRect(ix + iw / 2 - 1, iy - 4, 2, ih + 8);

        Gui.text(g, boss.mutated ? "DEAD  ENRAGED" : "DEAD", Gui.body(26),
                boss.mutated ? new Color(230, 140, 255) : Gui.GOLD_L, fx - 8, iy + ih + 2, 1);
    }

    // ---------- คำเตือนก่อนบอสกลายพันธุ์: ขอบจอแดงกะพริบ + ข้อความ + แถบนับถอยหลัง ----------
    private static void drawMutationWarning(Graphics2D g, GameWorld world, double time) {
        Enemy mutating = null;
        for (Enemy e : world.enemies) {
            if (e.alive && e.mutating) { mutating = e; break; }
        }
        if (mutating == null) return;

        double progress = 1 - mutating.mutationTimer / MUTATION_WARNING_TIME;       // 0 -> 1
        double pulse = 0.5 + 0.5 * Math.sin(time * (8 + 14 * progress));

        // ขอบจอเป็นสีแดงเลือด (กลางจอใส) กะพริบถี่ขึ้นเรื่อยๆ
        float alpha = (float) (0.25 + 0.35 * pulse);
        RadialGradientPaint vignette = new RadialGradientPaint(
                new Point2D.Double(SCREEN_W / 2.0, SCREEN_H / 2.0), (float) (SCREEN_W * 0.62),
                new float[] { 0f, 0.55f, 1f },
                new Color[] { new Color(200, 0, 40, 0), new Color(200, 0, 40, 0), new Color(200, 0, 50, (int) (255 * alpha)) });
        g.setPaint(vignette);
        g.fillRect(0, 0, SCREEN_W, SCREEN_H);

        // ข้อความ WARNING (อยู่ด้านบนจอ เพื่อไม่บังตัวบอส)
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.7 + 0.3 * pulse)));
        Gui.heading(g, "WARNING!", 40, Gui.BLOOD, Gui.BLOOD_D, SCREEN_W / 2.0, 198);
        g.setComposite(AlphaComposite.SrcOver);
        Gui.text(g, mutating.type.isReaper()
                        ? "Dead is awakening - he can't be hurt right now. Get ready!"
                        : "The Demon King is mutating - he can't be hurt right now. Get ready!",
                Gui.body(26), Gui.BONE, SCREEN_W / 2.0, 232, 0);

        // แถบนับถอยหลัง + ตัวเลข
        int bw = 360, bx = SCREEN_W / 2 - bw / 2 - 20, by = 248;
        Gui.bar(g, bx, by, bw, 12, progress, new Color(255, 120, 110), Gui.BLOOD_D, 0);
        Gui.text(g, "" + (int) Math.ceil(mutating.mutationTimer), Gui.title(16), Gui.GOLD_L, bx + bw + 20, by + 12, -1);
    }

    // ---------- ไอคอนโล่/ดาบด้านล่าง ----------
    private static void drawActionIcons(Graphics2D g, Player p) {
        Gui.panel(g, SCREEN_W / 2 - 112, SCREEN_H - 92, 224, 82, 0.88f);
        int shieldX = SCREEN_W / 2 - 80, swordX = SCREEN_W / 2 + 32, boxY = SCREEN_H - 82;
        int size = 44;

        Gui.slot(g, shieldX, boxY, size, p.blocking);
        Gui.sprite(g, Sprites.SHIELD, shieldX + 8, boxY + 5, 3, true);
        Gui.slot(g, swordX, boxY, size, p.attackTimer > 0);
        Gui.sprite(g, Sprites.SWORD, swordX + 11, boxY + 5, 3, true);

        // แถบมืดไล่ลงตามเวลาคูลดาวน์ดาบ
        double cd = Math.max(0, p.attackCooldown / ATTACK_COOLDOWN);
        if (cd > 0) {
            g.setColor(new Color(6, 5, 14, 170));
            g.fillRect(swordX, boxY, size, (int) (size * cd));
        }
        Gui.text(g, "R-CLICK", Gui.body(19), p.blocking ? Gui.GOLD_L : Gui.BONE_D, shieldX + size / 2.0, SCREEN_H - 16, 0);
        Gui.text(g, "L-CLICK", Gui.body(19), p.attackTimer > 0 ? Gui.GOLD_L : Gui.BONE_D, swordX + size / 2.0, SCREEN_H - 16, 0);
    }

    // ---------- ข้อความกลางจอ ----------
    private static void drawBanner(Graphics2D g, Banner banner, GameState state) {
        if (!banner.isActive() || state != GameState.PLAY) return;
        float a = banner.getAlpha();
        Gui.ribbon(g, 124, 48, a);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
        Gui.heading(g, banner.getText().toUpperCase(), 16, Gui.GOLD_L, Gui.GOLD_D, SCREEN_W / 2.0, 156);
        g.setComposite(AlphaComposite.SrcOver);
    }
}
