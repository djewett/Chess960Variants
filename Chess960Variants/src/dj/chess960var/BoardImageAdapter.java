package dj.chess960var;

import dj.chess960var.GameModel.pc;
import android.content.Context;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class BoardImageAdapter extends BaseAdapter
{ 
	private Context mContext;
	private GameModel mGameModel;
	private int mBoardWidthWithBorder_inPixels;
	
	private boolean mIsAutoRotateEnabled;
	private boolean mIsBoardExpanded;
	// TODO: Find a better way to accomplish this functionality (needing this
	// extra boolean and long boolean name makes things a little harry):
	private boolean mWasRotateDisabledDuringLightsTurn;

    public BoardImageAdapter(Context c)
    {
        mContext = c;
        mGameModel = new GameModel();
        mBoardWidthWithBorder_inPixels = 0;
        mIsAutoRotateEnabled = true;
        mIsBoardExpanded = false;
        mWasRotateDisabledDuringLightsTurn = false; // Irrelevant right now
    }
    
    public BoardImageAdapter( Context c, GameModel g, boolean autoRotEnabled, 
    						  boolean expand, boolean disabledDuringWhitesTurn)
    {
    	// CTOR used for restoring an existing game:
        mContext = c;
        mGameModel = g;
        mBoardWidthWithBorder_inPixels = 0;
        mIsAutoRotateEnabled = autoRotEnabled;
        mIsBoardExpanded = expand;
        mWasRotateDisabledDuringLightsTurn = disabledDuringWhitesTurn;
    }

    public int getCount()
    {
    	// A board has 64 squares:
        return 64;
    }

    public Object getItem(int position)
    {
    	throw new AssertionError("Not implemented");
    }

    public long getItemId(int position)
    {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {   
        // Create a new ImageView for each item referenced by the Adapter:
        
        ImageView singleSquare;
        
        if (convertView == null) 
        {  
        	// convertView is not recycled, so initialize some attributes:
            	
        	singleSquare = new ImageView(mContext);
            
            if( 0 == mBoardWidthWithBorder_inPixels )
            {
            	mBoardWidthWithBorder_inPixels = initialBoardWidth();
            }
            else
            {
            	// Do nothing.
            }
        } 
        else 
        {
        	singleSquare = (ImageView) convertView;
        }
        
        int squareWidth = this.squareWidth();
        
        GridView.LayoutParams gvLayoutParams = 
        	new GridView.LayoutParams(squareWidth, squareWidth);
        
        singleSquare.setLayoutParams( gvLayoutParams );
        
        // Only draw the square if there is a piece currently on it:
        if( mGameModel.getPieceAtSquare(position) != pc.eS )
        {
        	boolean useLight = this.useLightPerspective();
        	singleSquare.setImageResource(
        		mGameModel.getImageOfPieceAtSquare(position, useLight) );
        }
        else
        {
        	singleSquare.setImageDrawable(null);
        }
        
        return singleSquare;
    }
    
    public void setBoardWidth_inPixels(int newBoardWidth)
    {
    	if( 0 == newBoardWidth )
    		throw new AssertionError("Invalid newBoardWidth");
    	
    	mBoardWidthWithBorder_inPixels = newBoardWidth;
    }
	
	public void restart()
	{
		mGameModel = mGameModel.getStartPositionModel();
		this.notifyDataSetChanged();
	}
	
	public GameModel getGameModel()
	{
		return mGameModel;
	}
	
	public void setIsAutoRotateEnabled(boolean enabled)
	{
		mIsAutoRotateEnabled = enabled;
		if( !mIsAutoRotateEnabled && isLightsTurn() )
			mWasRotateDisabledDuringLightsTurn = true;
		else
			mWasRotateDisabledDuringLightsTurn = false;
	}
	
	public boolean getIsAutoRotateEnabled()
	{
		return mIsAutoRotateEnabled;
	}
	
	public boolean getRotateDisabledDuringLightsTurn()
	{
		return mWasRotateDisabledDuringLightsTurn;
	}
	
	public void setIsExpanded(boolean expanded)
	{
		mIsBoardExpanded = expanded;
	}
	
	public boolean getIsExpanded()
	{
		return mIsBoardExpanded;
	}
	
  // Call through methods:
	
	public GameModel getStartPositionModel()
	{
		// Call through to model:
		GameModel startPosModel = mGameModel.getStartPositionModel();
		return startPosModel;
	}
	
//    public int getImageOfSquare(int position)
//    {
//    	// Call through to model:
//    	int returnVal = mGameModel.getImageOfPieceAtSquare(position);
//    	return returnVal;
//    }
	
	public boolean updateAfterMove(int startPos, int endPos)
	{
    	// Call through to model:
		boolean isPawnPromotion = mGameModel.updateAfterMove(startPos, endPos);
		return isPawnPromotion;
	}
	
	public boolean isValidForDrag(int positionOfPiece)
	{
		// Call through to model:
		boolean isValid = mGameModel.isValidForDrag(positionOfPiece);
		return isValid;
	}
	
	public void resetRow(int row)
	{
		// Call through to model:
		mGameModel.resetRow(row);
		this.notifyDataSetChanged();
	}
	
	public void randomizeRow(int row)
	{
		// Call through to model:
		mGameModel.randomizeRow(row);
		this.notifyDataSetChanged();
	}
	
	public boolean isLightsTurn()
	{
		// Call through to model:
		boolean returnVal = mGameModel.isLightsTurn();
		return returnVal;
	}
	
	public int getImageOfPieceAtSquare(int position)
	{
		// ~ Call through to model:
		boolean useLight = this.useLightPerspective();
		int returnVal = mGameModel.getImageOfPieceAtSquare(position, useLight);
		return returnVal;
	}
	
	public boolean isValidMove(int startPosit, int endPosit)
	{
		// Call through to model:
		// Used for checking if move puts king in check:
		boolean checkOnlyKingCapture = false;
		boolean returnVal = 
			mGameModel.isValidMove(startPosit, endPosit, checkOnlyKingCapture);
		return returnVal;
	}
	
	public void promoteLightPawn(GameModel.pc toPiece)
	{
		// Call through to model:
		mGameModel.promoteLightPawn(toPiece);
		this.notifyDataSetChanged();
	}
	
	public void undoMove()
	{
		// Call through to model:
		mGameModel = mGameModel.getModelForUndo();
		this.notifyDataSetChanged();
	}
	
	public boolean hasGameStarted()
	{
		// Call through to model:
		boolean returnVal = mGameModel.hasGameStarted();
		return returnVal;
	}
	
	public boolean isSquareEmpty(int position)
	{
		// Call through to model:
		boolean returnVal = mGameModel.isSquareEmpty(position);
		return returnVal;
	}
	
	public void setLightAI1(boolean enabled)
	{
		mGameModel.setLightAI1(enabled);
	}
	
	public boolean getLightAI1()
	{
		return mGameModel.getLightAI1();
	}
	
	public void setLightAI2(boolean enabled)
	{
		mGameModel.setLightAI2(enabled);
	}
	
	public boolean getLightAI2()
	{
		return mGameModel.getLightAI2();
	}
	
	public void setDarkAI1(boolean enabled)
	{
		mGameModel.setDarkAI1(enabled);
	}
	
	public boolean getDarkAI1()
	{
		return mGameModel.getDarkAI1();
	}
	
	public void setDarkAI2(boolean enabled)
	{
		mGameModel.setDarkAI2(enabled);
	}
	
	public boolean getDarkAI2()
	{
		return mGameModel.getDarkAI2();
	}
	
  // Private methods:
	
	private int initialBoardWidth()
	{
    	// Just set the board width to the smaller screen dimension for now:
    	
        Display display = ((WindowManager) mContext.getSystemService(
        				   Context.WINDOW_SERVICE)).getDefaultDisplay();
        
        int screenWidth = display.getWidth();
        int screenHeight = display.getHeight();
        
        int initialWidth = Math.min(screenWidth, screenHeight);
        
        return initialWidth;
	}
	
	private int squareWidth()
	{
		int[] paddingAndBoardWidth = Utilities.findPaddingValuesAndWidthOfBoard(
										 mBoardWidthWithBorder_inPixels,
										 mIsBoardExpanded );
	        
	    int strictBoardWidth = paddingAndBoardWidth[4];
	        
	    if( 0 != strictBoardWidth % 8 )
			throw new AssertionError("Board width not divisible by 8");
	        
	    int squareWidth = strictBoardWidth / 8;
	    
	    return squareWidth;
	}
	
	private boolean useLightPerspective()
	{
		boolean useLight = false;
		
		if( mIsAutoRotateEnabled && isLightsTurn() )
			useLight = true;
		else if( mIsAutoRotateEnabled && !isLightsTurn() )
			useLight = false;
		else if( mWasRotateDisabledDuringLightsTurn )
			useLight = true;
		else 
			useLight = false;
			
		return useLight;
	}
}