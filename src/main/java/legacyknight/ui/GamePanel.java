package legacyknight.ui;

import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.JPanel;
import javax.swing.Timer;

import legacyknight.core.Game;
import legacyknight.core.InputHandler;

/**
 * พื้นที่วาดเกมใน Swing: ตั้งนาฬิกาให้ "อัปเดต + วาด" ประมาณ 60 ครั้งต่อวินาที
 * (เป็นส่วนเดียวที่ผูกกับ Swing ตรงๆ นอกจาก Main)
 */
public class GamePanel extends JPanel {

    private static final long serialVersionUID = 1L;

    private final Game game;
    private final Renderer renderer = new Renderer();
    private long lastNanos = System.nanoTime();

    public GamePanel() {
        setPreferredSize(new Dimension(SCREEN_W, SCREEN_H));
        setFocusable(true);

        InputHandler input = new InputHandler(this);
        game = new Game(input);
        input.setListener(game);
        addKeyListener(input);
        addMouseListener(input);
        addMouseMotionListener(input);

        // สลับไปหน้าต่างอื่นแล้วลืมปล่อยปุ่ม -> เคลียร์ทิ้ง ไม่งั้นตัวละครเดินไม่หยุด
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                input.clearAll();
            }
        });

        // นาฬิกาเกม: ทุก 16 มิลลิวินาที = ประมาณ 60 เฟรมต่อวินาที
        Timer timer = new Timer(16, e -> tick());
        timer.start();
    }

    /** ทำงาน 1 เฟรม: คำนวณเวลาที่ผ่านไป -> อัปเดตเกม -> สั่งวาดใหม่ */
    private void tick() {
        long now = System.nanoTime();
        // จำกัดไม่เกิน 0.033 วินาที กันเกมกระโดดข้ามเวลาถ้าเครื่องค้างชั่วขณะ
        double dt = Math.min(0.033, (now - lastNanos) / 1e9);
        lastNanos = now;

        game.update(dt);
        repaint();
        Toolkit.getDefaultToolkit().sync();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        renderer.render((Graphics2D) g, game);
    }
}
