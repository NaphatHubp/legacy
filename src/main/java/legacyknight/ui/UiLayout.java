package legacyknight.ui;

import static legacyknight.core.Constants.SCREEN_H;

import java.awt.Rectangle;

/**
 * ตำแหน่งปุ่มบนหน้าจอเมนู/แผนที่ ใช้ร่วมกันทั้งตอน "วาด" และตอน "เช็กว่าคลิกโดนปุ่มไหน"
 * (เก็บไว้ที่เดียวกัน ปุ่มที่วาดกับปุ่มที่คลิกได้จะได้ตรงกันเสมอ)
 */
public final class UiLayout {

    private UiLayout() { }

    /** ปุ่มเมนูหลัก 3 ปุ่ม: START / TUTORIAL / LEAVE */
    public static final Rectangle[] MENU_BUTTONS = {
            new Rectangle(340, 270, 280, 54),
            new Rectangle(340, 336, 280, 54),
            new Rectangle(340, 402, 280, 54)
    };
    public static final String[] MENU_LABELS = { "START", "TUTORIAL", "LEAVE" };

    /** ปุ่มกลับบนหน้าแผนที่ */
    public static final Rectangle BACK_BUTTON = new Rectangle(30, SCREEN_H - 78, 130, 48);

    /** กล่องด่านทั้ง 4 บนแผนที่ เรียงเป็นรูปตัว U: แถวบนซ้าย->ขวา แล้ววนกลับแถวล่างขวา->ซ้าย */
    public static final Rectangle[] MAP_NODES = {
            new Rectangle(170, 128, 250, 124),
            new Rectangle(540, 128, 250, 124),
            new Rectangle(540, 296, 250, 124),
            new Rectangle(170, 296, 250, 124)
    };
}
