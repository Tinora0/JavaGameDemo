package com.example.demo2;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;

public class EnemyComponent extends Component {
    private PhysicsComponent physics;
    private int direction = 1; // 1:右移, -1:左移
    private final double speed = 80;
    private double patrolRange = 100.0; // 巡逻范围
    private double startX;
    private double startY;
    private boolean isPhysicsInitialized = false; // 物理初始化标志
    private static final double EPSILON = 0.5;
    private double patrolCooldown = 0; //
    @Override
    public void onAdded() {
        physics = entity.getComponent(PhysicsComponent.class);

        physics.setOnPhysicsInitialized(() -> {
            startX = entity.getX();
            startY = entity.getY();
            physics.getBody().setGravityScale(0);
            physics.setVelocityX(direction * speed);
            isPhysicsInitialized = true;

            System.out.printf("[onAdded]  初始位置=(%.2f, %.2f) 速度=%.2f 方向=%d%n",
                    startX, startY, physics.getVelocityX(), direction);
        });
    }

    @Override
    public void onUpdate(double tpf) {
        if (!isPhysicsInitialized) return;

        double dx = entity.getX() - startX;
        boolean hitBoundary = Math.abs(dx) >= patrolRange;

        System.out.printf("[onUpdate] Enemy posX=%.2f dx=%.2f patrolRange=%.2f hitBoundary=%b velX=%.2f dir=%d%n",
                entity.getX(), dx, patrolRange, hitBoundary, physics.getVelocityX(), direction);

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

        System.out.printf("[reset] Enemy 重置到=(%.2f, %.2f) 方向=%d 速度=%.2f%n",
                entity.getX(), entity.getY(), direction, physics.getVelocityX());
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