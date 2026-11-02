package legacyknight.ui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;

import legacyknight.core.Game;
import legacyknight.core.GameState;

/**
 * ผู้จัดการการวาด: ดูว่าเกมอยู่หน้าไหน แล้วเรียกตัววาดของหน้านั้น
 * (ตัวนี้ไม่คิดเรื่องกติกาเกมเลย แค่อ่านค่าจาก Game มาวาด)
 */
public class Renderer {

    private final NightfieldRenderer night = new NightfieldRenderer();
    private final ScreenRenderer screens = new ScreenRenderer(night);
    private final WorldRenderer world = new WorldRenderer(night);

    public void render(Graphics2D g, Game game) {
        // เปิดการลบรอยหยักให้ภาพและตัวหนังสือเนียน
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        // รูปพิกเซลอาร์ตต้องขยายแบบ "เหลี่ยม" ไม่ใช่เบลอ
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        GameState state = game.getState();
        switch (state) {
            case MENU:
                screens.drawMenu(g, game);
                break;
            case TUTORIAL:
                screens.drawTutorial(g, game);
                break;
            case STORY:
                screens.drawStory(g, game);
                break;
            case MAP:
                screens.drawMap(g, game);
                break;
            default:
                // PLAY / WIN / LOSE ใช้ฉากเล่นเป็นพื้น แล้ววาดป้ายผลทับ
                world.draw(g, game);
                if (state == GameState.WIN) screens.drawWin(g, game);
                if (state == GameState.LOSE) screens.drawLose(g, game.getTime());
                if (game.isPaused()) screens.drawPause(g, game.getTime());
        }
    }
}
