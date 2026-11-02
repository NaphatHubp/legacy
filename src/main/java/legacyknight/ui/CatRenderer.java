package legacyknight.ui;

import static legacyknight.core.Constants.ATTACK_TIME;
import static legacyknight.core.Constants.CAT_SCALE;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import legacyknight.assets.Animation;
import legacyknight.assets.Assets;
import legacyknight.entity.Player;
import legacyknight.world.GameWorld;

/**
 * วาดแมวอัศวินจากสไปรต์พิกเซล และเลือกว่า "ตอนนี้ควรเล่นแอนิเมชันไหน" จากสถานะของผู้เล่น
 *
 *   ตาย > โดนตี > ยกโล่ (ยกขึ้น / ค้างไว้ / โล่สะท้อน) > ฟันดาบ > ย่อ > กระโดด > วิ่ง > ยืนนิ่ง
 */
final class CatRenderer {

    private static final Animation IDLE = Assets.animation("cat/idle.png");
    private static final Animation RUN = Assets.animation("cat/run.png");
    private static final Animation JUMP = Assets.animation("cat/jump.png");
    private static final Animation HURT = Assets.animation("cat/hurt.png");
    private static final Animation CROUCH = Assets.animation("cat/dodge.png");   // เฟรมที่ 4 = ท่าหมอบตัวกลม
    private static final Animation DEATH = Assets.animation("cat/death.png");
    private static final Animation[] SLASH = { Assets.animation("cat/slash1.png"), Assets.animation("cat/slash2.png") };
    private static final Animation BLOCK_RAISE = Assets.animation("cat/block_raise.png");
    private static final Animation BLOCK_HOLD = Assets.animation("cat/block_hold.png");
    private static final Animation BLOCK_HIT = Assets.animation("cat/block_hit.png");

    /** แถวในรูปที่เป็นระดับเท้า (ค่านี้มาจาก manifest ตอนหั่นรูป) */
    private static final int FEET_ROW = Assets.number("_cat_feet_row");

    /** แอนิเมชัน "ยกโล่ขึ้น" ใช้เวลากี่วินาที */
    private static final double BLOCK_RAISE_TIME = 0.18;
    private static final double BLOCK_HIT_TIME = 0.3;
    private static final double HURT_TIME = 0.2;
    private static final double DEATH_TIME = 0.9;

    private CatRenderer() { }

    /** วาดผู้เล่นในด่าน (world = ด่านที่เล่นอยู่, time = เวลาของเกม) */
    static void draw(Graphics2D g, GameWorld world, double time) {
        Player p = world.player;
        boolean dead = world.status == GameWorld.Status.LOST;
        BufferedImage frame = pickFrame(p, dead, time);

        // อมตะหลังโดนตี -> ตัวกะพริบจาง (ยกเว้นตอนตายหรือกำลังยกโล่)
        boolean blink = !dead && !p.blocking && p.invuln > 0 && ((int) (time * 20) % 2 == 0);
        if (blink) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f));
        drawFrame(g, frame, p.x + p.w / 2.0, p.y + p.h, p.facing, CAT_SCALE);
        if (blink) g.setComposite(AlphaComposite.SrcOver);
    }

    /** วาดแมวยืนนิ่งๆ (ใช้ในหน้าเมนูและหน้าเนื้อเรื่อง) */
    static void drawIdle(Graphics2D g, double cx, double feetY, int facing, double time, int scale) {
        drawFrame(g, IDLE.loop(time, 6), cx, feetY, facing, scale);
    }

    // ------------------------------------------------------------------

    private static BufferedImage pickFrame(Player p, boolean dead, double time) {
        if (dead) return DEATH.at(p.deadTime / DEATH_TIME);

        if (p.knockTimer > 0) return HURT.at(1 - p.knockTimer / HURT_TIME);

        if (p.blocking) {
            if (p.blockHitTimer > 0) return BLOCK_HIT.at(1 - p.blockHitTimer / BLOCK_HIT_TIME);
            if (p.blockTime < BLOCK_RAISE_TIME) return BLOCK_RAISE.at(p.blockTime / BLOCK_RAISE_TIME);
            return BLOCK_HOLD.loop(time, 8);
        }

        if (p.attackTimer > 0) {
            return SLASH[p.comboIndex].at(1 - p.attackTimer / ATTACK_TIME);
        }

        if (p.crouch) return CROUCH.frame(4);

        if (!p.onGround) {
            // ขึ้น: เฟรมช่วงต้น / ตก: เฟรมช่วงท้าย (ยิ่งเร็วยิ่งไปปลายแอนิเมชัน)
            if (p.vy < 0) return JUMP.at(0.17 + 0.25 * (1 - Math.min(1, -p.vy / 650.0)));
            return JUMP.at(0.58 + 0.4 * Math.min(1, p.vy / 600.0));
        }

        if (Math.abs(p.vx) > 20) return RUN.loop(time, 12);
        return IDLE.loop(time, 6);
    }

    /** วาดเฟรมให้ "เท้า" อยู่ที่ (cx, feetY) ถ้า facing = -1 จะกลับด้านซ้ายขวา */
    private static void drawFrame(Graphics2D g, BufferedImage frame, double cx, double feetY, int facing, int scale) {
        AffineTransform old = g.getTransform();
        g.translate(cx, feetY);
        g.scale(facing * scale, scale);
        g.drawImage(frame, -frame.getWidth() / 2, -FEET_ROW, null);
        g.setTransform(old);
    }
}
