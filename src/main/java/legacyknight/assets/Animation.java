package legacyknight.assets;

import java.awt.image.BufferedImage;

import legacyknight.util.MathUtil;

/**
 * แอนิเมชัน 1 อัน = รูปเรียงแนวนอนหลายเฟรม (sprite strip) ที่ถูกหั่นให้ขนาดเท่ากันทุกเฟรม
 *
 * มี 2 แบบการเล่น:
 *  - loop(time, fps)    เล่นวนไปเรื่อยๆ (เดิน, ยืนนิ่ง, ลอยตัว)
 *  - at(progress)       เล่นครั้งเดียวตามความคืบหน้า 0..1 (ฟันดาบ, ยกโล่, ตาย)
 */
public class Animation {

    private final BufferedImage[] frames;
    public final int cellWidth;
    public final int cellHeight;

    public Animation(BufferedImage strip, int frameCount) {
        cellWidth = strip.getWidth() / frameCount;
        cellHeight = strip.getHeight();
        frames = new BufferedImage[frameCount];
        for (int i = 0; i < frameCount; i++) {
            frames[i] = strip.getSubimage(i * cellWidth, 0, cellWidth, cellHeight);
        }
    }

    public int frameCount() {
        return frames.length;
    }

    public BufferedImage frame(int index) {
        return frames[MathUtil.clampInt(index, 0, frames.length - 1)];
    }

    /** เล่นวนซ้ำ: time = เวลา (วินาที), fps = กี่เฟรมต่อวินาที */
    public BufferedImage loop(double time, double fps) {
        int i = (int) Math.floor(time * fps) % frames.length;
        return frames[i < 0 ? i + frames.length : i];
    }

    /** เล่นครั้งเดียว: progress 0 = เฟรมแรก, 1 = เฟรมสุดท้าย */
    public BufferedImage at(double progress) {
        int i = (int) (MathUtil.clamp(progress, 0, 0.9999) * frames.length);
        return frames[i];
    }
}
