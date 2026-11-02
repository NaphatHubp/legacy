package legacyknight.ui;

import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;
import static legacyknight.core.Constants.TILE;
import static legacyknight.core.Constants.TILE_SCALE;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import legacyknight.assets.Assets;
import legacyknight.level.Level;
import legacyknight.level.Theme;

/**
 * วาดโลกแบบ "Peaceful Nightfield": ท้องฟ้ากลางคืน ดวงดาว ภูเขา พื้นหญ้า สะพานไม้ ต้นไม้ และดอกไม้
 *
 * ใช้รูปชุดเดียวกันทุกด่าน แต่หมุนเฉดสีตาม Theme ของแต่ละด่าน
 * (ชุดรูปที่แปลงสีแล้วจะถูกเก็บไว้ใช้ซ้ำ ไม่ต้องคำนวณทุกเฟรม)
 */
final class NightfieldRenderer {

    /** ภาพพื้นหลังภูเขาขยายกี่เท่า */
    private static final int BG_SCALE = 5;
    private static final int STRIP_TOP = 130;     // ตำแหน่งบนสุดของแถบภูเขา

    /** ชุดรูปที่ผ่านการเปลี่ยนสีแล้วของด่านหนึ่งด่าน */
    private static final class Tiles {
        BufferedImage grassTL, grassTM, grassTR, grassL, grassR;
        final BufferedImage[] dirt = new BufferedImage[6];
        BufferedImage plankL, plankM, plankPost, plankR;
        BufferedImage canopy, trunk, root;
        final BufferedImage[] flowers = new BufferedImage[4];
        BufferedImage sky, star0, star1;
        Color skyColor, deepColor;
    }

    /** ต้นไม้ 1 ต้นที่วางไว้บนพื้น */
    private static final class Tree {
        int x, baseY, trunkSegments;
    }

    /** ดอกไม้ 1 ดอก */
    private static final class Flower {
        int x, y, kind;
    }

    private final Map<Theme, Tiles> tilesCache = new HashMap<Theme, Tiles>();
    private final Map<Level, List<Tree>> treeCache = new HashMap<Level, List<Tree>>();
    private final Map<Level, List<Flower>> flowerCache = new HashMap<Level, List<Flower>>();

    // =====================================================================
    //  พื้นหลัง
    // =====================================================================
    void drawBackground(Graphics2D g, Level level, double camX, double time) {
        Tiles t = tiles(level.theme);
        g.setColor(t.skyColor);
        g.fillRect(0, 0, SCREEN_W, SCREEN_H);

        // ดาวกะพริบ (เลื่อนช้ามาก เพื่อให้รู้สึกไกล)
        Random rng = new Random(11);
        for (int i = 0; i < 18; i++) {
            double sx = rng.nextInt(SCREEN_W + 200);
            int sy = 10 + rng.nextInt(210);
            BufferedImage star = (i % 3 == 0) ? t.star1 : t.star0;
            double x = ((sx - camX * 0.04) % (SCREEN_W + 200) + SCREEN_W + 200) % (SCREEN_W + 200) - 100;
            float a = (float) (0.35 + 0.65 * (0.5 + 0.5 * Math.sin(time * 2 + i * 1.7)));
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
            g.drawImage(star, (int) x, sy, star.getWidth() * 2, star.getHeight() * 2, null);
        }
        g.setComposite(AlphaComposite.SrcOver);

        // ภูเขา: แถบเดียววนซ้ำ สลับกลับด้านทีละแผ่นเพื่อไม่ให้เห็นรอยต่อ
        int tileW = t.sky.getWidth() * BG_SCALE;
        int tileH = t.sky.getHeight() * BG_SCALE;
        double off = camX * 0.25;
        int first = (int) Math.floor(off / tileW);
        for (int n = first; n * tileW - off < SCREEN_W; n++) {
            int x = (int) Math.round(n * tileW - off);
            boolean flip = (n & 1) != 0;
            if (flip) g.drawImage(t.sky, x + tileW, STRIP_TOP, -tileW, tileH, null);
            else g.drawImage(t.sky, x, STRIP_TOP, tileW, tileH, null);
        }
        // ใต้แถบภูเขาเป็นสีมืดต่อลงไปถึงพื้น
        g.setColor(t.deepColor);
        g.fillRect(0, STRIP_TOP + tileH, SCREEN_W, SCREEN_H - STRIP_TOP - tileH);
    }

    // =====================================================================
    //  ของที่อยู่ "ด้านหลัง" ตัวละคร (วาดก่อนพื้น): ต้นไม้
    // =====================================================================
    void drawTrees(Graphics2D g, Level level, double camX) {
        Tiles t = tiles(level.theme);
        for (Tree tree : trees(level)) {
            int width = TILE * 3;
            if (tree.x + width < camX || tree.x - width > camX + SCREEN_W) continue;
            int x = tree.x - TILE / 2;
            int y = tree.baseY;
            g.drawImage(t.root, x, y - TILE, TILE, TILE, null);
            y -= TILE;
            for (int s = 0; s < tree.trunkSegments; s++) {
                y -= TILE * 2;
                g.drawImage(t.trunk, x, y, TILE, TILE * 2, null);
            }
            int canopy = TILE * 3;
            g.drawImage(t.canopy, tree.x - canopy / 2, y - canopy + TILE / 2, canopy, canopy, null);
        }
    }

    // =====================================================================
    //  พื้น + สะพานไม้ + ดอกไม้
    // =====================================================================
    void drawTerrain(Graphics2D g, Level level, double camX) {
        Tiles t = tiles(level.theme);
        for (Rectangle r : level.solids) {
            if (r.x > camX + SCREEN_W || r.x + r.width < camX) continue;   // อยู่นอกจอ ไม่ต้องวาด
            if (level.platforms.contains(r)) drawPlatform(g, t, r);
            else drawGround(g, t, r);
        }
        for (Flower f : flowers(level)) {
            if (f.x < camX - TILE || f.x > camX + SCREEN_W) continue;
            g.drawImage(t.flowers[f.kind], f.x, f.y, TILE, TILE, null);
        }
    }

    /** พื้นดินสี่เหลี่ยม: แถวบนเป็นหญ้า ขอบซ้ายขวาเป็นหญ้า ข้างในเป็นดิน */
    private void drawGround(Graphics2D g, Tiles t, Rectangle r) {
        Shape oldClip = g.getClip();
        g.clipRect(r.x, r.y, r.width, r.height);
        int cols = (r.width + TILE - 1) / TILE;
        int rows = Math.min((r.height + TILE - 1) / TILE, (SCREEN_H - r.y) / TILE + 2);
        for (int row = 0; row < rows; row++) {
            int y = r.y + row * TILE;
            for (int col = 0; col < cols; col++) {
                int x = r.x + col * TILE;
                BufferedImage tile;
                boolean leftEdge = col == 0, rightEdge = col == cols - 1;
                if (row == 0) {
                    tile = leftEdge ? t.grassTL : (rightEdge ? t.grassTR : t.grassTM);
                } else if (leftEdge) {
                    tile = t.grassL;
                } else if (rightEdge) {
                    tile = t.grassR;
                } else {
                    int pick = Math.abs(col * 7 + row * 13 + r.x / 5) % t.dirt.length;
                    tile = t.dirt[pick];
                }
                g.drawImage(tile, x, y, TILE, TILE, null);
            }
        }
        g.setClip(oldClip);
    }

    /** สะพานไม้: แถวไม้กระดานยาวตลอดความกว้างของแพลตฟอร์ม มีเสาค้ำทุก 5 ช่อง */
    private void drawPlatform(Graphics2D g, Tiles t, Rectangle r) {
        Shape oldClip = g.getClip();
        g.clipRect(r.x, r.y, r.width, TILE);
        int cols = (r.width + TILE - 1) / TILE;
        for (int col = 0; col < cols; col++) {
            BufferedImage tile;
            if (col == 0) tile = t.plankL;
            else if (col == cols - 1) tile = t.plankR;
            else tile = (col % 5 == 2) ? t.plankPost : t.plankM;
            g.drawImage(tile, r.x + col * TILE, r.y, TILE, TILE, null);
        }
        g.setClip(oldClip);
    }

    /** แถบพื้นหญ้ายาวทั้งจอที่ระดับ y (ใช้เป็นพื้นยืนในหน้าเมนูและหน้าเนื้อเรื่อง) */
    void drawFloor(Graphics2D g, Level level, int y) {
        Tiles t = tiles(level.theme);
        int cols = SCREEN_W / TILE + 2;
        int rows = (SCREEN_H - y) / TILE + 1;
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                BufferedImage tile = row == 0 ? t.grassTM : t.dirt[Math.abs(col * 7 + row * 13) % t.dirt.length];
                g.drawImage(tile, col * TILE, y + row * TILE, TILE, TILE, null);
            }
        }
    }

    // =====================================================================
    //  จัดวางต้นไม้/ดอกไม้ (สุ่มแบบคงที่: เปิดด่านกี่ครั้งก็ได้ตำแหน่งเดิม)
    // =====================================================================
    private List<Tree> trees(Level level) {
        List<Tree> list = treeCache.get(level);
        if (list != null) return list;
        list = new ArrayList<Tree>();
        Random rng = new Random(level.code.hashCode());
        for (Rectangle r : level.solids) {
            if (level.platforms.contains(r) || r.width < 160) continue;
            for (int x = r.x + 70 + rng.nextInt(120); x < r.x + r.width - 70; x += 190 + rng.nextInt(240)) {
                if (nearAnything(level, x, 70)) continue;
                Tree tr = new Tree();
                tr.x = x;
                tr.baseY = r.y + 6;
                tr.trunkSegments = 1 + rng.nextInt(2);
                list.add(tr);
            }
        }
        treeCache.put(level, list);
        return list;
    }

    private List<Flower> flowers(Level level) {
        List<Flower> list = flowerCache.get(level);
        if (list != null) return list;
        list = new ArrayList<Flower>();
        Random rng = new Random(level.code.hashCode() * 31L + 5);
        for (Rectangle r : level.solids) {
            if (r.width < 90) continue;
            for (int x = r.x + 20 + rng.nextInt(60); x < r.x + r.width - 30; x += 60 + rng.nextInt(130)) {
                if (nearAnything(level, x, 20)) continue;
                Flower f = new Flower();
                f.x = x;
                f.y = r.y - TILE + 2 * TILE_SCALE;
                f.kind = rng.nextInt(4);
                list.add(f);
            }
        }
        flowerCache.put(level, list);
        return list;
    }

    /** x อยู่ใกล้หนาม ประตู กุญแจ หรือจุดเริ่มต้นผู้เล่นภายใน margin พิกเซลหรือไม่ (ไม่วางต้นไม้/ดอกไม้ทับ) */
    private boolean nearAnything(Level level, int x, int margin) {
        for (Rectangle s : level.spikes) {
            if (x > s.x - margin && x < s.x + s.width + margin) return true;
        }
        if (x > level.door.x - margin && x < level.door.x + level.door.width + margin) return true;
        if (Math.abs(x - level.key.x) < margin) return true;
        return x < 120;
    }

    // =====================================================================
    //  โหลดชุดรูปพร้อมเปลี่ยนสีตาม Theme
    // =====================================================================
    private Tiles tiles(Theme th) {
        Tiles t = tilesCache.get(th);
        if (t != null) return t;
        t = new Tiles();
        double gh = th.groundHue, sh = th.skyHue;
        t.grassTL = Assets.tinted("tiles/grass_tl.png", gh, 1);
        t.grassTM = Assets.tinted("tiles/grass_tm.png", gh, 1);
        t.grassTR = Assets.tinted("tiles/grass_tr.png", gh, 1);
        t.grassL = Assets.tinted("tiles/grass_l.png", gh, 1);
        t.grassR = Assets.tinted("tiles/grass_r.png", gh, 1);
        for (int i = 0; i < t.dirt.length; i++) t.dirt[i] = Assets.tinted("tiles/dirt_" + i + ".png", sh * 0.5, 1);
        t.plankL = Assets.image("tiles/plank_l.png");
        t.plankM = Assets.image("tiles/plank_m.png");
        t.plankPost = Assets.image("tiles/plank_post.png");
        t.plankR = Assets.image("tiles/plank_r.png");
        t.canopy = Assets.tinted("tiles/tree_canopy.png", gh, 1);
        t.trunk = Assets.image("tiles/tree_trunk.png");
        t.root = Assets.image("tiles/tree_root.png");
        for (int i = 0; i < t.flowers.length; i++) t.flowers[i] = Assets.image("tiles/flower_" + i + ".png");
        t.sky = Assets.tinted("tiles/sky_mountains.png", sh, th.skyBrightness);
        t.star0 = Assets.tinted("tiles/star_0.png", sh, th.skyBrightness);
        t.star1 = Assets.tinted("tiles/star_1.png", sh, th.skyBrightness);
        t.skyColor = new Color(t.sky.getRGB(2, 2), true);
        t.deepColor = new Color(t.sky.getRGB(t.sky.getWidth() / 2, t.sky.getHeight() - 1), true);
        tilesCache.put(th, t);
        return t;
    }
}
