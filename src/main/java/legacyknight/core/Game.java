package legacyknight.core;

import java.awt.Point;
import java.awt.event.KeyEvent;

import legacyknight.level.Level;
import legacyknight.level.LevelFactory;
import legacyknight.story.Scene;
import legacyknight.story.Story;
import legacyknight.ui.UiLayout;
import legacyknight.world.GameWorld;

/**
 * หัวใจของเกม: จำว่าตอนนี้อยู่หน้าไหน (เมนู / เนื้อเรื่อง / แผนที่ / เล่น ...) ด่านไหนปลดล็อกแล้ว
 * และรับคำสั่งจากปุ่มกด/คลิก เพื่อเปลี่ยนหน้าหรือเริ่มด่าน
 *
 * ไม่มีโค้ดวาดภาพอยู่ในนี้ (การวาดอยู่ในแพ็กเกจ ui)
 */
public class Game implements InputHandler.Listener {

    /** พอผู้เล่นอ่านฉากเนื้อเรื่องจบแล้ว จะไปไหนต่อ */
    private enum AfterStory { MAP, PLAY }

    /** ความเร็วเอฟเฟกต์พิมพ์ดีดของเนื้อเรื่อง (ตัวอักษรต่อวินาที) */
    private static final double TYPEWRITER_SPEED = 55;

    private final InputHandler input;
    private final Level[] levels = LevelFactory.createAll();
    private final boolean[] cleared = new boolean[LevelFactory.LEVEL_COUNT];
    private final PlayerStats stats = new PlayerStats();

    private GameState state = GameState.MENU;
    private GameWorld world;
    private int levelIndex = 0;
    /** ด่านที่เล่นได้ถึงลำดับที่เท่าไหร่ (0 = เล่นได้แค่ด่านแรก) */
    private int unlocked = 0;
    private boolean paused;
    private boolean prologueSeen;

    // ---- หน้าเนื้อเรื่อง ----
    private Scene scene;
    private AfterStory afterStory = AfterStory.MAP;
    private int storyLevel;
    private double storyTime;

    /** นาฬิกาของทั้งเกม (ใช้ทำแอนิเมชัน) */
    private double time;
    /** เวลาตั้งแต่ชนะ/แพ้ (กันกดข้ามหน้าสรุปผลเร็วเกินไป) */
    private double endTime;

    public Game(InputHandler input) {
        this.input = input;
        // เตรียมโลกของด่านแรกไว้ก่อน เพื่อให้ฉากหลังไม่เป็น null
        this.world = new GameWorld(levels[0], stats);
    }

    // =====================================================================
    //  อัปเดตทุกเฟรม
    // =====================================================================
    public void update(double dt) {
        time += dt;
        if (state == GameState.STORY) storyTime += dt;
        if (paused) return;

        world.updateEffects(dt);
        if (state == GameState.WIN || state == GameState.LOSE) endTime += dt;
        if (state != GameState.PLAY) return;

        world.update(dt, input);

        // ด่านจบแล้วหรือยัง?
        if (world.status == GameWorld.Status.WON) {
            cleared[levelIndex] = true;
            unlocked = Math.max(unlocked, Math.min(LevelFactory.LEVEL_COUNT - 1, levelIndex + 1));
            state = GameState.WIN;
        } else if (world.status == GameWorld.Status.LOST) {
            state = GameState.LOSE;
        }
    }

    /** เริ่ม (หรือเริ่มใหม่) ด่านลำดับที่ index */
    private void startLevel(int index) {
        levelIndex = index;
        world = new GameWorld(levels[index], stats);
        paused = false;
        endTime = 0;
        input.clearMouseButtons();
        state = GameState.PLAY;
    }

    /** เปิดหน้าเนื้อเรื่อง: อ่านจบแล้วไปที่ next (ถ้า next = PLAY จะเริ่มด่าน level) */
    private void showStory(Scene s, AfterStory next, int level) {
        scene = s;
        afterStory = next;
        storyLevel = level;
        storyTime = 0;
        state = GameState.STORY;
    }

    /** ผู้เล่นกดข้ามไปหน้าต่อไป: ถ้าตัวหนังสือยังพิมพ์ไม่จบ จะแสดงให้ครบก่อน */
    private void advanceStory() {
        if (!isStoryTextComplete()) {
            storyTime = 1e6;
            return;
        }
        if (afterStory == AfterStory.PLAY) startLevel(storyLevel);
        else state = GameState.MAP;
    }

    /** เข้าด่านที่ index (ผ่านฉากเนื้อเรื่องก่อนเสมอ) */
    private void enterLevel(int index) {
        showStory(Story.intro(index), AfterStory.PLAY, index);
    }

    /** กดปุ่ม START: ครั้งแรกให้ดูฉากเปิดเรื่อง หลังจากนั้นไปแผนที่เลย */
    private void openMapFromMenu() {
        if (!prologueSeen) {
            prologueSeen = true;
            showStory(Story.prologue(), AfterStory.MAP, 0);
        } else {
            state = GameState.MAP;
        }
    }

    /** กดต่อจากหน้าชนะ: ถ้าเพิ่งชนะด่านสุดท้ายจะเข้าฉากจบเกม ไม่งั้นกลับแผนที่ */
    private void leaveWinScreen() {
        if (levelIndex == LevelFactory.LEVEL_COUNT - 1) showStory(Story.epilogue(), AfterStory.MAP, 0);
        else state = GameState.MAP;
    }

    // =====================================================================
    //  รับคำสั่งจากผู้เล่น
    // =====================================================================
    @Override
    public void onKeyPressed(int key) {
        switch (state) {
            case MENU:
                if (key == KeyEvent.VK_ENTER) openMapFromMenu();
                break;
            case TUTORIAL:
                if (key == KeyEvent.VK_ESCAPE || key == KeyEvent.VK_ENTER) state = GameState.MENU;
                break;
            case STORY:
                if (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE) advanceStory();
                if (key == KeyEvent.VK_ESCAPE) state = GameState.MAP;
                break;
            case MAP:
                if (key == KeyEvent.VK_ESCAPE) state = GameState.MENU;
                break;
            case PLAY:
                if (key == KeyEvent.VK_ESCAPE) state = GameState.MAP;
                if (key == KeyEvent.VK_P) paused = !paused;
                break;
            case WIN:
                if (endTime > 0.7 && (key == KeyEvent.VK_ENTER || key == KeyEvent.VK_SPACE
                        || key == KeyEvent.VK_ESCAPE)) {
                    leaveWinScreen();
                }
                break;
            case LOSE:
                if (endTime > 0.7 && (key == KeyEvent.VK_R || key == KeyEvent.VK_ENTER)) startLevel(levelIndex);
                if (key == KeyEvent.VK_ESCAPE) state = GameState.MAP;
                break;
        }
    }

    @Override
    public void onLeftClick(Point pt) {
        switch (state) {
            case MENU:
                if (UiLayout.MENU_BUTTONS[0].contains(pt)) openMapFromMenu();
                else if (UiLayout.MENU_BUTTONS[1].contains(pt)) state = GameState.TUTORIAL;
                else if (UiLayout.MENU_BUTTONS[2].contains(pt)) System.exit(0);
                break;
            case TUTORIAL:
                state = GameState.MENU;
                break;
            case STORY:
                advanceStory();
                break;
            case MAP:
                if (UiLayout.BACK_BUTTON.contains(pt)) {
                    state = GameState.MENU;
                    break;
                }
                for (int i = 0; i < LevelFactory.LEVEL_COUNT; i++) {
                    if (i <= unlocked && UiLayout.MAP_NODES[i].contains(pt)) {
                        enterLevel(i);
                        break;
                    }
                }
                break;
            case WIN:
                if (endTime > 0.7) leaveWinScreen();
                break;
            case LOSE:
                if (endTime > 0.7) startLevel(levelIndex);
                break;
            default:
                break;   // ตอนเล่น (PLAY) คลิกถูกใช้เป็นฟันดาบ ไม่ต้องทำอะไรตรงนี้
        }
    }

    // =====================================================================
    //  ให้ส่วนวาดภาพ (ui) มาอ่านค่า
    // =====================================================================
    public GameState getState()      { return state; }
    public GameWorld getWorld()      { return world; }
    public PlayerStats getStats()    { return stats; }
    public Level[] getLevels()       { return levels; }
    public int getLevelIndex()       { return levelIndex; }
    public int getUnlocked()         { return unlocked; }
    public boolean isCleared(int i)  { return cleared[i]; }
    public boolean isPaused()        { return paused; }
    public double getTime()          { return time; }
    public InputHandler getInput()   { return input; }

    public Scene getScene()          { return scene; }
    /** ด่านที่ใช้เป็นฉากหลัง/ธีมของหน้าเนื้อเรื่อง */
    public int getStoryLevel()       { return storyLevel; }
    /** ตัวอักษรที่พิมพ์ไปแล้วในหน้าเนื้อเรื่อง */
    public int getStoryChars()       { return (int) Math.min(Integer.MAX_VALUE, storyTime * TYPEWRITER_SPEED); }
    public boolean isStoryTextComplete() {
        return scene == null || getStoryChars() >= scene.fullText().length();
    }
}
