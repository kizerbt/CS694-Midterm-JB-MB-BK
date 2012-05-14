import java.awt.*;
import java.awt.event.*;
import java.util.*;


class Board extends Canvas implements MouseMotionListener, MouseListener {

    Image board;                                             // 1
    Image bgimage;                                           // 1
    Graphics bggraphics;                                     // 1
    static final long serialVersionUID = 21L;                // 1
    Image red_checker;                                       // 2
    Image blk_checker;
    Image red_king;
    Image blk_king;
    boolean myturn;
    int moving;                                              // 3
    int xdiff, ydiff;                                        // 4
    Vector<CheckersPiece> checkers;                               // 5
    boolean removing;                                        // 6
    int over;                                                // 6
    boolean moving_color; // red =  true, black = false;
    boolean moving_is_king;
    CheckersClient moving_owner;
    Player player_1;
    Player player_2;
    Player me;
    Game game;
    CheckersPiece opponentMove;

    Board(Game game) {
        this.game = game;
        // 1
        Toolkit tk = (Toolkit.getDefaultToolkit());        // 1
        board = tk.getImage("./src/images/board.jpg");           // 1
        red_checker = tk.getImage("./src/images/red_checker.gif"); // 2
        blk_checker = tk.getImage("./src/images/black_checker.gif");                // 1
        red_king = tk.getImage("./src/images/red_king.gif");
        blk_king = tk.getImage("./src/images/black_king.gif");
        setSize(640, 640);                                     // 1
        setVisible(true);                                     // 1

        moving = -1;                                          // 3
        opponentMove = null;
        addMouseListener(this);                               // 3
        addMouseMotionListener(this);                         // 3

        checkers = new Vector<CheckersPiece>();                    // 5
        removing = false;                                     // 6

        moving_color = true;
        moving_is_king = true;
        myturn = false;
    }

    public void setImage() {                                 // 1
        bgimage = createImage(640, 640);                       // 1  Dble buff
        bggraphics = bgimage.getGraphics();                   // 1
    }

    public void paint(Graphics g) {                          // 1
        if (bggraphics == null) {
            return;                       // 1
        }
        bggraphics.drawImage(board, 0, 0, this);                 // 1
        for (int i = checkers.size() - 1; i >= 0; i--) {        // 5
            CheckersPiece checker = checkers.get(i);
            int x = checker.getHorizPos();             // 5
            int y = checker.getVertPos();             // 5
            if (checker.getIsRed()) {
                bggraphics.drawImage((checker.isKing() ? red_king: red_checker), x, y, this);
            }else {
                bggraphics.drawImage((checker.isKing() ? blk_king: blk_checker), x, y, this);
            }
        }
        g.drawImage(bgimage, 0, 0, this);                     // 1
    }

    public int areWeOverAChecker(MouseEvent evt) {           // 3
        for (int i = 0; i < checkers.size(); i++) {
            CheckersPiece checker = checkers.get(i);
            int x = checker.getHorizPos();
            int y = checker.getVertPos();
            if (x < evt.getX() && evt.getX() < x + 64 && // 3
                    y < evt.getY() && evt.getY() < y + 64) {
                return i; // 3
            }
        }
        return -1;
    }

    public void mousePressed(MouseEvent evt) {              // 3
        if ( !myturn ) {
            return;
        }
        if ((moving = areWeOverAChecker(evt)) < 0) {
            return;    // 3
        }
        if (removing) {
            checkers.removeElementAt(moving);
            paint(this.getGraphics());
        } else {
            moving_color = (checkers.get(moving)).getIsRed();
            moving_is_king = (checkers.get(moving)).isKing();
            moving_owner = (checkers.get(moving)).getOwner();
            xdiff = (checkers.get(moving)).getHorizPos() - evt.getX();  // 4
            ydiff = (checkers.get(moving)).getVertPos() - evt.getY();  // 4
            checkers.add(0, checkers.remove(moving));

            game.mh.out.println("movestart " + checkers.get(moving).getHorizPos() + " " + checkers.get(moving).getVertPos());
            game.mh.out.println("movestart " + checkers.get(moving).getHorizPos() + " " + checkers.get(moving).getVertPos());
        }
    }

    public void mouseDragged(MouseEvent evt) {              // 3
        if (moving >= 0) {                                    // 3
            int x = evt.getX() + xdiff;                        // 3
            int y = evt.getY() + ydiff;                        // 3
            CheckersPiece cp = new CheckersPiece(x, y, moving_is_king, moving_owner, moving_color);      // 3
            checkers.setElementAt(cp, 0);                       // 3
            game.mh.out.println("movepos " + x + " " + y);
            paint(this.getGraphics());                         // 3
        }
    }

    public void mouseReleased(MouseEvent evt) {
        if ( moving >= 0 ) {
            int x = evt.getX() + xdiff;
            int y = evt.getY() + ydiff;
            game.yourTurn.setText("");
            if ( player_1 == me ) {
                game.yourTurn.setBackground(Color.black);
            } else {
                game.yourTurn.setBackground(Color.red);
            }
            game.mh.out.println("moveend " + x + " " + y);
            moving = -1;
            myturn = false;
        }
    } // 3

    public void mouseClicked(MouseEvent evt) {              // 3
        if (evt.getX() >= 640 || evt.getX() < 0
                || evt.getY() >= 640 || evt.getY() < 0) {
            return;
        }

        int o = areWeOverAChecker(evt);
        if (o >= 0) {
            over = o;
            return;
        }
        int col = (int) Math.floor(evt.getX() / 80);
        int row = (int) Math.floor(evt.getY() / 80);
        int x = col * 80 + 8;
        int y = row * 80 + 8;
        CheckersPiece cp = new CheckersPiece(x, y, checkers.get(over).isKing(), checkers.get(over).getOwner(), checkers.get(over).getIsRed());
        if (!removing) {
            checkers.setElementAt(cp, over);
        }
        paint(this.getGraphics());
    }                                                        // 3

    public void mouseEntered(MouseEvent evt) {
    }            // 3

    public void mouseExited(MouseEvent evt) {
    }             // 3

    public void mouseMoved(MouseEvent evt) {
    }              // 3

    public void addChecker(int i, int j, boolean isKing, boolean color) {       // 5
        removing = false;                                     // 5
        checkers.add(new CheckersPiece(10 + 80*j, 10 + 80*i, isKing, null, color));       // 5
    }
    
    public void removeChecker(CheckersPiece cp) {
        for(CheckersPiece piece: this.checkers){
            if (piece.equals(cp)) {
                this.checkers.remove(cp);
                return;
            }            
        }
    }
    
    public void removeAllCheckers(){
        this.checkers.removeAllElements();
    }
    
    void kingMe(int xpos, int ypos) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}

