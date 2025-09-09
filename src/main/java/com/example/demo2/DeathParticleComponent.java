package com.example.demo2;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;

public class DeathParticleComponent extends Component {

    private double lifeTime = 1.5; // 粒子存活时间（秒）
    private double time = 0;

    @Override
    public void onAdded() {
        entity.getComponent(PhysicsComponent.class)
                .setOnPhysicsInitialized(() -> {
                    double angle = Math.random() * 360;
                    double speed = 150 + Math.random() * 150;

                    double vx = Math.cos(Math.toRadians(angle)) * speed;
                    double vy = Math.sin(Math.toRadians(angle)) * speed;

                    entity.getComponent(PhysicsComponent.class).setVelocityX(vx);
                    entity.getComponent(PhysicsComponent.class).setVelocityY(vy);
                });
    }
    @Override
    public void onUpdate(double tpf) {
        time += tpf;
        if (time > lifeTime) {
            entity.removeFromWorld();
        }
    }
}
