import processing.core.PApplet;

/**
 * Created by sydney on 11.05.15.
 */
/* A simple class representing a board */
class Board {
    private PApplet parent;

    private final float board_width;
    private final float board_length;
    private final float board_height;

    Board(PApplet parent, float board_width, float board_height, float board_length) {
        this.parent = parent;

        this.board_width = board_width;
        this.board_length = board_length;
        this.board_height = board_height;
    }

    void display() {
        parent.stroke(30);
        parent.fill(255, 213 , 0);
        parent.box(board_length, board_height, board_width);
    }
}
