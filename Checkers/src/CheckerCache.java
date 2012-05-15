public class CheckerCache {
    Checkers checkerHead = null;
    Checkers checkerTail = null;

    public void add(CheckersPiece checker) {

    }

    public void draw() {
        Checkers currChecker;
        for ( currChecker = checkerHead; currChecker != null; currChecker = currChecker.next ) {
            currChecker.checker.draw();
        }
    }
}
