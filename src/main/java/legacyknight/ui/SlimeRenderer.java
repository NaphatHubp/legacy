package legacyknight.ui;

import static legacyknight.core.Constants.BRUTE_SCALE;
import static legacyknight.core.Constants.SLIME_SCALE;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import legacyknight.assets.Animation;
import legacyknight.assets.Assets;
import legacyknight.entity.Enemy;
import legacyknight.entity.EnemyState;
import legacyknight.entity.EnemyType;
import legacyknight.util.MathUtil;

/**
 * วาดสไลม์ 3 แบบจากชุดรูป Platformer Slimes (ภาพต้นฉบับ 32x32 หันไปทางซ้าย ตัวสไลม์นั่งอยู่ก้นภาพ):
 *   HOPPER  สีเขียว  | SPITTER สีน้ำเงิน | BRUTE สีแดง (วาดใหญ่กว่าเพื่อน)
 *
 * เลือกแอนิเมชันตามสถานะ: ตาย > โดนตี (กะพริบขาว) > ท่าพิเศษของมอนแต่ละแบบ > เดิน/ยืน
 *   ท่ากระโดด (jump.png 12 เฟรม): 0 = ย่อตัวสะสมแรง, 1-7 = ลอยขึ้น-ตกลง, 8-11 = ลงพื้นยุบตัว
 */
final class SlimeRenderer {

    private static final double DEATH_TIME = 0.5;
    /** สไลม์ตายแล้วค้างบนจอกี่วินาที ก่อนหายไป */
    static final double DEATH_LINGER = 0.6;

    private SlimeRenderer() { }

    static String colorOf(EnemyType t) {
        switch (t) {
            case SPITTER: return "blue";
            case BRUTE:   return "red";
            default:      return "green";
        }
    }

    static int scaleOf(EnemyType t) {
        return t == EnemyType.BRUTE ? BRUTE_SCALE : SLIME_SCALE;
    }

    private static Animation anim(String color, String name) {
        return Assets.animation("slime/" + color + "_" + name + ".png");
    }

    /** วาดสไลม์ในด่าน (ทั้งตัวเป็นๆ และแอนิเมชันตอนตาย) */
    static void draw(Graphics2D g, Enemy e, double time) {
        String color = colorOf(e.type);
        int scale = scaleOf(e.type);
        double cx = e.x + e.w / 2.0;
        double foot = e.y + e.h;
        double shakeX = 0;
        boolean flash = e.hurtTimer > 0;
        BufferedImage frame;

        if (!e.alive) {
            if (e.deathTime > DEATH_LINGER) return;
            frame = anim(color, "dead").at(e.deathTime / DEATH_TIME);
            flash = false;
        } else {
            frame = pickFrame(e, color);
            if (e.type == EnemyType.BRUTE && e.state == EnemyState.BRUTE_WINDUP) {
                // ตั้งท่าพุ่ง: ตัวสั่น + กะพริบขาวถี่ๆ เป็นสัญญาณเตือน
                shakeX = (Math.random() - 0.5) * 5;
                flash = flash || Math.sin(e.animTime * 38) > 0;
            }
        }

        drawFrame(g, frame, cx + shakeX, foot, e.dir > 0, scale, flash);

        if (e.alive && e.type == EnemyType.BRUTE && e.state == EnemyState.BRUTE_WINDUP) {
            g.setFont(new Font("SansSerif", Font.BOLD, 30));
            g.setColor(new Color(255, 70, 60));
            DrawUtil.centered(g, "!", cx, e.y - 12);
        }
    }

    private static BufferedImage pickFrame(Enemy e, String color) {
        Animation jump = anim(color, "jump");
        switch (e.type) {
            case HOPPER:
                if (e.state == EnemyState.HOP_WINDUP) return jump.frame(0);
                if (e.state == EnemyState.HOP_AIR) {
                    double p = MathUtil.clamp((e.vy + 520) / 1040.0, 0, 1);       // ขึ้น -> ตก
                    return jump.frame(1 + (int) (p * 6.99));
                }
                if (e.landTimer > 0) return jump.frame(8 + (int) ((1 - e.landTimer / 0.3) * 3.99));
                break;
            case SPITTER:
                if (e.state == EnemyState.SPIT_WINDUP) return jump.frame(0);       // ย่อตัวเล็งก่อนถ่ม
                if (e.landTimer > 0) return jump.frame(8 + (int) ((1 - e.landTimer / 0.25) * 3.99));   // แรงถีบ
                break;
            case BRUTE:
                if (e.state == EnemyState.BRUTE_WINDUP) return jump.frame(0);
                if (e.state == EnemyState.BRUTE_DASH) return anim(color, "walk").loop(e.animTime, 22);
                if (e.state == EnemyState.BRUTE_RECOVER) return anim(color, "idle").loop(e.animTime, 4);
                break;
            default:
        }
        if (Math.abs(e.vx) > 5) return anim(color, "walk").loop(e.animTime, 10);
        return anim(color, "idle").loop(e.animTime, 7);
    }

    /** วาดสไลม์ยืนเฉยๆ ในหน้าเมนู/เนื้อเรื่อง (color = "green" / "blue" / "red") */
    static void drawPortrait(Graphics2D g, double cx, double footY, boolean faceRight, double time,
                             String color, int scale) {
        drawFrame(g, anim(color, "idle").loop(time, 7), cx, footY, faceRight, scale, false);
    }

    /** ภาพต้นฉบับหันซ้าย: faceRight = true จะกลับด้าน (ก้นภาพ = เท้า อยู่ที่ footY) */
    private static void drawFrame(Graphics2D g, BufferedImage frame, double cx, double footY,
                                  boolean faceRight, int scale, boolean flash) {
        BufferedImage img = flash ? Assets.whitened(frame) : frame;
        AffineTransform old = g.getTransform();
        g.translate(cx, footY);
        g.scale(faceRight ? -scale : scale, scale);
        g.drawImage(img, -img.getWidth() / 2, -img.getHeight(), null);
        g.setTransform(old);
    }
}
