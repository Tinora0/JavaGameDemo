package com.example.demo2;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;

public class EnemyComponent extends Component {
    protected PhysicsComponent physics;
    protected int direction = 1; // 1:右移, -1:左移
    protected final double speed = 80;
    protected double patrolRange = 100.0; // 巡逻范围
    protected double startX;
    protected double startY;
    protected boolean isPhysicsInitialized = false; // 物理初始化标志
    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);

        physics.setOnPhysicsInitialized(() -> {
            startX = entity.getX();
            startY = entity.getY();
            physics.getBody().setGravityScale(0);
            physics.setVelocityX(direction * speed);
            isPhysicsInitialized = true;

        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (!isPhysicsInitialized) return;

        double dx = entity.getX() - startX;
        boolean hitBoundary = Math.abs(dx) >= patrolRange;


        if (hitBoundary) {
            direction *= -1;
            physics.setVelocityX(direction * speed);
            System.out.printf("[onUpdate] Enemy 触发反向 新方向=%d 新速度=%.2f%n",
                    direction, physics.getVelocityX());
        }
    }

    public void reset() {
        entity.setPosition(startX, startY);
        direction = 1;

        if (physics == null) {
            physics = entity.getComponent(PhysicsComponent.class);
        }

        physics.getBody().setAwake(true);
        physics.getBody().setGravityScale(0);
        physics.setVelocityX(direction * speed);
        isPhysicsInitialized = true;

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