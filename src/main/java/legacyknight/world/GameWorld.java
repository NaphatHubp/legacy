package legacyknight.world;

import static legacyknight.core.Constants.BLOCK_DAMAGE_MULTIPLIER;
import static legacyknight.core.Constants.BOSS_GUARD_DAMAGE_MULTIPLIER;
import static legacyknight.core.Constants.GROUND_Y;
import static legacyknight.core.Constants.PLAYER_W;
import static legacyknight.core.Constants.RAMSES_ARMOR_MULTIPLIER;
import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.SCREEN_W;
import static legacyknight.core.Constants.SLIME_HEAL;
import static legacyknight.core.Constants.STAND_H;

import java.awt.Color;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import legacyknight.core.InputHandler;
import legacyknight.core.PlayerStats;
import legacyknight.entity.Arrow;
import legacyknight.entity.Enemy;
import legacyknight.entity.EnemyState;
import legacyknight.entity.EnemyType;
import legacyknight.entity.Particle;
import legacyknight.entity.Player;
import legacyknight.entity.Telegraph;
import legacyknight.level.Level;
import legacyknight.level.Spawn;
import legacyknight.util.MathUtil;

/**
 * "โลกของด่านที่กำลังเล่นอยู่" รวมทุกอย่างที่อยู่ในด่านไว้ด้วยกัน:
 * ผู้เล่น ศัตรู ลูกไฟ เอฟเฟกต์ กุญแจ กล้อง ฯลฯ
 *
 * สร้างใหม่ทุกครั้งที่เริ่มด่าน (เลยทำให้ "เริ่มด่านใหม่" ง่ายมาก)
 * ส่วนพฤติกรรมของผู้เล่นและศัตรูแยกไว้ที่ PlayerController, EnemyAI และ BossAI
 */
public class GameWorld {

    /** ผลของด่านตอนนี้ */
    public enum Status { PLAYING, WON, LOST }

    public final Level level;
    public final PlayerStats stats;
    public final Player player = new Player();
    public final List<Enemy> enemies = new ArrayList<Enemy>();
    public final List<Arrow> arrows = new ArrayList<Arrow>();
    public final List<Telegraph> telegraphs = new ArrayList<Telegraph>();
    public final List<Particle> particles = new ArrayList<Particle>();
    public final Banner banner = new Banner();
    public final Random rng = new Random();

    public boolean hasKey;
    /** ตำแหน่งซ้ายสุดของภาพที่กล้องมองเห็น (กล้องเลื่อนตามผู้เล่น) */
    public double cameraX;
    public Status status = Status.PLAYING;

    /** จอสั่น: เวลาที่เหลือ และความแรง (พิกเซล) */
    public double shakeTime;
    public double shakeMagnitude;

    public GameWorld(Level level, PlayerStats stats) {
        this.level = level;
        this.stats = stats;

        // วางผู้เล่นที่จุดเริ่มต้น เลือดเต็ม
        player.w = PLAYER_W;
        player.h = STAND_H;
        player.x = 60;
        player.y = GROUND_Y - STAND_H;
        player.facing = 1;
        player.hp = stats.maxHp;

        // สร้างศัตรูตามจุดเกิดของด่าน
        for (Spawn s : level.spawns) {
            enemies.add(new Enemy(s));
        }

        banner.show(level.code + " - " + level.name, 2.5);
    }

    // =====================================================================
    //  อัปเดตทุกเฟรม
    // =====================================================================

    /** อัปเดตเฉพาะเอฟเฟกต์ (ข้อความ จุดสี จอสั่น แอนิเมชันตาย) ซึ่งทำงานต่อแม้ด่านจบแล้ว */
    public void updateEffects(double dt) {
        banner.update(dt);
        if (shakeTime > 0) shakeTime -= dt;
        if (status == Status.LOST) player.deadTime += dt;
        for (Enemy e : enemies) {
            if (!e.alive) e.deathTime += dt;
        }
        for (Iterator<Particle> it = particles.iterator(); it.hasNext(); ) {
            Particle q = it.next();
            q.life -= dt;
            if (q.life <= 0) { it.remove(); continue; }
            q.vy += 700 * dt;     // จุดสีโดนแรงโน้มถ่วงด้วย
            q.x += q.vx * dt;
            q.y += q.vy * dt;
        }
    }

    /** อัปเดตสิ่งที่เกิดในด่าน เรียกทุกเฟรมตอนสถานะ PLAY */
    public void update(double dt, InputHandler input) {
        PlayerController.update(this, input, dt);

        for (Enemy e : enemies) {
            if (!e.alive) continue;
            e.animTime += dt;
            e.hurtTimer = Math.max(0, e.hurtTimer - dt);
            e.contactCooldown -= dt;
            e.stun -= dt;
            e.landTimer -= dt;
            e.attackTimer -= dt;
            EnemyAI.update(this, e, dt);
        }

        updateTelegraphs(dt);
        updateArrows(dt);
        checkHazardsAndGoals();

        // กล้องตามผู้เล่น แต่ไม่เลื่อนเลยขอบด่าน
        cameraX = MathUtil.clamp(player.x + player.w / 2.0 - SCREEN_W / 2.0, 0, level.width - SCREEN_W);
    }

    /** จุดเตือนบนพื้นนับถอยหลังจนหมดเวลา แล้วปล่อยลูกไฟตกลงมา */
    private void updateTelegraphs(double dt) {
        for (Iterator<Telegraph> it = telegraphs.iterator(); it.hasNext(); ) {
            Telegraph t = it.next();
            t.timer -= dt;
            if (t.timer <= 0) {
                if (t.dropsProjectile) {
                    if (t.style == Arrow.SCYTHE) {
                        arrows.add(new Arrow(t.x, -40, 0, 560, t.damage, false, Arrow.SCYTHE, 2, 16));
                    } else {
                        arrows.add(new Arrow(t.x, -40, 0, 560, t.damage, false));
                    }
                }
                it.remove();
            }
        }
    }

    private void updateArrows(double dt) {
        for (Iterator<Arrow> it = arrows.iterator(); it.hasNext(); ) {
            Arrow a = it.next();
            a.x += a.vx * dt;
            a.y += a.vy * dt;

            // บินออกนอกด่านไปไกลแล้ว -> ลบทิ้ง
            if (a.x < -80 || a.x > level.width + 80 || a.y > SCREEN_H + 100 || a.y < -200) {
                it.remove();
                continue;
            }
            // ชนกำแพง/พื้น
            if (a.y > 0 && level.solidAt(a.x, a.y)) {
                burst(a.x, a.y, 6, impactColor(a));
                it.remove();
                continue;
            }
            // โดนผู้เล่น (ลูกไฟมีขนาด เลยเช็กเป็นกล่องเล็กๆ รอบจุดศูนย์กลาง)
            if (MathUtil.overlaps(a.x - a.radius, a.y - a.radius, a.radius * 2, a.radius * 2,
                    player.x, player.y, player.w, player.h)) {
                damagePlayer(a.damage, a.x - a.vx * 0.5, a.blockable);
                it.remove();
            }
        }
    }

    /** เช็กหนาม / เก็บกุญแจ / เดินเข้าประตู */
    private void checkHazardsAndGoals() {
        if (status != Status.PLAYING) return;

        for (Rectangle s : level.spikes) {
            if (MathUtil.overlaps(player.x + 4, player.y + player.h - 10, player.w - 8, 10, s)) {
                damagePlayer(15, s.x + s.width / 2.0, false);
            }
        }

        if (!hasKey && MathUtil.overlaps(player.x, player.y, player.w, player.h,
                level.key.x - 12, level.key.y - 12, 24, 24)) {
            hasKey = true;
            banner.show("You got the key!", 1.8);
            burst(level.key.x, level.key.y, 14, new Color(255, 220, 80));
        }

        if (status == Status.PLAYING && MathUtil.overlaps(player.x, player.y, player.w, player.h, level.door)) {
            if (hasKey && aliveCount() == 0) {
                status = Status.WON;
            } else if (!banner.isActive()) {
                // ประตูยังเปิดไม่ได้ -> บอกผู้เล่นว่าขาดอะไร
                String msg;
                if (!hasKey && aliveCount() > 0) msg = "Defeat all enemies and get the key!";
                else if (!hasKey) msg = "The door needs the key!";
                else msg = "Defeat all enemies first!";
                banner.show(msg, 1.5);
            }
        }
    }

    // =====================================================================
    //  การโดนดาเมจ / ให้ดาเมจ
    // =====================================================================

    /**
     * ผู้เล่นโดนโจมตี
     *
     * @param fromX     ตำแหน่ง x ที่การโจมตีมาจาก (ใช้ดูว่ามาจากด้านหน้าหรือหลัง และผลักไปทางไหน)
     * @param blockable โล่กันได้หรือไม่ (หนามและฝนไฟสีม่วงกันไม่ได้)
     */
    public void damagePlayer(int damage, double fromX, boolean blockable) {
        Player p = player;
        if (p.invuln > 0 || status != Status.PLAYING) return;

        double dx = fromX - (p.x + p.w / 2.0);
        boolean attackFromFront = (dx >= 0) == (p.facing > 0);

        if (blockable && p.blocking && attackFromFront) {
            // ยกโล่กันได้ -> เจ็บน้อยลงมาก ไม่โดนกระเด็น และโล่เล่นแอนิเมชันสะท้อน
            damage = Math.max(1, Math.round((float) (damage * BLOCK_DAMAGE_MULTIPLIER)));
            p.invuln = 0.25;
            p.blockHitTimer = 0.3;
            burst(p.x + p.w / 2.0 + p.facing * 22, p.y + p.h * 0.45, 8, new Color(140, 190, 255));
        } else {
            // โดนเต็มๆ -> กระเด็นถอยหลัง และอมตะชั่วคราว
            p.invuln = 0.8;
            p.knockTimer = 0.2;
            p.vx = (dx > 0 ? -1 : 1) * 240;
            p.vy = -260;
            burst(p.x + p.w / 2.0, p.y + p.h / 2.0, 10, new Color(255, 110, 150));
        }

        p.hp -= damage;
        if (p.hp <= 0) {
            p.hp = 0;
            status = Status.LOST;
        }
    }

    /**
     * ศัตรูโดนดาบ
     *
     * @param dir ทิศที่ฟัน (1 = ขวา, -1 = ซ้าย) ใช้ผลักสไลม์ให้กระเด็น
     */
    public void hurtEnemy(Enemy e, int damage, int dir) {
        double cx = e.x + e.w / 2.0;
        double cy = e.y + e.h / 2.0;

        // Dead กำลังหายตัว = ไม่มีตัวให้ฟัน
        if (e.untouchable() && !e.mutating) return;
        // บอสกำลังเปลี่ยนร่าง = อมตะ ดาบเด้งออก
        if (e.mutating) {
            burst(cx, cy, 6, new Color(230, 200, 255));
            return;
        }

        if (e.guardTimer > 0) {
            // Demon King กำลังยกโล่ = ดาเมจเหลือน้อยมาก และโล่กระพริบ
            damage = Math.max(1, Math.round((float) (damage * BOSS_GUARD_DAMAGE_MULTIPLIER)));
            e.guardHitTimer = 0.3;
            burst(cx - e.dir * e.w * 0.5, cy, 8, new Color(140, 190, 255));
        } else if (e.type == EnemyType.RAMSES && e.state != EnemyState.RAMSES_TIRED) {
            // Ramses ตอนมีเกราะคริสตัล = ดาเมจลดครึ่ง (ตอนเหนื่อยหลังพุ่งเกราะเปิด รับเต็ม)
            damage = Math.max(1, Math.round((float) (damage * RAMSES_ARMOR_MULTIPLIER)));
            e.hurtTimer = 0.08;
            burst(cx + dir * -e.w * 0.3, cy, 8, new Color(150, 230, 255));
        } else {
            e.hurtTimer = 0.15;
            burst(cx, cy, 8, new Color(255, 240, 150));
        }
        e.hp -= damage;

        // สไลม์/Ramses โดนตีแล้วกระเด็นและมึนงง (ตัวหนักกระเด็นน้อยกว่า)
        double knock = e.type.knockback();
        if (knock > 0) {
            e.vx = dir * 280 * knock;
            e.vy = -200 * knock;
            e.stun = e.type.isSlime() || e.type == EnemyType.RAMSES ? Math.max(0.1, 0.25 * knock) : 0;
        }

        if (e.hp <= 0) {
            e.alive = false;
            e.deathTime = 0;
            e.meleePending = false;
            burst(cx, cy, 22, deathColor(e));
            if (e.type.isSlime()) {
                player.hp = Math.min(stats.maxHp, player.hp + SLIME_HEAL);   // ฆ่าสไลม์ได้เลือดคืน
            } else if (e.type.isBoss()) {
                arrows.clear();            // บอสตาย ลูกยิงหายหมด
                telegraphs.clear();
                shake(8, 0.5);
            }
            gainExp(e.type.expReward);
        }
    }

    /** สีของเศษที่ระเบิดออกตอนศัตรูตาย */
    private static Color deathColor(Enemy e) {
        switch (e.type) {
            case HOPPER:  return new Color(130, 220, 90);
            case SPITTER: return new Color(110, 180, 255);
            case BRUTE:   return new Color(240, 90, 70);
            case RAMSES:  return new Color(90, 190, 230);
            case BOSS:    return new Color(255, 190, 90);
            default:      return new Color(255, 90, 90);
        }
    }

    /** สีของประกายตอนลูกยิงชนกำแพง */
    private static Color impactColor(Arrow a) {
        if (a.style == Arrow.SPIT) return new Color(120, 190, 255);
        if (a.style == Arrow.SCYTHE) return a.blockable ? new Color(255, 170, 70) : new Color(210, 70, 120);
        return a.blockable ? new Color(255, 170, 60) : new Color(200, 90, 255);
    }

    private void gainExp(int amount) {
        if (stats.addExp(amount)) {
            player.hp = stats.maxHp;     // เลเวลอัป = เลือดเต็ม
            banner.show("LEVEL UP!  Lv " + stats.level + "   (+HP  +ATK)", 2.5);
        }
    }

    // =====================================================================
    //  ตัวช่วย
    // =====================================================================

    /** ศัตรูที่ยังมีชีวิตอยู่กี่ตัว */
    public int aliveCount() {
        int n = 0;
        for (Enemy e : enemies) {
            if (e.alive) n++;
        }
        return n;
    }

    /** ทำให้จอสั่น magnitude พิกเซล นาน seconds วินาที (ถ้ากำลังสั่นแรงกว่าอยู่แล้วจะไม่ลดลง) */
    public void shake(double magnitude, double seconds) {
        if (magnitude >= shakeMagnitude || shakeTime <= 0) shakeMagnitude = magnitude;
        shakeTime = Math.max(shakeTime, seconds);
    }

    /** ยิงลูกไฟ 1 ลูกจาก (x, y) ไปทางมุม angle (เรเดียน) */
    public void shootFireball(double x, double y, double angle, double speed, int damage) {
        arrows.add(new Arrow(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, damage, true));
    }

    /** ถ่มก้อนเมือก 1 ก้อน (สไลม์น้ำเงิน) ยกโล่กันได้ */
    public void shootSpit(double x, double y, double angle, double speed, int damage) {
        arrows.add(new Arrow(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, damage, true,
                Arrow.SPIT, 0, 9));
    }

    /**
     * ปล่อยคลื่นเคียว 1 ลูกของ Dead ไปทางมุม angle
     *
     * @param frame เฟรมของ Fire.png: 0 = ส้ม, 1 = แดง (ทั้งคู่ยกโล่กันได้) / 2 = แดงเข้ม (กันไม่ได้)
     */
    public void shootScythe(double x, double y, double angle, double speed, int damage, int frame) {
        arrows.add(new Arrow(x, y, Math.cos(angle) * speed, Math.sin(angle) * speed, damage,
                frame < 2, Arrow.SCYTHE, frame, 16));
    }

    /** ปล่อยจุดสี n จุดกระจายออกจากตำแหน่ง (x, y) */
    public void burst(double x, double y, int n, Color color) {
        for (int i = 0; i < n; i++) {
            Particle q = new Particle();
            double angle = rng.nextDouble() * Math.PI * 2;
            double speed = 60 + rng.nextDouble() * 180;
            q.x = x;
            q.y = y;
            q.vx = Math.cos(angle) * speed;
            q.vy = Math.sin(angle) * speed - 60;
            q.life = 0.4 + rng.nextDouble() * 0.4;
            q.maxLife = q.life;
            q.size = 3 + rng.nextInt(4);
            q.color = color;
            particles.add(q);
        }
    }
}
