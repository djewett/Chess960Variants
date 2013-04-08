package dj.chess960var;

import dj.chess960var.GameModel.pc;

public class Utilities {

    public static int getRowGivenPosition(int position)
    {
    	if( position < 0 || position > 63 )
			throw new AssertionError("Invalid position");
    	
    	// Given the position (0-63), this function returns the corresponding
    	// row number.  Remember, the position starts from the top left and 
    	// goes to the left, whereas the row number (for our purposes) starts 
    	// from 0 on the bottom row and goes up to 7 on the top row:
    	
    	return (63-position)/8;
    }
    
    public static int getColumnGivenPosition(int position)
    {
    	if( position < 0 || position > 63 )
			throw new AssertionError("Invalid position");
    	
    	// Given the position (0-63), this function returns the corresponding
    	// column number.  Remember, the position starts from the top left and 
    	// goes to the left, whereas the column (for our purposes) starts 
    	// from 0 on the left column and goes up to 7 on the right column:
    	
    	return position%8;
    }
    
    public static int getPositionGivenRowAndCol(int row, int col)
    {
    	int position = 8*(7-row)-1+col;
    	
    	return position;
    }
    
	public static int[] findPaddingValuesAndWidthOfBoard(
		int totalAvailableWidth,
		boolean isBoardExpanded )
	{
		// Here we are given a totalAvailableWidth, from which we want to 
		// calculate and return (1) left, (2) top, (3) right and (4) bottom 
		// paddings for the frame containing the board as well as (5) the board 
		// width itself, such that the board width is divisible by 8 and as 
		// close as possible to 40/43 of totalAvailableWidth, and such that the 
		// padding values are all roughly equal:
		
		// {left, top, right, bottom, (strict) board width}:
		int[] returnValues = new int[5];
		
		if( isBoardExpanded )
		{
			// No padding:
			returnValues[0] = 0;
			returnValues[1] = 0;
			returnValues[2] = 0;
			returnValues[3] = 0;
			
			// Board width is total available width:
			returnValues[4] = totalAvailableWidth;
			
			// Ensure it is divisible by 8:
			returnValues[4] = returnValues[4] - (returnValues[4] % 8);
		}
		else
		{
			// Initially proposed width of board (don't worry about truncation):
			int b = totalAvailableWidth * 93/100;
		
			// Make it divisible by 8:
			b = b - (b % 8);
		
			// Total remaining space width-wise (or height-wise):
			int x = totalAvailableWidth - b;
			
			int a = x/2;
			
			int c = x/2 + x%2;
			
			if( a + b + c != totalAvailableWidth )
				throw new AssertionError("Padding values could not be constructed");
			
			//  ________________________
			// |            |           |
			// |            a           |
			// |      ______|_____      |
			// |     |      ^     |     |	
			// |     |      |     |     |
			// |<-a->|<-----b---->|<-c->|
			// |     |      |     |     |
			// |     |______v_____|     |
			// |            |           |
			// |            c           |
			// |____________|___________|
			
			// {left, top, right, bottom, (strict) board width}:
			returnValues[0] = a;
			returnValues[1] = a;
			returnValues[2] = c;
			returnValues[3] = c;
			returnValues[4] = b;
		}
		
		return returnValues;
	}
	
	public static int[] getFirstPositionOfPiece(pc thePiece, pc[][] gameRep)
	{
		int[] returnVals = new int[2];
		
		for(int r=0; r<gameRep.length; r++)
		{
			for(int c=0; c<gameRep[r].length; c++)
			{
				if( thePiece == gameRep[r][c] )
				{
					returnVals[0] = r;
					returnVals[1] = c;
					
					return returnVals;
				}
				else
				{
					// Do nothing
				}
			}
		}
		
		throw new AssertionError("Piece not found");
	}
}