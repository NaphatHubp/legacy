package legacyknight.ui;

import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ชุดเครื่องมือวาด UI กลางของเกม ("ปราสาทหินมืด + กรอบทอง + พิกเซลอาร์ต")
 * ทุกหน้าจอและ HUD เรียกใช้ที่นี่ เพื่อให้หน้าตาเป็นแบบเดียวกันทั้งเกม
 *
 *   สี        INK / STONE / GOLD / BONE / BLOOD ... (สีทองดึงมาจากกรอบแถบเลือดบอส Dead)
 *   ฟอนต์     title() = Press Start 2P (หัวข้อ ปุ่ม)   body() = VT323 (ข้อความอ่านยาวๆ)
 *   ชิ้นส่วน  panel / button / bar / divider / banner / vignette / sprite (ไอคอนพิกเซล)
 *
 * ทุกอย่างวาดเป็นสี่เหลี่ยมคมๆ (ไม่มีมุมโค้ง) ให้เข้ากับพิกเซลอาร์ตของตัวละคร
 */
final class Gui {

    private Gui() { }

    // ---------- พาเลตต์สี ----------
    static final Color INK = new Color(9, 10, 20);
    static final Color NIGHT = new Color(18, 16, 30);
    static final Color STONE_D = new Color(30, 28, 44);
    static final Color STONE = new Color(46, 43, 64);
    static final Color STONE_L = new Color(74, 70, 98);
    static final Color GOLD_L = new Color(238, 190, 92);
    static final Color GOLD = new Color(190, 119, 43);
    static final Color GOLD_D = new Color(136, 75, 43);
    static final Color BONE = new Color(235, 237, 233);
    static final Color BONE_D = new Color(170, 172, 180);
    static final Color BLOOD = new Color(200, 58, 58);
    static final Color BLOOD_D = new Color(117, 36, 56);
    static final Color MOSS = new Color(98, 190, 112);

    // ---------- ฟอนต์ ----------
    private static final Font TITLE_BASE = load("/assets/fonts/PressStart2P-Regular.ttf", new Font(Font.MONOSPACED, Font.BOLD, 8));
    private static final Font BODY_BASE = load("/assets/fonts/VT323-Regular.ttf", new Font(Font.MONOSPACED, Font.PLAIN, 16));

    private static Font load(String path, Font fallback) {
        try (InputStream in = Gui.class.getResourceAsStream(path)) {
            if (in == null) return fallback;
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (IOException | java.awt.FontFormatException e) {
            return fallback;
        }
    }

    /** ฟอนต์หัวข้อพิกเซล (ขนาดที่คมสุดคือ 8, 16, 24, 32, 40, 48 ...) */
    static Font title(float size) { return TITLE_BASE.deriveFont(size); }

    /** ฟอนต์เนื้อความพิกเซล (อ่านง่าย ขนาดแนะนำ 20-28) */
    static Font body(float size) { return BODY_BASE.deriveFont(size); }

    // =====================================================================
    //  ข้อความ (ปิด anti-alias ให้ฟอนต์พิกเซลคม)
    // =====================================================================

    private static Object crisp(Graphics2D g) {
        Object old = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        return old;
    }

    private static void restore(Graphics2D g, Object old) {
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                old == null ? RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT : old);
    }

    /** ข้อความมีเงาดำเยื้องลงขวา (align: -1 ชิดซ้าย, 0 กึ่งกลาง, 1 ชิดขวา ที่ตำแหน่ง x) */
    static void text(Graphics2D g, String s, Font f, Color c, double x, double y, int align) {
        Object old = crisp(g);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int w = fm.stringWidth(s);
        int px = (int) Math.round(align == 0 ? x - w / 2.0 : (align > 0 ? x - w : x));
        int py = (int) Math.round(y);
        int sh = Math.max(1, Math.round(f.getSize2D() / 12f));
        g.setColor(INK);
        g.drawString(s, px + sh, py + sh);
        g.setColor(c);
        g.drawString(s, px, py);
        restore(g, old);
    }

    /** หัวข้อใหญ่: ชั้นเงาเป็นขั้นบันได (ดำ -> ทองเข้ม) แล้วทับด้วยสีหลัก */
    static void heading(Graphics2D g, String s, float size, Color main, Color edge, double cx, double y) {
        Object old = crisp(g);
        Font f = title(size);
        g.setFont(f);
        int w = g.getFontMetrics().stringWidth(s);
        int px = (int) Math.round(cx - w / 2.0);
        int step = Math.max(2, Math.round(size / 8f));
        for (int i = step * 2; i >= 1; i--) {
            g.setColor(i > step ? INK : edge);
            g.drawString(s, px, (int) Math.round(y) + i);
        }
        // ขอบดำรอบตัวอักษร
        g.setColor(INK);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx != 0 || dy != 0) g.drawString(s, px + dx * step / 2, (int) Math.round(y) + dy * step / 2);
            }
        }
        g.setColor(main);
        g.drawString(s, px, (int) Math.round(y));
        restore(g, old);
    }

    /** ตัดข้อความให้พอดีความกว้าง maxWidth ตามฟอนต์ f */
    static List<String> wrap(Graphics2D g, String text, Font f, int maxWidth) {
        List<String> lines = new ArrayList<String>();
        Object old = crisp(g);
        FontMetrics fm = g.getFontMetrics(f);
        StringBuilder line = new StringBuilder();
        for (String word : text.split(" ")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) > maxWidth && line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder(word);
            } else {
                line = new StringBuilder(candidate);
            }
        }
        if (line.length() > 0) lines.add(line.toString());
        restore(g, old);
        return lines;
    }

    /** เขียนย่อหน้า (ตัดบรรทัดอัตโนมัติ) คืนค่า y ของบรรทัดถัดไป */
    static int paragraph(Graphics2D g, String text, Font f, Color c, int x, int y, int maxWidth, int lineH) {
        for (String line : wrap(g, text, f, maxWidth)) {
            text(g, line, f, c, x, y, -1);
            y += lineH;
        }
        return y;
    }

    // =====================================================================
    //  กรอบ / ปุ่ม / แถบ
    // =====================================================================

    /** แผ่นหินมืด + กรอบทอง 2 ชั้น + หมุดที่มุม (มุมตัดเฉียงแบบพิกเซล) */
    static void panel(Graphics2D g, int x, int y, int w, int h, float alpha) {
        int a = (int) (255 * alpha);
        Paint old = g.getPaint();
        g.setPaint(new GradientPaint(0, y, withAlpha(STONE, a), 0, y + h, withAlpha(STONE_D, a)));
        notched(g, x, y, w, h, 4, true);
        g.setPaint(old);

        g.setColor(INK);
        notched(g, x, y, w, h, 4, false);                                     // ขอบดำนอกสุด
        g.setColor(GOLD);
        notched(g, x + 2, y + 2, w - 4, h - 4, 3, false);                      // เส้นทอง
        g.setColor(withAlpha(GOLD_D, 255));
        notched(g, x + 3, y + 3, w - 6, h - 6, 2, false);
        g.setColor(withAlpha(INK, 160));
        g.fillRect(x + 5, y + 5, w - 10, 2);                                   // เงาในใต้เส้นทอง
        g.setColor(withAlpha(STONE_L, 90));
        g.fillRect(x + 5, y + 5, w - 10, 1);

        g.setColor(GOLD_L);                                                   // หมุดมุม
        int s = 4;
        g.fillRect(x + 5, y + 5, s, s);
        g.fillRect(x + w - 5 - s, y + 5, s, s);
        g.fillRect(x + 5, y + h - 5 - s, s, s);
        g.fillRect(x + w - 5 - s, y + h - 5 - s, s, s);
    }

    static void panel(Graphics2D g, Rectangle r) {
        panel(g, r.x, r.y, r.width, r.height, 0.94f);
    }

    /** สี่เหลี่ยมตัดมุม cut พิกเซล (fill = true ระบายเต็ม / false เป็นเส้นกรอบหนา 1) */
    private static void notched(Graphics2D g, int x, int y, int w, int h, int cut, boolean fill) {
        if (fill) {
            g.fillRect(x + cut, y, w - 2 * cut, h);
            g.fillRect(x, y + cut, w, h - 2 * cut);
            g.fillRect(x + 1, y + 1, w - 2, h - 2);
        } else {
            g.fillRect(x + cut, y, w - 2 * cut, 1);
            g.fillRect(x + cut, y + h - 1, w - 2 * cut, 1);
            g.fillRect(x, y + cut, 1, h - 2 * cut);
            g.fillRect(x + w - 1, y + cut, 1, h - 2 * cut);
            for (int i = 0; i < cut; i++) {                                   // มุมเฉียงเป็นขั้นบันได
                g.fillRect(x + cut - i - 1, y + i, 1, 1);
                g.fillRect(x + w - cut + i, y + i, 1, 1);
                g.fillRect(x + cut - i - 1, y + h - 1 - i, 1, 1);
                g.fillRect(x + w - cut + i, y + h - 1 - i, 1, 1);
            }
        }
    }

    /** ปุ่มหิน: ชี้เมาส์แล้วเรืองทอง มีลูกศรดาบชี้ขนาบข้าง */
    static void button(Graphics2D g, Rectangle r, String label, boolean hover, boolean primary, double time) {
        Paint old = g.getPaint();
        Color top = hover ? new Color(88, 70, 70) : (primary ? new Color(66, 52, 62) : STONE);
        Color bottom = hover ? new Color(52, 36, 44) : STONE_D;
        g.setPaint(new GradientPaint(0, r.y, top, 0, r.y + r.height, bottom));
        notched(g, r.x, r.y, r.width, r.height, 4, true);
        g.setPaint(old);
        g.setColor(INK);
        notched(g, r.x, r.y, r.width, r.height, 4, false);
        g.setColor(hover ? GOLD_L : (primary ? GOLD : GOLD_D));
        notched(g, r.x + 2, r.y + 2, r.width - 4, r.height - 4, 3, false);
        g.setColor(withAlpha(STONE_L, hover ? 150 : 80));
        g.fillRect(r.x + 5, r.y + 4, r.width - 10, 1);

        Color textColor = hover ? GOLD_L : BONE;
        text(g, label, title(16), textColor, r.getCenterX(), r.getCenterY() + 7, 0);
        if (hover) {
            int bob = (int) Math.round(Math.sin(time * 8) * 2);
            sprite(g, Sprites.POINTER, r.x + 18 + bob, r.y + r.height / 2 - 7, 3, false);
            sprite(g, Sprites.POINTER_L, r.x + r.width - 18 - 21 - bob, r.y + r.height / 2 - 7, 3, false);
        }
    }

    /**
     * แถบค่า (HP / EXP / เลือดบอส): กรอบดำ + ทอง ข้างในมืด เติมสีไล่เฉด มีเส้นสะท้อนและขีดแบ่งช่อง
     *
     * @param ticks จำนวนช่องที่ขีดแบ่ง (0 = ไม่มี)
     */
    static void bar(Graphics2D g, int x, int y, int w, int h, double frac, Color top, Color bottom, int ticks) {
        frac = Math.max(0, Math.min(1, frac));
        g.setColor(INK);
        g.fillRect(x - 3, y - 3, w + 6, h + 6);
        g.setColor(GOLD);
        g.fillRect(x - 2, y - 2, w + 4, h + 4);
        g.setColor(GOLD_D);
        g.fillRect(x - 1, y + h, w + 2, 1);
        g.setColor(NIGHT);
        g.fillRect(x - 1, y - 1, w + 2, h + 2);
        int fw = (int) Math.round(w * frac);
        if (fw > 0) {
            Paint old = g.getPaint();
            g.setPaint(new GradientPaint(0, y, top, 0, y + h, bottom));
            g.fillRect(x, y, fw, h);
            g.setPaint(old);
            g.setColor(new Color(255, 255, 255, 70));
            g.fillRect(x, y + 1, fw, Math.max(1, h / 5));
        }
        if (ticks > 1) {
            g.setColor(withAlpha(INK, 150));
            for (int i = 1; i < ticks; i++) g.fillRect(x + w * i / ticks - 1, y, 2, h);
        }
    }

    /** เส้นคั่นลวดลาย: เส้นทองสองข้าง มีเพชรตรงกลาง */
    static void divider(Graphics2D g, double cx, int y, int w) {
        int x0 = (int) (cx - w / 2.0);
        g.setColor(GOLD_D);
        g.fillRect(x0, y + 1, w / 2 - 12, 2);
        g.fillRect((int) cx + 12, y + 1, w / 2 - 12, 2);
        g.setColor(GOLD);
        g.fillRect(x0, y, w / 2 - 12, 2);
        g.fillRect((int) cx + 12, y, w / 2 - 12, 2);
        int c = (int) cx;
        g.setColor(INK);
        g.fillRect(c - 7, y - 5, 14, 14);
        g.setColor(GOLD_L);
        g.fillRect(c - 5, y - 3, 10, 10);
        g.setColor(BLOOD);
        g.fillRect(c - 3, y - 1, 6, 6);
    }

    /** ป้ายริบบิ้นแนวนอนกลางจอ (ใช้กับข้อความ Banner ตอนเล่น) */
    static void ribbon(Graphics2D g, int y, int h, float alpha) {
        int a = (int) (alpha * 210);
        g.setColor(new Color(9, 10, 20, a));
        g.fillRect(0, y, SCREEN_W, h);
        g.setColor(new Color(190, 119, 43, (int) (alpha * 255)));
        g.fillRect(0, y, SCREEN_W, 2);
        g.fillRect(0, y + h - 2, SCREEN_W, 2);
        g.setColor(new Color(136, 75, 43, (int) (alpha * 255)));
        g.fillRect(0, y + 2, SCREEN_W, 1);
        g.fillRect(0, y + h - 3, SCREEN_W, 1);
    }

    /** ขอบจอมืด (ทำให้สายตาโฟกัสกลางจอ) */
    static void vignette(Graphics2D g, float strength) {
        RadialGradientPaint p = new RadialGradientPaint(
                new Point2D.Double(SCREEN_W / 2.0, SCREEN_H / 2.0), (float) (SCREEN_W * 0.62),
                new float[] { 0f, 0.6f, 1f },
                new Color[] { new Color(0, 0, 0, 0), new Color(0, 0, 0, 0), new Color(4, 3, 12, (int) (255 * strength)) });
        Paint old = g.getPaint();
        g.setPaint(p);
        g.fillRect(0, 0, SCREEN_W, SCREEN_H);
        g.setPaint(old);
    }

    /** ชั้นสีมืดคลุมทั้งจอ */
    static void dim(Graphics2D g, int alpha) {
        g.setColor(new Color(6, 5, 14, alpha));
        g.fillRect(0, 0, SCREEN_W, SCREEN_H);
    }

    static Color withAlpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
    }

    /** วาดสไปรต์พิกเซลจากตาราง (ดู Sprites) scale เท่า; outline = true จะมีขอบดำรอบ */
    static void sprite(Graphics2D g, Sprites.Sprite sp, int x, int y, int scale, boolean outline) {
        sp.draw(g, x, y, scale, outline);
    }

    /** กล่องช่องใส่ (ไอคอนปุ่มกด): หินมืด + กรอบทอง เรืองเมื่อ active */
    static void slot(Graphics2D g, int x, int y, int size, boolean active) {
        g.setColor(INK);
        g.fillRect(x - 3, y - 3, size + 6, size + 6);
        g.setColor(active ? GOLD_L : GOLD_D);
        g.fillRect(x - 2, y - 2, size + 4, size + 4);
        Paint old = g.getPaint();
        g.setPaint(new GradientPaint(0, y, active ? new Color(96, 76, 60) : STONE, 0, y + size, STONE_D));
        g.fillRect(x, y, size, size);
        g.setPaint(old);
    }
}
