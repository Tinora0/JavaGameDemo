package com.example.demo2;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.entity.component.Required;
import com.almasb.fxgl.entity.components.CollidableComponent;

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
    }

    @Override
    public void onUpdate(double tpf) {
        FXGL.getGameWorld().getEntitiesByType(EntityType.PLAYER).forEach(player -> {
            if (player.isColliding(entity)) {
                FXGL.setLevelFromMap(targetLevel);
                player.setPosition(spawnX, spawnY);
            }
        });
    }
}
