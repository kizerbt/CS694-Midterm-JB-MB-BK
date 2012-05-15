public class Player {
    CheckerCache checkers;
    Board board;

    public Player(Board board) {
        this.board = board;
        checkers = new CheckerCache();
    }

    // displays all the checkers associated with this player.
    public void draw() {
        checkers.draw();
    }

    public void move(int x0, int y0, int x1, int y1) {
        System.out.println("(" + x0 + ", " + y0 + ") -> (" + x1 + ", " + y1 + ")");
    }
}
