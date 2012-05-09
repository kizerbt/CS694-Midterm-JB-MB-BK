
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;


public class MessageHandler extends Thread {
    boolean connected;
    BufferedReader in;
    PrintStream out;
    boolean expectingPlayerList;
    boolean canUnregister;
    String gameResult;
    Game game;
    boolean myTurn;
    
    public MessageHandler(Game game) {
        connected = true;
        this.game = game;
        canUnregister = false;
        gameResult = null;
    }
    
    public void getPlayerList() {
        if ( expectingPlayerList ) {
            return;
        }
        expectingPlayerList = true;
        game.playerList.removeAllItems();
    }
    
    public void invite(StringTokenizer st) {
        game.acceptInvite.setEnabled(true);
        game.declineInvite.setEnabled(true);
        game.invitor.setText(st.nextToken());
    }
    
    public void yourTurn(StringTokenizer st) {
        myTurn = true;
    }
    
    public void oppmovepos(StringTokenizer st) {
        
    }
    
    public void oppmove(StringTokenizer st) {
        
    }
    
    public void terminate() {
        // TODO: send "unregister" to server
        connected = false;
    }
    
    public void removeChecker(StringTokenizer st) {
        int xpos, ypos;
        xpos = Integer.parseInt(st.nextToken());
        ypos = Integer.parseInt(st.nextToken());
        game.board.player1.removeChecker(xpos, ypos);
        game.board.player2.removeChecker(xpos, ypos);
        game.board.paint(game.board.getGraphics());
    }
    
    public void kingCommand(StringTokenizer st) {
        int xpos, ypos;
        xpos = Integer.parseInt(st.nextToken());
        ypos = Integer.parseInt(st.nextToken());
        game.board.kingMe(xpos, ypos);
    }
    
    public void run() {
        try {
            while ( connected ) {
                String serverMessage = in.readLine();
                
                if ( expectingPlayerList ) {
                    // parse the player list response
                    StringTokenizer st = new StringTokenizer(serverMessage, "|");
                    while ( st.countTokens() > 0 ) {
                        game.playerList.addItem(st.nextToken());
                    }
                    expectingPlayerList = false;
                }
                StringTokenizer st = new StringTokenizer(serverMessage, " ");
                String command = st.nextToken();
                if ( command.equals("invite") ) {
                    
                } else if ( command.equals("oppmovestart") ) {
                    
                } else if ( command.equals("oppmove") ) {
                    
                } else if ( command.equals( "yourturn") ) {
                    
                } else if ( command.equals("gameend") ) {
                    gameResult = st.nextToken();
                    canUnregister = true;
                } else if ( command.equals("remove") ) {
                    removeChecker(st);
                } else if ( command.equals("king") ) {
                    kingCommand(st);
                }
            }
        } catch (IOException ex) {
                Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
