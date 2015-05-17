package imageprocessing;

import processing.core.*;

import java.util.ArrayList;

/* Mover class. An environnement where a ball rolls on a board and collides with Towers */
class Mover {
    private PApplet parent;

    final float gravityConstant = 0.2f;
    final float TowerRadius = 25;
    final float TowerHeight = -50;
    final float TowerResolution = 40;
    final float ballRadius;

    PVector location;
    PVector velocity;
    PVector gravity;

    float board_length;
    float board_height;
    float board_width;

    Ball ball;
    Board board;
    Tower placementTower;
    ArrayList<Tower> towers;
    ArrayList<PVector> towerPositions;

<<<<<<< HEAD:cs211/imageprocessing/Mover.java
    float score;
    float lastScore;
    
    boolean addingCylinderMode = false;
=======
    boolean addingTowerMode = false;
>>>>>>> origin/week8:src/cs11/Mover.java

    
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
        placementTower = new Tower(parent);

        towers = new ArrayList<Tower>();
        towerPositions = new ArrayList<PVector>();

        ballRadius = ball_radius;
        score = 0;
        lastScore = 0;
    }

    void setAddingTowerMode(boolean b) {
        addingTowerMode = b;
    }

    void update(float rotX, float rotZ) {
        if(!addingTowerMode) {
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
            checkTowerCollision();
            location.add(velocity);
        }
    }

    void placeTower(float x, float z) {
        if(addingTowerMode) {
            // Display board first
            display();

            parent.translate(x, -board_height/2, z);
            placementTower.display();
        }
    }

    void display() {
        board.display();

        // Place all Towers and draw it
        for(int i = 0; i < towerPositions.size(); i++) {
            Tower tower = towers.get(i);
            PVector position = towerPositions.get(i);

            parent.pushMatrix();
            parent.translate(position.x, -board_height/2, position.z);
            tower.display();
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

    void checkTowerCollision() {
        // Check the position of all Towers prior to the ball
        for(PVector TowerPosition : towerPositions) {
            float dist = PVector.dist(TowerPosition, location);

            //collision
            if(dist < TowerRadius+ballRadius) {
                PVector n = PVector.sub(TowerPosition, location);
                n.normalize();
                n.mult(2 * velocity.dot(n));
                velocity = PVector.sub(velocity, n);
                location.add(velocity);
                lastScore = velocity.mag();
                score += lastScore;
            }
        }
    }

    void addTower(float x, float z) {
        if(addingTowerMode) {
            // Distance between the center of the ball and the Tower
            float dist = PVector.dist(new PVector(x, 0, z), location);
            // If the Tower doesn't touch the ball, we can place it.
            if(dist > TowerRadius + ballRadius) {
                towers.add(new Tower(parent));
                towerPositions.add(new PVector(x, 0, z));
            }
        }
    }
}