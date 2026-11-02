package legacyknight.entity;

import java.awt.Color;

/**
 * จุดสีเล็กๆ ที่กระเด็นออกมาเป็นเอฟเฟกต์ (เช่น ตอนโดนตี หรือศัตรูตาย)
 */
public class Particle {

    public double x, y;
    public double vx, vy;
    /** อายุที่เหลือ (วินาที) */
    public double life;
    /** อายุตอนเกิด (ใช้คำนวณความจางหาย) */
    public double maxLife;
    public double size;
    public Color color;
}
