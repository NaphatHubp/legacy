package legacyknight.assets;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

/**
 * ตัวโหลดรูป/แอนิเมชันจากโฟลเดอร์ resources/assets (โหลดครั้งเดียวแล้วจำไว้)
 *
 * ไฟล์ manifest.json (สร้างโดย tools/prepare_assets.py) บอกว่าแต่ละ strip มีกี่เฟรม
 */
public final class Assets {

    private static final Map<String, BufferedImage> IMAGES = new HashMap<String, BufferedImage>();
    private static final Map<String, Animation> ANIMATIONS = new HashMap<String, Animation>();
    private static final Map<String, Integer> MANIFEST = readManifest();

    private static final Map<BufferedImage, BufferedImage> WHITE = new IdentityHashMap<BufferedImage, BufferedImage>();

    private Assets() { }

    /** รูปเดี่ยวๆ เช่น image("tiles/grass_tm.png") */
    public static BufferedImage image(String path) {
        BufferedImage img = IMAGES.get(path);
        if (img == null) {
            img = read("/assets/" + path);
            IMAGES.put(path, img);
        }
        return img;
    }

    /** แอนิเมชันจากไฟล์ strip เช่น animation("cat/run.png") */
    public static Animation animation(String path) {
        Animation a = ANIMATIONS.get(path);
        if (a == null) {
            Integer frames = MANIFEST.get(path);
            if (frames == null) throw new IllegalStateException("Not in manifest.json: " + path);
            a = new Animation(image(path), frames);
            ANIMATIONS.put(path, a);
        }
        return a;
    }

    /** ตัวเลขที่ manifest เก็บไว้ เช่น "_cat_feet_row" */
    public static int number(String key) {
        Integer v = MANIFEST.get(key);
        if (v == null) throw new IllegalStateException("Not in manifest.json: " + key);
        return v;
    }

    /**
     * รูปที่ถูกเปลี่ยนสี (หมุนเฉดสี hueDegrees องศา และปรับความสว่าง brightness เท่า)
     * ใช้ทำให้ด่านแต่ละด่านมีโทนสีไม่เหมือนกันจากชุดรูปเดียว
     */
    public static BufferedImage tinted(String path, double hueDegrees, double brightness) {
        String key = path + "|" + hueDegrees + "|" + brightness;
        BufferedImage img = IMAGES.get(key);
        if (img == null) {
            img = shiftColors(image(path), hueDegrees, brightness);
            IMAGES.put(key, img);
        }
        return img;
    }

    /**
     * แอนิเมชันที่เปลี่ยนสีทั้งแถบภาพ (ใช้ทำร่างที่ 2 ของ Dead ให้เป็นสีม่วง)
     * จำนวนเฟรมอ่านจาก manifest.json ของไฟล์ต้นฉบับ
     */
    public static Animation tintedAnimation(String path, double hueDegrees, double brightness) {
        String key = "anim|" + path + "|" + hueDegrees + "|" + brightness;
        Animation a = ANIMATIONS.get(key);
        if (a == null) {
            Integer frames = MANIFEST.get(path);
            if (frames == null) throw new IllegalStateException("Not in manifest.json: " + path);
            a = new Animation(tinted(path, hueDegrees, brightness), frames);
            ANIMATIONS.put(key, a);
        }
        return a;
    }

    /** ภาพเงาสีขาวของเฟรมนี้ (ทุกพิกเซลที่ไม่โปร่งใสกลายเป็นสีขาว) ใช้ทำกะพริบตอนโดนตี / ตั้งท่า */
    public static BufferedImage whitened(BufferedImage frame) {
        BufferedImage w = WHITE.get(frame);
        if (w == null) {
            w = new BufferedImage(frame.getWidth(), frame.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < frame.getHeight(); y++) {
                for (int x = 0; x < frame.getWidth(); x++) {
                    int alpha = frame.getRGB(x, y) >>> 24;
                    if (alpha != 0) w.setRGB(x, y, (alpha << 24) | 0xFFFFFF);
                }
            }
            WHITE.put(frame, w);
        }
        return w;
    }

    // ------------------------------------------------------------------

    private static BufferedImage shiftColors(BufferedImage src, double hueDegrees, double brightness) {
        BufferedImage out = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        float[] hsb = new float[3];
        for (int y = 0; y < src.getHeight(); y++) {
            for (int x = 0; x < src.getWidth(); x++) {
                int argb = src.getRGB(x, y);
                int alpha = argb >>> 24;
                if (alpha == 0) continue;
                Color.RGBtoHSB((argb >> 16) & 0xFF, (argb >> 8) & 0xFF, argb & 0xFF, hsb);
                float h = (float) (hsb[0] + hueDegrees / 360.0);
                h -= (float) Math.floor(h);
                float b = (float) Math.min(1.0, hsb[2] * brightness);
                int rgb = Color.HSBtoRGB(h, hsb[1], b) & 0xFFFFFF;
                out.setRGB(x, y, (alpha << 24) | rgb);
            }
        }
        return out;
    }

    private static BufferedImage read(String resource) {
        try (InputStream in = Assets.class.getResourceAsStream(resource)) {
            if (in == null) throw new IllegalStateException("Missing resource: " + resource);
            return ImageIO.read(in);
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read resource: " + resource, e);
        }
    }

    /** อ่าน manifest.json แบบง่ายๆ (รูปแบบ "ชื่อไฟล์": จำนวน) โดยไม่ต้องใช้ไลบรารี JSON */
    private static Map<String, Integer> readManifest() {
        Map<String, Integer> map = new HashMap<String, Integer>();
        try (InputStream in = Assets.class.getResourceAsStream("/assets/manifest.json")) {
            if (in == null) throw new IllegalStateException("Missing resource: /assets/manifest.json");
            java.io.ByteArrayOutputStream buf = new java.io.ByteArrayOutputStream();
            byte[] chunk = new byte[4096];
            int n;
            while ((n = in.read(chunk)) > 0) buf.write(chunk, 0, n);
            String text = new String(buf.toByteArray(), "UTF-8");
            Matcher m = Pattern.compile("\"([^\"]+)\"\\s*:\\s*(\\d+)").matcher(text);
            while (m.find()) map.put(m.group(1), Integer.valueOf(m.group(2)));
        } catch (IOException e) {
            throw new IllegalStateException("Cannot read manifest.json", e);
        }
        return map;
    }
}
