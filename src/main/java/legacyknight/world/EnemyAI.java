package legacyknight.world;

import static legacyknight.core.Constants.SCREEN_H;

import legacyknight.entity.Enemy;
import legacyknight.entity.EnemyState;
import legacyknight.entity.Player;
import legacyknight.util.MathUtil;

/**
 * "สมองของศัตรู"
 *
 * มอนปกติ 4 แบบ (ทุกแบบเดินเล่นแถวจุดเกิด แล้วทำท่าของตัวเองเมื่อเห็นผู้เล่น):
 *   Hopper  (สไลม์เขียว)    ย่อตัวแล้วกระโดดพุ่งหาผู้เล่น ข้ามหลุมเล็กๆ ได้
 *   Spitter (สไลม์น้ำเงิน)  ถอยห่างจากผู้เล่น แล้วถ่มก้อนเมือกจากระยะไกล (ยกโล่กันได้)
 *   Brute   (สไลม์แดง)      ตัวใหญ่ อึด ตั้งท่ากะพริบเตือนก่อนแล้วพุ่งชนแรงๆ จากนั้นเหนื่อยพัก
 *   Ramses  (เต่าหนาม)      เกราะแข็งรับดาเมจครึ่งเดียว เห็นผู้เล่นแล้วพุ่งใส่ พุ่งเสร็จเหนื่อยเกราะเปิด
 *
 * มินิบอส Demon King ใช้สมองที่ DemonKingAI ส่วนบอส Dead ใช้ ReaperAI
 */
final class EnemyAI {

    /** เวลาที่ปีศาจ "ง้างปาก" ก่อนยิง (ใช้เล่นแอนิเมชันโจมตี ภาพฝั่ง ui อ่านค่านี้ด้วย) */
    static final double WINDUP = 0.6;

    private EnemyAI() { }

    static void update(GameWorld world, Enemy e, double dt) {
        switch (e.type) {
            case HOPPER:
                updateHopper(world, e, dt);
                break;
            case SPITTER:
                updateSpitter(world, e, dt);
                break;
            case BRUTE:
                updateBrute(world, e, dt);
                break;
            case RAMSES:
                updateRamses(world, e, dt);
                break;
            case MINI_BOSS:
                DemonKingAI.update(world, e, dt);
                break;
            default:
                ReaperAI.update(world, e, dt);
        }
    }

    // =====================================================================
    //  ตัวช่วยที่มอนทุกแบบใช้ร่วมกัน
    // =====================================================================

    private static double centerX(Enemy e) {
        return e.x + e.w / 2.0;
    }

    /** ผู้เล่นอยู่ในระยะแนวนอน range และอยู่ระดับความสูงใกล้กัน (ต่างกันไม่เกิน dyMax) หรือไม่ */
    private static boolean sees(Player p, Enemy e, double range, double dyMax) {
        double dx = (p.x + p.w / 2.0) - centerX(e);
        return Math.abs(dx) < range && Math.abs((p.y + p.h) - (e.y + e.h)) < dyMax;
    }

    /** เดินเล่น: เดินไปไม่เกิน 140 พิกเซลจากจุดเกิดแล้วเดินกลับ (ปรับ e.dir ให้) */
    private static void wander(GameWorld world, Enemy e) {
        double cx = centerX(e);
        boolean walkingAwayFromHome = (cx > e.getHomeX()) == (e.dir > 0);
        if (Math.abs(cx - e.getHomeX()) > 140 && walkingAwayFromHome) e.dir = -e.dir;
        if (cliffAhead(world, e, e.dir)) e.dir = -e.dir;
    }

    /** ข้างหน้า (ทิศ dir) เป็นหน้าผาไหม */
    private static boolean cliffAhead(GameWorld world, Enemy e, int dir) {
        double aheadX = dir > 0 ? e.x + e.w + 4 : e.x - 4;
        return e.onGround && !world.level.solidAt(aheadX, e.y + e.h + 6);
    }

    /** ชนตัวผู้เล่น -> ทำดาเมจ (มีช่วงพักกันโดนรัวทุกเฟรม) */
    private static void contact(GameWorld world, Enemy e, int damage) {
        Player p = world.player;
        if (e.stun <= 0 && e.contactCooldown <= 0
                && MathUtil.overlaps(e.x, e.y, e.w, e.h, p.x, p.y, p.w, p.h)) {
            world.damagePlayer(damage, centerX(e), true);
            e.contactCooldown = 0.6;
        }
    }

    /** ตกหลุมแล้วหายไป / ชนกำแพงแล้วเดินกลับหลัง (ใช้หลัง applyPhysics) */
    private static void afterPhysics(Enemy e) {
        if (e.hitWall && e.stun <= 0 && e.state != EnemyState.HOP_AIR) e.dir = -e.dir;
        if (e.y > SCREEN_H + 100) e.die();
    }

    // =====================================================================
    //  Hopper: สไลม์เขียวกระโดด
    // =====================================================================
    private static final double HOP_SPEED = 215;
    private static final double HOP_POWER = -520;
    /** ระยะที่กระโดดหนึ่งครั้งพาไปได้ (ใช้เช็กว่าจุดลงมีพื้นรองรับไหม) */
    private static final double HOP_DISTANCE = HOP_SPEED * (2 * -HOP_POWER / 1800.0);

    private static void updateHopper(GameWorld world, Enemy e, double dt) {
        Player p = world.player;
        double pcx = p.x + p.w / 2.0;
        double cx = centerX(e);
        e.actionCooldown -= dt;

        // ลงพื้นแล้ว: หยุดไถล เล่นท่าลงพื้นสั้นๆ แล้วพักก่อนกระโดดรอบถัดไป
        if (e.state == EnemyState.HOP_AIR && e.onGround && e.vy >= 0) {
            e.state = EnemyState.HOP_GROUND;
            e.vx = 0;
            e.landTimer = 0.3;
            e.actionCooldown = 0.5 + world.rng.nextDouble() * 0.5;
        }

        if (e.stun > 0) {
            e.vx *= 0.94;                                   // โดนตีอยู่ -> ไถลตามแรงกระแทก
            if (e.state == EnemyState.HOP_WINDUP) e.state = EnemyState.HOP_GROUND;
        } else if (e.state == EnemyState.HOP_AIR) {
            // กำลังลอยอยู่: ปล่อยตามแรงที่กระโดดมา
        } else if (e.state == EnemyState.HOP_WINDUP) {
            e.vx = 0;
            e.stateTimer -= dt;
            if (e.stateTimer <= 0) {                        // ดีดตัว!
                e.vx = e.dir * HOP_SPEED;
                e.vy = HOP_POWER;
                e.state = EnemyState.HOP_AIR;
            }
        } else if (sees(p, e, 340, 120)) {
            e.dir = pcx > cx ? 1 : -1;
            boolean landingIsSafe = world.level.solidAt(cx + e.dir * HOP_DISTANCE, e.y + e.h + 6);
            if (e.onGround && e.landTimer <= 0 && e.actionCooldown <= 0 && landingIsSafe) {
                e.state = EnemyState.HOP_WINDUP;            // ย่อตัวสะสมแรง
                e.stateTimer = 0.28;
                e.vx = 0;
            } else if (!landingIsSafe && !cliffAhead(world, e, e.dir)) {
                e.vx = e.dir * 50;                          // จุดลงไม่มีพื้น: เดินเข้าไปใกล้ก่อน
            } else {
                e.vx = 0;
            }
        } else {
            wander(world, e);
            e.vx = e.dir * 40;
        }

        e.applyPhysics(dt, world.level);
        afterPhysics(e);
        contact(world, e, 10);
    }

    // =====================================================================
    //  Spitter: สไลม์น้ำเงินถ่มเมือก
    // =====================================================================
    private static void updateSpitter(GameWorld world, Enemy e, double dt) {
        Player p = world.player;
        double pcx = p.x + p.w / 2.0;
        double pcy = p.y + p.h / 2.0;
        double cx = centerX(e);
        double dx = pcx - cx;
        e.actionCooldown -= dt;

        if (e.stun > 0) {
            e.vx *= 0.94;
            if (e.state == EnemyState.SPIT_WINDUP) {        // โดนตีกลางท่า -> ถ่มพลาด
                e.state = EnemyState.SPIT_IDLE;
                e.actionCooldown = Math.max(e.actionCooldown, 0.8);
            }
        } else if (e.state == EnemyState.SPIT_WINDUP) {
            e.vx = 0;
            e.dir = dx >= 0 ? 1 : -1;
            e.stateTimer -= dt;
            if (e.stateTimer <= 0) {
                double sx = cx + e.dir * 14;
                double sy = e.y + e.h * 0.45;
                world.shootSpit(sx, sy, Math.atan2(pcy - sy, pcx - sx), 250, 9);
                e.state = EnemyState.SPIT_IDLE;
                e.actionCooldown = 2.0 + world.rng.nextDouble() * 0.6;
                e.landTimer = 0.25;                         // แรงถีบหลังถ่ม
            }
        } else if (sees(p, e, 480, 140)) {
            e.dir = dx >= 0 ? 1 : -1;                       // หันหน้าหาผู้เล่นเสมอ
            boolean tooClose = Math.abs(dx) < 190;
            boolean canBackOff = !e.hitWall && !cliffAhead(world, e, -e.dir);
            if (tooClose && canBackOff) {
                e.vx = -e.dir * 75;                         // ถอยหลังรักษาระยะ
            } else {
                e.vx = 0;
                if (e.actionCooldown <= 0 && e.onGround) {  // ถอยไม่ได้แล้ว/ระยะพอดี -> เล็งถ่ม
                    e.state = EnemyState.SPIT_WINDUP;
                    e.stateTimer = 0.45;
                }
            }
        } else {
            wander(world, e);
            e.vx = e.dir * 40;
        }

        e.applyPhysics(dt, world.level);
        afterPhysics(e);
        contact(world, e, 6);
    }

    // =====================================================================
    //  Brute: สไลม์แดงตัวใหญ่พุ่งชน
    // =====================================================================
    private static final double BRUTE_DASH_SPEED = 360;

    private static void updateBrute(GameWorld world, Enemy e, double dt) {
        Player p = world.player;
        double pcx = p.x + p.w / 2.0;
        double cx = centerX(e);
        e.actionCooldown -= dt;

        if (e.stun > 0) {
            e.vx *= 0.9;
            if (e.state == EnemyState.BRUTE_WINDUP || e.state == EnemyState.BRUTE_DASH) {
                e.state = EnemyState.BRUTE_NORMAL;          // โดนตีสะดุด -> ท่าพังพักก่อน
                e.actionCooldown = 1.0;
            }
        } else {
            switch (e.state) {
                case EnemyState.BRUTE_WINDUP:
                    e.vx = 0;
                    e.stateTimer -= dt;
                    if (e.stateTimer > 0.2) e.dir = pcx > cx ? 1 : -1;     // เล็งไว้ก่อน 0.2 วิสุดท้ายล็อกทิศ
                    if (e.stateTimer <= 0) {
                        e.state = EnemyState.BRUTE_DASH;
                        e.stateTimer = 0.7;
                    }
                    break;
                case EnemyState.BRUTE_DASH:
                    e.stateTimer -= dt;
                    e.vx = e.dir * BRUTE_DASH_SPEED;
                    if (cliffAhead(world, e, e.dir) || e.stateTimer <= 0) endDash(e);
                    break;
                case EnemyState.BRUTE_RECOVER:
                    e.vx = 0;
                    e.stateTimer -= dt;
                    if (e.stateTimer <= 0) e.state = EnemyState.BRUTE_NORMAL;
                    break;
                default: {
                    boolean chase = sees(p, e, 300, 90);
                    if (chase) {
                        e.dir = pcx > cx ? 1 : -1;
                        e.vx = cliffAhead(world, e, e.dir) ? 0 : e.dir * 55;
                        if (Math.abs(pcx - cx) < 240 && e.actionCooldown <= 0 && e.onGround) {
                            e.state = EnemyState.BRUTE_WINDUP;     // ตั้งท่า: ตัวกะพริบ + เครื่องหมายตกใจ
                            e.stateTimer = 0.65;
                            e.vx = 0;
                        }
                    } else {
                        wander(world, e);
                        e.vx = e.dir * 35;
                    }
                }
            }
        }

        e.applyPhysics(dt, world.level);
        if (e.state == EnemyState.BRUTE_DASH && e.hitWall) endDash(e);     // ชนกำแพง -> หน้ามืดพักทันที
        afterPhysics(e);
        contact(world, e, e.state == EnemyState.BRUTE_DASH ? 22 : 14);
    }

    private static void endDash(Enemy e) {
        e.state = EnemyState.BRUTE_RECOVER;
        e.stateTimer = 0.9;
        e.actionCooldown = 1.5;
        e.vx = 0;
    }

    // =====================================================================
    //  Ramses: เต่าหนามคริสตัล
    // =====================================================================
    private static final double RAMSES_RUSH_SPEED = 160;

    private static void updateRamses(GameWorld world, Enemy e, double dt) {
        Player p = world.player;
        double pcx = p.x + p.w / 2.0;
        double cx = centerX(e);
        e.actionCooldown -= dt;

        if (e.stun > 0) {
            e.vx *= 0.9;
        } else {
            switch (e.state) {
                case EnemyState.RAMSES_ALERT:                   // ท่า Track: หันมาจ้องผู้เล่น
                    e.vx = 0;
                    e.dir = pcx > cx ? 1 : -1;
                    e.stateTimer -= dt;
                    if (e.stateTimer <= 0) {
                        e.state = EnemyState.RAMSES_RUSH;
                        e.stateTimer = 1.8;
                    }
                    break;
                case EnemyState.RAMSES_RUSH:
                    e.stateTimer -= dt;
                    e.vx = e.dir * RAMSES_RUSH_SPEED;           // พุ่งตรงไปทางที่จ้องไว้
                    if (cliffAhead(world, e, e.dir) || e.stateTimer <= 0) tire(e);
                    break;
                case EnemyState.RAMSES_TIRED:
                    e.vx = 0;
                    e.stateTimer -= dt;
                    if (e.stateTimer <= 0) {
                        e.state = EnemyState.RAMSES_PATROL;
                        e.actionCooldown = 1.2;
                    }
                    break;
                default:
                    wander(world, e);
                    e.vx = e.dir * 38;
                    if (sees(p, e, 320, 90) && e.actionCooldown <= 0 && e.onGround) {
                        e.state = EnemyState.RAMSES_ALERT;
                        e.stateTimer = 0.7;
                        e.vx = 0;
                        e.dir = pcx > cx ? 1 : -1;
                    }
            }
        }

        e.applyPhysics(dt, world.level);
        if (e.state == EnemyState.RAMSES_RUSH && e.hitWall) tire(e);       // ชนกำแพง -> เหนื่อย
        afterPhysics(e);
        contact(world, e, 14);
    }

    private static void tire(Enemy e) {
        e.state = EnemyState.RAMSES_TIRED;
        e.stateTimer = 1.4;                                      // ช่วงที่เกราะเปิด รับดาเมจเต็ม
        e.vx = 0;
    }
}
