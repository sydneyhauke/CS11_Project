package cs11;

import processing.core.PApplet;
import processing.core.PGraphics;

public class BarChart {
	final PGraphics barChart;
	final Mover mover;
	final int block_size = 10;
	
	public BarChart(PGraphics barChart, Mover mover) {
		this.barChart = barChart;
		this.mover = mover;
	}
	
	public void display(PApplet parent){
    	barChart.beginDraw();
    	barChart.noStroke();
    	barChart.background(238,235,201);
    	
    	
    	barChart.endDraw();
    	parent.image(barChart, 0, 0);
	}
	
	
}
