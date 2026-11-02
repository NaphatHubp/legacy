package legacyknight.level;

import legacyknight.entity.EnemyType;

/**
 * โรงงานสร้างด่านทั้ง 4 ด่านของเกม
 * อยากเพิ่ม/แก้ด่านใหม่ ให้มาแก้ที่ไฟล์นี้ไฟล์เดียว
 *
 * พิกัด x เริ่มจาก 0 ที่ซ้ายสุดของด่าน, พื้นดินอยู่ที่ GROUND_Y = 420
 * ยิ่งค่า y น้อย = ยิ่งอยู่สูง
 */
public final class LevelFactory {

    private LevelFactory() { }

    public static final int LEVEL_COUNT = 4;

    public static Level[] createAll() {
        Level[] levels = new Level[LEVEL_COUNT];

        // MP1 - ด่านวอร์มอัพ มีแต่สไลม์ (เขียวกระโดด 2 ตัว + น้ำเงินถ่มเมือก 1 ตัว)
        levels[0] = new Level("MP1", "Slime Meadow", 1800, new Theme(0, 0, 1.0))
                .addGround(0, 1800)
                .addPlatform(520, 330, 120).addPlatform(900, 300, 140)
                .addHopper(650).addHopper(1050).addSpitter(1350)
                .addBlock(1480, 370, 40, 50).setKey(1500, 345).setDoor(1680);

        // MP2 - มีหนาม ได้เจอสไลม์แดงตัวใหญ่ + Ramses ตัวแรก กุญแจอยู่บนแพลตฟอร์มสูง
        levels[1] = new Level("MP2", "Spike Valley", 2200, new Theme(-70, 35, 1.05))
                .addGround(0, 2200)
                .addSpikes(700, 90).addSpikes(1250, 110)
                .addPlatform(880, 320, 150)
                .addHopper(500).addSpitter(950).addBrute(1120).addRamses(1600).addHopper(1900)
                .addPlatform(1480, 350, 90).addPlatform(1600, 280, 160)
                .setKey(1680, 255).setDoor(2100);

        // MP3 - มินิบอส: Demon King (ยืนบนที่สูง เลือด 220)
        levels[2] = new Level("MP3", "Archer's Perch", 1500, new Theme(25, -25, 0.95))
                .addGround(0, 1500)
                .addBlock(1050, 330, 450, 90).addBlock(900, 375, 150, 45)
                .addHopper(450).addSpitter(720).addRamses(250)
                .addEnemy(EnemyType.MINI_BOSS, 1100, 330, 220)
                .addBlock(620, 370, 40, 50).setKey(640, 345).setDoor(780);

        // MP4 - บอสตัวสุดท้าย: Dead (ยมทูตถือเคียว) เปลี่ยนร่างเมื่อเลือดเหลือครึ่ง
        levels[3] = new Level("MP4", "Legacy Throne", 1300, new Theme(-150, 105, 0.85))
                .addGround(0, 1300)
                .addBlock(900, 320, 400, 100).addBlock(760, 375, 140, 45)
                .addBrute(400).addSpitter(560)
                .addEnemy(EnemyType.BOSS, 1060, 320, 420)
                .addBlock(300, 370, 40, 50).setKey(320, 345).setDoor(620);

        return levels;
    }
}
