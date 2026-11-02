package legacyknight.entity;

/**
 * ลูกยิงของศัตรู (ชื่อคลาสยังเป็น Arrow เพราะเดิมเป็นลูกธนู)
 * มี 3 หน้าตา: ลูกไฟของปีศาจ / ก้อนเมือกที่สไลม์น้ำเงินถ่ม / คลื่นเคียวของ Dead
 */
public class Arrow {

    public static final int FIREBALL = 0;
    public static final int SPIT = 1;
    public static final int SCYTHE = 2;

    public double x, y;
    public double vx, vy;
    public final int damage;
    /** true = ยกโล่กันได้ / false = กันไม่ได้ (เช่นฝนไฟจากฟ้า) */
    public final boolean blockable;
    /** หน้าตา: FIREBALL / SPIT / SCYTHE */
    public final int style;
    /** SCYTHE เท่านั้น: เฟรมของ Fire.png (0 = ส้ม, 1 = แดง, 2 = แดงเข้ม) */
    public final int frame;
    /** ครึ่งหนึ่งของกล่องชน (พิกเซล) */
    public final double radius;

    public Arrow(double x, double y, double vx, double vy, int damage, boolean blockable) {
        this(x, y, vx, vy, damage, blockable, FIREBALL, 0, 10);
    }

    public Arrow(double x, double y, double vx, double vy, int damage, boolean blockable,
                 int style, int frame, double radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.blockable = blockable;
        this.style = style;
        this.frame = frame;
        this.radius = radius;
    }
}
