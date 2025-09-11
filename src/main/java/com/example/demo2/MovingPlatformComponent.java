package com.example.demo2;

import com.almasb.fxgl.entity.component.Component;
import com.almasb.fxgl.physics.PhysicsComponent;
import javafx.geometry.Point2D;

public class MovingPlatformComponent extends Component {

    private Point2D velocity = Point2D.ZERO;
    private double speed;            // 像素/秒
    private String direction;        // "left" | "right" | "up" | "down"

    public MovingPlatformComponent(double speed, String direction) {
        this.speed = speed;
        this.direction = direction;
    }

    @Override
    public void onAdded() {

        switch (direction) {
            case "left":
                velocity = new Point2D(-speed, 0);
                break;
            case "right":
                velocity = new Point2D(speed, 0);
                break;
            case "up":
                velocity = new Point2D(0, -speed);
                break; // FXGL Y 轴向下为正，这里用向上为负
            case "down":
                velocity = new Point2D(0, speed);
                break;
            default:
                velocity = Point2D.ZERO;
        }

    }

    @Override
    public void onUpdate(double tpf) {
        entity.getComponent(PhysicsComponent.class)
                .setLinearVelocity(velocity);
    }

    public void reverseX() {
        velocity = new Point2D(-velocity.getX(), velocity.getY());
    }

    public void reverseY() {
        velocity = new Point2D(velocity.getX(), -velocity.getY());
    }
}
