import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;

/**
 * Created by sydney on 11.05.15.
 */
public class Tower {
    private PApplet parent;
    private static PShape tower;

    public Tower(PApplet parent) {
        this.parent = parent;
        if(tower == null){
        	tower = parent.loadShape("Towerv2_2.obj");
        	tower.scale(1);
        }
    }

    public void display() {
        parent.pushMatrix();
        parent.rotateX(PConstants.PI);
        parent.rotateY(PConstants.PI/2);
        parent.shape(tower);
        parent.popMatrix();
    }
}
