package legacyknight.util;

import java.awt.Rectangle;

/**
 * เครื่องมือคำนวณเล็กๆ ที่ใช้ทั่วทั้งเกม
 */
public final class MathUtil {

    private MathUtil() { }

    /** บีบค่า v ให้อยู่ระหว่าง lo ถึง hi (ถ้าเกินก็ตัดให้พอดีขอบ) */
    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** เหมือน clamp แต่ใช้กับเลขจำนวนเต็ม (ใช้กับค่าสีที่ต้องอยู่ 0-255) */
    public static int clampInt(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    /** กล่องสองกล่องซ้อนทับกันอยู่หรือไม่ (กล่องที่ 1 = x1,y1,w1,h1 / กล่องที่ 2 = x2,y2,w2,h2) */
    public static boolean overlaps(double x1, double y1, double w1, double h1,
                                   double x2, double y2, double w2, double h2) {
        return x1 < x2 + w2 && x1 + w1 > x2 && y1 < y2 + h2 && y1 + h1 > y2;
    }

    /** กล่องที่กำหนดซ้อนทับกับสี่เหลี่ยม r อยู่หรือไม่ */
    public static boolean overlaps(double x, double y, double w, double h, Rectangle r) {
        return overlaps(x, y, w, h, r.x, r.y, r.width, r.height);
    }
}
