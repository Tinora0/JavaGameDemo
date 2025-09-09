package com.example.demo2;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.dsl.components.ExpireCleanComponent;
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
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.entityBuilder;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getAssetLoader;

public class BlockFactory implements EntityFactory {

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

    @Spawns("death")
    public Entity death(SpawnData data) {
        return entityBuilder(data)
                .view(new Rectangle(12, 12, Color.CRIMSON))
                .with(new ExpireCleanComponent(Duration.seconds(0.4))) // 0.4s 后自动销毁
                .build();
    }

}
