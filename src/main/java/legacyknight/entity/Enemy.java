package legacyknight.entity;

import legacyknight.core.Constants;
import legacyknight.level.Spawn;

/**
 * ศัตรู 1 ตัว (สไลม์ / Ramses / มินิบอส Demon King / บอส Dead) เก็บเฉพาะข้อมูลสถานะ
 * ส่วนสมองของศัตรูอยู่ใน EnemyAI (สไลม์/Ramses), DemonKingAI (มินิบอส) และ ReaperAI (บอส Dead)
 */
public class Enemy extends Entity {

    public final EnemyType type;
    private int hp;
    private final int maxHp;
    private boolean alive = true;
    /** เวลาตั้งแต่ตาย (ใช้เล่นแอนิเมชันตายของปีศาจ) */
    private double deathTime;

    /** ทิศที่เดิน: 1 = ขวา, -1 = ซ้าย */
    public int dir = -1;
    /** จุดเกิด ใช้เป็นศูนย์กลางที่สไลม์เดินเล่นไปมา */
    private final double homeX;

    /** "เฟส" ของปีศาจ: 0 = ปกติ, 1 = โหดขึ้น (บอส: ร่างกลายพันธุ์) */
    public int phase;
    /** ยิงไปแล้วกี่ครั้ง */
    public int shotCount;
    /** ท่าโจมตีลำดับถัดไปของบอส (วนไปเรื่อยๆ) */
    public int patternIndex;
    /** นับถอยหลังก่อนยิงครั้งต่อไป */
    public double shootTimer = 1.5;
    /** มุมที่เล็งไปหาผู้เล่น (เรเดียน) */
    public double aim = Math.PI;

    // ---------- เฉพาะบอส: ยกโล่ ----------
    /** เวลาที่เหลือของการยกโล่ (มากกว่า 0 = กำลังยกโล่ ดาเมจลดเหลือน้อยมาก) */
    public double guardTimer;
    /** เวลาที่ยกโล่มาแล้ว (ใช้เลือกเฟรมตอนยกโล่ขึ้น) */
    public double guardTime;
    /** รอกี่วินาทีถึงจะยกโล่ครั้งต่อไป */
    public double guardCooldown = 5;
    /** เวลาที่โล่กระพริบหลังโดนดาบ */
    public double guardHitTimer;

    // ---------- เฉพาะบอส: กลายพันธุ์ ----------
    /** กำลัง "ร่ายคำเตือน" ก่อนกลายพันธุ์ (อมตะ + หยุดโจมตี) */
    public boolean mutating;
    public double mutationTimer;
    /** กลายพันธุ์เสร็จแล้ว */
    public boolean mutated;

    // ---------- สถานะทั่วไปของมอนแต่ละแบบ (ความหมายของเลข state ดูที่ EnemyAI / ReaperAI) ----------
    /** สถานะของ "เครื่องสถานะ" (เช่น ตั้งท่า / พุ่ง / เหนื่อย) */
    public int state;
    /** เวลาที่เหลือของสถานะปัจจุบัน */
    public double stateTimer;
    /** ช่วงลงพื้นหลังกระโดด / แรงถีบหลังถ่ม ใช้เล่นแอนิเมชันสั้นๆ */
    public double landTimer;
    /** เวลาที่เหลือของท่าฟัน/ยิง (Dead ใช้เล่นเฟรมฟันเคียว) */
    public double attackTimer;
    /** นับถอยหลังก่อนจะเริ่มท่าพิเศษครั้งต่อไป (กระโดด / พุ่ง / แจ้งเตือน) */
    public double actionCooldown = 1.0;
    /** จุดที่ Dead จะวาร์ปไปโผล่ */
    public double targetX;
    /** Dead: ท่าที่กำลังง้างอยู่คือ "ฟันเคียวระยะประชิด" (ไม่ใช่ท่ายิงคลื่น) */
    public boolean meleePending;

    /** เวลาที่ตัวกระพริบขาวหลังโดนตี */
    public double hurtTimer;
    /** เวลาที่มึนงงหลังโดนตี (ขยับเองไม่ได้) */
    public double stun;
    /** กันสไลม์ชนตัวผู้เล่นแล้วทำดาเมจรัวทุกเฟรม */
    public double contactCooldown;
    /** นาฬิกาสำหรับทำแอนิเมชันของตัวเอง (สุ่มเริ่มให้แต่ละตัวขยับไม่พร้อมกัน) */
    public double animTime = Math.random() * 6;

    public Enemy(Spawn s) {
        type = s.type;
        w = type.width;
        h = type.height;
        x = s.x - w / 2.0;
        y = s.feetY - h;
        homeX = s.x;
        hp = maxHp = s.hp;
    }

    public int getHp()           { return hp; }
    public int getMaxHp()        { return maxHp; }
    public boolean isAlive()     { return alive; }
    public double getDeathTime() { return deathTime; }
    public double getHomeX()     { return homeX; }

    /** ลดเลือดตามดาเมจ (ไม่ต่ำกว่า 0) */
    public void takeDamage(int damage) {
        hp = Math.max(0, hp - damage);
    }

    /** ให้ตายทันที (เริ่มนับเวลาแอนิเมชันตายใหม่) */
    public void die() {
        alive = false;
        deathTime = 0;
    }

    /** นับเวลาตั้งแต่ตาย เรียกทุกเฟรมเฉพาะตอนที่ตายแล้ว */
    public void updateDeathTime(double dt) {
        if (!alive) deathTime += dt;
    }

    /** ตอนนี้โดนดาบไม่ได้ไหม (กำลังกลายพันธุ์/เปลี่ยนร่าง หรือ Dead กำลังหายตัว) */
    public boolean untouchable() {
        return mutating || (type == EnemyType.BOSS
                && (state == EnemyState.REAPER_VANISH || state == EnemyState.REAPER_HIDDEN));
    }

    /** กลายพันธุ์: ตัวใหญ่ขึ้น (เท้ายังอยู่ที่เดิม ตัวขยายออกจากกึ่งกลาง) */
    public void growForMutation() {
        int newW = (int) Math.round(w * Constants.MUTATION_GROWTH);
        int newH = (int) Math.round(h * Constants.MUTATION_GROWTH);
        x -= (newW - w) / 2.0;
        y -= (newH - h);
        w = newW;
        h = newH;
    }
}
