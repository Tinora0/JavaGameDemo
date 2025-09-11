package com.example.demo2;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import com.almasb.fxgl.physics.box2d.dynamics.Body;
import com.almasb.fxgl.core.math.Vec2;

public class EnemyComponent extends Component {
    private PhysicsComponent physics;
    private int direction = 1; // 1:右移, -1:左移
    private final double speed = 80;
    private double patrolRange = 100.0; // 巡逻范围
    private double startX;
    private double startY;
    private boolean isPhysicsInitialized = false; // 物理初始化标志

    @Override
    public void onAdded() {
        startX = entity.getX(); // 仅在此处记录初始位置
        startY = entity.getY();

        physics = entity.getComponent(PhysicsComponent.class);
        startX = entity.getX();
        startY = entity.getY();

        physics.setOnPhysicsInitialized(() -> {
            Body body = physics.getBody();
            body.setGravityScale(0); // 禁用重力
            physics.setVelocityX(direction * speed);
            isPhysicsInitialized = true;
        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (isPhysicsInitialized) {
            // 边界检测与转向逻辑
            if (entity.getX() - startX > patrolRange || entity.getX() < startX) {
                direction *= -1;
                physics.setVelocityX(direction * speed);
            }
        }
    }

    // 重置敌人状态
    public void reset() {
        entity.setPosition(startX, startY);
        direction = 1;
        if (isPhysicsInitialized) {
            physics.setVelocityX(direction * speed);
            physics.getBody().setLinearVelocity(new Vec2(direction * speed, 0));
            physics.getBody().setAwake(true);
            physics.getBody().setGravityScale(0);
        }
    }

    // 设置巡逻范围（从地图配置读取）
    public void setPatrolRange(double range) {
        if (range > 0) {
            patrolRange = range;
        }
    }

    // Getter方法
    public double getStartX() { return startX; }
    public double getStartY() { return startY; }
    public int getDirection() { return direction; }
    public double getSpeed() { return speed; }
}