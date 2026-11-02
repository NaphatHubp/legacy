package legacyknight.entity;

/**
 * "จุดเตือน" บนพื้น: โผล่เป็นเสาแดงกะพริบ แล้วพอหมดเวลาจะมีลูกไฟตกลงมาตรงนั้น
 * (ทำให้ผู้เล่นมีเวลาหลบฝนไฟ) หรือเป็นแค่ "เครื่องหมาย" บอกจุดที่ Dead จะวาร์ปมาโผล่ (ไม่มีลูกไฟตก)
 */
public class Telegraph {

    public final double x;
    /** ระดับพื้นผิวที่ลูกไฟจะตกลงไปกระทบ (วาดเสาเตือนลงมาถึงตรงนี้) */
    public final double surfaceY;
    public final int damage;
    /** เวลาที่เหลือก่อนลูกไฟตก */
    public double timer;
    /** เวลาเตือนทั้งหมด (ใช้คำนวณความเข้มของสี) */
    public final double total;
    /** true = หมดเวลาแล้วมีลูกไฟตก / false = เป็นแค่เครื่องหมายเตือน */
    public final boolean dropsProjectile;
    /** หน้าตาของลูกที่ตก (Arrow.FIREBALL หรือ Arrow.SCYTHE) */
    public final int style;

    public Telegraph(double x, double surfaceY, double seconds, int damage) {
        this(x, surfaceY, seconds, damage, true, Arrow.FIREBALL);
    }

    public Telegraph(double x, double surfaceY, double seconds, int damage,
                     boolean dropsProjectile, int style) {
        this.x = x;
        this.surfaceY = surfaceY;
        this.timer = seconds;
        this.total = seconds;
        this.damage = damage;
        this.dropsProjectile = dropsProjectile;
        this.style = style;
    }
}
