package legacyknight.ui;

import static legacyknight.core.Constants.MINI_BOSS_MUTATED_SCALE;
import static legacyknight.core.Constants.MINI_BOSS_SCALE;
import static legacyknight.core.Constants.MUTATION_WARNING_TIME;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import legacyknight.assets.Animation;
import legacyknight.assets.Assets;
import legacyknight.entity.Enemy;
import legacyknight.entity.Player;

/**
 * วาดปีศาจบิน Demon King (มินิบอส) และเลือกแอนิเมชันตามสถานะ:
 *
 *   ตาย > กำลังกลายพันธุ์ (กระพริบสลับร่าง + ตัวสั่น) > ยกโล่ (ใช้แอนิเมชันโล่แบบเดียวกับแมว)
 *       > โดนตี > ง้างปากก่อนยิง > ลอยตัว
 *
 * แต่ละชุดมี 2 สี: ปกติ (แดง) / "_mutated" (ม่วง ร่างกลายพันธุ์) (ชุด "_captain" เดิมไม่ได้ใช้แล้ว)
 */
final class DemonRenderer {

    private static final String[] NAMES = {
            "idle", "fly", "attack", "hurt", "death", "guard_raise", "guard_hold", "guard_hit" };

    private static final double GUARD_RAISE_TIME = 0.25;
    private static final double GUARD_HIT_TIME = 0.3;
    private static final double HURT_TIME = 0.15;
    private static final double DEATH_TIME = 0.9;
    /** ปีศาจตายแล้วค้างบนจอกี่วินาที ก่อนหายไป */
    static final double DEATH_LINGER = 1.4;

    private DemonRenderer() { }

    private static Animation anim(String name, String variant) {
        return Assets.animation("demon/" + name + variant + ".png");
    }

    /** ชุดสีของปีศาจตัวนี้ */
    private static String variantOf(Enemy e) {
        return e.mutated ? "_mutated" : "";
    }

    private static int scaleOf(Enemy e) {
        return e.mutated ? MINI_BOSS_MUTATED_SCALE : MINI_BOSS_SCALE;
    }

    /** วาดปีศาจในด่าน (player ใช้เช็กว่าอยู่ในระยะที่จะยิงหรือไม่) */
    static void draw(Graphics2D g, Enemy e, Player player, double time, double windup) {
        String variant = variantOf(e);
        int scale = scaleOf(e);
        double cx = e.x + e.w / 2.0;
        double feet = e.y + e.h;
        double hover = -10 - 5 * Math.sin(e.animTime * 3);     // ลอยขึ้นลงเบาๆ
        double shakeX = 0;
        BufferedImage frame;

        if (!e.isAlive()) {
            if (e.getDeathTime() > DEATH_LINGER) return;
            frame = anim("death", variant).at(e.getDeathTime() / DEATH_TIME);
            hover = -10;
        } else if (e.mutating) {
            // ร่างเก่ากับร่างใหม่กระพริบสลับกัน ยิ่งใกล้หมดเวลายิ่งถี่ ตัวสั่นด้วย
            double elapsed = MUTATION_WARNING_TIME - e.mutationTimer;
            double progress = elapsed / MUTATION_WARNING_TIME;
            boolean showNew = Math.sin(elapsed * (10 + 30 * progress)) > 0;
            frame = anim("idle", showNew ? "_mutated" : "").loop(e.animTime, 8);
            shakeX = (Math.random() - 0.5) * (4 + 8 * progress);
            hover -= 6 * progress;
        } else if (e.guardTimer > 0) {
            if (e.guardHitTimer > 0) frame = anim("guard_hit", variant).at(1 - e.guardHitTimer / GUARD_HIT_TIME);
            else if (e.guardTime < GUARD_RAISE_TIME) frame = anim("guard_raise", variant).at(e.guardTime / GUARD_RAISE_TIME);
            else frame = anim("guard_hold", variant).loop(e.animTime, 8);
        } else if (e.hurtTimer > 0) {
            frame = anim("hurt", variant).at(1 - e.hurtTimer / HURT_TIME);
        } else if (e.shootTimer >= 0 && e.shootTimer < windup && Math.abs(player.x - e.x) < 900) {
            frame = anim("attack", variant).at(1 - e.shootTimer / windup);   // ง้างปากก่อนยิง = สัญญาณเตือน
        } else {
            frame = anim("fly", variant).loop(e.animTime, 8);
        }

        // ออร่าม่วงของร่างกลายพันธุ์ (และตอนกำลังกลายพันธุ์)
        if (e.isAlive() && (e.mutated || e.mutating)) {
            double pulse = 0.5 + 0.5 * Math.sin(time * 5);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) (0.14 + 0.1 * pulse)));
            g.setColor(new Color(190, 90, 255));
            double r = Math.max(e.w, e.h) * 0.85;
            g.fill(new Ellipse2D.Double(cx - r, feet + hover - e.h * 0.9 - r * 0.3, r * 2, r * 1.5));
            g.setComposite(AlphaComposite.SrcOver);
        }

        drawFrame(g, frame, cx + shakeX, feet + hover + 6, e.dir > 0, scale);
    }

    /** วาดปีศาจยืนลอยเฉยๆ ในหน้าเมนู/เนื้อเรื่อง */
    static void drawPortrait(Graphics2D g, double cx, double feetY, boolean faceRight, double time,
                             String variant, int scale) {
        drawFrame(g, anim("fly", variant).loop(time, 8), cx, feetY + Math.sin(time * 3) * 5, faceRight, scale);
    }

    /** ภาพต้นฉบับหันซ้าย: faceRight = true จะกลับด้าน เท้า/ก้นของรูปอยู่ที่ (cx, feetY) */
    private static void drawFrame(Graphics2D g, BufferedImage frame, double cx, double feetY,
                                  boolean faceRight, int scale) {
        AffineTransform old = g.getTransform();
        g.translate(cx, feetY);
        g.scale(faceRight ? -scale : scale, scale);
        g.drawImage(frame, -frame.getWidth() / 2, -frame.getHeight(), null);
        g.setTransform(old);
    }
}
