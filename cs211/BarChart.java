import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;

public class BarChart {
	final PGraphics barChart;
	final Mover mover;
	final HScrollbar scrollBar;
	
	private int blockWidth = 20;
	private int blockHeight = 10;
	private int blockSpace = 2;
	private int timeInterval = 1000;
	private int scoreInterval = 10;
	private final List<Float> scores;
	private long lastTime;

	public BarChart(PGraphics barChart, HScrollbar scrollBar, Mover mover) {
		this.barChart = barChart;
		this.scrollBar = scrollBar;
		this.mover = mover;
		this.scores = new ArrayList<>();
		lastTime = System.currentTimeMillis();
	}

	public void display(PApplet parent){
		if(System.currentTimeMillis() - lastTime > timeInterval) {
			scores.add(mover.score);
			lastTime = System.currentTimeMillis();
		}

		barChart.beginDraw();
		barChart.noStroke();
		barChart.background(238,235,201);
//		barChart.translate(10, 0);
		
		
		barChart.fill(255,0,0);
		
		for(float score : scores) {
			int height = height = (int) (score/scoreInterval);
			while(blockHeight > 1 && height * (blockHeight + blockSpace) > barChart.height) {
				blockHeight -= Math.max(1, blockHeight/4);
				height = (int) (score/scoreInterval);
			}
			for(int i = 0; i < height; i++){
				barChart.rect(0,  barChart.height - (blockHeight + blockSpace)*i, blockWidth * scrollBar.getPos(), blockHeight);
			}
			barChart.translate( blockWidth * scrollBar.getPos() + blockSpace, 0);
		}
		barChart.endDraw();
		parent.image(barChart, 0, 0);
	}
	
	public int width(){
		return barChart.width;
	}
	
	public int height(){
		return barChart.height;
	}


}
