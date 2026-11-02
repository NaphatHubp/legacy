package legacyknight.entity;

import static legacyknight.core.Constants.GRAVITY;
import static legacyknight.core.Constants.MAX_FALL_SPEED;

import java.awt.Rectangle;

import legacyknight.level.Level;
import legacyknight.util.MathUtil;

/**
 * สิ่งมีชีวิตที่ขยับได้และโดนแรงโน้มถ่วง (ผู้เล่นและศัตรูสืบทอดจากคลาสนี้)
 * ตำแหน่ง (x, y) คือมุมซ้ายบนของกล่องตัวละคร
 */
public class Entity {

    public double x, y;
    public double vx, vy;
    public int w, h;

    /** ยืนอยู่บนพื้นหรือไม่ (ใช้ตัดสินว่ากระโดดได้ไหม) */
    public boolean onGround;
    /** เฟรมนี้ชนกำแพงหรือไม่ (ศัตรูใช้เพื่อเดินกลับหลัง) */
    public boolean hitWall;

    /**
     * ทำให้ตัวละครตกลงตามแรงโน้มถ่วง แล้วขยับไปตามความเร็ว พร้อมเช็กชนพื้น/กำแพง
     * วิธีเช็กชน: ขยับแกน X ก่อนแล้วดันออกจากสิ่งที่ชน จากนั้นค่อยขยับแกน Y แล้วดันออกอีกที
     * (แยกทีละแกนจะทำให้ไม่ติดมุมและไม่ทะลุ)
     */
    public void applyPhysics(double dt, Level level) {
        vy = Math.min(vy + GRAVITY * dt, MAX_FALL_SPEED);
        hitWall = false;

        // --- ขยับซ้าย/ขวา ---
        x += vx * dt;
        if (x < 0) { x = 0; hitWall = true; }
        if (x + w > level.width) { x = level.width - w; hitWall = true; }
        for (Rectangle r : level.solids) {
            if (MathUtil.overlaps(x, y, w, h, r)) {
                if (vx > 0) x = r.x - w;
                else if (vx < 0) x = r.x + r.width;
                else continue;
                hitWall = true;
            }
        }

        // --- ขยับขึ้น/ลง ---
        y += vy * dt;
        onGround = false;
        for (Rectangle r : level.solids) {
            if (MathUtil.overlaps(x, y, w, h, r)) {
                if (vy > 0) { y = r.y - h; onGround = true; }   // ตกลงมาเหยียบพื้น
                else if (vy < 0) { y = r.y + r.height; }         // หัวชนเพดาน
                vy = 0;
            }
        }
    }
}
