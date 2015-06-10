import java.util.ArrayList;
import java.util.List;

import processing.core.PApplet;
import processing.core.PGraphics;

public class BarChart {
	final PGraphics barChart;
	final Mover mover;
	private int blockSize = 10;
	private int timeInterval = 1000;
	private int scoreInterval = 10;
	private final List<Float> scores;
	private long lastTime;

	public BarChart(PGraphics barChart, Mover mover) {
		this.barChart = barChart;
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
		barChart.translate(10, 0);
		barChart.fill(255,0,0);
		for(float score : scores) {
			int height = height = (int) (score/scoreInterval);;
			while(blockSize > 0 && height * (blockSize + blockSize/2) > barChart.height) {
				blockSize -= Math.max(1, blockSize/4);
				height = (int) (score/scoreInterval);
			}
			for(int i = 0; i < height; i++){
				barChart.rect(0, (blockSize + blockSize/2)*i, blockSize, blockSize);
			}
			barChart.translate(blockSize + blockSize/2, 0);
		}
		barChart.endDraw();
		parent.image(barChart, 0, 0);
	}


}
