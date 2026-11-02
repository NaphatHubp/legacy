package legacyknight.ui;

import static legacyknight.core.Constants.MUTATION_WARNING_TIME;
import static legacyknight.core.Constants.REAPER_SCALE;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

import legacyknight.assets.Animation;
import legacyknight.assets.Assets;
import legacyknight.entity.Enemy;
import legacyknight.entity.EnemyState;
import legacyknight.entity.Player;

/**
 * วาด "Dead" ยมทูตถือเคียว (ภาพต้นฉบับ 80x80 หันไปทางขวา ตัวอยู่กึ่งกลางภาพ เท้าลอยอยู่แถวที่ 64)
 *
 * เลือกแอนิเมชันตามสถานะ:
 *   ตาย > หายตัว/ซ่อน > กำลังเปลี่ยนร่าง (กระพริบสลับสี + สั่น) > โดนตี (เฟรมขาว)
 *       > ฟันเคียว (attack เฟรม 3-7) > ง้างเคียวก่อนปล่อยท่า (attack เฟรม 0-2) > ลอยเฉยๆ
 * ร่างที่ 2 (คลั่ง) หมุนสีทั้งตัวเป็นม่วง/ชมพู และมีออร่าเรือง
 */
final class ReaperRenderer {

    private static final int FOOT_ROW = 64;
    private static final double HURT_TIME = 0.15;
    /** ต้องตรงกับ ReaperAI.SLASH_TIME (อยู่คนละแพ็กเกจ เลยประกาศซ้ำ) */
    private static final double SLASH_TIME = 0.4;
    private static final double DEATH_TIME = 1.2;
    /** Dead ตายแล้วค้างบนจอกี่วินาที ก่อนหายไป */
    static final double DEATH_LINGER = 1.9;
    private static final double RAGE_HUE = 255;

    private ReaperRenderer() { }

    private static Animation anim(String name, boolean raged) {
        String path = "dead/" + name + ".png";
        return raged ? Assets.tintedAnimation(path, RAGE_HUE, 1.05) : Assets.animation(path);
    }

    /** วาดในด่าน (player ใช้เช็กว่าอยู่ในระยะที่จะโจมตีหรือไม่) */
    static void draw(Graphics2D g, Enemy e, Player player, double time, double windup) {
        boolean raged = e.mutated;
        double cx = e.x + e.w / 2.0;
        double feet = e.y + e.h;
        double hover = -12 - 6 * Math.sin(e.animTime * 2.5);
        double shakeX = 0;
        float alpha = 1f;
        BufferedImage frame;

        if (!e.alive) {
            if (e.deathTime > DEATH_LINGER) return;
            frame = anim("death", raged).at(e.deathTime / DEATH_TIME);
            hover = -8;
        } else if (e.state == EnemyState.REAPER_HIDDEN) {
            return;                                                  // หายตัวอยู่ ไม่มีอะไรให้วาด
        } else if (e.state == EnemyState.REAPER_VANISH) {
            alpha = (float) Math.max(0, e.stateTimer / 0.4);
            frame = anim("idle", raged).loop(e.animTime, 9);
        } else if (e.mutating) {
            // ร่างเก่ากับร่างใหม่กระพริบสลับกัน ยิ่งใกล้หมดเวลายิ่งถี่ ตัวสั่นและลอยสูงขึ้น
            double elapsed = MUTATION_WARNING_TIME - e.mutationTimer;
            double progress = elapsed / MUTATION_WARNING_TIME;
            boolean showNew = Math.sin(elapsed * (10 + 30 * progress)) > 0;
            frame = anim("idle", showNew).loop(e.animTime, 9);
            shakeX = (Math.random() - 0.5) * (4 + 8 * progress);
            hover -= 14 * progress;
            raged = true;                                            // ออร่าม่วงเริ่มโผล่
        } else if (e.hurtTimer > 0) {
            frame = anim("hurt", raged).at(1 - e.hurtTimer / HURT_TIME);
        } else if (e.attackTimer > 0) {
            // ฟันเคียว: เฟรม 3-7 (พระจันทร์เสี้ยวแสงใหญ่ๆ อยู่เฟรม 3-5)
            double p = 1 - e.attackTimer / SLASH_TIME;
            frame = anim("attack", raged).frame(3 + (int) (p * 4.99));
        } else if (e.shootTimer >= 0 && e.shootTimer < windup && Math.abs(player.x - e.x) < 950) {
            frame = anim("attack", raged).frame((int) ((1 - e.shootTimer / windup) * 2.99));   // ง้างเคียว = สัญญาณเตือน
            if (e.state == EnemyState.REAPER_APPEAR) alpha = (float) Math.min(1, (0.55 - e.stateTimer) / 0.25);
        } else {
            frame = anim("idle", raged).loop(e.animTime, 9);
        }

        // ออร่าของร่างคลั่ง (และตอนกำลังเปลี่ยนร่าง)
        if (e.alive && raged) {
            double pulse = 0.5 + 0.5 * Math.sin(time * 5);
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float) ((0.14 + 0.1 * pulse) * alpha)));
            g.setColor(new Color(220, 70, 190));
            double r = Math.max(e.w, e.h) * 0.85;
            g.fill(new Ellipse2D.Double(cx - r, feet + hover - e.h * 0.95 - r * 0.2, r * 2, r * 1.6));
            g.setComposite(AlphaComposite.SrcOver);
        }

        AffineTransform old = g.getTransform();
        g.translate(cx + shakeX, feet + hover + 6);
        g.scale(e.dir > 0 ? REAPER_SCALE : -REAPER_SCALE, REAPER_SCALE);
        if (alpha < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(frame, -frame.getWidth() / 2, -FOOT_ROW, null);
        g.setComposite(AlphaComposite.SrcOver);
        g.setTransform(old);
    }

    /** วาด Dead ลอยเฉยๆ ในหน้าเมนู/เนื้อเรื่อง */
    static void drawPortrait(Graphics2D g, double cx, double footY, boolean faceRight, double time, int scale) {
        BufferedImage frame = anim("idle", false).loop(time, 9);
        AffineTransform old = g.getTransform();
        g.translate(cx, footY + Math.sin(time * 2.5) * 5);
        g.scale(faceRight ? scale : -scale, scale);
        g.drawImage(frame, -frame.getWidth() / 2, -FOOT_ROW, null);
        g.setTransform(old);
    }
}
