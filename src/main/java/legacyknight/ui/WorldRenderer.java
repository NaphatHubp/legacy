package legacyknight.ui;

import static legacyknight.core.Constants.ATTACK_TIME;
import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;

import legacyknight.assets.Assets;
import legacyknight.core.Game;
import legacyknight.entity.Arrow;
import legacyknight.entity.Enemy;
import legacyknight.entity.EnemyType;
import legacyknight.entity.Particle;
import legacyknight.entity.Telegraph;
import legacyknight.level.Level;
import legacyknight.util.MathUtil;
import legacyknight.world.GameWorld;

/**
 * วาดฉากตอนเล่นด่าน: พื้นหลังและพื้นแบบ Nightfield, หนาม ประตู กุญแจ, ศัตรู, ลูกไฟ,
 * จุดเตือนฝนไฟ, ผู้เล่น, เอฟเฟกต์ และ HUD
 */
final class WorldRenderer {

    /** ง้างปากก่อนยิงกี่วินาที (ต้องตรงกับ EnemyAI.WINDUP ซึ่งอยู่คนละแพ็กเกจ เลยประกาศซ้ำ) */
    private static final double WINDUP = 0.6;

    private static final BufferedImage FIREBALL = Assets.image("demon/fireball.png");
    private static final BufferedImage FIREBALL_DARK = Assets.image("demon/fireball_dark.png");

    private final NightfieldRenderer night;

    WorldRenderer(NightfieldRenderer night) {
        this.night = night;
    }

    void draw(Graphics2D g, Game game) {
        GameWorld world = game.getWorld();
        Level level = world.level;
        double time = game.getTime();
        double camX = world.cameraX;

        AffineTransform screen = g.getTransform();

        // จอสั่น (ตอนบอสกลายพันธุ์ ลูกไฟระเบิด ฯลฯ) สั่นเฉพาะฉาก ไม่สั่น HUD
        if (world.shakeTime > 0) {
            double m = world.shakeMagnitude * Math.min(1, world.shakeTime / 0.3);
            g.translate((Math.random() - 0.5) * 2 * m, (Math.random() - 0.5) * 2 * m);
        }

        night.drawBackground(g, level, camX, time);

        // ของในด่าน (เลื่อนตามกล้อง)
        AffineTransform beforeCamera = g.getTransform();
        g.translate(-Math.round(camX), 0);

        night.drawTrees(g, level, camX);
        night.drawTerrain(g, level, camX);
        drawSpikes(g, level);
        drawDoor(g, world, time);
        drawKey(g, world, time);
        for (Telegraph t : world.telegraphs) drawTelegraph(g, t, time);

        for (Enemy e : world.enemies) {
            if (e.isAlive()) drawEnemy(g, world, e, time);
            else drawEnemy(g, world, e, time);                      // แอนิเมชันตาย (ถ้ามี)
        }
        for (Arrow a : world.arrows) drawFireball(g, a);

        CatRenderer.draw(g, world, time);

        for (Particle q : world.particles) {
            int alpha = MathUtil.clampInt((int) (255 * q.life / q.maxLife), 0, 255);
            g.setColor(new Color(q.color.getRed(), q.color.getGreen(), q.color.getBlue(), alpha));
            g.fill(new Ellipse2D.Double(q.x - q.size / 2, q.y - q.size / 2, q.size, q.size));
        }
        g.setTransform(beforeCamera);
        g.setTransform(screen);

        HudRenderer.draw(g, game);
    }

    private static void drawSpikes(Graphics2D g, Level level) {
        for (Rectangle s : level.spikes) {
            for (int sx = s.x; sx + 16 <= s.x + s.width; sx += 16) {
                Path2D tri = new Path2D.Double();
                tri.moveTo(sx, s.y + s.height);
                tri.lineTo(sx + 8, s.y);
                tri.lineTo(sx + 16, s.y + s.height);
                tri.closePath();
                g.setColor(new Color(190, 195, 205));
                g.fill(tri);
                g.setColor(new Color(80, 85, 100));
                g.setStroke(new BasicStroke(1.5f));
                g.draw(tri);
            }
        }
    }

    // ---------- ประตู: ล็อกอยู่เป็นสีน้ำตาล / เปิดได้แล้วเรืองสีเขียว ----------
    private static void drawDoor(Graphics2D g, GameWorld world, double time) {
        Rectangle d = world.level.door;
        boolean open = world.hasKey && world.aliveCount() == 0;
        if (open) {
            int alpha = 60 + (int) (40 * Math.sin(time * 4));
            g.setColor(new Color(90, 240, 140, alpha));
            g.fill(new RoundRectangle2D.Double(d.x - 10, d.y - 10, d.width + 20, d.height + 20, 24, 24));
        }
        g.setColor(new Color(70, 48, 36));
        g.fill(new RoundRectangle2D.Double(d.x - 4, d.y - 4, d.width + 8, d.height + 4, 16, 16));
        g.setColor(open ? new Color(30, 40, 50) : new Color(140, 96, 60));
        g.fill(new RoundRectangle2D.Double(d.x, d.y, d.width, d.height, 14, 14));
        if (open) {
            g.setColor(new Color(120, 255, 170));
            g.fill(new Ellipse2D.Double(d.x + 12, d.y + 26, 20, 28));
        } else {
            g.setColor(new Color(105, 70, 42));
            g.setStroke(new BasicStroke(2));
            g.drawLine(d.x + d.width / 2, d.y + 6, d.x + d.width / 2, d.y + d.height);
            g.setColor(new Color(240, 200, 70));
            g.fill(new RoundRectangle2D.Double(d.x + 14, d.y + 40, 16, 13, 4, 4));
            g.setStroke(new BasicStroke(2.5f));
            g.draw(new Arc2D.Double(d.x + 17, d.y + 31, 10, 14, 0, 180, Arc2D.OPEN));
        }
    }

    // ---------- กุญแจลอยกระเพื่อม ----------
    private static void drawKey(Graphics2D g, GameWorld world, double time) {
        if (world.hasKey) return;
        double kx = world.level.key.x;
        double ky = world.level.key.y + Math.sin(time * 3) * 4;
        g.setColor(new Color(255, 230, 120, 70));
        g.fill(new Ellipse2D.Double(kx - 18, ky - 18, 36, 36));
        g.setColor(new Color(245, 200, 50));
        g.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.draw(new Ellipse2D.Double(kx - 6, ky - 12, 12, 12));
        g.draw(new Line2D.Double(kx, ky, kx, ky + 14));
        g.draw(new Line2D.Double(kx, ky + 8, kx + 6, ky + 8));
        g.draw(new Line2D.Double(kx, ky + 13, kx + 5, ky + 13));
    }

    // ---------- ศัตรู ----------
    private void drawEnemy(Graphics2D g, GameWorld world, Enemy e, double time) {
        if (e.type.isBoss()) {
            if (e.type.isReaper()) ReaperRenderer.draw(g, e, world.player, time, WINDUP);
            else DemonRenderer.draw(g, e, world.player, time, WINDUP);
            return;
        }
        double cx = e.x + e.w / 2.0;
        if (e.type == EnemyType.RAMSES) RamsesRenderer.draw(g, e, time);
        else SlimeRenderer.draw(g, e, time);
        if (e.isAlive() && e.getHp() < e.getMaxHp()) {
            // แถบเลือดเล็กๆ เหนือหัว (โผล่เมื่อโดนตีแล้ว)
            int top = (int) e.y - (e.type == EnemyType.RAMSES ? 44 : 14);
            g.setColor(new Color(0, 0, 0, 150));
            g.fillRect((int) cx - 20, top, 40, 5);
            g.setColor(new Color(255, 90, 110));
            g.fillRect((int) cx - 20, top, (int) (40.0 * e.getHp() / e.getMaxHp()), 5);
        }
    }

    // ---------- ลูกยิง: ลูกไฟ (ส้ม = กันได้ / ม่วง = กันไม่ได้), เมือกสไลม์น้ำเงิน, คลื่นเคียวของ Dead ----------
    private void drawFireball(Graphics2D g, Arrow a) {
        AffineTransform old = g.getTransform();
        g.translate(a.x, a.y);
        if (a.style == Arrow.SPIT) {
            g.setColor(new Color(60, 120, 215));
            g.fill(new Ellipse2D.Double(-10, -8, 20, 16));
            g.setColor(new Color(130, 200, 255));
            g.fill(new Ellipse2D.Double(-8, -6, 16, 12));
            g.setColor(new Color(255, 255, 255, 190));
            g.fill(new Ellipse2D.Double(-5, -5, 6, 4));
            g.setTransform(old);
            return;
        }
        if (a.style == Arrow.SCYTHE) {
            g.rotate(Math.atan2(a.vy, a.vx));                // รูป Fire.png พุ่งไปทางขวา
            BufferedImage f = Assets.animation("dead/fire.png").frame(a.frame);
            g.drawImage(f, -16, -28, 32, 56, null);
            g.setTransform(old);
            return;
        }
        g.rotate(Math.atan2(a.vy, a.vx) + Math.PI);      // รูปต้นฉบับพุ่งไปทางซ้าย
        g.drawImage(a.blockable ? FIREBALL : FIREBALL_DARK, -11, -16, null);
        g.setTransform(old);
    }

    // ---------- จุดเตือนฝนไฟ: เสาแดงกะพริบบนพื้น ยิ่งใกล้ตกยิ่งเข้ม ----------
    private void drawTelegraph(Graphics2D g, Telegraph t, double time) {
        double urgency = 1 - t.timer / t.total;                       // 0 -> 1
        float alpha = (float) (0.15 + 0.35 * urgency);
        double flicker = 0.7 + 0.3 * Math.sin(time * 30);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (alpha * flicker)));
        g.setColor(new Color(220, 70, 255));
        g.fill(new Rectangle((int) t.x - 16, 0, 32, (int) t.surfaceY));
        g.setComposite(AlphaComposite.SrcOver);

        // เครื่องหมายตกใจที่พื้น
        g.setColor(new Color(255, 230, 120));
        g.setFont(new Font("SansSerif", Font.BOLD, 30));
        DrawUtil.centered(g, "!", t.x, t.surfaceY - 6);
    }
}
