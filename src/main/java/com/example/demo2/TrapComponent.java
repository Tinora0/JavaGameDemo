package com.example.demo2;
import com.almasb.fxgl.entity.component.Component;
import javafx.geometry.Point2D;
import javafx.util.Duration;

import static com.almasb.fxgl.dsl.FXGL.animationBuilder;

public class TrapComponent extends Component {

    private String direction;
    private double distance;
    private double speed;

    public TrapComponent(String direction, double distance, double speed) {
        this.direction = direction;
        this.distance = distance;
        this.speed = speed;
    }

    public void activate() {
        double realDistance = distance > 0 ? distance : 800;
        Point2D moveVec = switch (direction) {
            case "up" -> new Point2D(0, -realDistance);
            case "down" -> new Point2D(0, realDistance);
            case "left" -> new Point2D(-realDistance, 0);
            default -> new Point2D(realDistance, 0);
        };

        double duration = realDistance / speed;

        animationBuilder()
                .duration(Duration.seconds(duration))
                .translate(entity)
                .from(entity.getPosition())
                .to(entity.getPosition().add(moveVec))
                .buildAndPlay();
    }
}
