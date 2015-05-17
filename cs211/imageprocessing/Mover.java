package imageprocessing;

import processing.core.*;

import java.util.ArrayList;

/* Mover class. An environnement where a ball rolls on a board and collides with cylinders */
class Mover {
    private PApplet parent;

    final float gravityConstant = 0.2f;
    final float cylinderRadius = 25;
    final float cylinderHeight = -50;
    final float cylinderResolution = 40;
    final float ballRadius;

    PVector location;
    PVector velocity;
    PVector gravity;

    float board_length;
    float board_height;
    float board_width;

    Ball ball;
    Board board;
    Cylinder placementCylinder;
    ArrayList<Cylinder> cylinders;
    ArrayList<PVector> cylinderPositions;

    float score;
    float lastScore;
    
    boolean addingCylinderMode = false;

    
    Mover(PApplet parent, float ball_radius, float board_length, float board_height, float board_width) {
        this.parent = parent;
        
        location = new PVector(0, 0, 0);
        velocity = new PVector(0, 0, 0);
        gravity = new PVector(0,0,0);

        this.board_length = board_length;
        this.board_height = board_height;
        this.board_width = board_width;

        ball = new Ball(parent, ball_radius);
        board = new Board(parent, board_width, board_height, board_length);
        placementCylinder = new Cylinder(parent, cylinderRadius, cylinderHeight, cylinderResolution);

        cylinders = new ArrayList<Cylinder>();
        cylinderPositions = new ArrayList<PVector>();

        ballRadius = ball_radius;
        score = 0;
        lastScore = 0;
    }

    void setAddingCylinderMode(boolean b) {
        addingCylinderMode = b;
    }

    void update(float rotX, float rotZ) {
        if(!addingCylinderMode) {
            gravity.x = (float)Math.sin(rotZ) * gravityConstant;
            gravity.z = (float)-Math.sin(rotX) * gravityConstant;

            float normalForce = 1;
            float mu = 0.02f;
            float frictionMagnitude = normalForce * mu;
            PVector friction = velocity.get();
            friction.mult(-1);
            friction.normalize();
            friction.mult(frictionMagnitude);

            velocity.add(gravity);
            velocity.add(friction);

            checkEdges();
            checkCylinderCollision();
            location.add(velocity);
        }
    }

    void placeCylinder(float x, float z) {
        if(addingCylinderMode) {
            // Display board first
            display();

            parent.translate(x, -board_height/2, z);
            placementCylinder.display();
        }
    }

    void display() {
        board.display();

        // Place all cylinders and draw it
        for(int i = 0; i < cylinderPositions.size(); i++) {
            Cylinder cylinder = cylinders.get(i);
            PVector position = cylinderPositions.get(i);

            parent.pushMatrix();
            parent.translate(position.x, -board_height/2, position.z);
            cylinder.display();
            parent.popMatrix();
        }

        // Draw the ball
        parent.pushMatrix();
        parent.translate(location.x, -board_height/2-ball.getRadius(), location.z);
        ball.display();
        parent.popMatrix();
    }

    void checkEdges() {
        float ballRadius = ball.getRadius();

        if(location.x > board_width/2-ballRadius) {
            location.x = board_width/2-ballRadius;
            velocity.x *= -1;
            lastScore = -velocity.mag();
            score += lastScore;
        }
        else if(location.x < -board_width/2+ballRadius) {
            location.x = -board_width/2+ballRadius;
            velocity.x *= -1;
            lastScore = -velocity.mag();
            score += lastScore;
        }
        if(location.z > board_length/2-ballRadius) {
            location.z = board_length/2-ballRadius;
            velocity.z *= -1;
            lastScore = -velocity.mag();
            score += lastScore;
        }
        else if(location.z < -board_length/2+ballRadius) {
            location.z = -board_length/2+ballRadius;
            velocity.z *= -1;
            lastScore = -velocity.mag();
            score += lastScore;
        }
    }

    void checkCylinderCollision() {
        // Check the position of all cylinders prior to the ball
        for(PVector cylinderPosition : cylinderPositions) {
            float dist = PVector.dist(cylinderPosition, location);

            //collision
            if(dist < cylinderRadius+ballRadius) {
                PVector n = PVector.sub(cylinderPosition, location);
                n.normalize();
                n.mult(2 * velocity.dot(n));
                velocity = PVector.sub(velocity, n);
                location.add(velocity);
                lastScore = velocity.mag();
                score += lastScore;
            }
        }
    }

    void addCylinder(float x, float z) {
        if(addingCylinderMode) {
            // Distance between the center of the ball and the cylinder
            float dist = PVector.dist(new PVector(x, 0, z), location);
            // If the cylinder doesn't touch the ball, we can place it.
            if(dist > cylinderRadius + ballRadius) {
                cylinders.add(new Cylinder(parent, cylinderRadius, cylinderHeight, cylinderResolution));
                cylinderPositions.add(new PVector(x, 0, z));
            }
        }
    }
}