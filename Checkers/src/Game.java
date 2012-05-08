
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.applet.*;

public class Game extends Applet implements ActionListener {
    JButton yourTurn;
    JButton initiate;
    JButton terminate;
    JButton updatePlayerList;
    JButton invitePlayer;
    JButton acceptInvite;
    JButton declineInvite;
    JButton resign;
    JButton getBoard;
    JTextField invitor;
    JComboBox playerlist;
    JComboBox hosts;
    JComboBox handle;
    JLabel invitor_label;
    Board board;

    public void init() {
        setLayout(new BorderLayout());
        
        add("Center", board = new Board());

        JPanel localPanel = new JPanel();
        localPanel.setLayout(new GridLayout(24, 1));
        localPanel.add(this.handle = new JComboBox());
        localPanel.add(this.hosts = new JComboBox());
        localPanel.add(this.initiate = new JButton("         Start         "));
        localPanel.add(this.terminate = new JButton("Stop"));
        localPanel.add(this.yourTurn = new JButton("     "));
        localPanel.add(new JLabel(""));
        localPanel.add(this.updatePlayerList = new JButton("Update"));
        localPanel.add(this.playerlist = new JComboBox());
        localPanel.add(this.invitePlayer = new JButton("Invite"));
        localPanel.add(new JLabel(""));
        localPanel.add(this.invitor_label = new JLabel("Invitor", 0));
        localPanel.add(this.invitor = new JTextField());
        localPanel.add(this.acceptInvite = new JButton("Accept"));
        localPanel.add(this.declineInvite = new JButton("Decline"));
        localPanel.add(new JLabel(""));
        localPanel.add(this.resign = new JButton("Resign"));
        localPanel.add(this.getBoard = new JButton("Get Board"));
        add("East", localPanel);
        
        setSize(785,640);
        this.yourTurn.addActionListener(this);
        this.yourTurn.setBackground(Color.red);

        this.initiate.addActionListener(this);
        this.initiate.setEnabled(true);

        this.terminate.addActionListener(this);
        this.terminate.setEnabled(false);

        this.acceptInvite.setEnabled(false);
        this.acceptInvite.addActionListener(this);

        this.declineInvite.setEnabled(false);
        this.declineInvite.addActionListener(this);

        this.invitor.setEnabled(false);
        this.invitor_label.setEnabled(false);
        this.invitor.setEditable(false);
        this.invitor.setFont(new Font("TimesRoman", 1, 16));

        this.resign.addActionListener(this);
        this.getBoard.addActionListener(this);
        this.updatePlayerList.addActionListener(this);
        this.invitePlayer.addActionListener(this);
        
        this.hosts.setEditable(true);
        this.hosts.addItem("localhost");
        this.hosts.addItem("gauss.ececs.uc.edu");
        this.hosts.addItem("helios.ececs.uc.edu");
        this.handle.addItem("player_1");
        this.handle.addItem("player_2");
        this.handle.addItem("player_3");
        this.handle.addItem("player_4");
        this.handle.addItem("franco");
        addMouseListener(this.board);
        addMouseMotionListener(this.board);
        
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

        
        board.setImage();
        board.paint(board.getGraphics());
        
    }

    public void actionPerformed(ActionEvent evt) {
        if ( evt.getSource() == initiate ) {
            
        } else if ( evt.getSource() == terminate ) {    
        
        } else if ( evt.getSource() == updatePlayerList ) {
            
        } else if ( evt.getSource() == invitePlayer ) {
            
        } else if ( evt.getSource() == acceptInvite ) {
            
        } else if ( evt.getSource() == declineInvite ) {
            
        } else if ( evt.getSource() == resign ) {
            
        } else if ( evt.getSource() == getBoard ) {
            
        }
    }
}
