public class CheckersPiece {
   private int horizPos;
   private int vertPos;
   private boolean isKing;
   private CheckersClient owner;
   
   public CheckersPiece(int hPos, int vPos, boolean king, CheckersClient o) {
      horizPos = hPos;
      vertPos = vPos;
      isKing = king;
      owner = o;
   }

   public int getHorizPos() {
      return horizPos;
   }

   public void setHorizPos(int hPos) {
      horizPos = hPos;
   }

   public int getVertPos() {
      return vertPos;
   }

   public void setVertPos(int vPos) {
      vertPos = vPos;
   };
   
   public boolean isKing() {
      return isKing;
   }

   public void setIsKing(boolean king) {
      isKing = king;
   }

   public CheckersClient getOwner() {
      return owner;
   }

   public String toString() {
      return (horizPos-32) + " " + (vertPos-32) + " " + 
	 (isKing ? "King" : "Normal") + " " + owner.getHandle();
   }
}
