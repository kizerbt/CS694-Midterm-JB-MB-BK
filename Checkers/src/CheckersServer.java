import java.net.*;
import java.io.*;
import java.util.*;

public class CheckersServer extends Thread {
   public static int LISTEN_PORT = 8180;
   
   private List <CheckersClient> clientList;
   private CheckersScoreboard scoreboard;
   private boolean threadShouldRun;
   
   public static void main(String argv[]) {
      CheckersServer server = new CheckersServer();
      
      server.mainThread(argv);
   }

   public void mainThread(String argv[]) {
      ServerSocket s = null;
      clientList = Collections.synchronizedList(new ArrayList <CheckersClient> ());
      scoreboard = new CheckersScoreboard();
      
      // Open the listening port
      try {
         s = new ServerSocket(LISTEN_PORT);
      } catch (IOException e) {
         System.out.println("Server threw: " + e);
         System.exit(-1);
      }
      
      // Kick off the heartbeat thread
      threadShouldRun = true;
      Thread heartbeat = new Thread(this);
      heartbeat.start();
      
      while (true) {
         Socket newConn = null;
         
         // Accept any new connection
         try {
            newConn = s.accept();
         } catch(IOException e) {
            System.out.println("Server threw: " + e);
            continue;
         }
         
         // Create a client object to handle this new connection
         CheckersClient newClient = new CheckersClient(this, newConn);
         // Add the client to the list
         clientList.add(newClient);
         // Start the thread to handle this client
         newClient.start();
      }
   }
   
   // Heartbeat thread
   public void run() {
      while (threadShouldRun == true) {
         // Loop over each client and hit it's heartbeat method
         CheckersClient curClient;
         
         try {
            for (int ii = 0; ii < clientList.size(); ) {
               curClient = (CheckersClient)clientList.get(ii);
               
               // If the client fails to respond, remove it
               if (curClient.checkClient() == false) {
                  curClient.nuke();
               } else {
                  // When a client is removed, the indexes shift
                  // Thus, we only increment if it responds
                  ii++;
               }
               
            }
            
            try {
               Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
         } catch (Exception e2) {
            System.err.println("Heartbeat exception handled:");
            e2.printStackTrace();
         }
      }
   }
   
   public CheckersClient getClientWithHandle(String handle) {
      CheckersClient ret = null;
      
      for (int ii = 0; ii < clientList.size(); ii++) {
         CheckersClient curClient = (CheckersClient)clientList.get(ii);
         if (curClient.getHandle() != null && 
             curClient.getHandle().equals(handle)) {
            ret = curClient;
            break;
         }
      }
      
      return ret;
   }

   public String getClients() {
      String ret = new String();
      
      for (int ii = 0; ii < clientList.size(); ii++) {
         CheckersClient curClient = (CheckersClient)clientList.get(ii);
         if (curClient.getHandle() != null) {
            ret += curClient.getHandle() + " " + 
               (curClient.isAvailable() ? "Yes" : "No") + "|";
         }
      }
      
      return ret + "\n";
   }
   
   public void removeClient(CheckersClient theClient) {
      clientList.remove(theClient);
   }
   
   public void gameWonBy(String handle) {
      scoreboard.gameWonBy(handle);
   }
   
   public void gameLostBy(String handle) {
      scoreboard.gameLostBy(handle);
   }
   
   public void gameDrewBy(String handle) {
      scoreboard.gameDrewBy(handle);
   }
}
