package cs11;

import processing.core.PApplet;
import processing.core.PShape;

/**
 * Created by sydney on 11.05.15.
 */
public class Tower {
    private PApplet parent;
    private PShape tower;

    public Tower(PApplet parent) {
        this.parent = parent;
        tower = parent.loadShape("/home/sydney/Documents/Uni/Assignments/VisualComputing/CS11_Project/src/tests/Towerv2.obj");
        tower.scale(1);
    }

    public void display() {
        parent.shape(tower);
    }
}
