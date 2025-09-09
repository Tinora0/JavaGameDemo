package com.example.demo2;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;

public class BulletComponent extends Component {

    private final int dir;
    private final double speed;

    public BulletComponent(int dir, double speed) {
        this.dir = dir;
        this.speed = speed;
    }

    @Override
    public void onAdded() {
        // 给刚体一个初速度
        entity.getComponent(PhysicsComponent.class)
                .setVelocityX(dir * speed);
    }

    @Override
    public void onUpdate(double tpf) {
        // 如果子弹飞出视野，则移除
        double x = entity.getX();
        double w = FXGL.getAppWidth();
        if (x < -50 || x > w + 50) {
            entity.removeFromWorld();
        }
    }
}
