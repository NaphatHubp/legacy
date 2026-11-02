package legacyknight.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashMap;
import java.util.Map;

/**
 * ไอคอนพิกเซลเล็กๆ ของ UI วาดจากตารางตัวอักษร ('.' = โปร่งใส ตัวอื่น = สีตามตารางสี)
 * ทุกอันวาดด้วยโค้ด ไม่ต้องใช้ไฟล์รูป ขยายเป็นเหลี่ยมคมตามค่า scale
 */
final class Sprites {

    private Sprites() { }

    static final class Sprite {
        private final String[] rows;
        private final Map<Character, Color> palette = new HashMap<Character, Color>();
        final int width;
        final int height;

        Sprite(String[] rows, Object... pairs) {
            this.rows = rows;
            for (int i = 0; i < pairs.length; i += 2) {
                palette.put((Character) pairs[i], (Color) pairs[i + 1]);
            }
            int w = 0;
            for (String r : rows) w = Math.max(w, r.length());
            width = w;
            height = rows.length;
        }

        void draw(Graphics2D g, int x, int y, int scale, boolean outline) {
            if (outline) {
                g.setColor(Gui.INK);
                for (int r = 0; r < height; r++) {
                    for (int c = 0; c < rows[r].length(); c++) {
                        if (rows[r].charAt(c) == '.') continue;
                        g.fillRect(x + (c - 1) * scale, y + (r - 1) * scale, scale * 3, scale * 3);
                    }
                }
            }
            for (int r = 0; r < height; r++) {
                for (int c = 0; c < rows[r].length(); c++) {
                    Color col = palette.get(rows[r].charAt(c));
                    if (col == null) continue;
                    g.setColor(col);
                    g.fillRect(x + c * scale, y + r * scale, scale, scale);
                }
            }
        }
    }

    private static final Color RED = new Color(200, 58, 58);
    private static final Color RED_L = new Color(255, 140, 130);
    private static final Color GREEN = new Color(98, 190, 112);
    private static final Color GREEN_L = new Color(190, 250, 190);
    private static final Color STEEL = new Color(120, 135, 160);
    private static final Color STEEL_L = new Color(190, 200, 220);
    private static final Color STEEL_D = new Color(70, 80, 105);
    private static final Color DARK = new Color(24, 18, 34);

    static final Sprite POINTER = new Sprite(new String[] {
            "g......",
            "ggg....",
            "ggggg..",
            "ggggggg",
            "ggggg..",
            "ggg....",
            "g......" }, 'g', Gui.GOLD_L);

    static final Sprite POINTER_L = new Sprite(new String[] {
            "......g",
            "....ggg",
            "..ggggg",
            "ggggggg",
            "..ggggg",
            "....ggg",
            "......g" }, 'g', Gui.GOLD_L);

    static final Sprite HEART = new Sprite(new String[] {
            ".rr.rr.",
            "rRrrrrr",
            "rrrrrrr",
            ".rrrrr.",
            "..rrr..",
            "...r..." }, 'r', RED, 'R', RED_L);

    /** EXP: อัญมณีเขียว */
    static final Sprite GEM = new Sprite(new String[] {
            "...e...",
            "..eEe..",
            ".eEEee.",
            "eEEeeee",
            ".eeeee.",
            "..eee..",
            "...e..." }, 'e', GREEN, 'E', GREEN_L);

    static final Sprite SHIELD = new Sprite(new String[] {
            "sssssgsss",
            "sSSssgsss",
            "ggggggggg",
            "sSsssgsss",
            "sSsssgsss",
            "sSsssgsss",
            ".sssgsss.",
            ".ssdgsss.",
            "..sdgss..",
            "...sgs...",
            "....s...." },
            's', STEEL, 'S', STEEL_L, 'd', STEEL_D, 'g', Gui.GOLD_L);

    static final Sprite SWORD = new Sprite(new String[] {
            "...w...",
            "..wWl..",
            "..wWl..",
            "..wWl..",
            "..wWl..",
            "..wWl..",
            "..wWl..",
            "ggggggg",
            "...h...",
            "...h...",
            "..ggg.." },
            'w', STEEL_L, 'W', Gui.BONE, 'l', STEEL, 'g', Gui.GOLD_L, 'h', Gui.GOLD_D);

    static final Sprite SKULL = new Sprite(new String[] {
            ".bbbbbbb.",
            "bbbbbbbbb",
            "bbbbbbbbb",
            "bkkbbbkkb",
            "bkkbbbkkb",
            "bbbbkbbbb",
            ".bbbbbbb.",
            "..bbbbb..",
            "..b.b.b.." }, 'b', Gui.BONE, 'k', DARK);

    static final Sprite LOCK = new Sprite(new String[] {
            "..sss..",
            ".s...s.",
            ".s...s.",
            "ggggggg",
            "gggkggg",
            "gggkggg",
            "ggggggg" }, 's', STEEL, 'g', Gui.GOLD, 'k', DARK);

    static final Sprite KEY = new Sprite(new String[] {
            ".gg.......",
            "g..g......",
            "g..ggggggg",
            ".gg...g.g." }, 'g', Gui.GOLD_L);

    static final Sprite CHECK = new Sprite(new String[] {
            "........g",
            ".......gg",
            "g.....gg.",
            "gg...gg..",
            ".gg.gg...",
            "..ggg....",
            "...g....." }, 'g', GREEN);

    static final Sprite CROWN = new Sprite(new String[] {
            "g...g...g",
            "gg.ggg.gg",
            "ggggggggg",
            "gGgGgGgGg",
            "ggggggggg" }, 'g', Gui.GOLD_L, 'G', Gui.BLOOD);
}
