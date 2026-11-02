package legacyknight.world;

import static legacyknight.core.Constants.MUTATION_WARNING_TIME;

import java.awt.Color;

import legacyknight.entity.Enemy;
import legacyknight.entity.Player;
import legacyknight.entity.Telegraph;
import legacyknight.util.MathUtil;

/**
 * สมองของ "Demon King" (มินิบอสของด่าน MP3)
 *
 * ร่างที่ 1 (เลือดมากกว่า 50%)
 *   วนท่า: ลูกไฟเล็งตรง -> ลูกไฟ 3 ทาง -> ลูกไฟเร็ว
 *   ทุกๆ ไม่กี่วินาทีจะ "ยกโล่" กันดาบ (ดาเมจเหลือน้อยมาก) แล้วสวนกลับ
 *
 * เลือดลดถึง 50%  ->  เตือนล่วงหน้า 3 วินาที (WARNING!)
 *   ระหว่างนี้บอสอมตะ ตัวสั่น จอสั่น กระพริบสลับสี และหยุดโจมตี ผู้เล่นใช้เวลานี้เตรียมตัวได้
 *
 * ร่างที่ 2 (กลายพันธุ์: ตัวใหญ่ขึ้น สีม่วง) ท่าใหม่ทั้งหมด วนท่า:
 *   วงแหวนลูกไฟ -> ลูกไฟ 5 ทางกว้าง -> ฝนไฟจากฟ้า (มีเสาแดงเตือนบนพื้นก่อน กันด้วยโล่ไม่ได้) -> ลูกไฟพัด 7 ลูก
 *   ยกโล่ถี่ขึ้นแต่สั้นลง และสวนกลับด้วยวงแหวนลูกไฟ
 */
final class DemonKingAI {

    private DemonKingAI() { }

    static void update(GameWorld world, Enemy e, double dt) {
        Player p = world.player;
        double px = p.x + p.w / 2.0;
        double py = p.y + p.h / 2.0;
        double cx = e.x + e.w / 2.0;
        double cy = e.y + e.h * 0.35;

        e.aim = Math.atan2(py - cy, px - cx);
        e.dir = px >= cx ? 1 : -1;
        e.vx = 0;
        e.applyPhysics(dt, world.level);
        e.guardHitTimer -= dt;

        // ---- กำลังกลายพันธุ์: อมตะ หยุดโจมตี รอจนหมดเวลา ----
        if (e.mutating) {
            updateMutation(world, e, dt, cx, cy);
            return;
        }
        // ---- เลือดถึงครึ่ง: เริ่มคำเตือน ----
        if (!e.mutated && e.getHp() <= e.getMaxHp() * 0.5) {
            beginMutation(world, e, cx, cy);
            return;
        }

        // ---- กำลังยกโล่ ----
        if (e.guardTimer > 0) {
            e.guardTimer -= dt;
            e.guardTime += dt;
            if (e.guardTimer <= 0) endGuard(e);
            return;
        }

        // ผู้เล่นไกลเกินไปก็ไม่ทำอะไร
        if (Math.abs(px - cx) > 900) return;

        // ---- เลือกว่าจะยกโล่ไหม (ไม่ยกกลางจังหวะง้างปาก) ----
        e.guardCooldown -= dt;
        boolean notWindingUp = e.shootTimer > EnemyAI.WINDUP + 0.1;
        boolean playerSwingingClose = p.attackTimer > 0 && Math.abs(px - cx) < e.w / 2.0 + 110
                && e.guardCooldown < 2.5;
        if (notWindingUp && (e.guardCooldown <= 0 || playerSwingingClose)) {
            e.guardTimer = e.mutated ? 1.5 : 2.0;
            e.guardTime = 0;
            return;
        }

        // ---- ยิง ----
        e.shootTimer -= dt;
        if (e.shootTimer <= 0) firePattern(world, e, cx, cy, px);
    }

    // =====================================================================
    //  ยกโล่
    // =====================================================================
    private static void endGuard(Enemy e) {
        e.guardCooldown = e.mutated ? 4.5 : 6.5;
        e.shootTimer = 0.8;                        // สวนกลับทันที
        e.patternIndex = 0;                        // ร่างกลายพันธุ์: ท่าแรกคือวงแหวนลูกไฟ (สวนกลับ)
    }

    // =====================================================================
    //  กลายพันธุ์
    // =====================================================================
    private static void beginMutation(GameWorld world, Enemy e, double cx, double cy) {
        e.mutating = true;
        e.mutationTimer = MUTATION_WARNING_TIME;
        e.guardTimer = 0;
        world.arrows.clear();            // ล้างลูกไฟเก่า ให้ผู้เล่นตั้งหลักได้
        world.telegraphs.clear();
        world.shake(5, 0.5);
        world.burst(cx, cy, 20, new Color(200, 120, 255));
    }

    private static void updateMutation(GameWorld world, Enemy e, double dt, double cx, double cy) {
        e.mutationTimer -= dt;
        double progress = 1 - e.mutationTimer / MUTATION_WARNING_TIME;     // 0 -> 1
        world.shake(3 + 6 * progress, 0.1);                                 // ยิ่งใกล้ ยิ่งสั่นแรง

        // ออร่าม่วงพุ่งเข้าหาตัวบอสมากขึ้นเรื่อยๆ
        if (world.rng.nextDouble() < 0.35 + 0.5 * progress) {
            double a = world.rng.nextDouble() * Math.PI * 2;
            double r = 80 + world.rng.nextDouble() * 60;
            world.burst(cx + Math.cos(a) * r, cy + Math.sin(a) * r, 1, new Color(190, 110, 255));
        }

        if (e.mutationTimer <= 0) finishMutation(world, e, cx, cy);
    }

    private static void finishMutation(GameWorld world, Enemy e, double cx, double cy) {
        e.mutating = false;
        e.mutated = true;
        e.phase = 1;
        e.growForMutation();
        e.shootTimer = 1.4;
        e.guardCooldown = 3.5;
        e.patternIndex = 0;

        world.burst(cx, cy, 45, new Color(210, 120, 255));
        world.burst(cx, cy, 20, new Color(255, 90, 200));
        world.shake(14, 0.8);
        world.banner.show("THE DEMON KING HAS MUTATED!", 2.8);

        // คลื่นกระแทกผลักผู้เล่นถอยหลัง (ไม่ทำดาเมจ)
        Player p = world.player;
        double dist = (p.x + p.w / 2.0) - cx;
        if (Math.abs(dist) < 300) {
            p.vx = (dist >= 0 ? 1 : -1) * 420;
            p.vy = -260;
            p.knockTimer = 0.25;
        }
    }

    // =====================================================================
    //  ท่าโจมตี
    // =====================================================================
    private static void firePattern(GameWorld world, Enemy e, double cx, double cy, double px) {
        int pattern = e.patternIndex++;
        e.shotCount++;

        if (!e.mutated) {
            switch (pattern % 3) {
                case 0:   // ลูกไฟเล็งตรง
                    world.shootFireball(cx, cy, e.aim, 380, 14);
                    e.shootTimer = 1.7;
                    break;
                case 1:   // 3 ทาง
                    for (int i = -1; i <= 1; i++) world.shootFireball(cx, cy, e.aim + i * 0.26, 360, 12);
                    e.shootTimer = 1.6;
                    break;
                default:  // ลูกไฟเร็ว
                    world.shootFireball(cx, cy, e.aim, 500, 16);
                    e.shootTimer = 1.4;
            }
            return;
        }

        switch (pattern % 4) {
            case 0: {     // วงแหวนลูกไฟรอบตัว 12 ลูก (หมุนเริ่มสุ่มทุกครั้ง)
                double start = world.rng.nextDouble() * Math.PI * 2;
                for (int k = 0; k < 12; k++) {
                    world.shootFireball(cx, cy, start + k * Math.PI * 2 / 12, 270, 12);
                }
                world.shake(4, 0.2);
                e.shootTimer = 1.5;
                break;
            }
            case 1:       // 5 ทางแบบกว้าง
                for (int i = -2; i <= 2; i++) world.shootFireball(cx, cy, e.aim + i * 0.3, 440, 14);
                e.shootTimer = 1.2;
                break;
            case 2: {     // ฝนไฟ: มีเสาแดงเตือนบนพื้น 1 วินาทีก่อนลูกไฟตก (กันด้วยโล่ไม่ได้ ต้องหลบ)
                for (int k = 0; k < 7; k++) {
                    double x = px + (k - 3) * 85 + world.rng.nextInt(41) - 20;
                    x = MathUtil.clamp(x, 30, world.level.width - 30);
                    world.telegraphs.add(new Telegraph(x, world.level.surfaceY(x), 1.0, 14));
                }
                e.shootTimer = 2.2;
                break;
            }
            default:      // พัด 7 ลูก
                for (int i = -3; i <= 3; i++) world.shootFireball(cx, cy, e.aim + i * 0.22, 400, 12);
                e.shootTimer = 1.2;
        }
    }
}
