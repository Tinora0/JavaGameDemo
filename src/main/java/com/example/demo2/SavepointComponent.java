package com.example.demo2;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.Texture;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGLForKtKt.getGameTimer;

public class SavepointComponent extends Component {

    private final Texture inactiveTex;
    private final Texture activeTex;

    public SavepointComponent(Texture inactiveTex, Texture activeTex) {
        this.inactiveTex = inactiveTex;
        this.activeTex = activeTex;
        activeTex.setVisible(false); // 初始为未激活状态
    }

    public void activate(Point2D playerPos) {
        // 切换贴图
        inactiveTex.setVisible(false);
        activeTex.setVisible(true);

        // 设置玩家重生点
        ((IWBTCSerApp) FXGL.getApp()).setRespawnPoint(playerPos);


        // 2 秒后恢复为未激活状态
        getGameTimer().runOnceAfter(() -> {
            activeTex.setVisible(false);
            inactiveTex.setVisible(true);
        }, Duration.seconds(2));
    }
}


