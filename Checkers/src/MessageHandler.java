
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
    boolean expectingBoard;
    boolean expectingInviteResponse;
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
    
    public void run() {
        try {
            
            // TODO: open socket connection to port 8180
            // set up socket in/out
            
            while ( connected ) {
                String serverMessage = in.readLine();
                
                if ( expectingPlayerList ) {
                    getPlayerList(serverMessage);
                } else if ( expectingBoard ) {
                    getBoard(serverMessage);
                } else if ( expectingInviteResponse ) {
                    getInviteResponse(serverMessage);
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

    private void getPlayerList(String serverMessage) {
        // parse the player list response
        StringTokenizer st = new StringTokenizer(serverMessage, "|");
        while (st.countTokens() > 0) {
            game.playerList.addItem(st.nextToken());
        }
        expectingPlayerList = false;
    }

    private void getBoard(String serverMessage) {
        StringTokenizer st = new StringTokenizer(serverMessage, "|");
        
        for ( int i = 0; i < st.countTokens(); i++ ) {
            String object = st.nextToken();
            StringTokenizer st2 = new StringTokenizer(object, " ");
            String piece;
            for ( int j = 0; j < st2.countTokens(); j++ ) {
                CheckersPiece checker;
                // TODO: finish making this CheckersPiece object, need to
                // create the CheckersClient as the last argument. Use each 
                // CheckerPiece object to update the board layout after each
                // iteration in this loop or all at once at the end
//                checker = new CheckersPiece(Integer.parseInt(st2.nextToken()),
//                        Integer.parseInt(st2.nextToken()),
//                        Boolean.parseBoolean(st2.nextToken()),
//                        (CheckersClient)st2.nextToken());
            }
        }
        
        expectingBoard = false;
    }

    private void getInviteResponse(String serverMessage) {
        StringTokenizer st = new StringTokenizer(serverMessage, " ");
        if ( st.nextToken().equals("invitation")) {
            // make sure response came from the player that we invited
            if ( st.nextToken().equals(game.playerList.getSelectedItem() )) {
                if ( st.nextToken().equals("accepted") ) {
                    // TODO: set up checkers for opponent (and player1 vs. player2)
                } else if ( st.nextToken().equals("declined") ) {
                    // TODO: print message that invite was declined
                }
            }
            
            expectingInviteResponse = false;
        }
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
}
