package legacyknight.core;

/**
 * ค่าความเก่งของอัศวินที่ "ติดตัวไปทุกด่าน" (เลเวล, EXP, เลือดสูงสุด, พลังโจมตี)
 * แยกออกจากตัวผู้เล่นในด่าน เพราะผู้เล่นจะถูกสร้างใหม่ทุกครั้งที่เริ่มด่าน
 * แต่ความเก่งต้องไม่หายไป
 */
public class PlayerStats {

    private int level = 1;
    private int exp = 0;
    private int maxHp = 100;
    private int attack = 25;

    public int getLevel()  { return level; }
    public int getExp()    { return exp; }
    public int getMaxHp()  { return maxHp; }
    public int getAttack() { return attack; }

    /** EXP ที่ต้องมีเพื่อขึ้นเลเวลถัดไป (เลเวลสูงขึ้น ยิ่งใช้เยอะ) */
    public int expNeeded() {
        return 50 + 30 * level;
    }

    /**
     * เพิ่ม EXP ให้อัศวิน
     *
     * @return true ถ้าเลเวลอัปจากการเพิ่มครั้งนี้
     */
    public boolean addExp(int amount) {
        exp += amount;
        boolean leveledUp = false;
        while (exp >= expNeeded()) {
            exp -= expNeeded();
            level++;
            maxHp += 10;
            attack += 5;
            leveledUp = true;
        }
        return leveledUp;
    }
}
