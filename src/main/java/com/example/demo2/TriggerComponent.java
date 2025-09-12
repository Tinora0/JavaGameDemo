package com.example.demo2;

import com.almasb.fxgl.entity.component.Component;

import static com.almasb.fxgl.dsl.FXGL.getGameWorld;

public class TriggerComponent extends Component {

    private String targetId;

    public TriggerComponent(String targetId) {
        this.targetId = targetId;
    }

    public void trigger() {
        getGameWorld().getEntitiesByComponent(TrapComponent.class).stream()
                .filter(e -> targetId.equals(e.getString("trapId")))
                .forEach(e -> e.getComponent(TrapComponent.class).activate());
    }
}
