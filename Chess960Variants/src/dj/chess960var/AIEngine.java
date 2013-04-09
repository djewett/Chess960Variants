package dj.chess960var;
import dj.chess960var.GameModel.pc;
import java.util.ArrayList;
import java.util.PriorityQueue;

// For reference - positions:
//
//  dR dN dB dQ dK dB dN dR  =  0  1  2  3  4  5  6  7
//  dP dP dP dP dP dP dP dP  =  8  9  10 11 12 13 14 15
//  eS eS eS eS eS eS eS eS  =  16 17 18 19 20 21 22 23
//  eS eS eS eS eS eS eS eS  =  24 25 26 27 28 29 30 31
//  eS eS eS eS eS eS eS eS  =  32 33 34 35 36 37 38 39
//  eS eS eS eS eS eS eS eS  =  40 41 42 43 44 45 46 47
//  lP lP lP lP lP lP lP lP  =  48 49 50 51 52 53 54 55
//  lR lN lB lQ lK lB lN lR  =  56 57 58 59 60 61 62 63

public class AIEngine 
{
	private static String[] mSquares = 
		{ "a8","b8","c8","d8","e8","f8","g8","h8",
		  "a7","b7","c7","d7","e7","f7","g7","h7",
		  "a6","b6","c6","d6","e6","f6","g6","h6",
		  "a5","b5","c5","d5","e5","f5","g5","h5",
		  "a4","b4","c4","d4","e4","f4","g4","h4",
		  "a3","b3","c3","d3","e3","f3","g3","h3",
		  "a2","b2","c2","d2","e2","f2","g2","h2",
		  "a1","b1","c1","d1","e1","f1","g1","h1" };

	public static String getNextMove( GameModel gameModel )
	{
		ArrayList<int[]> possibleMoves = getPossibleMoves( gameModel );
		
		// TODO: Factor this code with similar code in evaluateMove():

		boolean isLightsTurn = gameModel.isLightsTurn();
		
		int value;
		
		if( isLightsTurn )
			value = Integer.MIN_VALUE;
		else
			value = Integer.MAX_VALUE;
		
		int indexOfMoveToMake = -1;

		for( int i = 0; i < possibleMoves.size(); i++ )
		{
			GameModel newGM = new GameModel(gameModel);
			
			int newValue = evaluateMove( newGM, 
					 					 possibleMoves.get(i), 
					 					 2 );
			
			if( (isLightsTurn && newValue > value) ||
				(!isLightsTurn && newValue < value) )
			{
				value = newValue;
				indexOfMoveToMake = i;
			}
		}
		
		//ArrayList<int[]> decreasedPossibleMoves = 
		//	trimPossibleMoves( gameModel, possibleMoves );
		
		//int[] theMoveToMake = evaluateMoves( gameModel, 
		//									 decreasedPossibleMoves );
		
		return convertMoveToString( possibleMoves.get(indexOfMoveToMake) );
		
		////return "";
	}
	
	private static ArrayList<int[]> getPossibleMoves( GameModel gameModel )
	{
		boolean isLightsTurn = gameModel.isLightsTurn();
		
		ArrayList<int[]> possibleMoves = new ArrayList<int[]>(); 
		
		for( int oldPos = 0; oldPos < 64; oldPos++ )
		{
			// Create a list of possible moves in next two loops and sort
			// them according to some evaluation function and only keep
			// the top 10 or so:
				
			////pc piece = board_rep[i][j];
			pc currPiece = gameModel.getPieceAtSquare(oldPos);
				
			//if( BOARD_REP[i][j] != pc.eS )
			if( (isLightsTurn && GameModel.isLightPiece(currPiece)) ||
				(!isLightsTurn && GameModel.isDarkPiece(currPiece)) )
			{
				for( int newPos = 0; newPos < 64; newPos++ )
				{
					if( (oldPos != newPos)  &&
						gameModel.isValidMove(oldPos,newPos,false) )
					{
						int[] move = new int[3]; // {startPos,endPos,value}
						move[0] = oldPos;
						move[1] = newPos;
						move[2] = -17; // For now, obscure value for testing
								
						possibleMoves.add(move);
					}
				}
			}
		}
		
		return possibleMoves;
	}
	
	private static ArrayList<int[]> trimPossibleMoves( 
		GameModel gameModel,
		ArrayList<int[]> possibleMoves )
	{
		// This function should take in a list of possible moves and trim 
		// away some of the obviously bad moves (as well as some of the less 
		// obviously bad ones) and return a new list of potential moves that 
		// is as small as possible.
		
		boolean isLightsTurn = gameModel.isLightsTurn();
		pc[][] board_rep = gameModel.getBoardRep();

		// Use evaluatePosition() function and keep top 10 moves:
		
		/*
		int initCapacity = 10;
		PriorityQueue<int[]> thePQ = 
			new PriorityQueue<int[]>( initCapacity, 
									  new Comparator<int[]>() {
				public int compare(int[] n1, int[] n2) 
				{
					if( n1[4] < n2[4] )
						return -1;
					else if( n1[4] > n2[4] )
						return 1;
					else
						return 0;
				}
		});
		*/
		
		for( int i = 0; i < possibleMoves.size(); i++ )
		{
			int[] currMove = possibleMoves.get(i);
			
			pc movingPiece = board_rep[currMove[0]][currMove[1]];
			
			pc[][] new_board_rep = board_rep;
			
			new_board_rep[currMove[0]][currMove[1]] = pc.eS;
			new_board_rep[currMove[2]][currMove[3]] = movingPiece;
			
			//int currValue = evaluatePosition( new_board_rep, isLightsTurn );
			
			//thePQ.
		}

		possibleMoves.remove(0);
		return possibleMoves;
	}
	
	private static int[] evaluateMoves( GameModel gameModel,
										ArrayList<int[]> moves )
	{
		////boolean isLightsTurn = gameModel.isLightsTurn();
		pc[][] board_rep = gameModel.getBoardRep();
		
		// This function should take in a list of moves, evaluate all of them, 
		// and return the best one, according to the evaluation function.
		// This is the "imperfect information" step.
		
		// TODO: Run evaluations instead of simply always returning the first 
		// move:
		// Consider using evaluatePosition() function below
		return moves.get(0);
	}
	
	private static int evaluateMove( GameModel gameModel,
									 int[] theMove,
									 int depth )
	{
		// Assumption: we only ever care to evaluate a move if it is our turn
		// (so we will never perform an evaulation for white when it is black's
		// turn and vice-versa).
		
		// For now, just do minimax without alpa-beta pruning
		// TODO: Add alpha-beta pruning
		
		GameModel newGM = new GameModel(gameModel);
		
		newGM.updateAfterMove( theMove[0], theMove[1] );
		
		int value;
		
		if( 0 == depth )
		{		
			value = evaluatePosition( newGM );
		}
		else
		{
			// Set value to minimum value over all possible moves after 
			// "theMove":
			
			boolean isLightsTurn = newGM.isLightsTurn();
			
			if( isLightsTurn )
				value = Integer.MIN_VALUE;
			else
				value = Integer.MAX_VALUE;
			
			ArrayList<int[]> possibleMoves = getPossibleMoves( newGM );
			
			for( int i = 0; i < possibleMoves.size(); i++ )
			{
				int newValue = evaluateMove( newGM, 
											 possibleMoves.get(i), 
											 depth-1 );
				
				if( (isLightsTurn && newValue > value) ||
					(!isLightsTurn && newValue < value) )
						value = newValue;
			}
		}
		
		return value;
	}
	
	/*
	private static pc[][] getNewBoardAfterMove( GameModel gameModel,
			 						 			int[] theMove )
	{
		pc movingPiece = board_rep[theMove[0]][theMove[1]];
			
		pc[][] new_board_rep = board_rep;
			
		new_board_rep[theMove[0]][theMove[1]] = pc.eS;
		new_board_rep[theMove[2]][theMove[3]] = movingPiece;
		
		return new_board_rep;
	}
	*/

	private static int evaluatePosition( GameModel gameModel )
	{
		// Given a representation of the board, where pieces are on the
		// board and whose move it is, this function generates a "utility"
		// for that state in the game.  This utility should be a measure of
		// how good the given position is for white (so white wants to
		// maximize these values and black wants to minimize).  It
		// can be positive or negative.
		
		pc[][] board_rep = gameModel.getBoardRep();
		
		// We want to check for basic things here like:
		//
		//  (1) does one side have a material advantage and the next more; 
		// if so, how much is the material advantage?
		// 
		//  (2) some measure of how many squares in the center are occupied/
		// attacked
		
		int lightsScore = getLightsMaterial(board_rep);
		int darksScore = getDarksMaterial(board_rep);
		
		// http://www.gamedev.net/page/resources/_/technical/
		// artificial-intelligence/chess-programming-part-vi-evaluation-
		// functions-r1208 :
		//
		// "Yet, it [material balance] is by far the overwhelming factor in any
		// chess board evaluation function. CHESS 4.5's creators estimate that 
		// an enormous advantage in position, mobility and safety is worth less 
		// than 1.5 pawns."
		
		// And so we should weight a decisive advantage in control of the 
		// center (for opening) as about 125 points:
		
		// Count light and dark pieces occupying and/or controlling the center
		// (c3 to f6)
		
		int numberOfLightsInCenter = 0;
		int numberOfDarksInCenter = 0;
		
		for( int i = 2; i < 5; i++ ) // rows 3 to 6
			for( int j = 2; j < 5; j++ ) // columns c to f
				if( GameModel.isLightPiece(board_rep[i][j]) )
					numberOfLightsInCenter++;
				else if( GameModel.isDarkPiece(board_rep[i][j]) )
					numberOfDarksInCenter++;
				else {}
					// Do nothing
					
		if( numberOfLightsInCenter - numberOfDarksInCenter >= 2 )
			lightsScore += 125;
		else if( numberOfDarksInCenter - numberOfLightsInCenter >= 2 )
			darksScore += 125;
		else {}
			// Do nothing

		return lightsScore - darksScore;
	}
	
	private static int getLightsMaterial( pc[][] board_rep )
	{
		int lightsMaterial = 0;
		
		for( int i = 0; i < board_rep.length; i++ )
		{
			for( int j = 0; j < board_rep[0].length; j++ )
			{
				switch( board_rep[i][j] )
				{
				case lP:
					lightsMaterial += 100;
					break;
				case lN:
					lightsMaterial += 300;
					break;
				case lB:
					lightsMaterial += 325;
					break;
				case lR:
					lightsMaterial += 500;
					break;
				case lQ:
					lightsMaterial += 900;
					break;
				case lK:
				case dP:
				case dN:
				case dB:
				case dR:
				case dQ:
				case dK:
				case eS:
					// Do nothing
					break;
		    	default:
		    		throw new AssertionError("Invalid piece");
				}
			}
		}
		
		return lightsMaterial;
	}
	
	private static int getDarksMaterial( pc[][] board_rep )
	{
		int darksMaterial = 0;
		
		for( int i = 0; i < board_rep.length; i++ )
		{
			for( int j = 0; j < board_rep[0].length; j++ )
			{
				switch( board_rep[i][j] )
				{
				case eS:
				case lP:
				case lN:
				case lB:
				case lR:
				case lQ:
				case lK:
				case dK:
					// Do nothing
					break;
				case dP:
					darksMaterial += 100;
					break;
				case dN:
					darksMaterial += 300;
					break;
				case dB:
					darksMaterial += 325;
					break;
				case dR:
					darksMaterial += 500;
					break;
				case dQ:
					darksMaterial += 900;
					break;
		    	default:
		    		throw new AssertionError("Invalid piece");
				}
			}
		}
		
		return darksMaterial;
	}
	
	private static String convertMoveToString( int[] theMove )
	{
		// Moves are represented as arrays of ints, where the first element
		// is the start position of the move and the second element is the
		// ending position of the move.
		
		String moveAsString =
				mSquares[theMove[0]] + " to " + mSquares[theMove[1]];
		
		return moveAsString;
	}
	
	private static String convertNumberToCharacter( int num )
	{
		// This is a simple function that converts from a column number to a 
		// column letter (for output).
		
		String theChar = "";
		
		switch( num )
		{
		case 0:
			theChar = "a";
			break;
		case 1:
			theChar = "b";
			break;
		case 2:
			theChar = "c";
			break;
		case 3:
			theChar = "d";
			break;
		case 4:
			theChar = "e";
			break;
		case 5:
			theChar = "f";
			break;
		case 6:
			theChar = "g";
			break;
		case 7:
			theChar = "h";
			break;
    	default:
    		throw new AssertionError("Invalid column number");
		}
		
		return theChar;
	}
}
