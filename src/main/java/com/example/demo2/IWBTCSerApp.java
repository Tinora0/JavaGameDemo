package com.example.demo2;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;
import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.input.Input;
import com.almasb.fxgl.input.UserAction;
import com.almasb.fxgl.physics.CollisionHandler;
import com.almasb.fxgl.physics.PhysicsWorld;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

public class IWBTCSerApp extends GameApplication {

    private final File saveFile = new File("saves/save.dat");
    //
    private String currentLevel = "level1.tmx";
    private Entity player;
    private PlayerComponent playerComponent;
    // 检查点
    private Point2D respawnPoint = new Point2D(100, 100); // 默认重生点，可外部设置

    public static void main(String[] args) {
        launch(args);
    }

    public File getSaveFilePath() {
        return saveFile;
    }

    public void setRespawnPoint(Point2D p) {
        this.respawnPoint = p;
    }

    public void setPlayer(Entity pl) {
        this.player = pl;
        this.playerComponent = pl.getComponent(PlayerComponent.class);
    }
    // 保存当前帧的调试线引用

    @Override
    protected void initSettings(GameSettings settings) {
        settings.setTitle("I Wanna Be The CSer");
        settings.setVersion("0.1");
        settings.setWidth(800);
        settings.setHeight(608);
        settings.setAppIcon("cherry.png");
        settings.setMainMenuEnabled(true);

    }

    protected void initAssets() {
        // 把常用贴图缓存到内存，下次再 loadTexture 就是内存读取
        getAssetLoader().loadTexture("ground.png");
        getAssetLoader().loadTexture("playerSpriteSheet.png");

    }

    @Override
    protected void initGame() {
        initAssets();
        getGameWorld().addEntityFactory(new BlockFactory());
        loadLevel(currentLevel, null, null);
        spawnPlayerAtRespawn();

        spawn("ground", 100, 150);
        spawn("ground", 125, 150);
        spawn("ground", 150, 150);
        spawn("ground", 175, 150);
        spawn("spikeup", 200, 150);
        spawn("savepoint", 225, 100);

    }

    private void bindCameraToPlayer() {
        getGameWorld().getEntitiesByType(EntityType.PLAYER).stream().findFirst().ifPresent(player -> {
            getGameScene().getViewport().bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
            // 可选：限制相机滚动边界（根据关卡尺寸设置）
            // getGameScene().getViewport().setBounds(0, 0, 50 * 32, 20 * 32);
        });
    }

    public void loadLevel(String levelFile, Double spawnX, Double spawnY) {
        this.currentLevel = levelFile;

        // 切换关卡（会根据 TMX 对象层的 type 调用对应 @Spawns）
        FXGL.setLevelFromMap(levelFile);

        // 拿到玩家（地图应有一个 type="player" 的对象来触发 Factory 生成）
        Entity player = getGameWorld().getEntitiesByType(EntityType.PLAYER)
                .stream().findFirst()
                .orElseGet(() -> {
                    // 若地图里没放 player 对象，则兜底生成一个
                    return spawn("player", new SpawnData(64, 480));
                });

        // 若提供了出生点，就覆盖玩家坐标
        if (spawnX != null && spawnY != null) {
            respawnPoint = new Point2D(spawnX, spawnY);
            player.setPosition(spawnX, spawnY);
        }

        // 让相机重新绑定到玩家（防止切关后相机丢失跟随）
        bindCameraToPlayer();
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
                if (playerComponent.isDead()) return;
                playerComponent.setJumpHeld(true);
                playerComponent.jump();
            }

            @Override
            protected void onActionEnd() {
                if (playerComponent.isDead()) return;
                playerComponent.setJumpHeld(false);
                playerComponent.endJump();
            }
        }, KeyCode.SPACE);


        input.addAction(new UserAction("Respawn") {
            @Override
            protected void onActionBegin() {
                playerComponent.respawn();
                player.removeFromWorld();
                spawnPlayerAtRespawn();
                FXGL.inc("deathTime", +1);
            }
        }, KeyCode.R);
        input.addAction(new UserAction("Shoot") {
            @Override
            protected void onActionBegin() {
                if (playerComponent.isDead()) return;
                playerComponent.shoot();
            }
        }, KeyCode.Z); // 可改为你喜欢的键

    }

    @Override
    protected void initUI() {
        var MusicControl = FXGL.getAssetLoader().loadTexture("openmusic.png");
        MusicControl.setTranslateX(0);
        MusicControl.setTranslateY(0);
        FXGL.getGameScene().addChild(MusicControl);

        Text text = new Text();
        text.setFont(Font.font(24));
        text.setTranslateX(750);
        text.setTranslateY(25);
        text.textProperty().bind(getWorldProperties().intProperty("deathTime").asString());
        text.setFill(Color.BLACK);
        FXGL.getGameScene().addChild(text);

        Text totalTimeText = new Text("总游玩时间：00:00:00");
        totalTimeText.setFont(Font.font(18));
        totalTimeText.setFill(Color.DARKGREEN);
        totalTimeText.setTranslateX(600);
        totalTimeText.setTranslateY(75);
        FXGL.getGameScene().addChild(totalTimeText);
// 每秒更新一次
        FXGL.getGameTimer().runAtInterval(() -> {
            int stored = FXGL.geti("totalPlayTime");
            int current = (int) FXGL.getGameTimer().getNow();
            int total = stored + current;

            int hours = total / 3600;
            int minutes = (total % 3600) / 60;
            int secs = total % 60;

            totalTimeText.setText(String.format("总游玩时间：%02d:%02d:%02d", hours, minutes, secs));
        }, Duration.seconds(1));


    }

    @Override
    protected void initGameVars(Map<String, Object> vars) {
        vars.put("deathTime", 0);
        vars.put("totalPlayTime", 0);
    }

    @Override
    protected void initPhysics() {
        PhysicsWorld physics = getPhysicsWorld();

        // 死亡(刺，敌人)
        physics.addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.SPIKE) {
            @Override
            protected void onCollisionBegin(Entity player, Entity spike) {
                // 只在第一次碰撞时调用 die()
                player.getComponent(PlayerComponent.class).die();
            }
        });
        physics.addCollisionHandler(new CollisionHandler(EntityType.PLAYER, EntityType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity player, Entity enemy) {
                // 只在第一次碰撞时调用 die()
                player.getComponent(PlayerComponent.class).die();
            }
        });
        //子弹击杀敌人
        physics.addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.ENEMY) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity enemy) {
                bullet.removeFromWorld();
                enemy.removeFromWorld();
                spawn("explosion", enemy.getX(), enemy.getY());
            }
        });
        //子弹激活传送点
        physics.addCollisionHandler(new CollisionHandler(EntityType.BULLET, EntityType.SAVEPOINT) {
            @Override
            protected void onCollisionBegin(Entity bullet, Entity checkpoint) {
                bullet.removeFromWorld();

                Point2D playerPos = player.getPosition();
                checkpoint.getComponent(SavepointComponent.class).activate(playerPos);
            }
        });
        // 进入右向加速带 → 持续向右推
        //   FXGL.onCollision(EntityType.PLAYER, EntityType.BOOSTRIGHT, (player, boost) -> {
        //       double x= playerComponent.physics.getVelocityX();
        //       playerComponent.physics.setVelocityX(x+300);
        //   });
        //   FXGL.onCollisionEnd(EntityType.PLAYER, EntityType.BOOSTRIGHT, (player, boost) -> {
        //      double x= playerComponent.physics.getVelocityX();
        //      playerComponent.physics.setVelocityX(x-300);
        //   });
        //   // 进入左向加速带 → 持续向左推
        //   FXGL.onCollisionBegin(EntityType.PLAYER, EntityType.BOOSTLEFT, (player, boost) -> {
        //      double x= playerComponent.physics.getVelocityX();
        //   });
        // FXGL.onCollisionEnd(EntityType.PLAYER, EntityType.BOOSTLEFT, (player, boost) -> {
        //      double x= playerComponent.physics.getVelocityX();
        //     playerComponent.physics.setVelocityX(x+300);
        // });
    }

    public void spawnPlayerAtRespawn() {

        if (player != null) {
            player.removeFromWorld();
        }
        player = spawn("player", respawnPoint.getX(), respawnPoint.getY());
        playerComponent = player.getComponent(PlayerComponent.class);
        getGameScene().getViewport()
                .bindToEntity(player, getAppWidth() / 2.0, getAppHeight() / 2.0);
    }

    public void saveCheckpoint(Point2D playerPos) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(saveFile))) {
            Path saveDir = Paths.get("saves");
            if (!Files.exists(saveDir)) {
                Files.createDirectories(saveDir);
            }
            writer.write(playerPos.getX() + "," + playerPos.getY());
            writer.newLine();
            writer.write("deathTime=" + FXGL.geti("deathTime"));
            writer.newLine();
            int totalPlayTime = FXGL.geti("totalPlayTime") + (int) FXGL.getGameTimer().getNow();
            writer.write("playTime=" + totalPlayTime);
            System.out.println("✅ 存档成功：" + playerPos);
        } catch (IOException e) {
            System.out.println("❌ 存档失败：" + e.getMessage());
        }
    }

    public void loadCheckpoint() {
        if (!saveFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(saveFile))) {
            String[] pos = reader.readLine().split(",");
            respawnPoint = new Point2D(Double.parseDouble(pos[0]), Double.parseDouble(pos[1]));

            String deathLine = reader.readLine();
            if (deathLine != null && deathLine.startsWith("deathTime=")) {
                FXGL.set("deathTime", Integer.parseInt(deathLine.split("=")[1]));
            }

            String playTimeLine = reader.readLine();
            if (playTimeLine != null && playTimeLine.startsWith("playTime=")) {
                FXGL.set("totalPlayTime", Integer.parseInt(playTimeLine.split("=")[1]));
            } else {
                FXGL.set("totalPlayTime", 0);
            }

            System.out.println("📦 已加载存档点：" + respawnPoint);
        } catch (Exception e) {
            System.out.println("⚠️ 存档加载失败：" + e.getMessage());
        }
    }
}


