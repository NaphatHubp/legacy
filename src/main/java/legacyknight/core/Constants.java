package legacyknight.core;

/**
 * ค่าคงที่ทั้งหมดของเกม รวมไว้ที่เดียวเพื่อให้ "ปรับความรู้สึกของเกม" ได้ง่าย
 * เช่น อยากให้กระโดดสูงขึ้น ก็แก้ JUMP_SPEED ที่นี่ที่เดียว
 *
 * หน่วยความเร็ว = พิกเซลต่อวินาที, หน่วยเวลา = วินาที
 */
public final class Constants {

    private Constants() { }

    // ---------- ขนาดหน้าจอและพื้น ----------
    public static final int SCREEN_W = 960;
    public static final int SCREEN_H = 540;
    /** ความสูงของพื้นดิน (วัดจากขอบบนของหน้าจอลงมา) */
    public static final int GROUND_Y = 420;

    // ---------- ฟิสิกส์ ----------
    public static final double GRAVITY = 1800;
    /** ความเร็วตอนกระโดด (ค่าติดลบ = พุ่งขึ้นฟ้า) */
    public static final double JUMP_SPEED = -650;
    /** ความเร็วตกสูงสุด (กันไม่ให้ตกเร็วจนทะลุพื้น) */
    public static final double MAX_FALL_SPEED = 900;

    // ---------- ผู้เล่น ----------
    public static final double WALK_SPEED = 230;
    public static final double CROUCH_SPEED = 90;
    public static final double BLOCK_SPEED = 110;
    public static final int PLAYER_W = 34;
    public static final int STAND_H = 44;
    public static final int CROUCH_H = 28;

    // ---------- การโจมตีและป้องกัน ----------
    /** ดาบฟันนานกี่วินาที */
    public static final double ATTACK_TIME = 0.24;
    /** รอกี่วินาทีถึงจะฟันครั้งต่อไปได้ */
    public static final double ATTACK_COOLDOWN = 0.40;
    /** ดาบยาวกี่พิกเซล */
    public static final double ATTACK_REACH = 56;
    /** ตอนยกโล่ รับดาเมจแค่ 20% ของปกติ */
    public static final double BLOCK_DAMAGE_MULTIPLIER = 0.20;
    /** ฆ่าสไลม์หนึ่งตัว ฟื้นเลือดกี่หน่วย */
    public static final int SLIME_HEAL = 5;

    // ---------- ภาพ (พิกเซลอาร์ต) ----------
    /** แมวอัศวินวาดขยายกี่เท่า (ภาพต้นฉบับตัวเล็กมาก) */
    public static final int CAT_SCALE = 3;
    /** ขนาดรูปพื้น/ต้นไม้ของด่าน (1 ช่องกระเบื้อง = 8 พิกเซลต้นฉบับ x ค่านี้) */
    public static final int TILE_SCALE = 3;
    public static final int TILE = 8 * TILE_SCALE;

    // ---------- มินิบอส Demon King (ปีศาจบิน) ----------
    /** Demon King วาดขยายกี่เท่าในร่างปกติ / ร่างกลายพันธุ์ */
    public static final int MINI_BOSS_SCALE = 2;
    public static final int MINI_BOSS_MUTATED_SCALE = 3;
    /** ร่างกลายพันธุ์ตัวใหญ่ขึ้นกี่เท่า (กล่องชน) */
    public static final double MUTATION_GROWTH = 1.5;
    /**
     * เวลา "คำเตือน" ก่อนบอสเปลี่ยนร่าง (วินาที): ตอนนี้บอสอมตะและหยุดโจมตี
     * ใช้ทั้งตอน Demon King กลายพันธุ์ และตอน Dead ตื่นเข้าโหมดคลั่ง
     */
    public static final double MUTATION_WARNING_TIME = 3.0;
    /** Demon King ยกโล่รับดาบ: เหลือดาเมจกี่เท่า */
    public static final double BOSS_GUARD_DAMAGE_MULTIPLIER = 0.15;

    // ---------- บอส Dead (ยมทูต) ----------
    /** Dead วาดขยายกี่เท่า (ภาพต้นฉบับ 80x80) */
    public static final int REAPER_SCALE = 3;

    // ---------- สไลม์ 3 แบบ และ Ramses ----------
    /** สไลม์เขียว/น้ำเงินวาดขยายกี่เท่า (ภาพต้นฉบับ 32x32 ตัวสไลม์เล็ก) / สไลม์แดงตัวใหญ่กว่า */
    public static final int SLIME_SCALE = 3;
    public static final int BRUTE_SCALE = 4;
    public static final int RAMSES_SCALE = 3;
    /** Ramses ตอนมีเกราะ รับดาเมจกี่เท่า (ตอนเหนื่อยหลังพุ่งจะรับเต็ม) */
    public static final double RAMSES_ARMOR_MULTIPLIER = 0.5;
}
