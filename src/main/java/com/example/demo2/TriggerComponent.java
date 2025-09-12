package com.example.demo2;

import com.almasb.fxgl.entity.Entity;
import com.almasb.fxgl.entity.component.Component;

public class TriggerComponent extends Component {

    private Entity trapEntity;

    public TriggerComponent(Entity trapEntity) {
        this.trapEntity = trapEntity;
    }

    public void trigger() {
        if (trapEntity != null && trapEntity.isActive()) {
            trapEntity.getComponent(TrapComponent.class).activate();
        }
    }
}

