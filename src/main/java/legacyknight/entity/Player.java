package legacyknight.entity;

import java.util.HashSet;
import java.util.Set;

/**
 * อัศวินที่ผู้เล่นควบคุม (เก็บเฉพาะข้อมูลสถานะ ส่วนการควบคุมอยู่ใน PlayerController)
 */
public class Player extends Entity {

    /** หันไปทางไหน: 1 = ขวา, -1 = ซ้าย */
    public int facing = 1;
    private int hp;

    /** สร้างผู้เล่นที่จุดเริ่มต้น ด้วยขนาดกล่องชนและเลือดเริ่มต้น */
    public Player(double x, double y, int w, int h, int hp) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.hp = hp;
    }

    public int getHp() { return hp; }

    /** กำหนดเลือด (ไม่ต่ำกว่า 0) */
    public void setHp(int hp) { this.hp = Math.max(0, hp); }

    public boolean crouch;
    public boolean blocking;

    /** เวลาอมตะหลังโดนตี (กะพริบ) */
    public double invuln;
    /** เวลาที่ดาบกำลังฟันอยู่ (มากกว่า 0 = กำลังฟัน) */
    public double attackTimer;
    /** เวลารอก่อนฟันครั้งต่อไปได้ */
    public double attackCooldown;
    /** เวลาที่ถูกกระแทกถอยหลัง (ควบคุมตัวเองไม่ได้ชั่วคราว) */
    public double knockTimer;

    /** เวลาที่ยกโล่มาแล้ว (ใช้เล่นแอนิเมชัน "ยกโล่ขึ้น" ก่อนจะค้างในท่าป้องกัน) */
    public double blockTime;
    /** เวลาที่โล่กระพริบหลังรับการโจมตีสำเร็จ (ใช้เล่นแอนิเมชันโล่สะท้อน) */
    public double blockHitTimer;
    /** ฟันครั้งนี้ใช้ท่าไหน (0/1 สลับกันทุกครั้ง) */
    public int comboIndex;
    /** เวลาตั้งแต่ตาย (ใช้เล่นแอนิเมชันตาย) */
    public double deadTime;

    /** ศัตรูที่โดนดาบฟันนี้ไปแล้ว (กันไม่ให้ฟันตัวเดิมซ้ำหลายครั้งในการฟันครั้งเดียว) */
    public final Set<Enemy> alreadyHit = new HashSet<Enemy>();
}
