package dj.chess960var;
import dj.chess960var.GameModel.pc;
////import java.util.TreeSet;
import java.util.ArrayList;
import java.util.PriorityQueue;

public class AIEngine 
{
	public static String getNextMove( GameModel gameModel )
	{
		////TreeSet<int[]> possibleMoves;
		
		// Each move is represented as a 2x2 array of ints (the first two ints
		// represent the starting square and the second two ints represent
		// the square that the piece is on after the move).
		
		boolean isLightsTurn = gameModel.isLightsTurn();
		
		final pc[][] BOARD_REP = gameModel.getBoardRep();
		
		ArrayList<int[]> possibleMoves = getPossibleMoves( gameModel,
				  										   isLightsTurn,
				  										   BOARD_REP );
		
		ArrayList<int[]> decreasedPossibleMoves = 
			trimPossibleMoves( BOARD_REP, possibleMoves, isLightsTurn );
		
		int[] theMoveToMake = evaluateMoves( BOARD_REP, 
											 decreasedPossibleMoves );
		
		return convertMoveToString( theMoveToMake );
	}
	
	private static ArrayList<int[]> getPossibleMoves( GameModel gameModel,
													  boolean isLightsTurn,
													  pc[][] board_rep )
	{
		ArrayList<int[]> possibleMoves = new ArrayList<int[]>(); 
		
		for( int i = 0; i < board_rep.length; i++ )
		{
			for( int j = 0; j < board_rep[0].length; j++ )
			{
				// Create a list of possible moves in next two loops and sort
				// them according to some evaluation function and only keep
				// the top 10 or so:
				
				pc piece = board_rep[i][j];
				
				//if( BOARD_REP[i][j] != pc.eS )
				if( (isLightsTurn && GameModel.isLightPiece(piece)) ||
					(!isLightsTurn && GameModel.isDarkPiece(piece)) )
				{
					for( int newI = 0; newI < board_rep.length; newI++ )
					{
						for( int newJ = 0; newJ < board_rep.length; newJ++ )
						{
							if( (i != newI || j != newJ)  &&
								gameModel.isValidMove(i,j,newI,newJ,false) )
							{
								int[] move = new int[5]; 
									// {startRow,startCol,endRow,endCol,value}
								move[0] = i;
								move[1] = j;
								move[2] = newI;
								move[3] = newJ;
								
								possibleMoves.add(move);
							}
						}
					}
				}
			}
		}
		
		return possibleMoves;
	}
	
	private static ArrayList<int[]> trimPossibleMoves( 
		pc[][] board_rep,
		ArrayList<int[]> possibleMoves,
		boolean isLightsTurn )
	{
		// This function should take in a list of possible moves and trim 
		// away some of the obviously bad moves (as well as some of the less 
		// obviously bad ones) and return a new list of potential moves that 
		// is as small as possible.

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
			
			int currValue = evaluatePosition( new_board_rep, isLightsTurn );
			
			//thePQ.
		}

		possibleMoves.remove(0);
		return possibleMoves;
	}
	
	private static int[] evaluateMoves( pc[][] board_rep,
										ArrayList<int[]> moves )
	{
		// This function should take in a list of moves, evaluate all of them, 
		// and return the best one, according to the evaluation function.
		// This is the "imperfect information" step.
		
		// TODO: Run evaluations instead of simply always returning the first 
		// move:
		// Consider using evaluatePosition() function below
		return moves.get(0);
	}
	
	private static int evaluatePosition( pc[][] board_rep, 
										 boolean isLightsTurn )
	{
		// Given a representation of the board, where pieces are on the
		// board and whose move it is, this function generates a "utility"
		// for that state in the game.  This utility should be a measure of
		// how good the given position is for whoever's turn it is.  It
		// can be positive or negative.
		
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
			
		int returnVal = 0;

		if( isLightsTurn )
			returnVal = lightsScore - darksScore;
		else
			returnVal = darksScore - lightsScore;
		
		return returnVal;
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
					darksMaterial += 350;
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
		// This is a simple function that converts a 2x2 array representation
		// of a move to a String representation (for output).
		
		// TODO: Add an assertion/pre-condition that theMove is 2x2.
		
		String fromSquareChar = convertNumberToCharacter( theMove[1] );
		String toSquareChar = convertNumberToCharacter( theMove[3] );
		
		String moveAsString =
			fromSquareChar + Integer.toString( theMove[0]+1 ) + " to " +
			toSquareChar + Integer.toString( theMove[2]+1 );
		
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
