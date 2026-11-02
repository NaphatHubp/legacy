package legacyknight;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import legacyknight.ui.GamePanel;

/**
 * จุดเริ่มต้นของโปรแกรม: เปิดหน้าต่างและใส่เกมลงไป
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("The Legacy Knight");
            GamePanel game = new GamePanel();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setContentPane(game);
            frame.setResizable(false);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
            game.requestFocusInWindow();
        });
    }
}
