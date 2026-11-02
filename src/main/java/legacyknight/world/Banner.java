package legacyknight.world;

import legacyknight.util.MathUtil;

/**
 * ข้อความตัวใหญ่ที่เด้งกลางจอชั่วคราว เช่น "You got the key!" หรือ "LEVEL UP!"
 * แสดงอยู่ช่วงหนึ่งแล้วค่อยๆ จางหายไป
 */
public class Banner {

    private String text = "";
    private double timer;

    /** แสดงข้อความ text เป็นเวลา seconds วินาที */
    public void show(String text, double seconds) {
        this.text = text;
        this.timer = seconds;
    }

    public void update(double dt) {
        if (timer > 0) timer -= dt;
    }

    /** ตอนนี้ยังมีข้อความแสดงอยู่หรือไม่ */
    public boolean isActive() {
        return timer > 0;
    }

    public String getText() {
        return text;
    }

    /** ความโปร่งใส 0..1 (จางลงใน 0.5 วินาทีสุดท้าย) */
    public float getAlpha() {
        return (float) MathUtil.clamp(timer / 0.5, 0, 1);
    }
}
