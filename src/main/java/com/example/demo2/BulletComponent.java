package com.example.demo2;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class BulletComponent extends Component {

    private final int dir;
    private final double speed;
    private final double lifeTime = 10.0;
    private double time = 0;
    private PhysicsComponent physics;
    public BulletComponent(int dir, double speed) {
        this.dir = dir;
        this.speed = speed;
    }

    @Override
    public void onAdded() {
        entity.getComponent(PhysicsComponent.class)
                .setOnPhysicsInitialized(() -> {
                    entity.getComponent(PhysicsComponent.class)
                            .setVelocityX(dir * speed);
                });
    }

    @Override
    public void onUpdate(double tpf) {
        time += tpf;
        if (time > lifeTime) {
            entity.removeFromWorld();
        }
        physics.applyForceToCenter(new Point2D(0, -490));
    }
}
