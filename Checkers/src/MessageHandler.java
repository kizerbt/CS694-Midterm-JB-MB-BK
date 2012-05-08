
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
    Game game;
    
    public MessageHandler(Game game) {
        connected = true;
        this.game = game;
    }
    
    public void getPlayerList() {
        if ( expectingPlayerList ) {
            return;
        }
        expectingPlayerList = true;
        game.playerList.removeAllItems();
    }
    
    public void terminate() {
        connected = false;
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
            }
        } catch (IOException ex) {
                Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
