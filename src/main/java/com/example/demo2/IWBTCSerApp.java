package com.example.demo2;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class IWBTCSerApp extends GameApplication {

    private Entity player;
    private PlayerComponent playerComponent;

    // 检查点
    private Point2D respawnPoint = new Point2D(100, 100);


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("I Wanna Be The CSer");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(608);
        settings.setAppIcon("cherry.png");
        settings.setMainMenuEnabled(true);
    }

    @Override
    protected void initGame() {
        getGameWorld().addEntityFactory(new BlockFactory());

        Entity p = spawn("player", 100, 100);
        player = p;
        playerComponent = p.getComponent(PlayerComponent.class);
        spawn("ground", 100, 150);
        spawn("ground", 125, 150);
        spawn("ground", 150, 150);
        spawn("ground", 175, 150);
        spawn("spikeup", 200, 150);
        spawn("ground", 225, 150);

    }

    @Override
    protected void initInput() {
        // 获取输入系统
        Input input = getInput();

        // 向右移动动作
        input.addAction(new UserAction("Move Right") {
            @Override
            protected void onAction() {
                if (playerComponent.isDead()) return;
                playerComponent.moveRight(); // 按下时开始向右移动
            }

            @Override
            protected void onActionEnd() {
                if (playerComponent.isDead()) return;
                playerComponent.stop(); // 释放键时停止移动
            }
        }, KeyCode.D); // 绑定到D键

        // 向左移动动作
        input.addAction(new UserAction("Move Left") {
            @Override
            protected void onAction() {
                if (playerComponent.isDead()) return;
                playerComponent.moveLeft(); // 按下时开始向左移动
            }

            @Override
            protected void onActionEnd() {
                if (playerComponent.isDead()) return;
                playerComponent.stop(); // 释放键时停止移动
            }
        }, KeyCode.A); // 绑定到A键

        input.addAction(new UserAction("Jump") {
            @Override
            protected void onActionBegin() {
                playerComponent.setJumpHeld(true);
                playerComponent.jump();
            }

            @Override
            protected void onActionEnd() {
                playerComponent.setJumpHeld(false);
                playerComponent.endJump();
            }
        }, KeyCode.SPACE);


        input.addAction(new UserAction("Respawn") {
            @Override
            protected void onActionBegin() {
                // 1. 移除旧实体
                playerComponent.respawn();
                player.removeFromWorld();

                // 2. spawn 新玩家
                player = spawn("player",
                        respawnPoint.getX(),
                        respawnPoint.getY());
                playerComponent = player.getComponent(PlayerComponent.class);
                // 3. 相机重新跟随
                getGameScene().getViewport()
                        .bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);// 按下时开始向左移动

            }
        }, KeyCode.R);
    }

    @Override
    protected void initUI() {
        var MusicControl = FXGL.getAssetLoader().loadTexture("openmusic.png");
        MusicControl.setTranslateX(0);
        MusicControl.setTranslateY(0);
        FXGL.getGameScene().addChild(MusicControl);
    }

    @Override
    protected void initPhysics() {
        PhysicsWorld physics = getPhysicsWorld();

        // 玩家与尖刺碰撞时触发死亡
        physics.addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity spike) {
                // 只在第一次碰撞时调用 die()
                player.getComponent(PlayerComponent.class).die();
            }
        });
    }
}
