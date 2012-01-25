package dj.chess960var;

import java.util.Random;

public class GameModel 
{
    private pc[][] mGameRep;
	private boolean mIsLightsTurn;
	
	// Need two randomizers so that we can assign the same seeds to each so that
	// dark's setup will mirror light's:
	private long mSeed;
	private Random mRandomizer_lightPieces;
	private Random mRandomizer_darkPieces;
	
	// Stores the column of the last 2-square pawn move if that was the last 
	// move in the game.  Used for determining whether an en passant move is
	// valid:
	private int mEnPassantPotentialColumn;
	
	private final int mINVALIDROWCOL = -20;
	
	private boolean mIsLightKingSideCastleStillPoss;
	private boolean mIsLightQueenSideCastleStillPoss;
	private boolean mIsDarkKingSideCastleStillPoss;
	private boolean mIsDarkQueenSideCastleStillPoss;
	
	private int mLightQueenSideRookColumn;
	private int mLightKingSideRookColumn;
	private int mDarkQueenSideRookColumn;
	private int mDarkKingSideRookColumn;
	
	private int[] mPositionOfCurrentPawnPromo;
	
	private GameModel mModelForUndo;
	
    public enum pc
    {
    	// 'l' => light (referring to the PIECE, NOT the square)
    	// 'd' => dark (referring to the PIECE, NOT the square)
    	//
    	// 'P' => Pawn
    	// 'N' => kNight
    	// 'B' => Bishop
    	// 'R' => Rook
    	// 'Q' => Queen
    	// 'K' => King
    	//
    	// 'eS' => empty Square
    	//
    	lP, lN, lB, lR, lQ, lK, dP, dN, dB, dR, dQ, dK, eS
    }
    
    public GameModel() // Default CTOR
    {
        // This representation of the board is "flipped" (ie. indexed from 
        // (0,0) on the top left, corresponding to A1 on the bottom left of a 
    	// physical board):
        pc[][] initSetup = {{pc.lR,pc.lN,pc.lB,pc.lQ,pc.lK,pc.lB,pc.lN,pc.lR},
				   			{pc.lP,pc.lP,pc.lP,pc.lP,pc.lP,pc.lP,pc.lP,pc.lP},
				   			{pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS},
				   			{pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS},
				   			{pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS},
				   			{pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS,pc.eS},
				   			{pc.dP,pc.dP,pc.dP,pc.dP,pc.dP,pc.dP,pc.dP,pc.dP},
				   			{pc.dR,pc.dN,pc.dB,pc.dQ,pc.dK,pc.dB,pc.dN,pc.dR}};
        
        mGameRep = initSetup;
        mIsLightsTurn = true;
        
    	// Create seed so that the randomizations for Light and Dark Pieces
    	// will always be the same, as long as they're each pressed "Randomize"
    	// the same number of times, and so that the starting randomization is
        // different each time the application starts:
    	mSeed = (new Random()).nextLong();
    	
    	mRandomizer_lightPieces = new Random(mSeed);
    	mRandomizer_darkPieces = new Random(mSeed);
    	mEnPassantPotentialColumn = mINVALIDROWCOL;
    	mIsLightKingSideCastleStillPoss = true;
    	mIsLightQueenSideCastleStillPoss = true;
    	mIsDarkKingSideCastleStillPoss = true;
    	mIsDarkQueenSideCastleStillPoss = true;
    	mLightQueenSideRookColumn = 0;
    	mLightKingSideRookColumn = 7;
    	mDarkQueenSideRookColumn = 0;
    	mDarkKingSideRookColumn = 7;
    	
    	mPositionOfCurrentPawnPromo = new int[2];
    	mPositionOfCurrentPawnPromo[0] = mINVALIDROWCOL;
    	mPositionOfCurrentPawnPromo[1] = mINVALIDROWCOL;
    	
    	mModelForUndo = null;
    }
    
    public GameModel(GameModel gM) // Copy CTOR
    {
    	if(gM.mGameRep.length != 8)
    		throw new AssertionError("Expecting 8x8 game rep. passed in");
    	if(gM.mGameRep[0].length != 8)
    		throw new AssertionError("Expecting 8x8 game rep. passed in");
        
        // * Java always passes by value. The tricky part is that Java  
        // passes objects as references passed by value:
        mGameRep = new pc[8][8];
		for(int r=0; r<mGameRep.length; r++)
			for(int c=0; c<mGameRep[r].length; c++)
				mGameRep[r][c] = gM.mGameRep[r][c];
        
		// Primitive type member variables:
        mIsLightsTurn = gM.mIsLightsTurn;
    	mSeed = gM.mSeed;
    	mRandomizer_lightPieces = gM.mRandomizer_lightPieces;
    	mRandomizer_darkPieces = gM.mRandomizer_darkPieces;
    	mEnPassantPotentialColumn = gM.mEnPassantPotentialColumn;
    	mIsLightKingSideCastleStillPoss = gM.mIsLightKingSideCastleStillPoss;
    	mIsLightQueenSideCastleStillPoss = gM.mIsLightQueenSideCastleStillPoss;
    	mIsDarkKingSideCastleStillPoss = gM.mIsDarkKingSideCastleStillPoss;
    	mIsDarkQueenSideCastleStillPoss = gM.mIsDarkQueenSideCastleStillPoss;
    	mLightQueenSideRookColumn = gM.mLightQueenSideRookColumn;
    	mLightKingSideRookColumn = gM.mLightKingSideRookColumn;
    	mDarkQueenSideRookColumn = gM.mDarkQueenSideRookColumn;
    	mDarkKingSideRookColumn = gM.mDarkKingSideRookColumn;
    	
    	mPositionOfCurrentPawnPromo = new int[2];
    	mPositionOfCurrentPawnPromo[0] = gM.mPositionOfCurrentPawnPromo[0];
    	mPositionOfCurrentPawnPromo[1] = gM.mPositionOfCurrentPawnPromo[1];
    	
    	// Recursive:
    	if( null == gM.mModelForUndo )
    		mModelForUndo = null;
    	else
    		mModelForUndo = new GameModel(gM.mModelForUndo);
    }
    
	public void resetRow(int row)
	{
		if(0 == row)
		{
			// Set up light pieces:
			mGameRep[row][0] = pc.lR;
			mGameRep[row][1] = pc.lN;
			mGameRep[row][2] = pc.lB;
			mGameRep[row][3] = pc.lQ;
			mGameRep[row][4] = pc.lK;
			mGameRep[row][5] = pc.lB;
			mGameRep[row][6] = pc.lN;
			mGameRep[row][7] = pc.lR;
			
			// Also reset the appropriate randomizer so that we'll get the same 
			// sequence of randomized rows as before the reset:
			mRandomizer_lightPieces = new Random(mSeed);
		}
		else if(7 == row)
		{
			// Set up dark pieces:
			mGameRep[row][0] = pc.dR;
			mGameRep[row][1] = pc.dN;
			mGameRep[row][2] = pc.dB;
			mGameRep[row][3] = pc.dQ;
			mGameRep[row][4] = pc.dK;
			mGameRep[row][5] = pc.dB;
			mGameRep[row][6] = pc.dN;
			mGameRep[row][7] = pc.dR;
			
			// Also reset the appropriate randomizer so that we'll get the same 
			// sequence of randomized rows as before the reset:
			mRandomizer_darkPieces = new Random(mSeed);
		}
		else
		{
			throw new AssertionError(
				"Invalid row for randomization - must be 0 or 7");
		}
		
    	mLightQueenSideRookColumn = 0;
    	mLightKingSideRookColumn = 7;
    	mDarkQueenSideRookColumn = 0;
    	mDarkKingSideRookColumn = 7;
	}

	public void randomizeRow(int row)
	{	
		// Retrieve the appropriate randomizer:
		Random randomizer;
		if(0 == row)
			randomizer = mRandomizer_lightPieces;
		else if(7 == row)
			randomizer = mRandomizer_darkPieces;
		else
			throw new AssertionError(
				"Invalid row for randomization - must be 0 or 7");
		
		// Use this to keep track of which squares have been assigned:
		boolean[] occupiedSquares = 
			{false,false,false,false,false,false,false,false};
		
	  // KING:
		
		// nextInt(n) generates between 0 (inclusive) and n (exclusive).  But
		// kingColumn must be in the range 1 to 6 to make room for the rooks on
		// either side:
		int king_column = randomizer.nextInt(6) + 1;
		occupiedSquares[king_column] = true;
		
	  // ROOKS:
		
		int rook1_column = randomizer.nextInt(king_column);
		occupiedSquares[rook1_column] = true;
		int rook2_column = randomizer.nextInt(7-king_column) + king_column + 1;
		occupiedSquares[rook2_column] = true;
		
		if(0 == row)
		{
			mLightQueenSideRookColumn = rook1_column;
			mLightKingSideRookColumn = rook2_column;
		}
		else 
		{
			mDarkQueenSideRookColumn = rook1_column;
			mDarkKingSideRookColumn = rook2_column;
		}
		
	  // BISHOP 1:
		
		// 5 columns remaining; pick a random one of these for the first bishop:
		// "Relative" to the remaining empty squares:
		int bishop1_relativeIndex = randomizer.nextInt(5);
		int bishop1_column = -1;
		while(bishop1_column < 7 && bishop1_relativeIndex >= 0)
		{
			if(!occupiedSquares[bishop1_column+1])
				bishop1_relativeIndex--;
			
			bishop1_column++;
		}
		occupiedSquares[bishop1_column] = true;
		
		// Do some interim checking of the calculated squares so far:
		if( king_column == rook1_column )
			throw new AssertionError("king_column==rook1_column");
		if( king_column == rook2_column )
			throw new AssertionError("king_column==rook2_column");
		if( king_column == bishop1_column )
			throw new AssertionError("king_column==bishop1_column");
		if( rook1_column == rook2_column )
			throw new AssertionError("rook1_column==rook2_column");
		if( rook1_column == bishop1_column )
			throw new AssertionError("rook1_column==bishop1_column");
		if( rook2_column == bishop1_column )
			throw new AssertionError("rook2_column==bishop1_column");
		
	  // BISHOP 2:
		
		// Of the 4 squares remaining, find the ones that are the opposite
		// color of the above bishop's square.  From these, pick one randomly
		// for bishop2_column:

		boolean isBishop1SquareLight = isLightSquare(row,bishop1_column);
		
		// Count remaining empty squares of appropriate color:
		int countForBishop2 = 0;
		for(int i=0; i<8; i++)
		{
			if( !occupiedSquares[i] &&
				isLightSquare(row,i) != isBishop1SquareLight )
				countForBishop2++;
		}
		
		// Get a random in based on the size of this count:
		int bishop2_relativeIndex = randomizer.nextInt(countForBishop2);
		
		// Use the above count and relative index to find bishop2_column:
		int bishop2_column = -1;
		while(bishop2_column < 7 && bishop2_relativeIndex >= 0)
		{
			if( !occupiedSquares[bishop2_column+1] &&
				isLightSquare(row,bishop2_column+1) != isBishop1SquareLight )
				bishop2_relativeIndex--;
			
			bishop2_column++;
		}
		occupiedSquares[bishop2_column] = true;
		
	  // KNIGHTS and QUEEN:
		
		// Simply distribute the Queen and the two Knights randomly among the 
		// 3 remaining squares:
		
		int[] indexesForRemaining3Pieces = 
			generateIndexesForRemaining3(randomizer,occupiedSquares);
		
		int knight1_column = indexesForRemaining3Pieces[0];
		int knight2_column = indexesForRemaining3Pieces[1];	
		int queen_column = indexesForRemaining3Pieces[2];
				
		// Set the pieces up according to the above randomization:
		if(0 == row)
		{
			// Set up light pieces:
			mGameRep[row][king_column] = pc.lK;
			mGameRep[row][rook1_column] = pc.lR;
			mGameRep[row][rook2_column] = pc.lR;
			mGameRep[row][bishop1_column] = pc.lB;
			mGameRep[row][bishop2_column] = pc.lB;
			mGameRep[row][knight1_column] = pc.lN;
			mGameRep[row][knight2_column] = pc.lN;
			mGameRep[row][queen_column] = pc.lQ;
		}
		else if(7 == row)
		{
			// Set up dark pieces:
			mGameRep[row][king_column] = pc.dK;
			mGameRep[row][rook1_column] = pc.dR;
			mGameRep[row][rook2_column] = pc.dR;
			mGameRep[row][bishop1_column] = pc.dB;
			mGameRep[row][bishop2_column] = pc.dB;
			mGameRep[row][knight1_column] = pc.dN;
			mGameRep[row][knight2_column] = pc.dN;
			mGameRep[row][queen_column] = pc.dQ;
		}
		else
		{
			throw new AssertionError(
				"Invalid row for randomization - must be 0 or 7");
		}
	}
	
	public boolean hasGameStarted()
	{
		boolean hasStarted = null != mModelForUndo;
		return hasStarted;
	}
	
	public GameModel getModelForUndo()
	{
		return mModelForUndo;
	}
	
	public GameModel getStartPositionModel()
	{
		GameModel currModel = this;
		while(null != currModel.mModelForUndo)
			currModel = currModel.mModelForUndo;
		return currModel;
	}
	
	public pc getPieceAtSquare(int position)
    {
		int row = Utilities.getRowGivenPosition(position);
		int column = Utilities.getColumnGivenPosition(position);
    	pc pieceAtSquare = mGameRep[row][column];
    	return pieceAtSquare;
    }
    
    public int getImageOfPieceAtSquare(int position, boolean lightPersp)
    {
		int row = Utilities.getRowGivenPosition(position);
		int column = Utilities.getColumnGivenPosition(position);
    	return this.getImageOfPieceAtSquare(row, column, lightPersp);
    }
    
	public boolean updateAfterMove(int startPos, int endPos)
	{
		int startRow = Utilities.getRowGivenPosition(startPos);
		int startCol = Utilities.getColumnGivenPosition(startPos);
		int endRow = Utilities.getRowGivenPosition(endPos);
		int endCol = Utilities.getColumnGivenPosition(endPos);
		return updateAfterMove(startRow, startCol, endRow, endCol);
	}

	public boolean isLightsTurn()
	{
		return mIsLightsTurn;
	}
    
	public boolean isValidForDrag(int positionOfPiece)
	{
		// Need to check things like whose turn it is and what color piece is
		// being moved before we even allow a drag to start.
		
		boolean isValidForDrag = false;
		
		pc thePiece = getPieceAtPosition(positionOfPiece);
		
		if( (mIsLightsTurn && isLightPiece(thePiece)) ||
			(!mIsLightsTurn && isDarkPiece(thePiece)) )
		{
			isValidForDrag = true;
		}
		else
		{
			// Do nothing - allow isValid to remain false.
		}
		
		if( isValidForDrag )
		{
			isValidForDrag = false;
			boolean checkOnlyKingCapture = false;
			for(int i=0; i<64; i++)
			{
				if( isValidMove(positionOfPiece,i,checkOnlyKingCapture) )
				{
					isValidForDrag = true;
					break;
				}
			}
		}
		else
		{
			// Do nothing
		}
		
		return isValidForDrag;
	}

	public boolean isValidMove(
		int startPosit, int endPosit, boolean checkOnlyKingCapture)
	{
		int startRow = Utilities.getRowGivenPosition(startPosit);
		int startCol = Utilities.getColumnGivenPosition(startPosit);
			
		int endRow = Utilities.getRowGivenPosition(endPosit);
		int endCol = Utilities.getColumnGivenPosition(endPosit);
		
		boolean returnVal = 
			isValidMove(startRow,startCol,endRow,endCol,checkOnlyKingCapture);
		
		return returnVal;
	}
	
	public void promoteLightPawn(GameModel.pc toPiece)
	{
		int row = mPositionOfCurrentPawnPromo[0];
		int col = mPositionOfCurrentPawnPromo[1];
		
		if(mINVALIDROWCOL == row || mINVALIDROWCOL == col)
			throw new AssertionError("Invalid position for pawn promotion");
		
		mGameRep[row][col] = toPiece;
	}
	
	public boolean isSquareEmpty(int position)
	{
		int row = Utilities.getRowGivenPosition(position);
		int col = Utilities.getColumnGivenPosition(position);
		
		boolean isSquareEmpty = mGameRep[row][col] == pc.eS;
		
		return isSquareEmpty;
	}
	
	private boolean areOpposingPieces(int row1,int col1,int row2,int col2)
	{
		// * Not simply the negation of checking whether the squares contain
		// pieces of the same color (ie. via areSameColorPieces()), due to the 
		// existence of empty squares.

		boolean returnVal = true;
		
		pc piece1 = getPieceAtPosition(row1,col1);
		pc piece2 = getPieceAtPosition(row2,col2);
		
		if( isLightPiece(piece1) && isDarkPiece(piece2) )
		{
			// Do nothing - allow returnVal == true
		}
		else if( isDarkPiece(piece1) && isLightPiece(piece2) )
		{
			// Do nothing - allow returnVal == true
		}
		else
		{
			returnVal = false;
		}
		
		return returnVal;
	}
	
	private boolean isPawnMoveValid( int startRow, int startCol, int endRow, 
									 int endCol, boolean checkOnlyKingCapture )
	{
		boolean isValid = true;
		
		int forwardDir;
		int homeRow;
		
		pc pieceMoving = getPieceAtPosition(startRow, startCol);
		
		if(pieceMoving == pc.lP)
		{
			forwardDir = 1;
			homeRow = 1;
		}
		else
		{
			// Must be pc.dP:
			forwardDir = -1;
			homeRow = 6;
		}
		
		if( endCol == startCol && 
			endRow == startRow + forwardDir &&
			!checkOnlyKingCapture )
		{
			// Potentially 1 square jump on first or later pawn move. 
			// Still need to check if square in front of pawn's start 
			// square is empty:
			
			pc pieceOneSquareAhead = 
				getPieceAtPosition(startRow+forwardDir,startCol);
			
			if( pieceOneSquareAhead == pc.eS )
			{
				// Do nothing - leave isValid == true
			}
			else
			{
				// Pawn cannot move 2 forward because an opponent's
				// piece is blocking it:
				isValid = false;
			}
		}
		else if( endCol == startCol && 
				 homeRow == startRow && 
				 endRow == startRow + 2*forwardDir && 
				 !checkOnlyKingCapture )
		{
			// Potentially 2 square jump on first pawn move.  Still need
			// to check if two squares in front of pawn's start square 
			// are both empty:
			
			if( getPieceAtPosition(startRow+forwardDir,startCol) == pc.eS && 
				getPieceAtPosition(startRow+2*forwardDir,startCol) == pc.eS )
			{
				// Do nothing - leave isValid == true
			}
			else
			{
				// Pawn cannot move 2 forward because an opponent's
				// piece is blocking it:
				isValid = false;;
			}
		}
		else if( (endRow == startRow + forwardDir && endCol == startCol + 1) ||
				 (endRow == startRow + forwardDir && endCol == startCol - 1) )
		{
			// Potential capture.  Need to check for a regular capture
			// and also for en passant:

			if( areOpposingPieces(startRow, startCol, endRow, endCol) )
			{
				// Do nothing - leave isValid == true
			}
			else if( homeRow + 3*forwardDir == startRow && 
					 areOpposingPieces(startRow,startCol,startRow,endCol) &&
					 mEnPassantPotentialColumn == endCol )
				     // These conditions should be sufficient because of
					 // the way mEnPassantPotentialColumn is set up.
					 // * Don't need to check if the endPosit is empty,
				     // because that will be already covered by previous
					 // if statement.
					 // * (startRow,endCol) => piece being captured via en pas.
			{
				// Leave isValid == true
			}
			else
			{
				// Pawn cannot move diagonally like this because there is
				// no opposing piece to capture:
				isValid = false;
			}	
		}
		else
		{
			// If move is not 2 ahead on first move, one ahead or a 
			// diagonal capture, then it's an invalid move:
			isValid = false;
		}
		
		return isValid;
	}
	
	private boolean isKnightMoveValid(
		int startRow, int startCol, int endRow, int endCol)
	{
		boolean isValid = true;
		
		if( (endRow == startRow+2 && endCol == startCol+1) ||
			(endRow == startRow+1 && endCol == startCol+2) ||
			(endRow == startRow-1 && endCol == startCol+2) ||
			(endRow == startRow-2 && endCol == startCol+1) ||
			(endRow == startRow-2 && endCol == startCol-1) ||
			(endRow == startRow-1 && endCol == startCol-2) ||
			(endRow == startRow+1 && endCol == startCol-2) ||
			(endRow == startRow+2 && endCol == startCol-1) )
		{
			// Do nothing - leave isValid == true
		}
		else
		{
			// If move is not a 2x1 'L' shape, it's invalid:
			isValid = false;
		}
		
		return isValid;
	}
	
	private boolean isBishopMoveValid(
		int startRow, int startCol, int endRow, int endCol)
	{
		boolean isValid = true;
		
		int rowDiff = endRow-startRow;
		int colDiff = endCol-startCol;
		
		if(Math.abs(rowDiff) == Math.abs(colDiff))
		{
			if( 0 == Math.abs(rowDiff) )
				throw new AssertionError("Expecting non-zero rowDiff==colDiff");

			// Need to also check that all squares diagonally strictly 
			// between the start square and end square are empty:
			
			int normRowDiff = rowDiff/Math.abs(rowDiff); // +/- 1
			int normColDiff = colDiff/Math.abs(colDiff); // +/- 1
			
			for(int i=1; i<Math.abs(rowDiff); i++) // Could use colDiff too
			{
				int currRow = startRow+normRowDiff*i;
				int currCol = startCol+normColDiff*i;
				
				if( getPieceAtPosition(currRow, currCol) == pc.eS )
				{
					// Do nothing - leave isValid == true
				}
				else
				{
					// Move is invalid - there is a piece blocking the
					// move:
					isValid = false;
					break;
				}
			}
		}
		else
		{
			// If move is not diagonal, it's invalid:
			isValid = false;
		}
		
		return isValid;
	}
	
	private boolean isRookMoveValid(
		int startRow, int startCol, int endRow, int endCol)
	{
		boolean isValid = true;
		
		// Very similar to implementation for isBishopMoveValid
		
		if(startRow == endRow || startCol == endCol)
		{
			// Need to also check that all squares strictly 
			// between the start square and end square are empty
			
			// One of these will be 0:
			int rowDiff = endRow-startRow;
			int colDiff = endCol-startCol;
			
			int normRowDiff;
			if(rowDiff!=0)
				normRowDiff = rowDiff/Math.abs(rowDiff); // +/- 1
			else
				normRowDiff = 0;
			
			int normColDiff;
			if(colDiff!=0)
				normColDiff = colDiff/Math.abs(colDiff); // +/- 1
			else
				normColDiff = 0;
				
			for(int i=1; i < Math.abs(rowDiff+colDiff); i++)
			{
				int currRow = startRow+normRowDiff*i;
				int currCol = startCol+normColDiff*i;
				
				if( getPieceAtPosition(currRow, currCol) == pc.eS )
				{
					// Do nothing - leave isValid == true
				}
				else
				{
					// Move is invalid - there is a piece blocking the
					// move:
					isValid = false;
					break;
				}
			}
		}
		else
		{
			// If move is not horizontal or vertical, it's invalid:
			isValid = false;
		}
		
		return isValid;
	}
	
	private boolean isQueenMoveValid(
		int startRow, int startCol, int endRow, int endCol)
	{		
		boolean isValid = isBishopMoveValid(startRow,startCol,endRow,endCol) ||
				  		  isRookMoveValid(startRow,startCol,endRow,endCol);
		
		return isValid;
	}
	
	private boolean isKingMoveValid( int startRow, int startCol, int endRow, 
									 int endCol, boolean checkOnlyKingCapture )
	{
		boolean isValid = true;
		
		isValid = isBishopMoveValid(startRow,startCol,endRow,endCol) ||
			      isRookMoveValid(startRow,startCol,endRow,endCol);
		
		// At this point, castling should be valid under the method 
		// isRookMoveValid() (because within that specific method, we don't
		// check validity of dragging a piece onto one of its own - that is
		// done before and castling is an exempt from that rule):

		if(isValid)
		{
			// Check for maximum of one square move:
			
			int rowDiff = endRow-startRow;
			int colDiff = endCol-startCol;
			
			if( Math.abs(rowDiff)<=1 && Math.abs(colDiff)<=1 &&
				!areSameColorPieces(startRow,startCol,endRow,endCol) )
			{
				// Do nothing - leave isValid == true
			}
			else if( !checkOnlyKingCapture )
			{		
				// Check for castling:
				isValid = isCastlingMoveValid(startRow,startCol,endRow,endCol);
			}
			else
			{
				// This code will be reached if we only want to check for a
				// king moving to capture another king (in the hypothetical 
				// proposed new position that allows us to check if a side is 
				// trying to move its king into check), but the move is more 
				// than one square, so we want to return false (a king cannot 
				// be captured through castling)
				isValid = false;
			}
		}
		else
		{
			// Do nothing
		}
		
		return isValid;
	}
	
	private boolean isKingCapturable(int kingRow, int kingCol)
	{
		boolean returnVal = false;
		
		pc theKingPiece = getPieceAtPosition(kingRow,kingCol);
		
		if( theKingPiece != pc.lK && theKingPiece != pc.dK )
			throw new AssertionError("Passed in position must be a king");
		
		if( (theKingPiece == pc.lK && isLightsTurn()) ||
			(theKingPiece == pc.dK && !isLightsTurn()) )
			throw new AssertionError(
				"Passed in king and next move must be from opposite sides");
		
		for(int r=0; r<8; r++)
		{
			for(int c=0; c<8; c++)
			{
				pc currPiece = getPieceAtPosition(r,c);
				
				if( (isLightPiece(currPiece) && isDarkPiece(theKingPiece)) ||
					(isDarkPiece(currPiece) && isLightPiece(theKingPiece)) )
				{
					boolean checkOnlyKingCapture = true;
					
					if( isValidMove(r,c,kingRow,kingCol,checkOnlyKingCapture) )
						return true;
				}
			}
		}
		
		return returnVal;
	}
	
	private boolean isCastlingMoveValid(
		int startRow, int startCol, int endRow, int endCol)
	{	
		if( startRow != endRow )
			return false;
		
		pc startPiece = getPieceAtPosition(startRow,startCol);
		pc endPiece = getPieceAtPosition(endRow,endCol);
		
		if( (startPiece == pc.lK && endPiece == pc.lR) ||
			(startPiece == pc.dK && endPiece == pc.dR) )
			; // Do nothing - allow below checks to occur
		else
			return false;
		
		int finalKingColumn;
		int finalRookColumn;
		
		if( startPiece == pc.lK && endPiece == pc.lR )
		{	
			// Check for light that castling is occurring on row 0:
			if(startRow != 0)
				return false;
			
			if(endCol > startCol)
			{
				if(!mIsLightKingSideCastleStillPoss)
					return false;
				
				// => king side castling:
				finalKingColumn = 6;
				finalRookColumn = 5;
			}
			else
			{
				if(!mIsLightQueenSideCastleStillPoss)
					return false;
				
				// endColumn < StartColumn => queen side castling:
				finalKingColumn = 2;
				finalRookColumn = 3;
			}
		}
		else if( startPiece == pc.dK && endPiece == pc.dR )
		{
			// Check for dark that castling is occurring on row 7:
			if(startRow != 7)
				return false;
			
			if(endCol > startCol)
			{
				if(!mIsDarkKingSideCastleStillPoss)
					return false;
				
				// => king side castling:
				finalKingColumn = 6;
				finalRookColumn = 5;
			}
			else
			{
				if(!mIsDarkQueenSideCastleStillPoss)
					return false;
				
				// endColumn < StartColumn => queen side castling:
				finalKingColumn = 2;
				finalRookColumn = 3;
			}
		}
		else
		{
			throw new AssertionError("Expecting pieces to be king and rook");
		}

		// Check following condition:
		// "All the squares between the king's initial and final squares 
		// (including the final square), and all of the squares between the 
		// rook's initial and final squares (including the final square), must 
		// be vacant except for the king and castling rook." 
		//  - http://en.wikipedia.org/wiki/Chess960
		
		int leastColumn = Math.min( Math.min(startCol,endCol),
								    Math.min(finalRookColumn,finalKingColumn) );
		
		int mostColumn = Math.max( Math.max(startCol,endCol),
			    				   Math.max(finalRookColumn,finalKingColumn) );
		
		for(int i=leastColumn; i<=mostColumn; i++)
		{
			pc currPiece = getPieceAtPosition(startRow, i);
			
			if( currPiece != pc.eS &&
				currPiece != startPiece &&
				!(currPiece == endPiece && i == endCol) )
				return false;
		}
		
		// Need to check for each square the king moves through (including the
		// start and end squares) the king is in check:
		
		int leastKingCol = Math.min(startCol,finalKingColumn);
		int mostKingCol = Math.max(startCol,finalKingColumn);

		for(int i=leastKingCol; i<=mostKingCol; i++)
		{
			// Build up a new game with king moved to (startRow,i) and check
			// if king is capturable on that new game:
			
			// TODO: try to factor this code with common code in isValidMove():
			
			// Get the king piece of the moving side:
			pc kingPieceOfMovingSide;
			if( isLightsTurn() )
				kingPieceOfMovingSide = pc.lK;
			else
				kingPieceOfMovingSide = pc.dK;
			
			// Make a copy of the current game and then update it for the
			// proposed move:
			GameModel proposedNewGame = new GameModel(this);
			
			proposedNewGame.updateAfterMove(startRow, startCol, startRow, i);
			
			// Get the new location of the moving side's king after the 
			// proposed move:
			int[] kingPos = 
				Utilities.getFirstPositionOfPiece( kingPieceOfMovingSide, 
							   					   proposedNewGame.mGameRep );
			
			if( proposedNewGame.isKingCapturable(kingPos[0], kingPos[1]) )
				return false;
		}
		
		// If all of the above tests pass, then castling is valid:
		return true;
	}
	
	// Helper for randomizeRow():
	private int[] generateIndexesForRemaining3( Random randomizer, 
												boolean[] occupiedSquares )
	{
		int[] returnVal = new int[3];
		
		// Randomize by first randomizing the indices to assigned (and THEN
		// assigning them):
		int[] indexToAssign = new int[3];
		
		indexToAssign[0] = randomizer.nextInt(3);
		
		int piece2_relativeIndex = randomizer.nextInt(2);
		
		if( 2 == indexToAssign[0] )
		{
			indexToAssign[1] = piece2_relativeIndex;
			indexToAssign[2] = (indexToAssign[1] + 1) % 2;
		}
		else if( 0 == indexToAssign[0] )
		{
			indexToAssign[1] = piece2_relativeIndex + 1;
			int piece3_relativeIndex = (piece2_relativeIndex + 1) % 2;
			indexToAssign[2] = piece3_relativeIndex + 1;
		}
		else if( 1 == indexToAssign[0] )
		{
			if( 0 == piece2_relativeIndex )
			{
				indexToAssign[1] = 0;
				indexToAssign[2] = 2;
			}
			else if( 1 == piece2_relativeIndex )
			{
				indexToAssign[1] = 2;
				indexToAssign[2] = 0;
			}
			else
				throw new AssertionError("Invalid index for piece 2");
		}
		else
			throw new AssertionError("Invalid index for piece 1");
		
		// Now we need to convert relative indexes to actual ones:
		
		int currIndex = 0;
		for(int i=0; i<8; i++)
		{
			if(!occupiedSquares[i])
			{
				returnVal[indexToAssign[currIndex]] = i;
				currIndex++;
			}
		}
			
		return returnVal;
	}
	
	private int getImageOfPieceAtSquare(int row, int column, boolean lightPersp)
	{   	
		int returnImage;
		
    	pc pieceAtSquare = mGameRep[row][column];
    	
    	// For now, since I was having trouble rotating the images, I am simply
    	// using separate images for light and dark perspectives:
    	
    	if( lightPersp )
    	{
    		// Use images for light's perspective:
    		
        	switch(pieceAtSquare)
        	{
        	case lP:
        		returnImage = R.drawable.light_pawn;
        		break;
        	case dP:
        		returnImage = R.drawable.dark_pawn;      		
        		break;
        	case lN:
        		returnImage = R.drawable.light_knight;
        		break;
        	case dN:
        		returnImage = R.drawable.dark_knight;      		
        		break;
        	case lB:
        		returnImage = R.drawable.light_bishop;
        		break;
        	case dB:
        		returnImage = R.drawable.dark_bishop;      		
        		break;
        	case lR:
        		returnImage = R.drawable.light_rook;
        		break;
        	case dR:
        		returnImage = R.drawable.dark_rook;
        		break;
        	case lQ:
        		returnImage = R.drawable.light_queen;
        		break;
        	case dQ:
        		returnImage = R.drawable.dark_queen;      		
        		break;
        	case lK:
        		returnImage = R.drawable.light_king;
        		break;
        	case dK:
        		returnImage = R.drawable.dark_king;      		
        		break;
        	case eS:
        		throw new AssertionError("No image exists for empty square");
        	default:
        		throw new AssertionError("Invalid piece");
        	}
    	}
    	else
    	{
    		// Use images for dark's perspective:
    		
        	switch(pieceAtSquare)
        	{
        	case lP:
        		returnImage = R.drawable.light_pawn_dp;
        		break;
        	case dP:
        		returnImage = R.drawable.dark_pawn_dp;      		
        		break;
        	case lN:
        		returnImage = R.drawable.light_knight_dp;
        		break;
        	case dN:
        		returnImage = R.drawable.dark_knight_dp;      		
        		break;
        	case lB:
        		returnImage = R.drawable.light_bishop_dp;
        		break;
        	case dB:
        		returnImage = R.drawable.dark_bishop_dp;      		
        		break;
        	case lR:
        		returnImage = R.drawable.light_rook_dp;
        		break;
        	case dR:
        		returnImage = R.drawable.dark_rook_dp;
        		break;
        	case lQ:
        		returnImage = R.drawable.light_queen_dp;
        		break;
        	case dQ:
        		returnImage = R.drawable.dark_queen_dp;      		
        		break;
        	case lK:
        		returnImage = R.drawable.light_king_dp;
        		break;
        	case dK:
        		returnImage = R.drawable.dark_king_dp;      		
        		break;
        	case eS:
        		throw new AssertionError("No image exists for empty square");
        	default:
        		throw new AssertionError("Invalid piece");
        	}
    	}
    	
    	return returnImage;
    }
	
	private boolean updateAfterMove(
		int startRow, int startCol, int endRow, int endCol)
	{
		mModelForUndo = new GameModel(this);
		
		boolean isPawnPromotion = false;
		
		// When a piece makes a legal move, it is usually the case that it
		// occupies the square moved to and the square moved from becomes empty
		
		pc movingPiece = getPieceAtPosition(startRow,startCol);
		pc endSquarePiece = getPieceAtPosition(endRow,endCol);
		
	  // EN PASSANT:
		
		// Set mEnPassantPotentialColumn to an invalid value initially. 
		// This member variable will be reused each time en passant 
		// opportunities arise:
		mEnPassantPotentialColumn = mINVALIDROWCOL;
		
		if(movingPiece==pc.lP || movingPiece==pc.dP)
		{
			// If a pawn has moved 2 spaces and is now next to an opposing pawn
			// (ie. on the same column), then it needs to be marked as a 
			// potential en passant for the opposing player:
			//
			boolean potentialEnPassantForLight = 
				movingPiece == pc.dP && endRow == startRow-2;
			boolean potentialEnPassantForDark = 
				movingPiece == pc.lP && endRow == startRow+2;
			
			if(potentialEnPassantForLight || potentialEnPassantForDark)
			{
				if( startCol != endCol )
					throw new AssertionError("Expecting startCol == endCol");

				mEnPassantPotentialColumn = startCol;
			}
			else
			{
				// Do nothing
			}
		}
		else
		{
			// Do nothing
		}
		
		// Check if (1) piece is a pawn, (2) piece is moving diagonally and
		// (3) piece is landing on an empty square:
		if( (movingPiece==pc.lP || movingPiece==pc.dP) // (1)
			&& (startCol==endCol+1 || startCol==endCol-1) // (2)
			&& endSquarePiece==pc.eS )
		{
			// En passant - remove captured piece:
			mGameRep[startRow][endCol] = pc.eS;
		}
		else
		{
			// Do nothing
		}
		
	  // PAWN PROMOTION:
		
		if( (movingPiece==pc.lP && 7==endRow) || 
			(movingPiece==pc.dP && 0==endRow) )
		{
			isPawnPromotion = true;
			mPositionOfCurrentPawnPromo[0] = endRow;
			mPositionOfCurrentPawnPromo[1] = endCol;
		}
		else
		{
			// Do nothing
		}
		
	  // CASTLING:	
		
		if( movingPiece == pc.lK )
		{
			mIsLightKingSideCastleStillPoss = false;
			mIsLightQueenSideCastleStillPoss = false;
		}
		else if( movingPiece == pc.dK )
		{
			mIsDarkKingSideCastleStillPoss = false;
			mIsDarkQueenSideCastleStillPoss = false;
		}
		else if( movingPiece == pc.lR && startRow == 0)
		{
			if( startCol == mLightKingSideRookColumn )
			{
				// ... then ensure flag is set to false:
				mIsLightKingSideCastleStillPoss = false;
			}
			else if( startCol == mLightQueenSideRookColumn )
			{
				// ... then ensure flag is set to false:
				mIsLightQueenSideCastleStillPoss = false;
			}
			else
			{
				// Do nothing
			}
		}
		else if( movingPiece == pc.dR && startRow == 7)
		{
			if( startCol == mDarkKingSideRookColumn )
			{
				// ... then ensure flag is set to false:
				mIsDarkKingSideCastleStillPoss = false;
			}
			else if( startCol == mDarkQueenSideRookColumn )
			{
				// ... then ensure flag is set to false:
				mIsDarkQueenSideCastleStillPoss = false;
			}
			else
			{
				// Do nothing
			}
		}
		else
		{
			// Do nothing
		}
		
		if( movingPiece == pc.lK && endSquarePiece == pc.lR )
		{
			if(endCol > startCol)
			{
				// => king side castling:
				mGameRep[startRow][startCol] = pc.eS;
				mGameRep[endRow][endCol] = pc.eS;
				mGameRep[0][6] = pc.lK;
				mGameRep[0][5] = pc.lR;
			}
			else
			{
				// endColumn < StartColumn => queen side castling:
				mGameRep[startRow][startCol] = pc.eS;
				mGameRep[endRow][endCol] = pc.eS;
				mGameRep[0][2] = pc.lK;
				mGameRep[0][3] = pc.lR;
			}
		}
		else if( movingPiece == pc.dK && endSquarePiece == pc.dR )
		{
			if(endCol > startCol)
			{
				// => king side castling:
				mGameRep[startRow][startCol] = pc.eS;
				mGameRep[endRow][endCol] = pc.eS;
				mGameRep[7][6] = pc.dK;
				mGameRep[7][5] = pc.dR;
			}
			else
			{
				// endCol < StartCol => queen side castling:
				mGameRep[startRow][startCol] = pc.eS;
				mGameRep[endRow][endCol] = pc.eS;
				mGameRep[7][2] = pc.dK;
				mGameRep[7][3] = pc.dR;
			}
		}
		else if(startRow==endRow && startCol==endCol)
		{
			// Start square is the same as end square - this case is used only
			// for validating a castling attempt and is never used in an actual
			// valid move.  Here we want all the pieces to stay in the exact  
			// same positions as before the "move" (but we still want to update
			// mIsLightsTurn at the very end of this method):
			
			// So do nothing
		}
		else
		{
			// Typical case for all non-castling moves - update start square 
			// and end square:
			mGameRep[endRow][endCol] = mGameRep[startRow][startCol];
			mGameRep[startRow][startCol] = pc.eS;
		}
		
		mIsLightsTurn = !mIsLightsTurn;
		
		return isPawnPromotion;
	}
	
	private boolean areSameColorPieces(
		int startRow, int startCol, int endRow, int endCol) 
	{
		// * Not simply the negation of checking whether the squares contain
		// pieces of opposite color (ie. via areOpposingPieces()), due to 
		// the existence of empty squares.
		
		boolean result = false;
		
		if( isLightPiece(mGameRep[startRow][startCol]) &&
			isLightPiece(mGameRep[endRow][endCol]) )
		{
			result = true;
		}
		else if( isDarkPiece(mGameRep[startRow][startCol]) &&
				 isDarkPiece(mGameRep[endRow][endCol]))
		{
			result = true;
		}
		else
		{
			// Do nothing - allow return of false
		}
		
		return result;
	}
	
	private pc getPieceAtPosition(int position)
	{
		int row = Utilities.getRowGivenPosition(position);
		int column = Utilities.getColumnGivenPosition(position);
		pc thePiece = getPieceAtPosition(row, column);
		return thePiece;
	}
	
	private pc getPieceAtPosition(int row, int column)
	{
		pc thePiece = mGameRep[row][column];
		return thePiece;
	}
	
	private boolean isValidMove( int startRow, int startCol, int endRow, 
			 					 int endCol, boolean checkOnlyKingCapture )
	{
		// checkOnlyKingCapture - this flag will be used to determine if we 
		// want to check validity of moves like en passant, pins and castling. 
		// When we initially call this method, we want this value to be false.
		// But near the end of this method, we make recursive calls to this
		// this function from a proposed new GameModel object (through 
		// isKingInCheck()), at which point we want this flag to be true.
		// This will tell isValidMove() to avoid checking for things like en
		// passant, castling and pins, which are all irrelevant when checking
		// if a king is in check (a king can't be in check through an opposing 
		// side's en passant or castling maneuver, and the opponent would in
		// theory be allowed to break a pin in order to capture the king).

		boolean isValid = true;

		pc pieceMoving = getPieceAtPosition(startRow,startCol);
		pc pieceAtEndSquare = getPieceAtPosition(endRow,endCol);

		boolean isPotentialCastle = 
			(pieceMoving == pc.lK && pieceAtEndSquare == pc.lR) ||
			(pieceMoving == pc.dK && pieceAtEndSquare == pc.dR) ;

		boolean isValidSoFar = 
			// Can't drag a piece onto itself:
			!(startRow == endRow && startCol == endCol) && 
			// Can't drag a piece onto a piece of same color unless castling:
			( !areSameColorPieces(startRow,startCol,endRow,endCol) ||	
			  isPotentialCastle );

		if( isValidSoFar )  
		{	
			switch(pieceMoving)
			{
			case lP:
			case dP:
				isValid = isPawnMoveValid( startRow,startCol, endRow, 
								   		   endCol, checkOnlyKingCapture );
				break;
			case lN:
			case dN:
				isValid = isKnightMoveValid(startRow, startCol, endRow, endCol);
				break;
			case lB:
			case dB:
				isValid = isBishopMoveValid(startRow, startCol, endRow, endCol);
				break;
			case lR:
			case dR:
				isValid = isRookMoveValid(startRow, startCol, endRow, endCol);
				break;
			case lQ:
			case dQ:
				isValid = isQueenMoveValid(startRow,startCol,endRow,endCol);
				break;
			case lK:
			case dK:
				isValid = isKingMoveValid( startRow, startCol, endRow, endCol, 
							   			   checkOnlyKingCapture );
				break;
			default:
				throw new AssertionError("Invalid piece at start position");
			}
		}
		else
		{
			isValid = false;
		}

		if(isValid && !checkOnlyKingCapture)
		{
			// Get the king piece of the moving side:
			pc kingPieceOfMovingSide;
			if( isLightsTurn() )
				kingPieceOfMovingSide = pc.lK;
			else
				kingPieceOfMovingSide = pc.dK;

			// Make a copy of the current game and then update it for the
			// proposed move:
			GameModel proposedNewGame = new GameModel(this);
			proposedNewGame.updateAfterMove(startRow, startCol, endRow, endCol);

			// Get the new location of the moving side's king after the 
			// proposed move:
			int[] kingPos = 
				Utilities.getFirstPositionOfPiece( kingPieceOfMovingSide, 
									   			   proposedNewGame.mGameRep );

			isValid = 
				!proposedNewGame.isKingCapturable(kingPos[0], kingPos[1]);
		}

		return isValid;
	}
	
	private boolean isLightPiece(pc thePiece)
	{
		// Returns true iff the passed in piece is a light piece:
		
		boolean isLight = true;
		
		switch( thePiece )
		{
		case lP:
		case lN:
		case lB:
		case lR:
		case lQ: 
		case lK:
			break;
		default:
			isLight = false;
		}
		
		return isLight;
	}

	private boolean isDarkPiece(pc thePiece)
	{
		// Returns true iff the passed in piece is a light piece:
		
		boolean isDark = true;
		
		switch( thePiece )
		{
		case dP:
		case dN:
		case dB:
		case dR:
		case dQ: 
		case dK:
			break;
		default:
			isDark = false;
		}
		
		return isDark;
	}
	
	private boolean isLightSquare(int row, int column)
	{
		// (0,0) is dark:
		return ((row + column) % 2 == 1);
	}
}
