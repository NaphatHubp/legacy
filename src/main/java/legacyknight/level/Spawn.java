package legacyknight.level;

import legacyknight.entity.EnemyType;

/**
 * จุดเกิดของศัตรู 1 ตัวในด่าน
 */
public class Spawn {

    public final EnemyType type;
    /** ตำแหน่งกึ่งกลางตัวศัตรูในแนวนอน */
    public final double x;
    /** ตำแหน่งเท้าของศัตรู (ความสูงของพื้นที่มันยืน) */
    public final double feetY;
    public final int hp;

    public Spawn(EnemyType type, double x, double feetY, int hp) {
        this.type = type;
        this.x = x;
        this.feetY = feetY;
        this.hp = hp;
    }
}
