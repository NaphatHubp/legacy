package legacyknight.level;

/**
 * โทนสีของด่าน: ใช้รูป Nightfield ชุดเดียวกัน แต่หมุนเฉดสีให้แต่ละด่านดูไม่เหมือนกัน
 * (ด่านแรกสีเขียวใต้แสงจันทร์ ด่านท้ายๆ ค่อยๆ เปลี่ยนเป็นสีแดงอมม่วงน่ากลัวขึ้น)
 */
public class Theme {

    /** หมุนเฉดสีหญ้า/ต้นไม้/ดอกไม้ (องศา) */
    public final double groundHue;
    /** หมุนเฉดสีท้องฟ้า/ภูเขา/ดิน (องศา) */
    public final double skyHue;
    /** ความสว่างของท้องฟ้า (1.0 = ปกติ) */
    public final double skyBrightness;

    public Theme(double groundHue, double skyHue, double skyBrightness) {
        this.groundHue = groundHue;
        this.skyHue = skyHue;
        this.skyBrightness = skyBrightness;
    }
}
