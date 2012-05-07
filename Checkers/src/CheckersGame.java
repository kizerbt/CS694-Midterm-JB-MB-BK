import java.util.*;

public class CheckersGame {
    private CheckersClient player1;
    private CheckersClient player2;
    private CheckersClient curPlayer;
    
    private List <CheckersPiece> pieces;
    private CheckersPiece movePiece;
    private int movePieceHOffset;
    private int movePieceVOffset;
    
    private CheckersPiece doubleJump;
    
    public CheckersGame( CheckersClient p1, CheckersClient p2 ) {
        movePiece = null;
        
        player1 = p1;
        player2 = p2;
        
        // Always assume that p2 is a the opponent
        curPlayer = p2;
    }
    
    public CheckersClient getOpponent( CheckersClient player ) {
        if ( player == player1 ) {
            return player2;
        } else {
            return player1;
        }
    }
    
    public String getBoard() {
        String ret = new String();
        
        // Pieces have a toString that formats things correctly
        for ( int ii = 0; ii < pieces.size(); ii++ ) {
            ret += pieces.get( ii ) + "|";
        }
        
        return ret + "\n";
    }
    
    public void startGame() {
        // Create the board
        pieces = Collections.synchronizedList( new ArrayList <CheckersPiece> () );
        // Player 2
        pieces.add( new CheckersPiece( 120, 40, false, player2 ) );
        pieces.add( new CheckersPiece( 280, 40, false, player2 ) );
        pieces.add( new CheckersPiece( 440, 40, false, player2 ) );
        pieces.add( new CheckersPiece( 600, 40, false, player2 ) );
        pieces.add( new CheckersPiece( 40, 120, false, player2 ) );
        pieces.add( new CheckersPiece( 200, 120, false, player2 ) );
        pieces.add( new CheckersPiece( 360, 120, false, player2 ) );
        pieces.add( new CheckersPiece( 520, 120, false, player2 ) );
        pieces.add( new CheckersPiece( 120, 200, false, player2 ) );
        pieces.add( new CheckersPiece( 280, 200, false, player2 ) );
        pieces.add( new CheckersPiece( 440, 200, false, player2 ) );
        pieces.add( new CheckersPiece( 600, 200, false, player2 ) );
        
        // Player 1
        pieces.add( new CheckersPiece( 40, 440, false, player1 ) );
        pieces.add( new CheckersPiece( 200, 440, false, player1 ) );
        pieces.add( new CheckersPiece( 360, 440, false, player1 ) );
        pieces.add( new CheckersPiece( 520, 440, false, player1 ) );
        pieces.add( new CheckersPiece( 120, 520, false, player1 ) );
        pieces.add( new CheckersPiece( 280, 520, false, player1 ) );
        pieces.add( new CheckersPiece( 440, 520, false, player1 ) );
        pieces.add( new CheckersPiece( 600, 520, false, player1 ) );
        pieces.add( new CheckersPiece( 40, 600, false, player1 ) );
        pieces.add( new CheckersPiece( 200, 600, false, player1 ) );
        pieces.add( new CheckersPiece( 360, 600, false, player1 ) );
        pieces.add( new CheckersPiece( 520, 600, false, player1 ) );
        
        // Clear the doubleJump
        doubleJump = null;
        
        // Tell the current player that it is there turn
        curPlayer.sendCommand( "yourturn " + getBoard() );
    }
    
    public void resign( CheckersClient player ) {
        // Tell them the game is over
        getOpponent( player ).gameWon();
        player.gameLost();
    }
    
    public boolean startMove( CheckersClient player, int horizPos, int vertPos ) {
        CheckersPiece curPiece = null;
        int ii;
        
        if ( movePiece != null ) {
            System.out.println( player + " tried to start a new move without ending the old one" );
            return false;
        }
        
        if ( curPlayer != player ) {
            // It's not their turn!
            System.out.println( player + " tried to move on the other player's turn" );
            return false;
        }
        
        // Figure out which piece we are in the bounds of
        for ( ii = 0; ii < pieces.size(); ii++ ) {
            curPiece = ( CheckersPiece ) pieces.get( ii );
            
            // The piece coordinates are from the center
            // The image size is 64x64.
            if ( curPiece.getHorizPos() - 32 <= horizPos &&
                    horizPos < curPiece.getHorizPos() + 32 &&
                    curPiece.getVertPos() - 32 <= vertPos &&
                    vertPos < curPiece.getVertPos() + 32 ) {
                // The point is withing this pieces bounds;
                break;
            }
        }
        
        if ( doubleJump != null && curPiece != doubleJump ) {
            System.out.println( player + " tried to move a different piece for a double jump" );
            return false;
        }
        
        // As long as they are in a piece that is owned by the player,
        // let them move it
        if ( ii != pieces.size() && curPiece.getOwner() == player ) {
            // Hold onto the piece
            movePiece = curPiece;
            
            // Calculate the position offset
            movePieceHOffset = horizPos - movePiece.getHorizPos();
            movePieceVOffset = vertPos - movePiece.getVertPos();
            
            // Let the opponent know that the move is started
            getOpponent( player ).sendCommand( "oppmovestart " +
                    ( curPiece.getHorizPos() - 32 ) + " " +
                    ( curPiece.getVertPos() - 32 ) + "\n" );
            return true;
        } else {
            if ( ii == pieces.size() ) {
                System.out.println( player + " tried to start a move where there is no piece" );
                System.out.println( player + " current piece status: " +
                        getBoard() );
            } else {
                System.out.println( player + " tried to move a piece they do not own" );
            }
            return false;
        }
    }
    
    public void sendMovement( CheckersClient player, int horizPos, int vertPos ) {
        if ( player == curPlayer && movePiece != null ) {
            getOpponent( player ).sendCommand( "oppmovepos " +
                    ( horizPos - movePieceHOffset - 32 ) +
                    " " +
                    ( vertPos - movePieceVOffset - 32 ) +
                    "\n" );
        }
    }
    
    public boolean endMove( CheckersClient player, int horizPos, int vertPos ) {
        int tempHPos;
        int tempVPos;
        CheckersPiece jumpedPiece = null;
        
        boolean ret = false;
        
        if ( movePiece != null && movePiece.getOwner() == player ) {
            // Figure out where the center should be
            tempHPos = horizPos - movePieceHOffset;
            tempVPos = vertPos - movePieceVOffset;
            
            // Must move to an empty, black space
            if ( isBlackSpace( tempHPos, tempVPos ) &&
                    opponentPieceInSquare( player, tempHPos / 80, tempVPos / 80 ) == null ) {
                // Valid moves:
                // Any piece - forward diagonal, but not when doubleJumping
                if ( doubleJump == null &&
                        isForwardDiagonal( player, tempHPos, tempVPos ) ) {
                    ret = true;
                    // King piece - backward diagonal, but not when doubleJumping
                } else if ( doubleJump == null &&
                        movePiece.isKing() &&
                        isBackwardDiagonal( player, tempHPos, tempVPos ) ) {
                    ret = true;
                    // Normal piece - forward jump (cannot jump own piece)
                } else if ( ( jumpedPiece = isForwardJump( player, tempHPos, tempVPos ) ) != null &&
                        jumpedPiece.getOwner() != player ) {
                    ret = true;
                    // King piece - backward jump (cannot jump own piece)
                } else if ( movePiece.isKing() &&
                        ( jumpedPiece = isBackwardJump( player, tempHPos, tempVPos ) ) != null &&
                        jumpedPiece.getOwner() != player ) {
                    ret = true;
                }
            }
            
            if ( ret == true ) {
                // Acknowledge the move
                player.sendCommand( "ok\n" );
                
                // Inform the opponent
                getOpponent( player ).sendCommand( "oppmove " +
                        ( movePiece.getHorizPos() - 32 ) +
                        " " +
                        ( movePiece.getVertPos() - 32 ) +
                        " " +
                        ( tempHPos - 32 ) + " " +
                        ( tempVPos - 32 ) + "\n" );
                        
                // Move the piece
                movePiece.setHorizPos( tempHPos );
                movePiece.setVertPos( tempVPos );
                
                // If a piece was jumped, handle it
                if ( jumpedPiece != null ) {
                    // Tell both people that the piece has been removed
                    player.sendCommand( "remove " + jumpedPiece + "\n" );
                    getOpponent( player ).sendCommand( "remove " + jumpedPiece + "\n" );
                    
                    // Remove the piece
                    pieces.remove( jumpedPiece );
                }
                
                // If they made it to the end, king them
                if ( isAtOpponentEnd( player, tempHPos, tempVPos ) ) {
                    movePiece.setIsKing( true );
                    
                    player.sendCommand( "king " + ( tempHPos - 32 ) + " " +
                            ( tempVPos - 32 ) + "\n" );
                    getOpponent( player ).sendCommand( "king " + ( tempHPos - 32 ) +
                            " " + ( tempVPos - 32 ) + "\n" );
                }
                
                // Make sure the opponent still has at least one piece left
                int oppCount = 0;
                int playerCount = 0;
                int playerKingCount = 0;
                int oppKingCount = 0;
                
                for ( int ii = 0; ii < pieces.size(); ii++ ) {
                    CheckersPiece curPiece = ( CheckersPiece ) pieces.get( ii );
                    
                    if ( curPiece.getOwner() == player ) {
                        playerCount++;
                        if ( curPiece.isKing() ) {
                            playerKingCount++;
                        }
                    } else {
                        oppCount++;
                        if ( curPiece.isKing() ) {
                            oppKingCount++;
                        }
                    }
                }
                
                if ( oppCount == 0 ) {
                    // Opponent has no pieces, they have lost
                    getOpponent( player ).gameLost();
                    player.gameWon();
                } else if ( playerCount == 1 &&
                        oppCount == 1 &&
                        playerKingCount == 1 &&
                        oppKingCount == 1 ) {
                    // Each player has 1 King, it is a draw
                    getOpponent( player ).gameDraw();
                    player.gameDraw();
                } else {
                    // Only allow a double-jump, not three, not four,
                    // and five is right out the window!
                    // Also make sure some other piece can be jumped
                    if ( jumpedPiece != null &&
                            doubleJump == null &&
                            jumpIsPossible( player ) ) {
                        System.out.println( player + " is eligible for a double jump" );
                        // They jumped a piece, it is still his/her turn
                        player.sendCommand( "yourturn " + getBoard() );
                        // Since the next jump MUST be done by the same piece
                        // do not let go of movePiece
                        doubleJump = movePiece;
                    } else {
                        // Make sure the doubleJump is not set
                        doubleJump = null;
                        // Tell the opponent that it is their turn
                        getOpponent( player ).sendCommand( "yourturn " + getBoard() );
                        // Set them as the current player
                        curPlayer = getOpponent( player );
                    }
                    
                    // Clear movePiece so a new move has to be started
                    movePiece = null;
                }
            } else {
                player.sendCommand( "fail\n" );
		if (player == player1) player2.sendCommand("reset\n");
		else player1.sendCommand("reset\n");
                // Clear it so the player can choose a different piece
                movePiece = null;
                
                // DEBUG: Dump out all pieces for debug
                System.out.println( player + " Failed to make a valid move.  Current pieces: " + getBoard() );
            }
        }
        
        return ret;
    }
    
    public boolean isBlackSpace( int horizPos, int vertPos ) {
        if ( ( ( vertPos / 80 ) % 2 == 0 && ( horizPos / 80 ) % 2 == 1 ) ||
                ( ( vertPos / 80 ) % 2 == 1 && ( horizPos / 80 ) % 2 == 0 ) ) {
            return true;
        } else {
            return false;
        }
    }
    
    // Note that the terminology is from the perspective of a piece.  Technically,
    // one player moves down while the other moves up.  Forward, in this terminology,
    // is the direction toward the opponent.
    public boolean isForwardDiagonal( CheckersClient player, int horizPos, int vertPos ) {
        int horizSquare = horizPos / 80;
        int vertSquare = vertPos / 80;
        
        int origHorizSquare = movePiece.getHorizPos() / 80;
        int origVertSquare = movePiece.getVertPos() / 80;
        
        if ( player == player1 ) {
            // Forward = above
            if ( origVertSquare - 1 >= 0 && vertSquare == origVertSquare - 1 ) {
                if ( origHorizSquare + 1 < 8 && horizSquare == origHorizSquare + 1 ) {
                    return true;
                } else if ( origHorizSquare - 1 >= 0 &&
                        horizSquare == origHorizSquare - 1 ) {
                    return true;
                }
            }
        } else {
            // Forward = below
            if ( origVertSquare + 1 < 8 && vertSquare == origVertSquare + 1 ) {
                if ( origHorizSquare + 1 < 8 && horizSquare == origHorizSquare + 1 ) {
                    return true;
                } else if ( origHorizSquare - 1 >= 0 &&
                        horizSquare == origHorizSquare - 1 ) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public boolean isBackwardDiagonal( CheckersClient player, int horizPos, int vertPos ) {
        int horizSquare = horizPos / 80;
        int vertSquare = vertPos / 80;
        
        int origHorizSquare = movePiece.getHorizPos() / 80;
        int origVertSquare = movePiece.getVertPos() / 80;
        
        if ( player == player1 ) {
            // Forward = above
            if ( origVertSquare + 1 < 8 && vertSquare == origVertSquare + 1 ) {
                if ( origHorizSquare + 1 < 8 && horizSquare == origHorizSquare + 1 ) {
                    return true;
                } else if ( origHorizSquare - 1 >= 0 &&
                        horizSquare == origHorizSquare - 1 ) {
                    return true;
                }
            }
        } else {
            // Forward = below
            if ( origVertSquare - 1 >= 0 && vertSquare == origVertSquare - 1 ) {
                if ( origHorizSquare + 1 < 8 && horizSquare == origHorizSquare + 1 ) {
                    return true;
                } else if ( origHorizSquare - 1 >= 0 &&
                        horizSquare == origHorizSquare - 1 ) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    public CheckersPiece opponentPieceInSquare( CheckersClient player, int horizSquare, int vertSquare ) {
        CheckersPiece curPiece;
        
        for ( int ii = 0; ii < pieces.size(); ii++ ) {
            curPiece = ( CheckersPiece ) pieces.get( ii );
            int curHorizSquare = curPiece.getHorizPos() / 80;
            int curVertSquare = curPiece.getVertPos() / 80;
            
            if ( curHorizSquare == horizSquare && curVertSquare == vertSquare ) {
                System.out.println( player + " Found a piece at " + curPiece );
                return curPiece;
            }
        }
        
        System.out.println( player + "No piece was found" );
        return null;
    }
    
    public boolean jumpIsPossible( CheckersClient player ) {
        int startHoriz = movePiece.getHorizPos();
        int startVert = movePiece.getVertPos();
        
        if ( player == player1 ) {
            if ( isForwardJump( player, startHoriz - 160, startVert - 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz - 80 ) / 80, ( startVert - 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump ahead to the left" );
                return true;
            } else if ( isForwardJump( player, startHoriz + 160, startVert - 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz + 80 ) / 80, ( startVert - 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump ahead to the right" );
                return true;
            } else if ( movePiece.isKing() &&
                    isBackwardJump( player, startHoriz - 160, startVert + 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz - 80 ) / 80, ( startVert + 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump backward to the left" );
                return true;
            } else if ( movePiece.isKing() &&
                    isBackwardJump( player, startHoriz + 160, startVert + 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz + 80 ) / 80, ( startVert + 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump backward to the right" );
                return true;
            }
        } else {
            if ( isForwardJump( player, startHoriz - 160, startVert + 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz - 80 ) / 80, ( startVert + 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump ahead to the left" );
                return true;
            } else if ( isForwardJump( player, startHoriz + 160, startVert + 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz + 80 ) / 80, ( startVert + 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump ahead to the right" );
                return true;
            } else if ( movePiece.isKing() &&
                    isBackwardJump( player, startHoriz - 160, startVert - 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz - 80 ) / 80, ( startVert - 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump backward to the left" );
                return true;
            } else if ( movePiece.isKing() &&
                    isBackwardJump( player, startHoriz + 160, startVert - 160 ) != null &&
                    opponentPieceInSquare( player, ( startHoriz + 80 ) / 80, ( startVert - 80 ) / 80 ) == null ) {
                System.out.println( player + "can jump backward to the right" );
                return true;
            }
        }
        
        System.out.println( player + "has no eligible jumps" );
        return false;
    }
    
    public CheckersPiece isForwardJump( CheckersClient player, int horizPos, int vertPos ) {
        int horizSquare = horizPos / 80;
        int vertSquare = vertPos / 80;
        
        int origHorizSquare = movePiece.getHorizPos() / 80;
        int origVertSquare = movePiece.getVertPos() / 80;
        
        if ( player == player1 ) {
            // Forward = above
            if ( origVertSquare - 2 >= 0 && vertSquare == origVertSquare - 2 ) {
                if ( origHorizSquare + 2 < 8 && horizSquare == origHorizSquare + 2 ) {
                    System.out.println( player + " sent a valid forward jump to the right, checking to make sure a piece was jumped" );
                    return opponentPieceInSquare( player, origHorizSquare + 1, origVertSquare - 1 );
                } else if ( origHorizSquare - 2 >= 0 &&
                        horizSquare == origHorizSquare - 2 ) {
                    System.out.println( player + " sent a valid forward jump to the left, checking to make sure a piece was jumped" );
                    return opponentPieceInSquare( player, origHorizSquare - 1, origVertSquare - 1 );
                }
            }
        } else {
            // Forward = below
            if ( origVertSquare + 2 < 8 && vertSquare == origVertSquare + 2 ) {
                if ( origHorizSquare + 2 < 8 && horizSquare == origHorizSquare + 2 ) {
                    System.out.println( player + " sent a valid forward jump to the right, checking to make sure a piece was jumped" );
                    return opponentPieceInSquare( player, origHorizSquare + 1, origVertSquare + 1 );
                } else if ( origHorizSquare - 2 >= 0 &&
                        horizSquare == origHorizSquare - 2 ) {
                    System.out.println( player + " sent a valid forward jump to the left, checking to make sure a piece was jumped" );
                    return opponentPieceInSquare( player, origHorizSquare - 1, origVertSquare + 1 );
                }
            }
        }
        
        return null;
    }
    
    public CheckersPiece isBackwardJump( CheckersClient player, int horizPos, int vertPos ) {
        int horizSquare = horizPos / 80;
        int vertSquare = vertPos / 80;
        
        int origHorizSquare = movePiece.getHorizPos() / 80;
        int origVertSquare = movePiece.getVertPos() / 80;
        
        if ( player == player1 ) {
            // Forward = above
            if ( origVertSquare + 2 < 8 && vertSquare == origVertSquare + 2 ) {
                if ( origHorizSquare + 2 < 8 && horizSquare == origHorizSquare + 2 ) {
                    System.out.println( player + " sent a valid backward jump to the right, checking to make sure a piece was jumped" );
                    // Modified on 2007/05/04 by M.Y.
                    return opponentPieceInSquare( player, origHorizSquare + 1, origVertSquare + 1 );
                } else if ( origHorizSquare - 2 >= 0 && horizSquare == origHorizSquare - 2 ) {
                    System.out.println( player + " sent a valid backward jump to the left, checking to make sure a piece was jumped" );
                    // Modified on 2007/05/04 by M.Y.
                    return opponentPieceInSquare( player, origHorizSquare - 1, origVertSquare + 1 );
                }
            }
        } else {
            // Forward = below
            if ( origVertSquare - 2 >= 0 && vertSquare == origVertSquare - 2 ) {
                if ( origHorizSquare + 2 < 8 && horizSquare == origHorizSquare + 2 ) {
                    System.out.println( player + " sent a valid backward jump to the right, checking to make sure a piece was jumped" );
                    // Modified on 2007/05/04 by M.Y.
                    return opponentPieceInSquare( player, origHorizSquare + 1, origVertSquare - 1 );
                } else if ( origHorizSquare - 2 >= 0 && horizSquare == origHorizSquare - 2 ) {
                    System.out.println( player + " sent a valid backward jump to the left, checking to make sure a piece was jumped" );
                    // Modified on 2007/05/04 by M.Y.
                    return opponentPieceInSquare( player, origHorizSquare - 1, origVertSquare - 1 );
                }
            }
        }
        
        return null;
    }
    
    public boolean isAtOpponentEnd( CheckersClient player, int horizPos, int vertPos ) {
        int vertSquare = vertPos / 80;
        
        if ( player == player1 ) {
            // End == top
            if ( vertSquare == 0 ) return true;
        } else {
            // End == bottom
            if ( vertSquare == 7 ) return true;
        }
        
        return false;
    }
}
