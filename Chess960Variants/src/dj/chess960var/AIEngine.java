package dj.chess960var;
import dj.chess960var.GameModel.pc;
////import java.util.TreeSet;
import java.util.ArrayList;

public class AIEngine 
{
	public static String getNextMove( GameModel gameModel )
	{
		////TreeSet<int[]> possibleMoves;
		
		// TODO: Use a typedef to make move a 2x2 int[]
		
		ArrayList<int[]> possibleMoves = new ArrayList<int[]>(); 
		
		final pc[][] BOARD_REP = gameModel.getBoardRep();
		
		boolean isLightsTurn = gameModel.isLightsTurn();
		
		for( int i = 0; i < BOARD_REP.length; i++ )
		{
			for( int j = 0; j < BOARD_REP[0].length; j++ )
			{
				// Create a list of possible moves in next two loops and sort
				// them according to some evaluation function and only keep
				// the top 10 or so:
				
				pc piece = BOARD_REP[i][j];
				
				//if( BOARD_REP[i][j] != pc.eS )
				if( (isLightsTurn && GameModel.isLightPiece(piece)) ||
					(!isLightsTurn && GameModel.isDarkPiece(piece)) )
				{
					for( int newI = 0; newI < BOARD_REP.length; newI++ )
					{
						for( int newJ = 0; newJ < BOARD_REP.length; newJ++ )
						{
							if( (i != newI || j != newJ)  &&
								gameModel.isValidMove(i,j,newI,newJ,false) )
							{
								int[] move = new int[4];
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
		
		ArrayList<int[]> decreasedPossibleMoves = 
			trimPossibleMoves( possibleMoves );
		
		int[] theMoveToMake = evaluateMoves( decreasedPossibleMoves );
		
		return convertMoveToString( theMoveToMake );
	}
	
	private static ArrayList<int[]> trimPossibleMoves( 
		ArrayList<int[]> possibleMoves )
	{
		// This function should take in a list of possible moves and trim 
		// away some of the obviously bad moves (as well as some of the less 
		// obviously bad ones) and return a new list of potential moves that 
		// is as small as possible.
		
		// TODO: Need to run some kind of evaluations to rank these moves 
		// and trim off the bottom x moves (need to figure out what x
		// should be as well)
		// 
		//Trim off first move, just for testing:
		// Consider using evaluatePosition() function below
		possibleMoves.remove(0);
		return possibleMoves;
	}
	
	private static int[] evaluateMoves( ArrayList<int[]> moves )
	{
		// This function should take in a list of moves, evaluate all of them, 
		// and return the best one, according to the evaluation function.
		// This is the "imperfect information" step.
		
		// TODO: Run evaluations instead of simply always returning the first 
		// move:
		// Consider using evaluatePosition() function below
		return moves.get(0);
	}
	
	private static int evaluatePosition( final pc[][] board_rep, 
										 boolean isLightsTurn )
	{
		// Given a representation of the board, where pieces are on the
		// board and whose move it is, this function generates a "utility"
		// for that state in the game.  This utility should be a measure of
		// how good the given position is for whoever's turn it is.  It
		// can be positive or negative.
		
		// TODO
		return 0;
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
