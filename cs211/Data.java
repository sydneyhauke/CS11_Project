import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PVector;

public class Data {
    final PGraphics dataBackground;
    final PGraphics topView;
    final PGraphics scoreboard;
    final BarChart barChart;
    final HScrollbar scrollBar;
    
    private final float BOARDWIDTH;
    private final float BOARDLENGTH;
    private final float BALL_RADIUS;
    private final Mover mover;
    private final int SCORE_SQUARE;
    private final float ratio;
    
    public Data(PGraphics dataBackground, PGraphics topView, PGraphics scoreboard, PGraphics barChart,
    		HScrollbar scrollBar, Mover mover, int SCORE_SQUARE, float BOARDWIDTH, float BOARDLENGTH, float BALL_RADIUS) {
        
    	this.dataBackground = dataBackground;
    	this.topView = topView;
    	this.scoreboard = scoreboard;
    	//barChar is complex, it has his own class
    	this.barChart = new BarChart(barChart, scrollBar, mover); 
    	this.scrollBar = scrollBar;
    	
    	
		this.mover = mover;
		this.BALL_RADIUS = BALL_RADIUS;
		this.BOARDWIDTH = BOARDWIDTH;
		this.BOARDLENGTH = BOARDLENGTH;
		this.SCORE_SQUARE = SCORE_SQUARE;
		this.ratio = SCORE_SQUARE/BOARDLENGTH;
    }
    
    void display(PApplet parent) {
    	parent.pushMatrix();

    	background();
   		parent.image(dataBackground, 0, 0);
        
   		topView();
    	parent.image(topView, 0, 0);
    	parent.translate(20 + SCORE_SQUARE,0);
        
    	score();
    	parent.image(scoreboard, 0, 0);
    	parent.translate(20 + SCORE_SQUARE, 10);
        
    	barChart.display(parent);
    	
    	parent.translate(0, barChart.height() + 6);
    	scrollBar.update();
    	scrollBar.display();
    	//System.out.println(scrollBar.getPos());
    	
    	parent.popMatrix();
    }
    
    private void background(){
    	//background
    	dataBackground.beginDraw();
    	dataBackground.noStroke();
    	dataBackground.background(229,225,174);
        dataBackground.endDraw();
    }
    
    private void topView(){
        //cylinders
        topView.beginDraw();
        topView.noStroke();
        topView.background(229,225,174);
        topView.fill(255,255,255);
        topView.rect(10, 10, SCORE_SQUARE, SCORE_SQUARE);
        topView.fill(0,0,255);
        for(PVector v : mover.getTowerPositions()){
        	float x = (v.x + BOARDWIDTH/2) * ratio;
        	float z = (v.z + BOARDLENGTH/2) * ratio;
        	topView.ellipse(10+x, 10+z, mover.getTowerRadius() * ratio*2, mover.getTowerRadius() * ratio*2);
        }

        //ball
        topView.fill(255,0,0);
        float ballX = (mover.location.x + BOARDWIDTH/2) * ratio;
        float ballZ = (mover.location.z + BOARDLENGTH/2) * ratio;
        topView.ellipse(10+ballX, 10+ballZ, BALL_RADIUS * ratio * 2, BALL_RADIUS * ratio * 2);
    	topView.endDraw();
    }
    
    private void score(){
        scoreboard.beginDraw();
    	scoreboard.stroke(255,255,255);
    	scoreboard.noFill();
    	scoreboard.background(229,225,174);
    	scoreboard.rect(10, 10, SCORE_SQUARE, SCORE_SQUARE);
    	scoreboard.fill(0,0,0);
    	scoreboard.text("Total Score :", 20, 30);
    	scoreboard.text(mover.score, 20, 50);
    	scoreboard.text("velocity :", 20, 70);
    	scoreboard.text(mover.velocity.mag(), 20, 90);
    	scoreboard.text("Last Score :", 20, 110);
    	scoreboard.text(mover.lastScore, 20, 130);
    	scoreboard.endDraw();
    }
}
