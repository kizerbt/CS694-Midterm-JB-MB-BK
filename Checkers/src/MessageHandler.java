
import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class MessageHandler extends Thread {

    boolean connected;
    BufferedReader in;
    PrintWriter out;
    boolean expectingPlayerList;
    boolean expectingBoard;
    boolean expectingInviteResponse;
    boolean expectingMoveEnd;
    boolean moveSuccessful;
    boolean canUnregister;
    String gameResult;
    Game game;
    boolean myTurn;
    int opponentPiece;
    boolean opponentColor;
    boolean opponentIsKing;

    public MessageHandler(Game game) {
        connected = true;
        this.game = game;
        canUnregister = false;
        gameResult = null;
        moveSuccessful = false;
        opponentPiece = -1;
    }

    public void run() {
        try {
            boolean loginSucceeded = false;
            Socket s = new Socket((String)this.game.hosts.getSelectedItem(), 8180);
            in = new BufferedReader(new InputStreamReader(s.getInputStream()));
            out = new PrintWriter(s.getOutputStream(), true);
            
            out.println("register " + this.game.handle.getSelectedItem());
            System.out.println("register " + this.game.handle.getSelectedItem());

            while (connected) {
                String serverMessage = in.readLine();
                if (!serverMessage.equals("")
                        && !serverMessage.equals("U")) {
                    System.out.println("From server: " + serverMessage);
                    if (loginSucceeded) {
                        if (expectingPlayerList) {
                            getPlayerList(serverMessage);
                        } else if (expectingBoard) {
                            getBoard(serverMessage);
                        } else if (expectingInviteResponse) {
                            getInviteResponse(serverMessage);
                        } else if (expectingMoveEnd) {
                            getMoveEndStatus(serverMessage);
                        }
                        StringTokenizer st = new StringTokenizer(serverMessage, " ");
                        //System.out.println(serverMessage);
                        if (st.hasMoreTokens()) {
                            String command = st.nextToken();
                            if (command.equals("invite")) {
                                this.showInvitation(st.nextToken());                            
                            } else if (command.equals("oppmovestart")) {
                                oppmovestart(st);
                            } else if (command.equals("oppmovepos")) {
                                oppmovepos(st);
                            } else if (command.equals("oppmove")) {
                                oppmove(st);
                            } else if (command.equals("yourturn")) {
                                yourturn(st);
                            } else if (command.equals("reset")) {
                                opponentReset();
                            } else if (command.equals("gameend")) {
                                boolean winner;
                                gameResult = st.nextToken();
                                String message = "You have " + gameResult + "!";
                                
                                canUnregister = true;
                                this.game.getBoard.setEnabled(false);
                                this.game.resign.setEnabled(false);
                                this.game.invitePlayer.setEnabled(true);
                                this.game.invitor.setText("");
                                this.game.yourTurn.setText("");
                                
                                
                                JOptionPane.showMessageDialog(game, message);
                            } else if (command.equals("remove")) {
                                removeChecker(st);
                            } else if (command.equals("king")) {
                                kingCommand(st);
                            }
                        }
                        
                    } else {
                        if(serverMessage.contains("is in use")){
                            loginSucceeded = false;
                            this.connected = false;
                            this.game.start.setEnabled(true);
                            this.game.terminate.setEnabled(false);
                            this.game.handle.setEnabled(true);
                            this.game.hosts.setEnabled(true);
                            JOptionPane.showMessageDialog(game, serverMessage);
                        } else if (serverMessage.contains("registered on machine")) {
                            loginSucceeded = true;
                            this.connected = true;
                            this.game.start.setEnabled(false);
                            this.game.terminate.setEnabled(true);
                            this.game.handle.setEnabled(false);
                            this.game.hosts.setEnabled(false); 
                            this.game.updatePlayerList.setEnabled(true);
                            this.game.invitePlayer.setEnabled(true);
                            this.game.updatePlayerList();
                        }
                    }
                }
            }
            
            s.close();
        } catch (IOException ex) {
            Logger.getLogger(MessageHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void yourturn(StringTokenizer stringTokenizer) {
        System.out.println("It's our turn");
        game.board.myturn = true;

        game.yourTurn.setBackground((this.game.isRed) ? Color.red : Color.black);
        game.yourTurn.setForeground((this.game.isRed) ? Color.black : Color.white);
        
        game.yourTurn.setText("Your Turn");
    }

    private void getMoveEndStatus(String serverMessage) {
        if (serverMessage.equals("fail")) {
            moveSuccessful = false;
        } else {
            moveSuccessful = true;
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
        int numTokens = st.countTokens();
        this.game.board.removeAllCheckers();
        System.out.println("numTokens= " + numTokens);
        for (int i = 0; i < numTokens; i++) {
            String piece = st.nextToken();
            System.out.println(piece);
            String[] checkerArgs = piece.split(" ");
            Boolean isMe = checkerArgs[3].equals(this.game.handle.getSelectedItem());
            
            CheckersPiece checker = new CheckersPiece(Integer.parseInt(checkerArgs[1]),
                                                      Integer.parseInt(checkerArgs[0]),
                                                      checkerArgs[2].equals("King"),
                                                      ((isMe) ? this.game.isRed : !this.game.isRed));
            
                this.game.board.addChecker(checker.getHorizPos(), checker.getVertPos(), checker.isKing(), checker.getIsRed());
            
            
            }
        
        this.game.board.repaint();
        this.expectingBoard = false;
    
    }

    private void getInviteResponse(String serverMessage) {
        StringTokenizer st = new StringTokenizer(serverMessage, " ");
        if (st.nextToken().equals("invitation")) {
            // make sure response came from the player that we invited
            if (st.nextToken().equals(((String)game.playerList.getSelectedItem()).split(" ")[0])) {
                String response = st.nextToken();
                if (response.equals("accepted")) {
                    this.game.terminate.setEnabled(false);
                    this.game.resign.setEnabled(true);
                    this.game.invitePlayer.setEnabled(false);
                    this.game.getBoard.setEnabled(true);
                    
                    // TODO: SETUP GAME HERE ISH
                    
                } else if (response.equals("declined")) {
                    JOptionPane.showMessageDialog(game, 
                            game.playerList.getSelectedItem() + " has declined your invitation");
                    
                    this.game.invitePlayer.setEnabled(true);
                }
            }

            expectingInviteResponse = false;
        }
    }

    public void getPlayerList() {
        if (expectingPlayerList) {
            return;
        }
        expectingPlayerList = true;
        game.playerList.removeAllItems();
        
        this.out.println("getclients");
    }

    public void invite(StringTokenizer st) {
        game.acceptInvite.setEnabled(true);
        game.declineInvite.setEnabled(true);
        game.invitor.setText(st.nextToken());
    }

    public void opponentReset() {
        opponentPiece = -1;
        game.getBoard();
    }

    public void oppmovestart(StringTokenizer st) {
        int hPos = Integer.parseInt(st.nextToken());
        int vPos = Integer.parseInt(st.nextToken());
        for ( int i = 0; i < game.board.checkers.size(); i++ ) {
            CheckersPiece checker = game.board.checkers.get(i);
            int x = checker.getHorizPos();
            int y = checker.getVertPos();
            if (hPos == x && vPos == y) {
                opponentPiece = i;
                opponentColor = game.board.checkers.get(i).getIsRed();
                opponentIsKing = game.board.checkers.get(i).isKing();
                game.board.checkers.add(0, game.board.checkers.remove(opponentPiece));
            }
        }
        System.out.println("piece at " + hPos + ", " + vPos + " (" + opponentPiece + ") is moving");
        game.board.paint(game.board.getGraphics());
    }

    public void oppmovepos(StringTokenizer st) {
        if ( opponentPiece >= 0 ) {
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            CheckersPiece cp = new CheckersPiece(x, y, opponentIsKing, opponentColor);
            game.board.checkers.setElementAt(cp, 0);
        }
        game.board.paint(game.board.getGraphics());
    }

    public void oppmove(StringTokenizer st) {
        int initHoriz = Integer.parseInt(st.nextToken());
        int initVert = Integer.parseInt(st.nextToken());
        int finalHoriz = Integer.parseInt(st.nextToken());
        int finalVert = Integer.parseInt(st.nextToken());
        
        CheckersPiece cp = game.board.checkers.get(0); //this.game.board.getChecker(initHoriz, initVert);
        
        cp.setHorizPos(finalHoriz);
        cp.setVertPos(finalVert);
        
        game.board.paint(game.board.getGraphics());
    }

    public void terminate() {
        connected = false;
        this.out.println("unregister");
    }

    public void removeChecker(StringTokenizer st) {
        int xpos, ypos;
        boolean isKing, isMe, checkerIsRed;
        xpos = Integer.parseInt(st.nextToken());
        ypos = Integer.parseInt(st.nextToken());
        isKing = st.nextToken().equals("King");
        isMe = st.nextToken().equals(this.game.handle.getSelectedItem());
        checkerIsRed = (isMe) ? this.game.isRed : !this.game.isRed;
        CheckersPiece cp = this.game.board.getChecker(xpos, ypos);
        
        if ( cp != null ) {
            if ( cp.isKing() == isKing && cp.getIsRed() == checkerIsRed) {
                this.game.board.checkers.remove(cp);
            }
        }
        
        game.board.repaint();
    }

    public void kingCommand(StringTokenizer st) {
        int xpos, ypos;
        xpos = Integer.parseInt(st.nextToken());
        ypos = Integer.parseInt(st.nextToken());
        game.board.kingMe(xpos, ypos);
    }

    public void invitePlayer(String player) {
        this.out.println("invite " + player);
        this.expectingInviteResponse = true;
    }

    private void showInvitation(String player) {
        this.game.invitor_label.setEnabled(true);
        this.game.declineInvite.setEnabled(true);
        this.game.acceptInvite.setEnabled(true);
        this.game.invitor.setText(player);
    }

    public void acceptInvite(String player) {
        this.out.println("invitation " + player + " accepted");
    }

    public void declineInvite(String player) {
        this.out.println("invitation " + player + " declined");
    }
    
    public void resign() {
        this.out.println("resign");
    }

    void sendGetBoard() {
        this.out.println("getboard");
    }
}
