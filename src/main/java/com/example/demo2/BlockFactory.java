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
import com.almasb.fxgl.physics.box2d.dynamics.FixtureDef;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGLForKtKt.entityBuilder;
import static com.almasb.fxgl.dsl.FXGLForKtKt.texture;

public class BlockFactory implements EntityFactory {

    @Spawns("bullet")
    public Entity spawnBullet(SpawnData data) {
        int dir = data.hasKey("dir") ? data.get("dir") : 1;

        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.KINEMATIC);

        Texture tex = texture("bullet.png");
        tex.setScaleX(0.5); // 水平缩小为原来的一半
        tex.setScaleY(0.5); // 垂直缩小为原来的一半

        return entityBuilder(data)
                .type(EntityType.BULLET)
                .viewWithBBox(tex) // 建议使用小图，如 8x8
                .with(physics, new CollidableComponent(true))
                .with(new BulletComponent(dir, 400)) // 速度可调
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

    @Spawns("ground")
    public Entity ground(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.BLOCK)
                .viewWithBBox("ground.png")
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("platform")
    public Entity platform(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.PLATFORM)
                .viewWithBBox("platform.png")
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
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
    @Spawns("block")
    public Entity block(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.BLOCK)
                .viewWithBBox("block.png")
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
    }
    @Spawns("soil")
    public Entity soil(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.STATIC);
        return FXGL.entityBuilder(data)
                .type(EntityType.BLOCK)
                .viewWithBBox("soil.png")
                .with(physics)
                .with(new CollidableComponent(true))
                .build();
    }

    @Spawns("ice")
    public Entity ice(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.setDensity(0.0001f);
        physics.setFixtureDef(fixtureDef);
        return FXGL.entityBuilder()
                .type(EntityType.ICE)
                .with(new CollidableComponent(true), physics)
                .viewWithBBox(new Rectangle(100, 40, Color.BLUE))
                .build();
    }

    @Spawns("boostLeft")
    public Entity spawnBoostLeft(SpawnData data) {
        return FXGL.entityBuilder()
                .type(EntityType.BOOSTLEFT)
                .with(new CollidableComponent(true))
                .viewWithBBox(new Rectangle(100, 40, Color.ORANGE))
                .build();
    }

    public Entity spawnBoostRight(SpawnData data) {
        return FXGL.entityBuilder()
                .type(EntityType.BOOSTRIGHT)
                .viewWithBBox(new Rectangle(100, 40, Color.ORANGE))
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


}
