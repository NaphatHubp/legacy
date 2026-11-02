package legacyknight.entity;

/**
 * ค่าของ Enemy.state ของมอนแต่ละแบบ (สมองใน world/ และตัววาดใน ui/ อ่านค่าชุดเดียวกัน)
 */
public final class EnemyState {

    private EnemyState() { }

    // ---- Hopper (สไลม์เขียว): ยืน -> ย่อตัว -> พุ่งกระโดดอยู่กลางอากาศ ----
    public static final int HOP_GROUND = 0;
    public static final int HOP_WINDUP = 1;
    public static final int HOP_AIR = 2;

    // ---- Spitter (สไลม์น้ำเงิน): ปกติ -> ย่อตัวเล็งก่อนถ่ม ----
    public static final int SPIT_IDLE = 0;
    public static final int SPIT_WINDUP = 1;

    // ---- Brute (สไลม์แดง): ปกติ -> ตั้งท่า (กะพริบเตือน) -> พุ่ง -> เหนื่อยพัก ----
    public static final int BRUTE_NORMAL = 0;
    public static final int BRUTE_WINDUP = 1;
    public static final int BRUTE_DASH = 2;
    public static final int BRUTE_RECOVER = 3;

    // ---- Ramses: เดินตรวจ -> เห็นผู้เล่น (ท่า Track) -> พุ่ง -> เหนื่อย (เกราะเปิด รับดาเมจเต็ม) ----
    public static final int RAMSES_PATROL = 0;
    public static final int RAMSES_ALERT = 1;
    public static final int RAMSES_RUSH = 2;
    public static final int RAMSES_TIRED = 3;

    // ---- Dead (ยมทูต): ปกติ -> หายตัว -> ซ่อน (เห็นแค่จุดเตือน) -> โผล่ฟัน ----
    public static final int REAPER_NORMAL = 0;
    public static final int REAPER_VANISH = 1;
    public static final int REAPER_HIDDEN = 2;
    public static final int REAPER_APPEAR = 3;
}
