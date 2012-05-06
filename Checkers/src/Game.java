
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;

class Position {                                           // 3

    int x, y;                                               // 3
    boolean type;  // true = challenger, false = challengee

    public Position(int x, int y, boolean t) {             // 3
        this.x = x;
        this.y = y;
        this.type = t;              // 3
    }                                                       // 3
}                                                          // 3

class Board extends Canvas implements MouseMotionListener, MouseListener {

    Image board;                                             // 1
    Image bgimage;                                           // 1
    Graphics bggraphics;                                     // 1
    static final long serialVersionUID = 21L;                // 1
    Image red_checker;                                       // 2
    Image blk_checker;
    int moving;                                              // 3
    int xdiff, ydiff;                                        // 4
    Vector<Position> checkers;                               // 5
    boolean removing;                                        // 6
    int over;                                                // 6
    boolean moving_type;
    GameFrame app;

    Board(GameFrame app) {
        // 1
        Toolkit tk = (Toolkit.getDefaultToolkit());        // 1
        board = tk.getImage(".\\src\\images\\board.jpg");           // 1
        red_checker = tk.getImage(".\\src\\images\\red_checker.gif"); // 2
        blk_checker = tk.getImage(".\\src\\images\\black_checker.gif");                // 1

        setSize(640, 640);                                     // 1
        setVisible(true);                                     // 1

        moving = -1;                                          // 3
        addMouseListener(this);                               // 3
        addMouseMotionListener(this);                         // 3

        checkers = new Vector<Position>();                    // 5
        removing = false;                                     // 6

        this.app = app;

        moving_type = true;
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
            int x = ((Position) checkers.get(i)).x;             // 5
            int y = ((Position) checkers.get(i)).y;             // 5
            if (((Position) checkers.get(i)).type) {
                bggraphics.drawImage(blk_checker, x, y, this);
            } else {
                bggraphics.drawImage(red_checker, x, y, this);     // 2
            }
        }
        g.drawImage(bgimage, 0, 0, this);                     // 1
    }

    public int areWeOverAChecker(MouseEvent evt) {           // 3
        for (int i = 0; i < checkers.size(); i++) {
            int x = ((Position) checkers.get(i)).x;
            int y = ((Position) checkers.get(i)).y;
            if (x < evt.getX() && evt.getX() < x + 64 && // 3
                    y < evt.getY() && evt.getY() < y + 64) {
                return i; // 3
            }
        }
        return -1;
    }

    public void mousePressed(MouseEvent evt) {              // 3
        if ((moving = areWeOverAChecker(evt)) < 0) {
            return;    // 3
        }
        if (removing) {
            checkers.removeElementAt(moving);
            paint(this.getGraphics());
        } else {
            moving_type = ((Position) checkers.get(moving)).type;
            xdiff = ((Position) checkers.get(moving)).x - evt.getX();  // 4
            ydiff = ((Position) checkers.get(moving)).y - evt.getY();  // 4
            checkers.add(0, checkers.remove(moving));
        }
    }

    public void mouseDragged(MouseEvent evt) {              // 3
        if (moving >= 0) {                                    // 3 
            int x = evt.getX() + xdiff;                        // 3
            int y = evt.getY() + ydiff;                        // 3
            Position p = new Position(x, y, moving_type);      // 3
            checkers.setElementAt(p, 0);                       // 3
            paint(this.getGraphics());                         // 3
        }
    }

    public void mouseReleased(MouseEvent evt) {
        moving = -1;
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
        Position p = new Position(x, y, ((Position) checkers.get(over)).type);
        if (!removing) {
            checkers.setElementAt(p, over);
        }
        paint(this.getGraphics());
    }                                                        // 3

    public void mouseEntered(MouseEvent evt) {
    }            // 3

    public void mouseExited(MouseEvent evt) {
    }             // 3

    public void mouseMoved(MouseEvent evt) {
    }              // 3

    public void addChecker(int i, int j, boolean t) {       // 5
        removing = false;                                     // 5
        checkers.add(new Position(80 * j + 8, 80 * i + 8, t));       // 5
        paint(this.getGraphics());                            // 5
    }                                                        // 5

    public void removeChecker() {                           // 6
        if (removing) {                                       // 6
            removing = false;                                  // 6
            app.remove.setText("Remove Checker");              // 6
        } else {                                              // 6
            removing = true;                                   // 6
            app.remove.setText("Stop Removing");               // 6
        }                                                     // 6
    }                                                        // 6
}

class GameFrame extends JFrame implements ActionListener { // 1

    Board board;                                             // 1
    static final long serialVersionUID = 23L;                // 1
    JButton remove;

    public GameFrame() {                                   // 1
        setLayout(new BorderLayout());                        // 1
        setBackground(new Color(255, 255, 224));                // 1

        add("Center", board = new Board(this));               // 1

        JPanel p = new JPanel();                              // 5
        p.setLayout(new FlowLayout());                        // 5
        p.setBackground(new Color(255, 255, 224));              // 5
        p.add(remove = new JButton("Remove Checker"));        // 5
        add("South", p);

        // button.addActionListener(this);                    // 5
        remove.addActionListener(this);

        board.addChecker(0, 1, true);
        board.addChecker(0, 3, true);
        board.addChecker(0, 5, true);
        board.addChecker(0, 7, true);
        board.addChecker(1, 0, true);
        board.addChecker(1, 2, true);
        board.addChecker(1, 4, true);
        board.addChecker(1, 6, true);
        board.addChecker(2, 1, true);
        board.addChecker(2, 3, true);
        board.addChecker(2, 5, true);
        board.addChecker(2, 7, true);

        board.addChecker(5, 0, false);
        board.addChecker(5, 2, false);
        board.addChecker(5, 4, false);
        board.addChecker(5, 6, false);
        board.addChecker(6, 1, false);
        board.addChecker(6, 3, false);
        board.addChecker(6, 5, false);
        board.addChecker(6, 7, false);
        board.addChecker(7, 0, false);
        board.addChecker(7, 2, false);
        board.addChecker(7, 4, false);
        board.addChecker(7, 6, false);
    }                                                        // 1

    public void paint(Graphics g) {                         // 1
        board.paint(board.getGraphics());                     // 1
        remove.paint(remove.getGraphics());
    }                                                        // 1

    public void actionPerformed(ActionEvent evt) {
        if (evt.getSource() == remove) {
            board.removeChecker();
        }
        // else board.addChecker();                           // 5
    }

    public void start() {
        board.setImage();
    }
}

public class Game extends Applet implements ActionListener {

    JButton go;

    public void init() {
        setLayout(new BorderLayout());
        add("Center", go = new JButton("Applet"));
        go.addActionListener(this);
    }

    public void actionPerformed(ActionEvent evt) {
        GameFrame rb4 = new GameFrame();
        rb4.setSize(650, 710);
        rb4.setVisible(true);
        rb4.start();
    }
}
