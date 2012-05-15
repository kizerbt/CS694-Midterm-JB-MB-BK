public class Checkers {
    public CheckersPiece checker;
    public Checkers next;
    public Checkers prev;

    public Checkers(CheckersPiece checker) {
        this.checker = checker;
        next = null;
        prev = null;

    }
}
