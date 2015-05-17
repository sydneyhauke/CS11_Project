package cs11;

import java.awt.Color;
import java.util.ArrayList;

import processing.core.PGraphics;
import processing.core.PVector;

public class Data {

	private final PGraphics parent;
    private final float BOARDWIDTH;
    private final float BOARDLENGTH;
    private final float BALL_RADIUS;
    private final Mover mover;
    private final int squareLength = 130;
    private final float ratio;
    
    public Data(PGraphics parent, float BOARDWIDTH, float BOARDLENGTH, float BALL_RADIUS, Mover mover) {
		this.parent = parent;
		this.mover = mover;
		this.BALL_RADIUS = BALL_RADIUS;
		this.BOARDWIDTH = BOARDWIDTH;
		this.BOARDLENGTH = BOARDLENGTH;
		this.ratio = squareLength/BOARDLENGTH;
    }
    
    void display() {
        parent.beginDraw();
        parent.background(229,225,174);
        parent.noStroke();

        //background
        parent.fill(255,255,255);
        parent.rect(10, 10, squareLength, squareLength);
        
        //cylinders
        parent.fill(0,0,255);
        for(PVector v : mover.cylinderPositions){
        	float x = (v.x + BOARDWIDTH/2) * ratio;
        	float z = (v.z + BOARDLENGTH/2) * ratio;
        	parent.ellipse(10+x, 10+z, mover.cylinderRadius * ratio*2, mover.cylinderRadius * ratio*2);
        }
        
        parent.fill(255,0,0);
        //ball
        float ballX = (mover.location.x + BOARDWIDTH/2) * ratio;
        float ballZ = (mover.location.z + BOARDLENGTH/2) * ratio;
    	parent.ellipse(10+ballX, 10+ballZ, BALL_RADIUS * ratio * 2, BALL_RADIUS * ratio * 2);
    	
        parent.endDraw();
    }
    
    
    
}
