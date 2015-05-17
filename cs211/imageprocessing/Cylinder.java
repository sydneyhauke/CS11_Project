package imageprocessing;

import processing.core.*;

/**
 * Created by sydney on 11.05.15.
 */
/* A class representing a cylinder and draws it */
class Cylinder {
    private PApplet parent;

    PShape openCylinder = new PShape();
    PShape topCylinder = new PShape();
    PShape bottomCylinder = new PShape();

    Cylinder(PApplet parent, float cylinderBaseSize, float cylinderHeight, float cylinderResolution) {
        this.parent = parent;
<<<<<<< HEAD:cs211/imageprocessing/Cylinder.java
    	
    	float angle;
=======

        float angle;
>>>>>>> origin/week8:src/cs11/Cylinder.java
        float[] x = new float[(int)cylinderResolution + 1];
        float[] z = new float[(int)cylinderResolution + 1];

        //get the x and y position on a circle for all the sides
        for(int i = 0; i < x.length; i++) {
            angle = ((float)(2*Math.PI) / cylinderResolution) * i;
            x[i] = (float)Math.sin(angle) * cylinderBaseSize;
            z[i] = (float)Math.cos(angle) * cylinderBaseSize;
        }

        openCylinder = parent.createShape();
        openCylinder.beginShape(PApplet.QUAD_STRIP);
        //draw the border of the cylinder
        for(int i = 0; i < x.length; i++) {
            openCylinder.vertex(x[i], 0 , z[i]);
            openCylinder.vertex(x[i], cylinderHeight, z[i]);
        }
        openCylinder.endShape();

        bottomCylinder = parent.createShape();
        bottomCylinder.beginShape(PApplet.TRIANGLE_FAN);
        //draw the bottom of the cylinder
        bottomCylinder.vertex(0, 0, 0);
        for(int i = 0; i < x.length; i++) {
            bottomCylinder.vertex(x[i], 0, z[i]);
        }
        bottomCylinder.endShape();

        topCylinder = parent.createShape();
        topCylinder.beginShape(PApplet.TRIANGLE_FAN);
        //draw the top of the cylinder
        topCylinder.vertex(0,cylinderHeight,0);
        for(int i = 0; i < x.length; i++) {
            topCylinder.vertex(x[i], cylinderHeight, z[i]);
        }
        topCylinder.endShape();
    }

    void display() {
        parent.noStroke();
        parent.fill(59, 20, 175);
        parent.shape(openCylinder);
        parent.shape(topCylinder);
        parent.shape(bottomCylinder);
    }
}