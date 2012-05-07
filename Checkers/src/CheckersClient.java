import java.net.*;
import java.io.*;
import java.util.*;

public class CheckersClient extends Thread {
   private Socket clientSocket;
   private CheckersServer clientServer;
   private BufferedReader clientInput;
   private DataOutputStream clientOutput;

   private boolean keepRunning;
   private String clientHandle;
   private CheckersGame clientGame;

   private boolean inviteResponse;  // Major backfill kludge since code
   private boolean gotAccepted;     // working on earlier versions of java
                                    // does not work on 1.5.0 (synchronize)

   public CheckersClient(CheckersServer serv, Socket s) {
      keepRunning = true;
      clientHandle = null;
      clientGame = null;
      
      clientSocket = s;
      clientServer = serv;
      
      // To allow the server to send things asynchronously, we need
      // to have reads on the socket timeout.  We set this at 100ms
      // as that should be plenty of time to see if something has
      // arrived
      /*******************************************************************/
      /****** JVF - Had to remove this because messages ******************/
      /**** were being eaten up when doing incomingCommand.readLine() ****/
      /*******************************************************************/
      //try { clientSocket.setSoTimeout(100); } catch (SocketException e) { }
      /*******************************************************************/
      /****** JVF - Had to remove this because messages ******************/
      /**** were being eaten up when doing incomingCommand.readLine() ****/
      /*******************************************************************/
      
      // Grab the streams
      InputStream ins = null;
      OutputStream outs = null;
      try {
	 ins = clientSocket.getInputStream();
	 outs = clientSocket.getOutputStream();
      } catch (IOException e) { }

      // Setup the wrappers to make communications easier
      clientInput = new BufferedReader(new InputStreamReader(ins));
      clientOutput = new DataOutputStream(outs);
      
      System.out.println(this + "Created new client");
   }

   public String getHandle() {	return clientHandle; }

   public boolean isAvailable() {
      // We are available if we are registered and not in a game
      return (clientGame == null && clientHandle != null ? true : false);
   }

   // The listening thread for this client
   public void run() {
      while (keepRunning == true) {
	 // Continuously poll for new commands from the client
	 handleIncomingCommand();
      }
      // The only way we get here is if the client was nuked
   }

   // JVF - I pulled synchronized out until further notice (3/15/06)
   public /* synchronized */ boolean handleIncomingCommand() {
      try {
	 // Attempt to get a command
	 String incomingCommand = clientInput.readLine();
	 System.out.println("Incoming:["+incomingCommand+"]");

	 // If the command is null, something is wrong
	 // We ignore it and try again, the heartbeat is the
	 // final say on whether someone is alive, not us
	 if (incomingCommand == null) {  return false;  }

	 // So we can figure out how many tokens were sent
	 String[] tokens = incomingCommand.split(" ");

	 System.out.println(this + "Got command: [" + incomingCommand+"]");
	 
	 // Figure out which command it was and do the right thing
	 if (incomingCommand.startsWith("register") && tokens.length == 2) {
	    // If we have not already registered and the name is available,
	    // take the name, otherwise tell them it is in use.
	    if (clientHandle == null && tokens[1].indexOf('|') >= 0 &&
		clientServer.getClientWithHandle("Gotcha_Adam") == null) {
	       clientHandle = "Gotcha_Adam";
	       clientOutput.writeBytes(clientHandle + 
				       " registered on machine " + 
				       clientSocket.getInetAddress() + 
				       " at " + clientSocket.getPort() + "\n");
	       System.out.println(this + "registration successful");
	    } else if (clientHandle == null && 
		clientServer.getClientWithHandle(tokens[1]) == null) {
	       clientHandle = tokens[1];
	       clientOutput.writeBytes(clientHandle + 
				       " registered on machine " + 
				       clientSocket.getInetAddress() + 
				       " at " + clientSocket.getPort() + "\n");
	       System.out.println(this + "registration successful");
	    } else {
	       clientOutput.writeBytes(tokens[1] + " is in use\n");
	       System.out.println(this + "registration failed");
	    }
	 } else if (clientHandle != null) {
	    if (incomingCommand.startsWith("unregister") && 
		tokens.length == 1) {
	       // Must have a handle and not be in a game
	       if (clientGame == null) {
		  System.out.println(this + "unregister successful");
		  clientOutput.writeBytes(clientHandle + 
					  " unregistered from machine " + 
					  clientSocket.getInetAddress() + 
					  " at " + clientSocket.getPort() + 
					  "\n");
		  clientHandle = null;
	       } else {
		  System.out.println(this + "unregister failed");
		  clientOutput.writeBytes("fail\n");
		  sendCommand("reset\n");
	       }
	    } else if (incomingCommand.startsWith("getclients") && 
		       tokens.length == 1) {
	       clientOutput.writeBytes(clientServer.getClients());
	    } else if (incomingCommand.startsWith("getboard") && 
		       tokens.length == 1) {
	       // If we have a game, give the locations of the pieces
	       // If not, give an empty list
	       if (clientGame != null) {
		  clientOutput.writeBytes(clientGame.getBoard());
	       } else {
		  clientOutput.writeBytes("\n");
	       }
	    } else if (incomingCommand.startsWith("movestart") && 
		       tokens.length == 3) {
	       int horizPos = 0;
	       int vertPos = 0;

	       try {
		  horizPos = Integer.parseInt(tokens[1]);
		  vertPos = Integer.parseInt(tokens[2]);
	       } catch (NumberFormatException e) {
		  System.out.println(this + "invalid command");
		  clientOutput.writeBytes("invalid command\n");
		  return false;
	       }

	       if (clientGame != null && 
		   clientGame.startMove(this, horizPos, vertPos)) {
		  clientOutput.writeBytes("ok\n");
		  System.out.println(this + "move start successful");
	       } else {
		  clientOutput.writeBytes("fail\n");
		  sendCommand("reset\n");
		  System.out.println(this + "move start failed");
	       }
	    } else if (incomingCommand.startsWith("movepos") && 
		       tokens.length == 3) {
	       int horizPos = 0;
	       int vertPos = 0;
	       
	       try {
		  horizPos = Integer.parseInt(tokens[1]);
		  vertPos = Integer.parseInt(tokens[2]);
	       } catch (NumberFormatException e) {
		  System.out.println(this + "invalid command");
		  clientOutput.writeBytes("invalid command\n");
		  return false;
	       }
	       
	       if (clientGame != null) {
		  clientGame.sendMovement(this, horizPos, vertPos);
	       }
	    } else if (incomingCommand.startsWith("moveend") && 
		       tokens.length == 3) {
	       int horizPos = 0;
	       int vertPos = 0;

	       try {
		  horizPos = Integer.parseInt(tokens[1]);
		  vertPos = Integer.parseInt(tokens[2]);
	       } catch (NumberFormatException e) {
		  System.out.println(this + "invalid command");
		  clientOutput.writeBytes("invalid command\n");
		  return false;
	       }
	       
	       if (clientGame != null) {
		  if (clientGame.endMove(this, horizPos, vertPos)) {
		     System.out.println(this + "end move successful");
		  } else {
		     System.out.println(this + "end move failed");
		  }
	       }
	    } else if (incomingCommand.startsWith("invitation")) {
	       if (tokens[2].equals("accepted")) gotAccepted = true;
	       else gotAccepted = false;
	       inviteResponse = true;
	    } else if (incomingCommand.startsWith("invite") && 
		       tokens.length == 2) {
	       CheckersClient invitee;

	       // Ignore invites while in a game
	       if (clientGame == null) {
		  // Find the client associated with the handle
		  invitee = clientServer.getClientWithHandle(tokens[1]);
		  
		  if (invitee == null || invitee == this) {
		     // Invitee is not registered or it is ourthis
		     System.out.println(this + "attempted to invite self");
		     clientOutput.writeBytes("invitation " + tokens[1] + 
					     " declined\n");
		  } else if (!invitee.isAvailable()) {
		     // Invitee is unvailable
		     System.out.println(this + tokens[1] + " unavailable");
		     clientOutput.writeBytes(tokens[1] + " unavailable\n");
		  } else { 
		     // Game on!
		     clientGame = new CheckersGame(this,invitee);
		     
		     // See if they wish to play
		     if (invitee.inviteToGame(clientGame)) {
			System.out.println(this + "invite to " + tokens[1] + 
					   " accepted");
			clientOutput.writeBytes("invitation " + tokens[1] + 
						" accepted\n");
			clientGame.startGame();
		     } else {
			clientGame = null;
			System.out.println(this + "invite to " + tokens[1] + 
					   " declined");
			clientOutput.writeBytes("invitation " + tokens[1] + 
						" declined\n");
		     }
		  }
	       }
	    } else if (incomingCommand.startsWith("resign") && 
		       tokens.length == 1) {
	       if (clientGame != null) {
		  System.out.println(this + "resigned from the game");
		  clientGame.resign(this);
	       }
	    } else {
	       System.out.println(this + "invalid command");
	       clientOutput.writeBytes("invalid command\n");
	    }
	 } else {
	    System.out.println(this + "command sent while unregistered");
	    clientOutput.writeBytes("you must register first\n");
	 }
      } catch(SocketTimeoutException e) {
	 // This is normal
      } catch(IOException e) {
	 // We ignore it and try again, the heartbeat is the
	 // final say on if someone is alive, not us
      }
      return true;
   }

   // JVF - I pulled synchronized out until further notice (3/15/06)
   // Doubtful it will be needed here - there is only one readLine
   // now and it is located in "handleIncomingCommand()"
   public /* synchronized */ boolean inviteToGame(CheckersGame game) {
      boolean ret = false;
      boolean gotResponse = false;
      // String inviteResponse = null;
      
      // Send the invite command to the invitee
      try {
	 System.out.println(this + "invited to game by " + 
			    game.getOpponent(this).getHandle());
	 clientOutput.writeBytes("invite " + 
			    game.getOpponent(this).getHandle() + "\n");
	 
      } catch (IOException e) {
	 System.out.println(e);
      }

      // Wait for the response to the invitation and act accordingly
      // when it arrives - must check once a second to see if the 
      // response has arrived - handled by "handleIncomingCommand()"
      inviteResponse = false;
      while (!inviteResponse) {
	 try { Thread.sleep(1000); } catch (Exception e) {}
      }
      if (gotAccepted) {
	 clientGame = game;
	 System.out.println(this + "accepted invitation");
      } else {
	 clientGame = null;
	 System.out.println(this + "declined invitation");
      }
      return gotAccepted;
   }

   // This is a simple wrapper so the CheckersGame can send
   // things to the client and respect the synchronization needed.
   // JVF - I pulled synchronized out until further notice (3/15/06)
   public /* synchronized */ void sendCommand(String command) {
      try {
	 System.out.print(this + "Command sent to client: " + command);
	 clientOutput.writeBytes(command);
      } catch (IOException e) {
      }
   }

   // JVF - I pulled synchronized out until further notice (3/15/06)
   public /* synchronized */ boolean checkClient() {
      try {
	 clientOutput.write((new Byte((byte)10)).byteValue());
	 clientOutput.write((new Byte((byte)85)).byteValue());
	 clientOutput.write((new Byte((byte)10)).byteValue());
      } catch (IOException e) {
	 return false;
      }

      return true;
   }

   public void nuke() {
      System.out.println(this + "Removing client from system");

      // End any game in progress
      if (clientGame != null) {
	 clientGame.resign(this);
	 clientGame = null;
      }
      
      // Stop the handler thread
      keepRunning = false;
      
      // Remove ourself from the server
      clientServer.removeClient(this);
   }

   public void nukeGame() {
      clientGame = null;
   }

   public void gameLost() {
      sendCommand("gameend Lost\n");
      clientServer.gameLostBy(clientHandle);
      nukeGame();
   }

   public void gameWon() {
      sendCommand("gameend Won\n");
      clientServer.gameWonBy(clientHandle);
      nukeGame();
   }

   public void gameDraw() {
      sendCommand("gameend Draw\n");
      clientServer.gameDrewBy(clientHandle);
      nukeGame();
   }
   
   public String toString() {
      // Returns [IP:Port Handle Date]
      return new String("[" + clientSocket.getInetAddress() + ":" + 
			clientSocket.getPort() + " " + clientHandle + " " + 
			new Date() + "] ");
   }
}
