import java.net.*;
import java.util.*;
import java.lang.*;
import java.io.*;

class CheckersScore {
   public int wins;
   public int losses;
   public int draws;

   public CheckersScore() {
      wins = 0;
      losses = 0;
      draws = 0;
   }
}

public class CheckersScoreboard extends Thread {
   private Map <String, CheckersScore> scores;
   private boolean keepRunning;
   private Thread webserver;
   
   public static final int SCORE_PORT = 8181;
   
   public CheckersScoreboard() {
      // Set up the storage
      scores = Collections.synchronizedMap(new HashMap <String,CheckersScore> ());
      
      // Kick off the webserver
      webserver = new Thread(this);
      webserver.start();
   }
   
   public void run() {
      
      // Grab the current date
      Date startDate = new Date();
      
      // Set up a listener
      ServerSocket listener = null;
      try {
         listener = new ServerSocket(SCORE_PORT);
      } catch (IOException e) { }
      
      // Keep it going
      keepRunning = true;
      
      while (keepRunning == true) {
         // Wait for requests
         Socket newConn = null;
         
         try {
            newConn = listener.accept();
         } catch (IOException e) {
            continue;
         }
         
         // Spit out the current stats as HTML
         OutputStream outs = null;
         try {
            outs = newConn.getOutputStream();
         } catch (IOException e) {
         }
         
         DataOutputStream scoreOut = new DataOutputStream(outs);
         
         try {
            // Header
            scoreOut.writeBytes("HTTP/1.0 200 OK\r\n");
            scoreOut.writeBytes("Server: CheckersServer\r\n");
            scoreOut.writeBytes("Connection: close\r\n");
            scoreOut.writeBytes("Content-Type: text/html\r\n\r\n");
            scoreOut.writeBytes("<HTML>\n");
            scoreOut.writeBytes("<HEAD><TITLE>Checkers Scores</TITLE></HEAD>\n");
            scoreOut.writeBytes("<center>\n");
            scoreOut.writeBytes("<font color=\"#0000bb\" size=+2><b>Checkers Tournament</b></font>\n");
            scoreOut.writeBytes("<br>\n");
            scoreOut.writeBytes("<font color=\"#bb0000\"><b>Current scores starting from " + startDate + "</b></font>\n");
            scoreOut.writeBytes("</center>\n");
            scoreOut.writeBytes("<p>\n");
				scoreOut.writeBytes("<div style=\"margin:0em 4em;\">\n");
            scoreOut.writeBytes("<TABLE BORDER=1 ALIGN=CENTER>\n");
            scoreOut.writeBytes("<TR><TH><font color=\"#0000bb\">Handle</font></TH><TH><font color=\"#0000bb\">&nbsp;&nbsp;&nbsp;Wins&nbsp;&nbsp;&nbsp;</font></TH><TH><font color=\"#0000bb\">&nbsp;&nbsp;&nbsp;Losses&nbsp;&nbsp;&nbsp;</font></TH><TH><font color=\"#0000bb\">&nbsp;&nbsp;&nbsp;Draws&nbsp;&nbsp;&nbsp;</font></TH></TR>\n");
            
            // Get the scores
            Set <String> keySet = scores.keySet();
            
            if (keySet.isEmpty() == false) {
               
               String[] keys = new String[1];
               keys = (String [])keySet.toArray(keys);
               for (int ii = 0; ii < keys.length; ii++) {
                  CheckersScore curScore = (CheckersScore) scores.get(keys[ii]);
                  scoreOut.writeBytes("<TR><TD>&nbsp;&nbsp;&nbsp;" + keys[ii] + "&nbsp;&nbsp;&nbsp;");
                  scoreOut.writeBytes("</TD><TD align=center>" + curScore.wins);
                  scoreOut.writeBytes("</TD><TD align=center>" + curScore.losses);
                  scoreOut.writeBytes("</TD><TD align=center>" + curScore.draws);
                  scoreOut.writeBytes("</TD></TR>\n");
               }
            }
            
            // Footer
            scoreOut.writeBytes("</TABLE>\n");
				scoreOut.writeBytes("</div>\n");
            scoreOut.writeBytes("</BODY>\n");
            scoreOut.writeBytes("</HTML>\n");
            
            newConn.close();                        
         } catch (IOException e) {
         }
      }
   }
   
   public void nuke() {
      keepRunning = false;
   }
   
   public void gameWonBy(String handle) {
      if (scores.containsKey(handle)) {
         // Yank the scores and add to it
         CheckersScore curScore = (CheckersScore) scores.get(handle);
         
         curScore.wins++;
      } else {
         // Create a new score
         CheckersScore curScore = new CheckersScore();
         curScore.wins++;
         scores.put(handle, curScore);
      }
   }
   
   public void gameLostBy(String handle) {
      if (scores.containsKey(handle)) {
         // Yank the scores and add to it
         CheckersScore curScore = (CheckersScore) scores.get(handle);
         
         curScore.losses++;
      } else {
         // Create a new score
         CheckersScore curScore = new CheckersScore();
         curScore.losses++;
         scores.put(handle, curScore);
      }
   }
   
   public void gameDrewBy(String handle) {
      if (scores.containsKey(handle)) {
         // Yank the scores and add to it
         CheckersScore curScore = (CheckersScore) scores.get(handle);
         
         curScore.draws++;
      } else {
         // Create a new score
         CheckersScore curScore = new CheckersScore();
         curScore.draws++;
         scores.put(handle, curScore);
      }
   }
}
