package cs11;

import processing.core.PApplet;

/**
 * Created by sydney on 11.05.15.
 */
/* A simple class that draws a ball of radius "radius" */
class Ball {
    private PApplet parent;

    final float radius;

    Ball(PApplet parent, float radius) {
        this.parent = parent;
        this.radius = radius;
    }

    float getRadius() {
        return radius;
    }

    void display() {
        parent.noStroke();
        parent.fill(255, 0, 7);
        parent.sphere(radius);
    }
}
