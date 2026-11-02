package legacyknight.world;

import static legacyknight.core.Constants.MUTATION_WARNING_TIME;

import java.awt.Color;

import legacyknight.entity.Arrow;
import legacyknight.entity.Enemy;
import legacyknight.entity.EnemyState;
import legacyknight.entity.Player;
import legacyknight.entity.Telegraph;
import legacyknight.util.MathUtil;

/**
 * สมองของ "Dead" ยมทูตถือเคียว (บอสตัวสุดท้ายของด่าน MP4)
 *
 * ทุกท่าจะมี "ง้างเคียว" ให้เห็นก่อนปล่อย 0.6 วินาที (เฟรมแอนิเมชันโจมตี 3 เฟรมแรก)
 * Dead ไม่ยืนนิ่ง: ลอยเข้า-ออกรักษาระยะประมาณ 300 พิกเซลจากผู้เล่น (ไม่ออกนอกแท่นบัลลังก์)
 *
 * ร่างที่ 1 (เลือดมากกว่า 50%)  วนท่า:
 *   คลื่นเคียวเล็งตรง -> คลื่นเคียว 3 ทาง -> คลื่นเคียวแดงเร็ว 2 ลูก
 *
 * เลือดลดถึง 50%  ->  เตือนล่วงหน้า 3 วินาที (WARNING!) Dead อมตะ ตัวสั่น กระพริบสลับสี หยุดโจมตี
 *
 * ร่างที่ 2 (คลั่ง ตัวสีม่วง เร็วขึ้น)  วนท่า:
 *   วงแหวนคลื่นเคียว 10 ลูก -> พัด 5 ทาง -> ฝนวิญญาณ (มีเสาเตือนบนพื้น กันด้วยโล่ไม่ได้)
 *   -> วาร์ปหายตัวไปโผล่ข้างตัวผู้เล่นแล้วฟันเคียว (มีเสาเตือนบอกจุดที่จะโผล่) -> พัด 7 ทาง
 *
 * ทุกร่าง: ถ้าผู้เล่นเข้าใกล้เกินไป Dead จะ "ฟันเคียวระยะประชิด" (ดาเมจแรง แต่ง้างนาน ถอยหรือยกโล่ทัน)
 */
final class ReaperAI {

    /** เวลาง้างเคียวก่อนปล่อยท่า (ตัววาดฝั่ง ui ใช้เลข 0.6 เหมือนกัน) */
    static final double WINDUP = 0.6;
    /** เวลาเล่นเฟรมฟัน (เฟรม 3-7 ของแอนิเมชันโจมตี) */
    static final double SLASH_TIME = 0.4;

    private static final double MELEE_REACH = 170;
    private static final int MELEE_DAMAGE = 22;

    private ReaperAI() { }

    static void update(GameWorld world, Enemy e, double dt) {
        Player p = world.player;
        double px = p.x + p.w / 2.0;
        double py = p.y + p.h / 2.0;
        double cx = e.x + e.w / 2.0;
        double cy = e.y + e.h * 0.45;

        e.aim = Math.atan2(py - cy, px - cx);
        boolean hidden = e.state == EnemyState.REAPER_VANISH || e.state == EnemyState.REAPER_HIDDEN;
        if (!hidden) e.dir = px >= cx ? 1 : -1;
        e.vx = 0;

        // ---- กำลังเปลี่ยนร่าง: อมตะ หยุดโจมตี รอจนหมดเวลา ----
        if (e.mutating) {
            e.applyPhysics(dt, world.level);
            updateRage(world, e, dt, cx, cy);
            return;
        }

        // ---- ลำดับวาร์ป (หายตัว -> ซ่อน -> โผล่ฟัน) ----
        switch (e.state) {
            case EnemyState.REAPER_VANISH:
                e.applyPhysics(dt, world.level);
                e.stateTimer -= dt;
                if (e.stateTimer <= 0) {
                    e.state = EnemyState.REAPER_HIDDEN;
                    e.stateTimer = 0.6;
                }
                return;
            case EnemyState.REAPER_HIDDEN:
                e.applyPhysics(dt, world.level);
                e.stateTimer -= dt;
                if (e.stateTimer <= 0) reappear(world, e);
                return;
            case EnemyState.REAPER_APPEAR:
                e.applyPhysics(dt, world.level);
                e.stateTimer -= dt;
                e.shootTimer = e.stateTimer;                 // ให้ตัววาดเล่นท่าง้างเคียว
                if (e.stateTimer <= 0) {
                    e.state = EnemyState.REAPER_NORMAL;
                    e.shootTimer = 1.3;
                    slash(world, e, e.x + e.w / 2.0, e.y + e.h * 0.45);
                }
                return;
            default:
        }

        // ---- เลือดถึงครึ่ง: เริ่มคำเตือน ----
        if (!e.mutated && e.getHp() <= e.getMaxHp() * 0.5) {
            beginRage(world, e, cx, cy);
            return;
        }

        // ---- ผู้เล่นไกลเกินไปก็ไม่ทำอะไร ----
        if (Math.abs(px - cx) > 950) {
            e.applyPhysics(dt, world.level);
            return;
        }

        // ---- ผู้เล่นเข้าประชิด -> เปลี่ยนเป็นท่าฟันเคียว ----
        e.actionCooldown -= dt;
        boolean closeRange = Math.abs(px - cx) < 150 && Math.abs(py - cy) < 130;
        if (!e.meleePending && closeRange && e.actionCooldown <= 0 && e.shootTimer > WINDUP) {
            e.meleePending = true;
            e.shootTimer = WINDUP;
        }

        // ---- ลอยรักษาระยะ (หยุดตอนง้างเคียว) ----
        boolean winding = e.shootTimer < WINDUP + 0.05 || e.attackTimer > 0;
        if (!winding) {
            double want = e.mutated ? 250 : 300;
            double dist = px - cx;
            double speed = e.mutated ? 115 : 70;
            if (Math.abs(dist) < want - 40) e.vx = -Math.signum(dist) * speed;        // ถอย
            else if (Math.abs(dist) > want + 90) e.vx = Math.signum(dist) * speed;    // เข้าหา
        }
        e.applyPhysics(dt, world.level);
        double minCx = e.getHomeX() - 150, maxCx = e.getHomeX() + 190;       // ไม่ออกนอกแท่นบัลลังก์
        double ncx = MathUtil.clamp(e.x + e.w / 2.0, minCx, maxCx);
        e.x = ncx - e.w / 2.0;

        // ---- ปล่อยท่า ----
        e.shootTimer -= dt;
        if (e.shootTimer <= 0) {
            cx = e.x + e.w / 2.0;
            if (e.meleePending) {
                e.meleePending = false;
                e.actionCooldown = 2.4;
                e.shootTimer = 1.0;
                slash(world, e, cx, cy);
            } else {
                firePattern(world, e, cx, cy, px);
            }
        }
    }

    // =====================================================================
    //  ท่าฟันเคียวระยะประชิด (ใช้ทั้งตอนผู้เล่นเข้าใกล้ และตอนวาร์ปมาโผล่)
    // =====================================================================
    private static void slash(GameWorld world, Enemy e, double cx, double cy) {
        Player p = world.player;
        e.attackTimer = SLASH_TIME;
        e.meleePending = false;

        double zx = e.dir > 0 ? cx : cx - MELEE_REACH;
        if (MathUtil.overlaps(zx, e.y - 10, MELEE_REACH, e.h + 40, p.x, p.y, p.w, p.h)) {
            world.damagePlayer(MELEE_DAMAGE, cx, true);
        }
        // คลื่นเคียวพุ่งตามรอยฟันออกไปไกลกว่าเดิม
        world.shootScythe(cx + e.dir * 60, cy + 25, e.dir > 0 ? 0 : Math.PI, 430, 14, 1);
        world.burst(cx + e.dir * 80, cy, 10, new Color(255, 190, 90));
        world.shake(4, 0.25);
    }

    // =====================================================================
    //  เปลี่ยนร่าง (คลั่ง)
    // =====================================================================
    private static void beginRage(GameWorld world, Enemy e, double cx, double cy) {
        e.mutating = true;
        e.mutationTimer = MUTATION_WARNING_TIME;
        e.meleePending = false;
        world.arrows.clear();            // ล้างคลื่นเก่า ให้ผู้เล่นตั้งหลักได้
        world.telegraphs.clear();
        world.shake(5, 0.5);
        world.burst(cx, cy, 20, new Color(200, 120, 255));
    }

    private static void updateRage(GameWorld world, Enemy e, double dt, double cx, double cy) {
        e.mutationTimer -= dt;
        double progress = 1 - e.mutationTimer / MUTATION_WARNING_TIME;      // 0 -> 1
        world.shake(3 + 6 * progress, 0.1);

        // วิญญาณม่วง-แดงพุ่งเข้าหาตัว Dead มากขึ้นเรื่อยๆ
        if (world.rng.nextDouble() < 0.35 + 0.5 * progress) {
            double a = world.rng.nextDouble() * Math.PI * 2;
            double r = 90 + world.rng.nextDouble() * 70;
            world.burst(cx + Math.cos(a) * r, cy + Math.sin(a) * r, 1,
                    world.rng.nextBoolean() ? new Color(190, 110, 255) : new Color(255, 80, 90));
        }
        if (e.mutationTimer <= 0) finishRage(world, e, cx, cy);
    }

    private static void finishRage(GameWorld world, Enemy e, double cx, double cy) {
        e.mutating = false;
        e.mutated = true;
        e.phase = 1;
        e.shootTimer = 1.4;
        e.actionCooldown = 1.0;
        e.patternIndex = 0;

        world.burst(cx, cy, 45, new Color(210, 120, 255));
        world.burst(cx, cy, 20, new Color(255, 80, 90));
        world.shake(14, 0.8);
        world.banner.show("DEAD HAS AWAKENED!", 2.8);

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
    //  วาร์ป
    // =====================================================================
    private static void startTeleport(GameWorld world, Enemy e, double cx, double cy, double px) {
        // เลือกด้านซ้ายหรือขวาของผู้เล่น ห่างประมาณ 130 พิกเซล (อยู่ในแท่นบัลลังก์เท่านั้น)
        double minCx = e.getHomeX() - 150, maxCx = e.getHomeX() + 190;
        double side = world.rng.nextBoolean() ? 1 : -1;
        double dest = MathUtil.clamp(px + side * 130, minCx, maxCx);
        if (Math.abs(dest - px) < 90) dest = MathUtil.clamp(px - side * 130, minCx, maxCx);

        e.targetX = dest;
        e.state = EnemyState.REAPER_VANISH;
        e.stateTimer = 0.4;
        e.shootTimer = 1.0;
        // เสาเตือนบอกจุดที่จะโผล่ (1 วินาที = หายตัว 0.4 + ซ่อน 0.6)
        world.telegraphs.add(new Telegraph(dest, world.level.surfaceY(dest), 1.0, 0, false, Arrow.SCYTHE));
        world.burst(cx, cy, 24, new Color(190, 110, 255));
    }

    private static void reappear(GameWorld world, Enemy e) {
        e.x = e.targetX - e.w / 2.0;
        e.state = EnemyState.REAPER_APPEAR;
        e.stateTimer = 0.55;
        e.shootTimer = e.stateTimer;
        Player p = world.player;
        e.dir = (p.x + p.w / 2.0) >= e.targetX ? 1 : -1;
        world.burst(e.targetX, e.y + e.h * 0.5, 24, new Color(190, 110, 255));
        world.shake(3, 0.2);
    }

    // =====================================================================
    //  ท่ายิงคลื่นเคียว
    // =====================================================================
    private static void firePattern(GameWorld world, Enemy e, double cx, double cy, double px) {
        int pattern = e.patternIndex++;
        e.shotCount++;
        double ox = cx + e.dir * 40;                     // ปลายเคียวอยู่ด้านหน้าตัว
        e.attackTimer = SLASH_TIME;

        if (!e.mutated) {
            switch (pattern % 3) {
                case 0:   // คลื่นเล็งตรง
                    world.shootScythe(ox, cy, e.aim, 400, 14, 0);
                    e.shootTimer = 1.8;
                    break;
                case 1:   // 3 ทาง
                    for (int i = -1; i <= 1; i++) world.shootScythe(ox, cy, e.aim + i * 0.26, 380, 12, 0);
                    e.shootTimer = 1.7;
                    break;
                default:  // คู่แดงเร็ว
                    world.shootScythe(ox, cy, e.aim - 0.1, 520, 15, 1);
                    world.shootScythe(ox, cy, e.aim + 0.1, 520, 15, 1);
                    e.shootTimer = 1.5;
            }
            return;
        }

        switch (pattern % 5) {
            case 0: {     // วงแหวนคลื่นเคียว 10 ลูก (เริ่มหมุนสุ่มทุกครั้ง)
                double start = world.rng.nextDouble() * Math.PI * 2;
                for (int k = 0; k < 10; k++) {
                    world.shootScythe(cx, cy, start + k * Math.PI * 2 / 10, 250, 12, 1);
                }
                world.shake(4, 0.2);
                e.shootTimer = 1.5;
                break;
            }
            case 1:       // พัด 5 ทางกว้าง
                for (int i = -2; i <= 2; i++) world.shootScythe(ox, cy, e.aim + i * 0.3, 430, 13, 1);
                e.shootTimer = 1.3;
                break;
            case 2: {     // ฝนวิญญาณ: เสาเตือน 1 วินาทีก่อนคลื่นตกลงมา (กันด้วยโล่ไม่ได้ ต้องหลบ)
                for (int k = 0; k < 7; k++) {
                    double x = px + (k - 3) * 85 + world.rng.nextInt(41) - 20;
                    x = MathUtil.clamp(x, 30, world.level.width - 30);
                    world.telegraphs.add(new Telegraph(x, world.level.surfaceY(x), 1.0, 15, true, Arrow.SCYTHE));
                }
                e.shootTimer = 2.3;
                break;
            }
            case 3:       // วาร์ปไปโผล่ข้างผู้เล่นแล้วฟัน
                e.attackTimer = 0;
                startTeleport(world, e, cx, cy, px);
                break;
            default:      // พัด 7 ทาง
                for (int i = -3; i <= 3; i++) world.shootScythe(ox, cy, e.aim + i * 0.22, 400, 12, 1);
                e.shootTimer = 1.3;
        }
    }
}
