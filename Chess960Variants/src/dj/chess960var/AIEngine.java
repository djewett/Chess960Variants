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
				// them according to some evaluation function and only keep the
				// top 10 or so:
				
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
								gameModel.isValidMove( i, j, newI, newJ, false ) )
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
		
		return convertMoveToString( possibleMoves.get(0) );
	}
	
	private static String convertMoveToString( int[] theMove )
	{
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
