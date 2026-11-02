package legacyknight.level;

import static legacyknight.core.Constants.GROUND_Y;
import static legacyknight.core.Constants.SCREEN_H;

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import legacyknight.entity.EnemyType;
import legacyknight.util.MathUtil;

/**
 * ข้อมูลของด่าน 1 ด่าน: พื้น แพลตฟอร์ม หนาม จุดเกิดศัตรู กุญแจ และประตู
 *
 * ตัวสร้างด่านใช้สไตล์ต่อเมธอดเป็นสายได้ เช่น
 * new Level(...).addGround(0, 1800).addSlime(650).setKey(1500, 345)
 */
public class Level {

    public final String code;     // รหัสด่าน เช่น "MP1"
    public final String name;     // ชื่อด่าน เช่น "Slime Meadow"
    public final int width;       // ความกว้างของด่านทั้งหมด (พิกเซล)
    public final Theme theme;     // โทนสีของด่าน

    /** ของแข็งที่เดินทับ/ชนได้ (พื้น แพลตฟอร์ม บล็อก) */
    public final List<Rectangle> solids = new ArrayList<Rectangle>();
    /** แพลตฟอร์มไม้ลอย (อยู่ใน solids ด้วย แต่แยกไว้เพื่อวาดเป็นสะพานไม้) */
    public final List<Rectangle> platforms = new ArrayList<Rectangle>();
    /** หนามที่ทำให้เจ็บเมื่อเหยียบ */
    public final List<Rectangle> spikes = new ArrayList<Rectangle>();
    /** จุดเกิดของศัตรูทั้งหมด */
    public final List<Spawn> spawns = new ArrayList<Spawn>();

    public Point key;
    public Rectangle door;

    public Level(String code, String name, int width, Theme theme) {
        this.code = code;
        this.name = name;
        this.width = width;
        this.theme = theme;
    }

    // ------------------------------------------------------------------
    //  เมธอดสำหรับ "สร้างด่าน"
    // ------------------------------------------------------------------

    /** พื้นดินตั้งแต่ x1 ถึง x2 (เว้นช่วงระหว่างพื้นไว้ = หลุม) */
    public Level addGround(int x1, int x2) {
        solids.add(new Rectangle(x1, GROUND_Y, x2 - x1, SCREEN_H - GROUND_Y + 200));
        return this;
    }

    /** แพลตฟอร์มบางๆ ลอยอยู่กลางอากาศ */
    public Level addPlatform(int x, int y, int width) {
        Rectangle r = new Rectangle(x, y, width, 16);
        solids.add(r);
        platforms.add(r);
        return this;
    }

    /** บล็อกสี่เหลี่ยมหนาๆ (ใช้ทำเนินสูงหรือขั้นบันได) */
    public Level addBlock(int x, int y, int width, int height) {
        solids.add(new Rectangle(x, y, width, height));
        return this;
    }

    /** หนามบนพื้น */
    public Level addSpikes(int x, int width) {
        spikes.add(new Rectangle(x, GROUND_Y - 16, width, 16));
        return this;
    }

    public Level addEnemy(EnemyType type, double x, double feetY, int hp) {
        spawns.add(new Spawn(type, x, feetY, hp));
        return this;
    }

    // ---- มอนปกติ 4 แบบ (วางบนพื้นดินที่ตำแหน่ง x) ----

    /** สไลม์เขียว "Hopper" เลือด 30: กระโดดพุ่งเข้าใส่ */
    public Level addHopper(double x) {
        return addEnemy(EnemyType.HOPPER, x, GROUND_Y, 30);
    }

    /** สไลม์น้ำเงิน "Spitter" เลือด 35: ถอยห่างแล้วถ่มเมือกใส่ */
    public Level addSpitter(double x) {
        return addEnemy(EnemyType.SPITTER, x, GROUND_Y, 35);
    }

    /** สไลม์แดง "Brute" เลือด 80: ตัวใหญ่ พุ่งชน */
    public Level addBrute(double x) {
        return addEnemy(EnemyType.BRUTE, x, GROUND_Y, 80);
    }

    /** Ramses เลือด 60: เต่าหนามเกราะแข็ง */
    public Level addRamses(double x) {
        return addEnemy(EnemyType.RAMSES, x, GROUND_Y, 60);
    }

    public Level setKey(int x, int y) {
        key = new Point(x, y);
        return this;
    }

    public Level setDoor(int x) {
        door = new Rectangle(x, GROUND_Y - 80, 44, 80);
        return this;
    }

    // ------------------------------------------------------------------
    //  เมธอดสำหรับ "ถามข้อมูลเรขาคณิตของด่าน"
    // ------------------------------------------------------------------

    /** กล่องนี้ชนกับของแข็งชิ้นไหนหรือไม่ */
    public boolean hitsSolid(double x, double y, double w, double h) {
        for (Rectangle r : solids) {
            if (MathUtil.overlaps(x, y, w, h, r)) return true;
        }
        return false;
    }

    /** ระดับพื้นผิวบนสุดที่ตำแหน่งแนวนอน x (ของที่ตกลงมาจากฟ้าจะไปหยุดที่ระดับนี้) */
    public double surfaceY(double x) {
        double top = SCREEN_H + 200;
        for (Rectangle r : solids) {
            if (x >= r.x && x <= r.x + r.width && r.y < top) top = r.y;
        }
        return top;
    }

    /** จุด (px, py) อยู่ในของแข็งชิ้นไหนหรือไม่ (ใช้เช็กลูกธนูชนกำแพง และสไลม์เช็กว่าข้างหน้าเป็นหน้าผาไหม) */
    public boolean solidAt(double px, double py) {
        for (Rectangle r : solids) {
            if (r.contains(px, py)) return true;
        }
        return false;
    }
}
