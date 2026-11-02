package legacyknight.world;

import static legacyknight.core.Constants.ATTACK_COOLDOWN;
import static legacyknight.core.Constants.ATTACK_REACH;
import static legacyknight.core.Constants.ATTACK_TIME;
import static legacyknight.core.Constants.BLOCK_SPEED;
import static legacyknight.core.Constants.CROUCH_H;
import static legacyknight.core.Constants.CROUCH_SPEED;
import static legacyknight.core.Constants.JUMP_SPEED;
import static legacyknight.core.Constants.SCREEN_H;
import static legacyknight.core.Constants.STAND_H;
import static legacyknight.core.Constants.WALK_SPEED;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.util.Iterator;

import legacyknight.core.InputHandler;
import legacyknight.entity.Arrow;
import legacyknight.entity.Enemy;
import legacyknight.entity.Player;
import legacyknight.util.MathUtil;

/**
 * "สมองของผู้เล่น" อ่านปุ่มที่กดแล้วสั่งให้อัศวินเดิน กระโดด ย่อ ฟัน และยกโล่
 */
final class PlayerController {

    private PlayerController() { }

    static void update(GameWorld world, InputHandler input, double dt) {
        Player p = world.player;

        // นับเวลาถอยหลังของสถานะต่างๆ
        p.invuln = Math.max(0, p.invuln - dt);
        p.attackCooldown -= dt;
        p.attackTimer -= dt;
        p.knockTimer -= dt;
        p.blockHitTimer -= dt;

        boolean left = input.isKeyDown(KeyEvent.VK_A) || input.isKeyDown(KeyEvent.VK_LEFT);
        boolean right = input.isKeyDown(KeyEvent.VK_D) || input.isKeyDown(KeyEvent.VK_RIGHT);
        boolean down = input.isKeyDown(KeyEvent.VK_S) || input.isKeyDown(KeyEvent.VK_DOWN);
        boolean jump = input.isKeyDown(KeyEvent.VK_W) || input.isKeyDown(KeyEvent.VK_UP)
                || input.isKeyDown(KeyEvent.VK_SPACE);

        // --- ยกโล่: กดเมาส์ขวาค้าง (ระหว่างฟันดาบยกโล่ไม่ได้) ---
        p.blocking = input.mouseRight && p.attackTimer <= 0;
        if (p.blocking) {
            p.facing = mouseFacing(world, input);
            p.blockTime += dt;       // นับเวลาไว้เล่นแอนิเมชันยกโล่ขึ้น
        } else {
            p.blockTime = 0;
        }

        // --- ฟันดาบ: คลิกซ้าย ---
        if (input.mouseLeft && p.attackCooldown <= 0 && !p.blocking) {
            p.attackTimer = ATTACK_TIME;
            p.attackCooldown = ATTACK_COOLDOWN;
            p.alreadyHit.clear();
            p.comboIndex = 1 - p.comboIndex;   // สลับท่าฟัน 2 ท่า
            p.facing = mouseFacing(world, input);
        }
        if (p.attackTimer > 0) {
            swordHit(world, p);
        }

        // --- ย่อตัว ---
        int diff = STAND_H - CROUCH_H;
        if (down && p.onGround && !p.crouch) {
            p.crouch = true;
            p.y += diff;
            p.h = CROUCH_H;
        } else if (!down && p.crouch && !world.level.hitsSolid(p.x, p.y - diff, p.w, STAND_H)) {
            // ลุกขึ้นได้ก็ต่อเมื่อเหนือหัวไม่มีเพดานขวางอยู่
            p.crouch = false;
            p.y -= diff;
            p.h = STAND_H;
        }

        // --- เดิน / กระโดด ---
        int dir = (right ? 1 : 0) - (left ? 1 : 0);
        if (dir != 0 && !p.blocking && p.attackTimer <= 0) p.facing = dir;

        double speed = p.crouch ? CROUCH_SPEED : (p.blocking ? BLOCK_SPEED : WALK_SPEED);
        if (p.knockTimer <= 0) p.vx = dir * speed;   // ปกติ: เดินตามปุ่ม
        else p.vx *= 0.93;                           // โดนกระแทก: ไถลไปเรื่อยๆ ควบคุมไม่ได้

        if (jump && p.onGround && !p.crouch) p.vy = JUMP_SPEED;

        p.applyPhysics(dt, world.level);

        // ตกหลุมนอกจอ = แพ้ทันที
        if (p.y > SCREEN_H + 80) {
            p.hp = 0;
            world.status = GameWorld.Status.LOST;
        }
    }

    /** ดาบกำลังฟัน: เช็กว่าโดนศัตรูตัวไหนบ้าง และปัดลูกไฟทิ้งได้ด้วย */
    private static void swordHit(GameWorld world, Player p) {
        // กล่องโจมตีอยู่หน้าตัวผู้เล่น (ซ้ายหรือขวาตามที่หันอยู่)
        double ax = p.facing > 0 ? p.x + p.w : p.x - ATTACK_REACH;
        double ay = p.y - 6;
        double ah = p.h + 6;

        for (Enemy e : world.enemies) {
            if (e.alive && !p.alreadyHit.contains(e)
                    && MathUtil.overlaps(ax, ay, ATTACK_REACH, ah, e.x, e.y, e.w, e.h)) {
                p.alreadyHit.add(e);
                world.hurtEnemy(e, world.stats.attack, p.facing);
            }
        }

        // ดาบฟันลูกไฟทิ้งได้ทุกชนิด
        for (Iterator<Arrow> it = world.arrows.iterator(); it.hasNext(); ) {
            Arrow a = it.next();
            if (MathUtil.overlaps(a.x - 10, a.y - 10, 20, 20, ax, ay, ATTACK_REACH, ah)) {
                world.burst(a.x, a.y, 6, new Color(255, 240, 180));
                it.remove();
            }
        }
    }

    /** เมาส์อยู่ด้านขวาของตัวผู้เล่นบนจอหรือไม่ (1 = ขวา, -1 = ซ้าย) */
    private static int mouseFacing(GameWorld world, InputHandler input) {
        Player p = world.player;
        double playerScreenX = p.x + p.w / 2.0 - world.cameraX;
        return input.mouseX >= playerScreenX ? 1 : -1;
    }
}
