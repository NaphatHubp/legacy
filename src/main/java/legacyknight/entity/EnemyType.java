package legacyknight.entity;

/**
 * ชนิดของศัตรู พร้อมขนาดกล่องชนและ EXP ที่ได้เมื่อฆ่า
 *
 * มอนปกติ (ฆ่าให้หมดเพื่อเปิดประตู):
 *   HOPPER  สไลม์เขียว  กระโดดพุ่งเข้าใส่ (ข้ามหลุมเล็กๆ ได้)
 *   SPITTER สไลม์น้ำเงิน ถอยห่างแล้วถ่มก้อนเมือกใส่จากระยะไกล
 *   BRUTE   สไลม์แดง    ตัวใหญ่ อึด ตั้งท่าแล้วพุ่งชน (มีสัญญาณเตือนก่อนพุ่ง)
 *   RAMSES  เต่าหนามคริสตัล เกราะแข็ง (โดนดาเมจแค่ครึ่งเดียว) เว้นแต่ตอนมันเหนื่อยหลังพุ่ง
 *
 * บอส:
 *   MINI_BOSS  Demon King (บอสตัวเก่า: ยกโล่ + กลายพันธุ์ที่ 50%) ตอนนี้เป็นมินิบอสของด่าน MP3
 *   BOSS       Dead (ยมทูตถือเคียว) บอสตัวสุดท้ายของด่าน MP4
 */
public enum EnemyType {

    //            กว้าง, สูง, EXP
    HOPPER    (34,  30,  20),
    SPITTER   (34,  30,  30),
    BRUTE     (50,  44,  50),
    RAMSES    (62,  42,  40),
    MINI_BOSS (84,  92,  150),
    BOSS      (70, 120,  400);

    public final int width;
    public final int height;
    public final int expReward;

    EnemyType(int width, int height, int expReward) {
        this.width = width;
        this.height = height;
        this.expReward = expReward;
    }

    /** เป็นสไลม์ (ฆ่าแล้วได้เลือดคืน) หรือไม่ */
    public boolean isSlime() {
        return this == HOPPER || this == SPITTER || this == BRUTE;
    }

    /** เป็นมินิบอสหรือบอส (มีแถบเลือดใหญ่ขึ้นบนจอ) หรือไม่ */
    public boolean isBoss() {
        return this == MINI_BOSS || this == BOSS;
    }

    /** เป็นปีศาจบินที่ยิงลูกไฟ (Demon King) หรือไม่ */
    public boolean isDemon() {
        return this == MINI_BOSS;
    }

    /** เป็นยมทูต Dead หรือไม่ */
    public boolean isReaper() {
        return this == BOSS;
    }

    /** ถูกดาบตีแล้วกระเด็นแค่ไหน (1 = เต็มที่, 0 = ไม่กระเด็นเลย) */
    public double knockback() {
        switch (this) {
            case HOPPER:
            case SPITTER: return 1.0;
            case BRUTE:   return 0.3;
            case RAMSES:  return 0.2;
            default:      return 0;
        }
    }
}
