package dj.chess960var;
import dj.chess960var.GameModel.pc;
////import java.util.TreeSet;
import java.util.ArrayList;

public class AIEngine 
{
	public static String getNextMove( GameModel gameModel )
	{
		////TreeSet<int[]> possibleMoves;
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
		
		return Integer.toString( possibleMoves.get(0)[0] ) + 
			   Integer.toString( possibleMoves.get(0)[1] ) + "to" +
			   Integer.toString( possibleMoves.get(0)[2] ) + 
			   Integer.toString( possibleMoves.get(0)[3] );
	}
}