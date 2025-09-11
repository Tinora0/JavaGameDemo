package com.example.demo2;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.EntityFactory;
import com.almasb.fxgl.entity.SpawnData;
import com.almasb.fxgl.entity.Spawns;
import com.almasb.fxgl.entity.components.CollidableComponent;
import com.almasb.fxgl.physics.BoundingShape;
import com.almasb.fxgl.physics.HitBox;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.BodyType;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGLForKtKt.entityBuilder;
import static com.almasb.fxgl.dsl.FXGLForKtKt.texture;

public class BlockFactory implements EntityFactory {

    private Entity createTiledEntity(SpawnData data, String textureName, EntityType type) {
        double w = data.hasKey("width") ? ((Number) data.get("width")).doubleValue() : 32;
        double h = data.hasKey("height") ? ((Number) data.get("height")).doubleValue() : 32;

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);

        Texture tex = texture(textureName);
        double tileW = tex.getImage().getWidth();
        double tileH = tex.getImage().getHeight();

        Pane pane = new Pane();

        // 平铺纹理
        for (double x = 0; x < w; x += tileW) {
            for (double y = 0; y < h; y += tileH) {
                Texture t = tex.copy();
                t.setTranslateX(x);
                t.setTranslateY(y);
                pane.getChildren().add(t);
            }
        }

        // 直接用 w×h 生成碰撞箱，不依赖 Pane 的大小
        return FXGL.entityBuilder(data)
                .type(type)
                .view(pane) // 只负责显示
                .bbox(new HitBox(BoundingShape.box(w, h))) // 碰撞箱
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
    }


    @Spawns("block")
    public Entity block(SpawnData data) {
        return createTiledEntity(data, "block.png", EntityType.BLOCK);
    }

    @Spawns("soil")
    public Entity soil(SpawnData data) {
        return createTiledEntity(data, "soil.png", EntityType.BLOCK);
    }

    @Spawns("ground")
    public Entity ground(SpawnData data) {
        return createTiledEntity(data, "ground.png", EntityType.BLOCK);
    }

    @Spawns("ice")
    public Entity ice(SpawnData data) {
        return createTiledEntity(data, "block.png", EntityType.PLATFORM);
    }

    @Spawns("boostLeft")
    public Entity boostLeft(SpawnData data) {
        return createTiledEntity(data, "block.png", EntityType.PLATFORM);
    }

    @Spawns("boostRight")
    public Entity boostRight(SpawnData data) {
        return createTiledEntity(data, "block.png", EntityType.PLATFORM);
    }
    @Spawns("bullet")
    public Entity spawnBullet(SpawnData data) {
        int dir = data.hasKey("dir") ? data.get("dir") : 1;

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        Texture tex = texture("bullet.png");
        tex.setScaleX(0.5); // 水平缩小为原来的一半
        tex.setScaleY(0.5); // 垂直缩小为原来的一半

        return entityBuilder(data)
                .type(EntityType.BULLET)
                .viewWithBBox(tex)
                .with(physics, new CollidableComponent(true))
                .with(new BulletComponent(dir, 150))
                // 速度可调
                .build();
    }

    @Spawns("player")
    public Entity spawnPlayer(SpawnData data) {
        // 1. 构建玩家实体
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        Entity pl = entityBuilder(data)
                .type(EntityType.PLAYER)
                .bbox(new HitBox(new Point2D(0, 3),
                        BoundingShape.box(21, 21)))
                .with(physics,
                        new CollidableComponent(true),
                        new PlayerComponent())
                .build();  // build() 后由 FXGL 自动 attach

        // 2) 更新主类的 player 引用
        IWBTCSerApp app = (IWBTCSerApp) FXGL.getApp();
        app.setPlayer(pl);

        // 3) 立即绑定摄像机到这个玩家
        FXGL.getGameScene().getViewport()
                .bindToEntity(pl,
                        FXGL.getAppWidth() / 2.0,
                        FXGL.getAppHeight() / 2.0);
        return pl;
    }

    @Spawns("spikeup")
    public Entity spikeup(SpawnData data) {
        // 三角形顶点，按贴图像素坐标顺序：
        // (0, height) → (width/2, 0) → (width, height)
        double w = 32, h = 32;
        double[] points = {
                0, h,
                w / 2, 0,
                w, h
        };
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.SPIKE)
                .view("spikeup.png")
                // 把三角形多边形作为碰撞箱
                .bbox(new HitBox(new Point2D(0, 0),
                        BoundingShape.polygon(points)))
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
    }


    @Spawns("deathParticle")
    public Entity spawnDeathParticle(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);
        Rectangle view = new Rectangle(2, 2, Color.RED);

        return entityBuilder(data)
                .view(view)
                .with(physics, new DeathParticleComponent())
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("platform")
    public Entity spawnPlatform(SpawnData data) {

        double speed = data.hasKey("speed") ? data.get("speed") : 100.0;
        String dir = data.hasKey("dir") ? data.get("dir") : "right";
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.PLATFORM)
                .viewWithBBox("platform.png")
                .with(new CollidableComponent(true), physics)
                .with(new MovingPlatformComponent(speed, dir))
                .build();
    }
    @Spawns("savepoint")
    public Entity spawnCheckpoint(SpawnData data) {
        Texture inactiveTex = texture("save_normal.png");
        Texture activeTex = texture("save_activated.png");
        activeTex.setVisible(false); // 初始为未激活

        return entityBuilder(data)
                .type(EntityType.SAVEPOINT)
                .view(inactiveTex)
                .view(activeTex)
                .zIndex(-10)
                .bbox(new HitBox("BODY", BoundingShape.box(32, 32)))
                .with(new CollidableComponent(true))
                .with(new SavepointComponent(inactiveTex, activeTex))
                .build();
    }

    @Spawns("portal")
    public Entity spawnPortal(SpawnData data) {
        String targetLevel = data.get("targetLevel");

        // 从 Map<String,Object> 里取出，值可能是 Float/Integer
        double spawnX = ((Number) data.get("spawnX")).doubleValue();
        double spawnY = ((Number) data.get("spawnY")).doubleValue();

        Texture tex = texture("warp.png");

        return entityBuilder(data)
                .type(EntityType.TP)
                .viewWithBBox(tex)
                .with(new CollidableComponent(true))
                .with(new PortalComponent(targetLevel, spawnX, spawnY))
                .build();
    }

    @Spawns("portalLoad")
    public Entity spawnPortalLoad(SpawnData data) {
        Texture tex = texture("warp.png");
        return entityBuilder(data)
                .type(EntityType.TPLOAD)
                .viewWithBBox(tex)
                .with(new CollidableComponent(true))
                .build();
    }
    @Spawns("enemy")
    public Entity spawnEnemy(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();

        physics.setBodyType(BodyType.KINEMATIC);

        // 获取巡逻范围
        double patrolRange = 100.0; // 默认值
        if (data.hasKey("patrolRange")) {
            Object value = data.get("patrolRange");
            if (value instanceof Number) {
                patrolRange = ((Number) value).doubleValue();
            }
        }

        // 将巡逻范围传递给EnemyComponent
        EnemyComponent enemyComponent = new EnemyComponent();
        enemyComponent.setPatrolRange(patrolRange);

        Texture tex = texture("enemy.png");
        return entityBuilder(data)
                .type(EntityType.ENEMY)
                .viewWithBBox(tex)
                .with(physics, new CollidableComponent(true))
                .with(enemyComponent)
                .build();
    }
}
