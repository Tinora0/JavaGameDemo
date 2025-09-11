package com.example.demo2;

import com.almasb.fxgl.core.math.Vec2;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.entity.components.TransformComponent;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.function.Function;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;


public class PlayerComponent extends Component {

    // 玩家属性常量
    private final double moveSpeed = 180;// 水平移动速度（像素/秒）
    private final double jumpSpeed = 150;
    private final double doubleJumpSpeed = 100;
    private final double maxJumpTime = 0.3;
    // 动画帧尺寸常量
    private final int frameWidth = 25;     // 单帧宽度（像素）
    private final int frameHeight = 25;    // 单帧高度（像素）
    // 动画通道：定义不同的动画序列
    private final AnimationChannel animIdle;     // 静止状态动画
    private final AnimationChannel animFall;      // 移动状态动画
    private final AnimationChannel animJump;
    private final AnimationChannel animMove;// 跳跃状态动画
    private final AnimatedTexture texture;       // 动画纹理，用于渲染动画
    private final BodyType prevBodyType = BodyType.DYNAMIC;
    private final double savedGravityScale = 1.0;
    public int deathTime = 0;
    // 组件依赖：FXGL 会自动注入这些组件
    TransformComponent transform;  // 处理实体位置、旋转和缩放
    PhysicsComponent physics;      // 处理物理模拟和碰撞
    // 玩家状态标志
    private boolean isMoving = false;      // 是否正在移动
    private boolean isOnGround = false;    // 是否接触地面
    private boolean isFalling = false;
    private boolean isFacingRight = true;  // 角色朝向（右=true，左=false）
    //跳跃相关
    private boolean canDoubleJump = true;
    private boolean isJumping = false;
    private double jumpTime = 0;
    private boolean jumpHeld = false;
    //死亡相关
    private boolean isDead = false;
    private ImageView deathOverlay;

    public PlayerComponent() {
        // 加载精灵图 - 包含所有动画帧的单张图片
        Image image = image("playerSpriteSheet.png");

        // 初始化动画通道
        // 参数：图像, 每行动画帧数, 帧宽度, 帧高度, 动画持续时间, 起始帧, 结束帧
        animIdle = new AnimationChannel(image, 4, frameWidth, frameHeight, Duration.seconds(1), 0, 3);
        animMove = new AnimationChannel(image, 4, frameWidth, frameHeight, Duration.seconds(1), 8, 11);
        animJump = new AnimationChannel(image, 4, frameWidth, frameHeight, Duration.seconds(1), 4, 5);
        animFall = new AnimationChannel(image, 4, frameWidth, frameHeight, Duration.seconds(1), 6, 7);


        // 创建动画纹理，使用静止动画作为初始状态
        texture = new AnimatedTexture(animIdle);
        texture.loop();  // 设置动画循环播放
    }


    /**
     * 组件添加到实体时调用
     * 设置初始状态和事件监听
     */
    @Override
    public void onAdded() {
        // 将动画纹理添加到实体的视图组件，使其可见
        entity.getViewComponent().addChild(texture);
    }

    /**
     * 每帧更新时调用
     *
     * @param tpf 每帧时间（Time Per Frame），单位秒
     */
    @Override
    public void onUpdate(double tpf) {
        updateAnimation();

        double vy = physics.getVelocityY();

        if (vy < 0) {
            isJumping = true;
            isOnGround = false;
            isFalling = false;
            jumpTime += tpf;    // 累加真实秒数

        } else if (vy > 0) {
            isJumping = false;
            isFalling = true;
        } else {
            isFalling = false;
        }

        if (isJumping && jumpHeld && jumpTime < maxJumpTime) {
            // 将当前上升速度限制在一个上限（更负代表更快的上升）
            double target = -jumpSpeed;
            if (vy > target) {
                physics.setVelocityY(target);
            }
        }
        checkGroundStatus();
    }

    public void jump() {
        if (!isJumping) {
            if (isOnGround) {
                physics.setVelocityY(-jumpSpeed);  // 向上
                isJumping = true;
                isOnGround = false;
                jumpTime = 0;
                canDoubleJump = true;
                play("jump1.wav");
            } else if (canDoubleJump) {
                isJumping = true;
                physics.setVelocityY(-doubleJumpSpeed); // 向上
                jumpTime = 0;
                canDoubleJump = false;
                FXGL.getAudioPlayer().playSound(FXGL.getAssetLoader().loadSound("jump2.wav"));

            }
        }
    }

    public void endJump() {
        // 切跳：如果仍在上升，则削减上升速度，制造“短跳”
        if (physics.getVelocityY() < 0) {
            physics.setVelocityY(physics.getVelocityY() * 0);
        }
        isJumping = false;
    }

    private void updateAnimation() {
        // 根据状态选择适当的动画
        if (isJumping) {
            // 跳跃状态 - 播放跳跃动画
            if (texture.getAnimationChannel() != animJump) {
                texture.loopAnimationChannel(animJump);
            }
        } else if (isFalling) {
            // 坠落状态 - 播放坠落动画
            if (texture.getAnimationChannel() != animFall) {
                texture.loopAnimationChannel(animFall);
            }
        } else if (isMoving) {
            // 移动状态 - 播放移动动画
            if (texture.getAnimationChannel() != animMove) {
                texture.loopAnimationChannel(animMove);
            }
        } else {
            // 静止状态 - 播放静止动画
            if (texture.getAnimationChannel() != animIdle) {
                texture.loopAnimationChannel(animIdle);
            }
        }

        // 根据朝向翻转纹理（实现左右转身效果）
        if (isFacingRight) {
            texture.setScaleX(1);  // 正常朝向（右）
        } else {
            texture.setScaleX(-1); // 翻转朝向（左）
        }
    }

    private void checkGroundStatus() {

        // 记录之前的地面状态，用于检测状态变化
        boolean wasOnGround = isOnGround;
        Point2D pos = transform.getPosition();
        double yOff = frameHeight - 1;  // 从脚底稍微往下发射

        Point2D leftStart = pos.add(2, yOff);
        Point2D centerStart = pos.add(frameWidth / 2, yOff);
        Point2D rightStart = pos.add(frameWidth - 2, yOff);

        Point2D leftEnd = leftStart.add(0, 8);
        Point2D centerEnd = centerStart.add(0, 8);
        Point2D rightEnd = rightStart.add(0, 8);

        // 封装成一个方法：一根射线检测是否击中地面
        Function<Pair<Point2D, Point2D>, Boolean> hitGround = pair ->
                getPhysicsWorld()
                        .raycast(pair.getKey(), pair.getValue())
                        .getEntity()
                        .filter(e ->
                                e.isType(EntityType.BLOCK)
                                        || e.isType(EntityType.PLATFORM)
                                        || e.isType(EntityType.BOOSTLEFT)
                                        || e.isType(EntityType.BOOSTRIGHT)
                                        || e.isType(EntityType.ICE)

                        )
                        .isPresent();

        // 三根只要一根返回 true，就判定在地面上
        boolean leftHit = hitGround.apply(new Pair<>(leftStart, leftEnd));
        boolean centerHit = hitGround.apply(new Pair<>(centerStart, centerEnd));
        boolean rightHit = hitGround.apply(new Pair<>(rightStart, rightEnd));

        isOnGround = leftHit || centerHit || rightHit;

        if (isOnGround) {
            // 刚落地时（wasOnGround=false → isOnGround=true）才执行一次
            if (!wasOnGround) {
                canDoubleJump = true;
                jumpTime = 0;
            }
            isJumping = false;
            isFalling = false;
        }
        // 可以在这里添加落地粒子效果或声音
        //       spawn("dust", transform.getX() + frameWidth / 2.0, transform.getY() + frameHeight);
        //  }

    }

    public void die() {
        if (isDead) return;
        isDead = true;

        // 停止当前速度
        physics.setVelocityX(0);
        physics.setVelocityY(0);

        // 保存并禁用物理（STATIC 不受重力、不被推动）
        physics.setBodyType(BodyType.STATIC);
        physics.getBody().setLinearVelocity(new Vec2(0, 0));
        physics.getBody().setAngularVelocity(0);

        // 让刚体休眠，彻底断开与世界的物理更新
        physics.getBody().setAwake(false);

        entity.getComponent(CollidableComponent.class).setValue(false);

        texture.stop();

        Texture tex = getAssetLoader().loadTexture("gameover.png");
        deathOverlay = new ImageView(tex.getImage());
        deathOverlay.setTranslateX((getAppWidth() - tex.getWidth()) / 2);
        deathOverlay.setTranslateY((getAppHeight() - tex.getHeight()) / 2);
        getGameScene().addUINode(deathOverlay);

        // 禁用碰撞，避免动画期间再次触发
        entity.getComponent(CollidableComponent.class).setValue(false);


        getGameScene().getViewport().shakeTranslational(3);
        play("death.wav");
        double x = entity.getX() + frameWidth / 2.0;
        double y = entity.getY() + frameHeight / 2.0;
        getGameTimer().runOnceAfter(() -> {
            for (int i = 0; i < 200; i++) {
                spawn("deathParticle", x, y);
            }
        }, Duration.seconds(0));
    }

    public void respawn() {

        // 恢复物理与碰撞
        physics.setBodyType(prevBodyType != null ? prevBodyType : BodyType.DYNAMIC);
        entity.getComponent(CollidableComponent.class).setValue(true);
        //移除死亡画面
        if (deathOverlay != null) {
            getGameScene().removeUINode(deathOverlay);
            deathOverlay = null;
        }
        // 清速度与状态
        physics.setVelocityX(0);
        physics.setVelocityY(0);

        isDead = false;
        isJumping = false;
        isFalling = false;
        isOnGround = false;
        canDoubleJump = true;
        deathTime++;

        getGameScene().getViewport()
                .bindToEntity(entity, getAppWidth() / 2.0, getAppHeight() / 2.0);
    }

    public void moveLeft() {
        isMoving = true;     // 设置移动状态
        isFacingRight = false; // 设置朝向左
        physics.setVelocityX(-moveSpeed); // 设置向左的速度
    }

    public void moveRight() {
        isMoving = true;     // 设置移动状态
        isFacingRight = true;  // 设置朝向右
        physics.setVelocityX(moveSpeed); // 设置向右的速度
    }

    public void stop() {
        isMoving = false;    // 清除移动状态
        physics.setVelocityX(0); // 停止水平移动
    }

    public void shoot() {
        if (isDead) return;

        double x = entity.getX() + (isFacingRight ? frameWidth : -8);
        double y = entity.getY() + frameHeight / 2.1;

        SpawnData data = new SpawnData(x, y)
                .put("dir", isFacingRight ? 1 : -1);

        spawn("bullet", data);
        play("shoot.wav");
    }


    public boolean isDead() {
        return isDead;
    }

    public void setJumpHeld(boolean held) {
        this.jumpHeld = held;
    }
}