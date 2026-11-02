package legacyknight.core;

import java.awt.Component;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Arrays;

import javax.swing.SwingUtilities;

/**
 * รับคีย์บอร์ดและเมาส์ แล้วจดไว้ว่า "ตอนนี้ปุ่มไหนถูกกดค้างอยู่"
 * ส่วนเกมจะมาถามเองตอนต้องใช้ เช่น input.isKeyDown(KeyEvent.VK_A)
 *
 * สำหรับเหตุการณ์ที่เกิดแค่ครั้งเดียว (กดปุ่มหนึ่งครั้ง / คลิกซ้ายหนึ่งครั้ง)
 * จะแจ้งเกมผ่าน Listener แทน เพื่อใช้กับเมนูและปุ่มต่างๆ
 */
public class InputHandler implements KeyListener, MouseListener, MouseMotionListener {

    /** ผู้ที่อยากรู้เหตุการณ์ "กดครั้งเดียว" (ในที่นี้คือ Game) */
    public interface Listener {
        void onKeyPressed(int keyCode);
        void onLeftClick(Point point);
    }

    private final boolean[] keys = new boolean[256];

    public boolean mouseLeft;
    public boolean mouseRight;
    public int mouseX;
    public int mouseY;

    private final Component focusTarget;
    private Listener listener;

    /** focusTarget = หน้าต่างเกม (คลิกแล้วต้องดึงโฟกัสกลับมาเพื่อให้รับคีย์บอร์ดได้) */
    public InputHandler(Component focusTarget) {
        this.focusTarget = focusTarget;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isKeyDown(int keyCode) {
        return keyCode < keys.length && keys[keyCode];
    }

    /** ลืมการกดทุกปุ่ม (ใช้ตอนเริ่มด่านใหม่ หรือตอนหน้าต่างเสียโฟกัส) */
    public void clearAll() {
        Arrays.fill(keys, false);
        clearMouseButtons();
    }

    public void clearMouseButtons() {
        mouseLeft = false;
        mouseRight = false;
    }

    // ---------- คีย์บอร์ด ----------
    @Override
    public void keyPressed(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = true;
        if (listener != null) listener.onKeyPressed(code);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int code = e.getKeyCode();
        if (code < keys.length) keys[code] = false;
    }

    @Override
    public void keyTyped(KeyEvent e) { }

    // ---------- เมาส์ ----------
    @Override
    public void mousePressed(MouseEvent e) {
        focusTarget.requestFocusInWindow();
        mouseX = e.getX();
        mouseY = e.getY();
        boolean left = SwingUtilities.isLeftMouseButton(e);
        boolean right = SwingUtilities.isRightMouseButton(e);
        if (left) mouseLeft = true;
        if (right) mouseRight = true;
        if (left && listener != null) listener.onLeftClick(e.getPoint());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) mouseLeft = false;
        if (SwingUtilities.isRightMouseButton(e)) mouseRight = false;
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }
}
