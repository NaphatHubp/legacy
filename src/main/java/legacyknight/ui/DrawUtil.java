package legacyknight.ui;

import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.GlyphVector;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

/**
 * เครื่องมือวาดเล็กๆ ที่ใช้ซ้ำหลายหน้าจอ
 */
final class DrawUtil {

    private DrawUtil() { }

    /** เขียนข้อความให้กึ่งกลางอยู่ที่ x = cx (ใช้ฟอนต์ที่ตั้งไว้ใน g ตอนนี้) */
    static void centered(Graphics2D g, String s, double cx, double y) {
        FontMetrics fm = g.getFontMetrics();
        g.drawString(s, (float) (cx - fm.stringWidth(s) / 2.0), (float) y);
    }

    /** ข้อความตัวใหญ่มีขอบ: ระบายสี fill แล้ววาดเส้นขอบสี stroke หนา sw */
    static void outlined(Graphics2D g, String s, Font f, double cx, double baseY,
                         Color fill, Color stroke, float sw) {
        GlyphVector gv = f.createGlyphVector(g.getFontRenderContext(), s);
        Rectangle2D b = gv.getVisualBounds();
        Shape shape = gv.getOutline((float) (cx - b.getWidth() / 2 - b.getX()), (float) baseY);
        // วาดขอบก่อน แล้วระบายสีตัวอักษรทับ ขอบจะอยู่ด้านนอกตัวอักษรเสมอ (ตัวหนังสือไม่ถูกขอบกิน)
        g.setStroke(new BasicStroke(sw, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g.setColor(stroke);
        g.draw(shape);
        g.setColor(fill);
        g.fill(shape);
    }

    /** ภูเขาลูกคลื่นเป็นฉากหลัง (off = ระยะเลื่อน ใช้ทำให้ภูเขาเลื่อนช้ากว่าพื้นเวลากล้องเคลื่อน) */
    static void drawHills(Graphics2D g, double off, int base, int amp, double freq, Color c) {
        Path2D path = new Path2D.Double();
        path.moveTo(0, SCREEN_H);
        for (int x = 0; x <= SCREEN_W; x += 16) {
            path.lineTo(x, base + amp * Math.sin((x + off) * freq));
        }
        path.lineTo(SCREEN_W, SCREEN_H);
        path.closePath();
        g.setColor(c);
        g.fill(path);
    }
}
