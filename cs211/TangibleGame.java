import processing.core.PApplet;
import processing.core.PGraphics;
import processing.core.PImage;
import processing.core.PVector;
import processing.event.MouseEvent;
import processing.video.Capture;

import java.util.List;
import java.util.Scanner;

/**
 * Created by sydney on 11.05.15.
 */
public class TangibleGame extends PApplet {
    final boolean ImageProcessingTestMode = true;

    final int WINDOW_HEIGHT = 1000;
    final int WINDOW_WIDTH = 1500;

    final int BACKGROUND_HEIGHT = 150;

    final int BOARDWIDTH = 400;
    final int BOARDLENGTH = 400;
    final int BOARDHEIGHT = 20;

    final float ROTY_COEFF = PI/64;
    final float DEFAULT_TILT_COEFF = 0.01f;
    final float MAX_TILT_COEFF = 1.5f*DEFAULT_TILT_COEFF;
    final float MIN_TILT_COEFF = 0.2f*DEFAULT_TILT_COEFF;
    final float TILT_MAX = PI/3;
    final float UP_TILT = -PI/6;

    final float BALL_RADIUS = 25;

    float tilt_coeff = DEFAULT_TILT_COEFF;

    float rotation = 0.0f;
    float tiltX = 0.0f;
    float tiltZ = 0.0f;
    float tiltXBackup = 0.0f;
    float tiltZBackup = 0.0f;
    float rotationBackup = 0.0f;

    Mover mover;

    boolean showAxis = false;
    boolean drawOrigin = false;
    float longueurAxes = 1000;

    boolean addingCylinderMode = false;

    private int SCORE_SQUARE = 130;
    PGraphics dataBackground;
    PGraphics topView;
    PGraphics scoreboard;
    PGraphics barChart;
    Data data;

    Capture cam;
    PImage img;

    ImageProcessing imgProcessor;
    TwoDThreeD corresponder;

    public void setup() {
        size(WINDOW_WIDTH, WINDOW_HEIGHT, P3D);
        noStroke();
        mover = new Mover(this, BALL_RADIUS, BOARDLENGTH, BOARDHEIGHT, BOARDWIDTH);
        
        dataBackground = createGraphics(WINDOW_WIDTH, BACKGROUND_HEIGHT, P2D);
        topView = createGraphics(SCORE_SQUARE + 20, SCORE_SQUARE + 20, P2D);
        scoreboard = createGraphics(SCORE_SQUARE + 20, SCORE_SQUARE + 20, P2D);
        barChart = createGraphics(WINDOW_WIDTH - topView.width - scoreboard.width - 10, SCORE_SQUARE - 20, P2D);
        data = new Data(dataBackground, topView, scoreboard, barChart, mover, SCORE_SQUARE, BOARDWIDTH, BOARDLENGTH, BALL_RADIUS);
        imgProcessor = new ImageProcessing(this);

        String[] cameras = Capture.list();
        if (cameras.length == 0) {
            println("There are no cameras available for capture.");
            exit();
        } else {
            println("Available cameras:");
            for (int i = 0; i < cameras.length; i++) {
                println(i + ": " + cameras[i]);
            }

            println("Choose your camera [1-100] : ");
            Scanner keyboard = new Scanner(System.in);
            int camOpt = keyboard.nextInt();
            cam = new Capture(this, cameras[camOpt]);
            cam.start();
        }

        /*img = loadImage("board1.jpg");
        List<PVector> corners = imgProcessor.process(img, 100, 135, 0, 255, 0, 255);
        corresponder = new TwoDThreeD(img.width, img.height);
        PVector rotations = corresponder.get3DRotations(TwoDThreeD.sortCorners(corners));
        println("rotX = " + rotations.x*(360/(2*PI)) + ", rotY = " + rotations.y*(360/(2*PI)) + ", rotZ = " + rotations.z*(360/(2*PI)));
        noLoop();*/
    }

    public void draw() {
        if(cam.available()) cam.read();
        img = cam.get();


        background(200);
        
        //the default camera of Processing to use for the data info
        camera(width/2.0f, height/2.0f, (float)((height/2.0) / Math.tan(PI*30.0 / 180.0)), width/2.0f, height/2.0f, 0, 0, 1, 0);
        
        pushMatrix();
        translate(0,WINDOW_HEIGHT-BACKGROUND_HEIGHT);
        data.display(this);
        popMatrix();
        
        
        directionalLight(255, 255, 255, 0, 1, -1);
        ambientLight(102, 102, 102);

//        dataBackground.beginDraw();
//        dataBackground.background(255, 255, 200);
//        dataBackground.endDraw();
//        image(dataBackground, 0, WINDOW_HEIGHT-BACKGROUND_HEIGHT);
        
        // Based on which mode we are, camera is placed nearer the board
        if(addingCylinderMode)
            camera(width/2, height/2, 400, width/2, height/2, 0, 0, 1, 0);
        else
            camera(width/2, height/2, 600, width/2, height/2, 0, 0, 1, 0);

        // Place the coordinate system
        translate(width/2, height/2, 0);
        if(!addingCylinderMode) rotateX(tiltX + UP_TILT);
        else rotateX(tiltX);
        rotateZ(tiltZ);
        rotateY(rotation);

        // Optional : show Axis
        if(showAxis) {
            //Axe X
            stroke(0, 255, 0);
            line(-longueurAxes/2, 0, 0, longueurAxes/2, 0, 0);
            //Axe Y
            stroke(255, 0, 0);
            line(0, -longueurAxes/2, 0, 0, longueurAxes/2, 0);
            //Axe Z
            stroke(0,0,255);
            line(0, 0, -longueurAxes/2, 0, 0, longueurAxes/2);
        }

        //If we are in adding cylinder mode, place a cylinder
        if(addingCylinderMode) {
            mover.placeTower(map(mouseX, 0, width, -BOARDLENGTH/2, BOARDLENGTH/2), map(mouseY, 0, height, -BOARDWIDTH/2, BOARDWIDTH/2));
        }
        else {
            // update and display environnement here
            mover.update(tiltX, tiltZ);
            mover.display();
        }
    }

    public void keyPressed() {
        if(key == CODED) {
            if(keyCode == SHIFT && !addingCylinderMode) {
                addingCylinderMode = true;
                mover.setAddingTowerMode(true);
                tiltXBackup = tiltX; // Needed to restore the tilt after adding cylinder(s)
                tiltZBackup = tiltZ; // Same here
                rotationBackup = rotation; // Same here
                rotation = 0; // Clear rotation

                tiltX = -PI/2; // Rotate the board in front of the camera
                tiltZ = 0;
            }
            if(!addingCylinderMode) {
                if(keyCode == LEFT) {
                    rotation += ROTY_COEFF;
                }
                else if(keyCode == RIGHT) {
                    rotation -= ROTY_COEFF;
                }
            }
        }
    }

    public void keyReleased() {
        if(key == CODED) {
            if(keyCode == SHIFT) {
                addingCylinderMode = false;
                mover.setAddingTowerMode(false);
                tiltX = tiltXBackup;
                tiltZ = tiltZBackup;
                rotation = rotationBackup;
            }
        }
    }

    public void mouseDragged() {
        if(!addingCylinderMode) {
            float tiltXIncrement = -tilt_coeff*(mouseY - pmouseY);
            float tiltZIncrement = tilt_coeff*(mouseX - pmouseX);

            if(abs(tiltX + tiltXIncrement) < TILT_MAX)
                tiltX += tiltXIncrement;
            if(abs(tiltZ + tiltZIncrement) < TILT_MAX)
                tiltZ += tiltZIncrement;
        }
    }

    public void mousePressed() {
        mover.addTower(map(mouseX, 0, width, -BOARDLENGTH/2, BOARDLENGTH/2), map(mouseY, 0, height, -BOARDWIDTH/2, BOARDWIDTH/2));
    }

    public void mouseWheel(MouseEvent event) {
        float newTilt = tilt_coeff + -event.getCount()*0.1f*DEFAULT_TILT_COEFF;

        if(newTilt > MIN_TILT_COEFF && newTilt < MAX_TILT_COEFF)
            tilt_coeff = newTilt;
    }
}
