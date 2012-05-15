public class CheckersPiece {
   private int horizPos;
   private int vertPos;
   private boolean isKing;
   private CheckersClient owner;
   private boolean isRed;

   public CheckersPiece(int hPos, int vPos, boolean king, boolean red) {
      horizPos = hPos;
      vertPos = vPos;
      isKing = king;
      isRed = red;
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
      return this.owner;
   }

   public boolean getIsRed() {
       return this.isRed;
   }
   public void draw() {
       System.out.println("Drawing a checker");
   }
}
