package com.example.demo2;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.entity.components.CollidableComponent;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.onCollisionBegin;
import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameTimer;

@Required(CollidableComponent.class)
public class PortalComponent extends Component {

    private final String targetLevel;
    private final double spawnX;
    private final double spawnY;

    public PortalComponent(String targetLevel, double spawnX, double spawnY) {
        this.targetLevel = targetLevel;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }


    @Override
    public void onAdded() {
        entity.getComponent(CollidableComponent.class).setValue(true);

        onCollisionBegin(EntityType.PLAYER, EntityType.TP, (player, tp) -> {
            getGameTimer().runOnceAfter(() -> {
                // 1) 先设置下一个关卡的重生坐标
                IWBTCSerApp app = (IWBTCSerApp) FXGL.getApp();
                app.loadLevel(targetLevel, spawnX, spawnY);
            }, Duration.seconds(0));
        });
    }
}