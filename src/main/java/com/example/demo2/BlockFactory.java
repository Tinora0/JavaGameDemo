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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import static com.almasb.fxgl.dsl.FXGLForKtKt.*;

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
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC); // 动态物体，受物理影响
        Texture texture = getAssetLoader().loadTexture("playerSpriteSheet.png");
        return FXGL.entityBuilder(data)
                .type(EntityType.PLAYER)
                .bbox(new HitBox(new Point2D(0, 0),
                        BoundingShape.box(25, 24)))
                .with(physics) // 添加物理组件
                .with(new CollidableComponent(true)) // 添加可碰撞组件
                .with(new PlayerComponent()) //添加玩家组件
                .build();
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
                .buildAndAttach();
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
                .buildAndAttach();
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
                .buildAndAttach();
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
                .buildAndAttach();
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
                .buildAndAttach();
    }

    @Spawns("deathParticle")
    public Entity spawnDeathParticle(SpawnData data) {
        PhysicsComponent physics = new PhysicsComponent();
        physics.setBodyType(BodyType.DYNAMIC);

        Rectangle view = new Rectangle(4, 4, Color.RED);

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
                .bbox(new HitBox("BODY", BoundingShape.box(25, 25)))
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
