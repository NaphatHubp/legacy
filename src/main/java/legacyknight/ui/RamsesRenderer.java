package legacyknight.ui;

import static legacyknight.core.Constants.RAMSES_SCALE;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

import legacyknight.assets.Assets;
import legacyknight.entity.Enemy;
import legacyknight.entity.EnemyState;

/**
 * วาด Ramses เต่าหนามคริสตัล (ภาพต้นฉบับ 40x40 หันไปทางขวา ตัวอยู่กลางภาพ เท้าอยู่แถวที่ 28)
 *   เดินตรวจ = walk | เห็นผู้เล่น = track (ท่าจ้อง เล่นครั้งเดียว) | พุ่ง = walk เร็ว
 *   เหนื่อย (เกราะเปิด) = idle ช้าๆ + เรืองขาวกะพริบ ให้รู้ว่าตอนนี้ตีเจ็บเต็มที่
 */
final class RamsesRenderer {

    private static final int FOOT_ROW = 28;
    private static final double ALERT_TIME = 0.7;
    private static final double DEATH_TIME = 0.4;

    private RamsesRenderer() { }

    static void draw(Graphics2D g, Enemy e, double time) {
        double cx = e.x + e.w / 2.0;
        double foot = e.y + e.h;
        BufferedImage frame;
        float alpha = 1f;
        double squash = 1;

        if (!e.alive) {
            // Ramses ไม่มีท่าตาย: ยุบตัวลงแล้วจางหาย
            if (e.deathTime > DEATH_TIME) return;
            double p = e.deathTime / DEATH_TIME;
            frame = Assets.animation("ramses/idle.png").frame(0);
            alpha = (float) (1 - p);
            squash = 1 - 0.6 * p;
        } else {
            switch (e.state) {
                case EnemyState.RAMSES_ALERT:
                    frame = Assets.animation("ramses/track.png").at(1 - e.stateTimer / ALERT_TIME);
                    break;
                case EnemyState.RAMSES_RUSH:
                    frame = Assets.animation("ramses/walk.png").loop(e.animTime, 16);
                    break;
                case EnemyState.RAMSES_TIRED:
                    frame = Assets.animation("ramses/idle.png").loop(e.animTime, 4);
                    break;
                default:
                    frame = Math.abs(e.vx) > 5
                            ? Assets.animation("ramses/walk.png").loop(e.animTime, 8)
                            : Assets.animation("ramses/idle.png").loop(e.animTime, 6);
            }
        }

        AffineTransform old = g.getTransform();
        g.translate(cx, foot);
        g.scale(e.dir > 0 ? RAMSES_SCALE : -RAMSES_SCALE, RAMSES_SCALE * squash);
        if (alpha < 1f) g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(frame, -frame.getWidth() / 2, -FOOT_ROW, null);

        BufferedImage glow = null;
        float glowAlpha = 0;
        if (e.alive && e.hurtTimer > 0) {
            glow = Assets.whitened(frame);
            glowAlpha = 0.85f;
        } else if (e.alive && e.state == EnemyState.RAMSES_TIRED) {
            glow = Assets.whitened(frame);
            glowAlpha = (float) (0.15 + 0.25 * (0.5 + 0.5 * Math.sin(e.animTime * 14)));
        }
        if (glow != null) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, glowAlpha));
            g.drawImage(glow, -frame.getWidth() / 2, -FOOT_ROW, null);
        }
        g.setComposite(AlphaComposite.SrcOver);
        g.setTransform(old);
    }

    /** วาด Ramses ยืนเฉยๆ ในหน้าเมนู/เนื้อเรื่อง */
    static void drawPortrait(Graphics2D g, double cx, double footY, boolean faceRight, double time, int scale) {
        BufferedImage frame = Assets.animation("ramses/idle.png").loop(time, 6);
        AffineTransform old = g.getTransform();
        g.translate(cx, footY);
        g.scale(faceRight ? scale : -scale, scale);
        g.drawImage(frame, -frame.getWidth() / 2, -FOOT_ROW, null);
        g.setTransform(old);
    }
}
