package legacyknight.story;

/**
 * ฉากเนื้อเรื่อง 1 ฉาก = หัวข้อ + ข้อความหลายย่อหน้า
 */
public class Scene {

    public final String title;
    public final String[] paragraphs;

    public Scene(String title, String... paragraphs) {
        this.title = title;
        this.paragraphs = paragraphs;
    }

    /** ข้อความทั้งหมดต่อกัน (ใช้คำนวณตัวอักษรที่พิมพ์ไปแล้วตอนทำเอฟเฟกต์พิมพ์ดีด) */
    public String fullText() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < paragraphs.length; i++) {
            if (i > 0) sb.append('\n');
            sb.append(paragraphs[i]);
        }
        return sb.toString();
    }
}
