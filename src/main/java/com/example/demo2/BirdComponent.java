// BirdComponent.java
package com.example.demo2;

import com.almasb.fxgl.dsl.FXGL;
import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.texture.AnimatedTexture;
import com.almasb.fxgl.texture.AnimationChannel;
import javafx.scene.image.Image;
import javafx.util.Duration;
import java.util.Arrays;
import java.util.List;

public class BirdComponent extends EnemyComponent {

    private final AnimatedTexture texture;
    private boolean isFacingRight = true;

    public BirdComponent() {
        // 加载两张单独的鸟图片
        Image frame1 = FXGL.image("bird1.png"); // 第一张图片
        Image frame2 = FXGL.image("bird2.png"); // 第二张图片

        // 将图片放入列表中
        List<Image> frames = Arrays.asList(frame1, frame2);

        // 初始化动画通道 - 使用两张单独的图片
        AnimationChannel animFly = new AnimationChannel(frames, Duration.seconds(0.5));

        // 创建动画纹理
        texture = new AnimatedTexture(animFly);
        texture.loop();
    }

    @Override
    public void onAdded() {
        super.onAdded();

        // 将动画纹理添加到实体的视图组件
        entity.getViewComponent().addChild(texture);

        // 设置初始朝向
        updateTextureDirection();
    }

    @Override
    public void onUpdate(double tpf) {
        super.onUpdate(tpf);

        // 更新朝向
        boolean shouldFaceRight = getDirection() > 0;
        if (shouldFaceRight != isFacingRight) {
            isFacingRight = shouldFaceRight;
            updateTextureDirection();
        }
    }

    private void updateTextureDirection() {
        if (isFacingRight) {
            texture.setScaleX(1);  // 正常朝向（右）
        } else {
            texture.setScaleX(-1); // 翻转朝向（左）
        }
    }

    @Override
    public void reset() {
        super.reset();
        // 确保动画继续播放
        texture.loop();
    }
}