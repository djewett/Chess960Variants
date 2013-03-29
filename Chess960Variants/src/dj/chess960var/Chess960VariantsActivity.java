package dj.chess960var;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

public class Chess960VariantsActivity extends Activity 
{	
	// TODO: Try to do this using Bundle:
	static GameModel mGameModelForRestore;
	
	private PopupWindow mPawnPromoPopup;
	
	private boolean mIsPawnPromo;
	private boolean mLightHasRandomized;
	private boolean mDarkHasRandomized;
	private int mStartOfMoveSquare;
	private boolean mHasEnteredOtherSquare;
	
	private boolean mIsSoundEnabled;
	
	// For touch-start-square-touch-end-square (two touch) functionality:
	private final int mINVALIDSQUARE = -20;
	
	@Override public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
	  // Note: These need to be done BEFORE setContentView(R.layout.main):
		// Remove title bar:
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar, disable dimming / automatic screen shutoff:
		this.getWindow().addFlags( 
			WindowManager.LayoutParams.FLAG_FULLSCREEN | 
			WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
		
        setContentView(R.layout.main);
        
		this.setUpButtons();

        GridView gv1 = (GridView) findViewById(R.id.boardGrid);
        
		if(null == savedInstanceState)
		{
			mIsPawnPromo = false;
			mLightHasRandomized = false;
			mDarkHasRandomized = false;
			mStartOfMoveSquare = mINVALIDSQUARE;
			mHasEnteredOtherSquare = false;
			mIsSoundEnabled = true;
			
			gv1.setAdapter(new BoardImageAdapter(this));
		}
		else
		{
			mIsPawnPromo = savedInstanceState.getBoolean("is pawn promo");
			mLightHasRandomized = 
				savedInstanceState.getBoolean("light has randomized");
			mDarkHasRandomized = 
				savedInstanceState.getBoolean("dark has randomized");
			mStartOfMoveSquare = 
				savedInstanceState.getInt("start of move square");
			mHasEnteredOtherSquare = 
				savedInstanceState.getBoolean("has entered other square");
			
			mIsSoundEnabled = savedInstanceState.getBoolean("is sound enabled");
			
			boolean autoRotateEnabled = 
				savedInstanceState.getBoolean("is auto rotate enabled");
			boolean rotateDisabledDuringLightsTurn =
				savedInstanceState.getBoolean(
					"rotate disabled during lights turn");
			
			boolean expandEnabled = 
				savedInstanceState.getBoolean("is expand enabled");
			
			BoardImageAdapter newAdap = 
				new BoardImageAdapter( this,
									   mGameModelForRestore,
									   autoRotateEnabled,
									   expandEnabled,
									   rotateDisabledDuringLightsTurn );
			
			gv1.setAdapter( newAdap );
		}
		
        gv1.setOnTouchListener( new Chess960VariantsOnTouchListener() );
    }
	
	@Override public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		
		// Use Bundle to save primitive types for restoring after rotation:
		outState.putBoolean("is pawn promo", mIsPawnPromo);
		outState.putBoolean("light has randomized", mLightHasRandomized);
		outState.putBoolean("dark has randomized", mDarkHasRandomized);
		outState.putInt("start of move square", mStartOfMoveSquare);
		outState.putBoolean("has entered other square", mHasEnteredOtherSquare);
		outState.putBoolean("is sound enabled", mIsSoundEnabled);
		
		boolean isAutoRotateEnabled = 
			getBoardImageAdapter().getIsAutoRotateEnabled();
		outState.putBoolean("is auto rotate enabled", isAutoRotateEnabled);
		
		boolean rotateDisabledDuringLightsTurn =
			getBoardImageAdapter().getRotateDisabledDuringLightsTurn();
		outState.putBoolean("rotate disabled during lights turn", 
							rotateDisabledDuringLightsTurn);
		
		boolean isExpandEnabled = 
			getBoardImageAdapter().getIsExpanded();
		outState.putBoolean("is expand enabled", isExpandEnabled);
		
        // Save the modeled game for restoring it (ie. after screen has been
        // rotated, for example):
		mGameModelForRestore = this.getBoardImageAdapter().getGameModel();
		
		// If we rotate the screen in the middle of a pawn promo, we need to 
		// dismiss the pawn promo popup and redisplay it after all the drawing
		// has been completed for the rotation:
		if(mIsPawnPromo)
			mPawnPromoPopup.dismiss();
	}
	
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		redrawScreen();
	}

	public void resetLight(View view)
	{   
        int lightPiecesRow = 0;
		this.getBoardImageAdapter().resetRow(lightPiecesRow);
        
		Button lightResetButton = (Button) findViewById(R.id.light_reset);
		lightResetButton.setEnabled(false);
		
		mLightHasRandomized = false;
		
		this.clearHighlightsOnBoard();
	}
	
	public void resetDark(View view)
	{
        int darkPiecesRow = 7;
        this.getBoardImageAdapter().resetRow(darkPiecesRow);
        
		Button darkResetButton = (Button) findViewById(R.id.dark_reset);
		darkResetButton.setEnabled(false);
		
		mDarkHasRandomized = false;
		
		this.clearHighlightsOnBoard();
	}
	
	public void randomizeLightPieces(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
        
        int lightPiecesRow = 0;
        
        this.getBoardImageAdapter().randomizeRow(lightPiecesRow);
        
		Button lightResetButton = (Button) findViewById(R.id.light_reset);
		lightResetButton.setEnabled(true);
		
		mLightHasRandomized = true;
		
		this.clearHighlightsOnBoard();
	}
	
	public void randomizeDarkPieces(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
        
        int darkPiecesRow = 7;
        
        this.getBoardImageAdapter().randomizeRow(darkPiecesRow);
        
		Button darkResetButton = (Button) findViewById(R.id.dark_reset);
		darkResetButton.setEnabled(true);
		
		mDarkHasRandomized = true;
		
		this.clearHighlightsOnBoard();
	}
	
	public void restartLight(View view)
	{	
		// * Note: view passed in is a reference to the widget that was clicked
		
    	Button lightRestartButton = (Button) findViewById(R.id.light_restart);
		Button darkRestartButton = (Button) findViewById(R.id.dark_restart);
       
        if( !darkRestartButton.isEnabled() )
        {
        	this.getBoardImageAdapter().restart();
        	mLightHasRandomized = false;
        	this.setUpButtons();
    		this.clearHighlightsOnBoard();
        }
        else
        {
        	lightRestartButton.setEnabled(false);
        	// Wait until BOTH restart buttons are pressed
        }
	}
	
	public void restartDark(View view)
	{	
		// * Note: view passed in is a reference to the widget that was clicked
		
		Button lightRestartButton = (Button) findViewById(R.id.light_restart);
		Button darkRestartButton = (Button) findViewById(R.id.dark_restart);
        
        if( !lightRestartButton.isEnabled() )
        {
        	this.getBoardImageAdapter().restart();
        	mDarkHasRandomized = false;
    		this.setUpButtons();
    		this.clearHighlightsOnBoard();
        }
        else
        {
        	darkRestartButton.setEnabled(false);
        	// Wait until BOTH restart buttons are pressed
        }
	}
	
	public void undoLight(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		Button darkUndoButton = (Button) findViewById(R.id.dark_undo);
        
        if( !darkUndoButton.isEnabled() )
        {
        	this.getBoardImageAdapter().undoMove();
        	this.setUpButtons();
        	this.clearHighlightsOnBoard();
        }
        else
        {
        	Button lightUndoButton = (Button) findViewById(R.id.light_undo);
        	lightUndoButton.setEnabled(false);
        	// Wait until BOTH undo buttons are pressed
        }
	}
	
	public void undoDark(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		Button lightUndoButton = (Button) findViewById(R.id.light_undo);
		
        if( !lightUndoButton.isEnabled() )
        {
        	this.getBoardImageAdapter().undoMove();
        	this.setUpButtons();
        	this.clearHighlightsOnBoard();
        }
        else
        {
        	Button darkUndoButton = (Button) findViewById(R.id.dark_undo);
        	darkUndoButton.setEnabled(false);
        	// Wait until BOTH undo buttons are pressed
        }
	}
	
	public void toggleSound(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		// Negate current value:
		mIsSoundEnabled = !mIsSoundEnabled;
		
		ImageButton soundToggleButton = 
			(ImageButton) findViewById(R.id.soundToggleButton);
		
		if(mIsSoundEnabled)
			soundToggleButton.setImageResource(R.drawable.sound_enabled);
		else
			soundToggleButton.setImageResource(R.drawable.sound_disabled);
	}
	
	public void toggleAutoRotate(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		// Negate current value:
		boolean isAutoRotateEnabled = 
			!getBoardImageAdapter().getIsAutoRotateEnabled();
		
		getBoardImageAdapter().setIsAutoRotateEnabled(isAutoRotateEnabled);
		
		ImageButton autoRotateToggleButton = 
			(ImageButton) findViewById(R.id.autoRotateToggleButton);
		
		if(isAutoRotateEnabled)
			autoRotateToggleButton.setImageResource(
				R.drawable.auto_rotate_enabled);
		else
			autoRotateToggleButton.setImageResource(
				R.drawable.auto_rotate_disabled);
	}
	
	public void toggleBoardExpand(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		// Negate current value:
		boolean isExpanded = !getBoardImageAdapter().getIsExpanded();
		
		getBoardImageAdapter().setIsExpanded(isExpanded);
		
		ImageButton expandButtonToggleButton = 
			(ImageButton) findViewById(R.id.expandButton);
		
		if(isExpanded)
			expandButtonToggleButton.setImageResource(
				R.drawable.expand_enabled);
		else
			expandButtonToggleButton.setImageResource(
				R.drawable.expand_disabled);
		
		this.redrawScreen();
	}
	
	public void toggleLightAI1(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		ImageButton lightAI1ToggleButton = 
			(ImageButton) findViewById(R.id.lightAI1ToggleButton);
		
		if( getBoardImageAdapter().getLightAI1() )
		{
			lightAI1ToggleButton.setImageResource(R.drawable.light_ai1_disabled);
			getBoardImageAdapter().setLightAI1(false);
		}
		else
		{
			// Shut off AI 2 before enabling AI 1:
			
			ImageButton lightAI2ToggleButton = 
				(ImageButton) findViewById(R.id.lightAI2ToggleButton);
			lightAI2ToggleButton.setImageResource(R.drawable.light_ai2_disabled);
			getBoardImageAdapter().setLightAI2(false);
			
			lightAI1ToggleButton.setImageResource(R.drawable.light_ai1_enabled);
			getBoardImageAdapter().setLightAI1(true);
		}
	}
	
	public void toggleLightAI2(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		ImageButton lightAI2ToggleButton = 
			(ImageButton) findViewById(R.id.lightAI2ToggleButton);
		
		if( getBoardImageAdapter().getLightAI2() )
		{
			lightAI2ToggleButton.setImageResource(R.drawable.light_ai2_disabled);
			getBoardImageAdapter().setLightAI2(false);
		}
		else
		{
			// Shut off AI 1 before enabling AI 2:
			
			ImageButton lightAI1ToggleButton = 
				(ImageButton) findViewById(R.id.lightAI1ToggleButton);
			lightAI1ToggleButton.setImageResource(R.drawable.light_ai1_disabled);
			getBoardImageAdapter().setLightAI1(false);
			
			lightAI2ToggleButton.setImageResource(R.drawable.light_ai2_enabled);
			getBoardImageAdapter().setLightAI2(true);
		}
	}
	
	public void toggleDarkAI1(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		ImageButton darkAI1ToggleButton = 
			(ImageButton) findViewById(R.id.darkAI1ToggleButton);
		
		if( getBoardImageAdapter().getDarkAI1() )
		{
			darkAI1ToggleButton.setImageResource(R.drawable.dark_ai1_disabled);
			getBoardImageAdapter().setDarkAI1(false);
		}
		else
		{
			// Shut off AI 2 before enabling AI 1:
			
			ImageButton darkAI2ToggleButton = 
				(ImageButton) findViewById(R.id.darkAI2ToggleButton);
			darkAI2ToggleButton.setImageResource(R.drawable.dark_ai2_disabled);
			getBoardImageAdapter().setDarkAI2(false);
			
			darkAI1ToggleButton.setImageResource(R.drawable.dark_ai1_enabled);
			getBoardImageAdapter().setDarkAI1(true);
		}
	}
	
	public void toggleDarkAI2(View view)
	{
		// * Note: view passed in is a reference to the widget that was clicked
		
		ImageButton darkAI2ToggleButton = 
			(ImageButton) findViewById(R.id.darkAI2ToggleButton);
		
		if( getBoardImageAdapter().getDarkAI2() )
		{
			darkAI2ToggleButton.setImageResource(R.drawable.dark_ai2_disabled);
			getBoardImageAdapter().setDarkAI2(false);
		}
		else
		{
			// Shut off AI 1 before enabling AI 2:
			
			ImageButton darkAI1ToggleButton = 
				(ImageButton) findViewById(R.id.darkAI1ToggleButton);
			darkAI1ToggleButton.setImageResource(R.drawable.dark_ai1_disabled);
			getBoardImageAdapter().setDarkAI1(false);
			
			darkAI2ToggleButton.setImageResource(R.drawable.dark_ai2_enabled);
			getBoardImageAdapter().setDarkAI2(true);
		}
	}
	
	public void selectLightQueen(View view)
	{
        selectPiece(GameModel.pc.lQ);
	}
	
	public void selectLightRook(View view)
	{
        selectPiece(GameModel.pc.lR);
	}
	
	public void selectLightBishop(View view)
	{
        selectPiece(GameModel.pc.lB);
	}
	
	public void selectLightKnight(View view)
	{
        selectPiece(GameModel.pc.lN);
	}
	
	public void selectDarkQueen(View view)
	{
        selectPiece(GameModel.pc.dQ);
	}
	
	public void selectDarkRook(View view)
	{
        selectPiece(GameModel.pc.dR);
	}
	
	public void selectDarkBishop(View view)
	{
        selectPiece(GameModel.pc.dB);
	}
	
	public void selectDarkKnight(View view)
	{
        selectPiece(GameModel.pc.dN);
	}
	
	private void selectPiece(GameModel.pc thePiece)
	{
		mPawnPromoPopup.dismiss();	
		mIsPawnPromo = false;
		unGreyOutPopupBackground();
        getBoardImageAdapter().promoteLightPawn(thePiece);
        
        // Pawn promotion causes some temporary minor problems with display.
        // This line has been added to avoid these problems:
        redrawScreen();
	}
	
	private void unGreyOutPopupBackground()
	{
	    float unGreyOut = (float) 1;
	    ((LinearLayout) findViewById(R.id.rootlayout)).setAlpha(unGreyOut);
	}
	
	private BoardImageAdapter getBoardImageAdapter()
	{
		GridView gv1 = (GridView) findViewById(R.id.boardGrid);
		BoardImageAdapter boardImageAdap = (BoardImageAdapter) gv1.getAdapter();
        return boardImageAdap;
	}
	
	private void setUpButtons()
	{
		// Enables/disables button based on whether or not the game has started.  
		// Note: This method does not cover all cases (such as enabling Reset 
		// after Randomize has been clicked) - these must be handled separately 
		// and individually.
		
		if( null != this.getBoardImageAdapter() && 
		    this.getBoardImageAdapter().hasGameStarted() )
		{
			// Enable buttons that should be enabled:
			Button lightRestButton = (Button) findViewById(R.id.light_restart);
			lightRestButton.setEnabled(true);
			Button darkRestButton = (Button) findViewById(R.id.dark_restart);
			darkRestButton.setEnabled(true);
			Button lightUndoButton = (Button) findViewById(R.id.light_undo);
			lightUndoButton.setEnabled(true);
			Button darkUndoButton = (Button) findViewById(R.id.dark_undo);
			darkUndoButton.setEnabled(true);
			
			// Disable buttons that should be disabled:
			Button lightResetButton = (Button) findViewById(R.id.light_reset);
			lightResetButton.setEnabled(false);
			Button lightRandButt = (Button) findViewById(R.id.light_randomize);
			lightRandButt.setEnabled(false);
			Button darkResetButton = (Button) findViewById(R.id.dark_reset);
			darkResetButton.setEnabled(false);
			Button darkRandButton = (Button) findViewById(R.id.dark_randomize);
			darkRandButton.setEnabled(false);
		}
		else
		{
			// Enable buttons that should be enabled:
			Button lightRandButt = (Button) findViewById(R.id.light_randomize);
			lightRandButt.setEnabled(true);
			Button darkRandButton = (Button) findViewById(R.id.dark_randomize);
			darkRandButton.setEnabled(true);
			
			// Disable buttons that should be disabled:
			Button lightRestButton = (Button) findViewById(R.id.light_restart);
			lightRestButton.setEnabled(false);
			Button darkRestButton = (Button) findViewById(R.id.dark_restart);
			darkRestButton.setEnabled(false);
			Button lightUndoButton = (Button) findViewById(R.id.light_undo);
			lightUndoButton.setEnabled(false);
			Button darkUndoButton = (Button) findViewById(R.id.dark_undo);
			darkUndoButton.setEnabled(false);
			
			// Reset buttons depend on whether corresponding Randomize button
			// has been pushed:
			Button lightResetButton = (Button) findViewById(R.id.light_reset);
			lightResetButton.setEnabled(mLightHasRandomized);
			Button darkResetButton = (Button) findViewById(R.id.dark_reset);
			darkResetButton.setEnabled(mDarkHasRandomized);
		}
	}
	
	private void redrawScreen()
	{
		LinearLayout rootLayout = (LinearLayout) findViewById(R.id.rootlayout);
		
		int width = rootLayout.getWidth();
		int height = rootLayout.getHeight();
		
		int newBoardWidth_withBorder = Math.min(width, height);
		
		int largerDimension = Math.max(width, height);
		
		// Find the new width and height of frameLayout1 and frameLayout3:
		// Note: We're doing it this way to avoid needing to know the default
		// orientation of a device, which can be tricky to find.
		
		// It's also important to note that there could be some scenarios where
		// even though the screen is in portrait mode, the LinearLayout 
		//(rootLayout) portion of this app. could still potentially be a 
		// landscape (ie. due to the existence of the title bar, etc.)!
		
		int sideFrame_width = 0;
		int sideFrame_height = 0;

		int textSize;

		if( height > width )
		{
			// Do this to ensure that even in the case where the device is
			// positioned horizontally but the available screen space for the 
			// rootLayout is a portrait for some reason:

			rootLayout.setOrientation(LinearLayout.VERTICAL);
			
			// LinearLayout portion is a "portrait":
			
			// Note: Shouldn't be any integer division problems here 
			// (truncating / rounding down will always be acceptable):
			sideFrame_height = (largerDimension - newBoardWidth_withBorder)/4;
			sideFrame_width = newBoardWidth_withBorder;

			textSize = sideFrame_width/37; //sideFrame_width/34;
			
			FrameLayout dcp = 
				(FrameLayout) findViewById(R.id.darkCapturedPieces);
			dcp.setLayoutParams( new LinearLayout.LayoutParams(
				sideFrame_width, sideFrame_height) );
			
			LinearLayout dbl = 
				(LinearLayout) findViewById(R.id.darkButtonLayout);
			dbl.setLayoutParams( new LinearLayout.LayoutParams(
				sideFrame_width, sideFrame_height) );
			
			LinearLayout lbl = 
				(LinearLayout) findViewById(R.id.lightButtonLayout);
			lbl.setLayoutParams( new LinearLayout.LayoutParams(
				sideFrame_width, sideFrame_height) );
			
			FrameLayout lcp = 
				(FrameLayout) findViewById(R.id.lightCapturedPieces);
			lcp.setLayoutParams( new LinearLayout.LayoutParams(
				sideFrame_width, sideFrame_height) );
			
			// Add drag listener for all dialogs outside of board so that if a 
			// piece is dragged there, nothing happens:
			dcp.setOnDragListener(new OnDragListenerForDragOffBoard());
			dbl.setOnDragListener(new OnDragListenerForDragOffBoard());
			lbl.setOnDragListener(new OnDragListenerForDragOffBoard());
			lcp.setOnDragListener(new OnDragListenerForDragOffBoard());
		}
		else if( height < width )
		{
			// Do this to ensure that even in the case where the device is
			// positioned vertically but the available screen space for the 
			// rootLayout is a landscape due to the existence of a title bar, 
			// for example):
			
			rootLayout.setOrientation(LinearLayout.HORIZONTAL);
			
			// LinearLayout portion is "landscape":
			
			sideFrame_width = largerDimension - newBoardWidth_withBorder;
			
			sideFrame_height = newBoardWidth_withBorder;
			
			textSize = sideFrame_width/20; //sideFrame_width/18;
			
			LinearLayout dialogLayout = 
				(LinearLayout) findViewById(R.id.dialogLayout);
			
			LinearLayout.LayoutParams dialogLayoutParams = 
				new LinearLayout.LayoutParams(sideFrame_width,sideFrame_height);
			
			dialogLayout.setLayoutParams(dialogLayoutParams);
			
			// Add drag listener for dialog so that if a piece is
			// dragged there, nothing happens:
			dialogLayout.setOnDragListener(new OnDragListenerForDragOffBoard());
			
			// Redraw the toggle buttons ONLY if they should currently be 
			// disabled (they are enabled by default):
			
			if(!mIsSoundEnabled)
			{
				ImageButton soundToggleButton = 
					(ImageButton) findViewById(R.id.soundToggleButton);
				soundToggleButton.setImageResource(
					R.drawable.sound_disabled);
			}
			
			if( !getBoardImageAdapter().getIsAutoRotateEnabled() )
			{
				ImageButton autoRotateToggleButton = 
					(ImageButton) findViewById(R.id.autoRotateToggleButton);
				autoRotateToggleButton.setImageResource(
					R.drawable.auto_rotate_disabled);
			}
			
			// Expand is DISABLED by default:
			if( getBoardImageAdapter().getIsExpanded() )
			{
				ImageButton expandToggleButton = 
					(ImageButton) findViewById(R.id.expandButton);
				expandToggleButton.setImageResource(
					R.drawable.expand_enabled);
			}
			
			if( getBoardImageAdapter().getGameModel().getLightAI1() )
			{
				ImageButton lightAI1ToggleButton = 
					(ImageButton) findViewById(R.id.lightAI1ToggleButton);

				lightAI1ToggleButton.setImageResource(R.drawable.light_ai1_enabled);
			}
			
			if( getBoardImageAdapter().getGameModel().getLightAI2() )
			{
				ImageButton lightAI2ToggleButton = 
					(ImageButton) findViewById(R.id.lightAI2ToggleButton);

				lightAI2ToggleButton.setImageResource(R.drawable.light_ai2_enabled);
			}
			
			if( getBoardImageAdapter().getGameModel().getDarkAI1() )
			{
				ImageButton darkAI1ToggleButton = 
					(ImageButton) findViewById(R.id.darkAI1ToggleButton);

				darkAI1ToggleButton.setImageResource(R.drawable.dark_ai1_enabled);
			}
			
			if( getBoardImageAdapter().getGameModel().getDarkAI2() )
			{
				ImageButton darkAI2ToggleButton = 
					(ImageButton) findViewById(R.id.darkAI2ToggleButton);

				darkAI2ToggleButton.setImageResource(R.drawable.dark_ai2_enabled);
			}
		}
		else
		{
			// height == width (square portion of screen available for 
			// displaying frames 1, 2 and 3)
			
			// Need to be careful in this case to avoid zero-width frame 1 and 
			// frame 3.  Should shrink board down a bit (maybe to ~80% in order
			// to make room for frame 1 and frame 3 on the screen).
			
			// Suggestion: In this case, "chop" a portion off the width of 
			// rootLayout, to automatically turn it into a (centered) vertical
			// layout similar to the one for the height > width case.
			
			// TODO: Consider using a new layout for a square screen.  This new
			// layout might be a mixture between the above portrait and 
			// landscape layouts.  Maybe something like this:
			// 	   	_____________________________________
			//		|___button__button__button__button__|
			//		|				|					|
			//		|	volume and	|	   square		|
			//		|  auto-rotate	|		 board		|
			//		|	 buttons	|					|
			//		|_______________|___________________|
			//		|___button__button__button__button__|
			
			throw new AssertionError("Not implemented yet");
		}
		
		// Rotate only buttons for dark side:
		final int darkRot = 180;
		((Button) findViewById(R.id.dark_undo)).setRotation(darkRot);
		((Button) findViewById(R.id.dark_restart)).setRotation(darkRot);
		((Button) findViewById(R.id.dark_randomize)).setRotation(darkRot);
		((Button) findViewById(R.id.dark_reset)).setRotation(darkRot);
		
		// Set text sizes for all buttons:
		((Button) findViewById(R.id.light_undo)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		((Button) findViewById(R.id.light_restart)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		((Button) findViewById(R.id.light_randomize)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		((Button) findViewById(R.id.light_reset)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		((Button) findViewById(R.id.dark_undo)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		((Button) findViewById(R.id.dark_restart)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		((Button) findViewById(R.id.dark_randomize)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		((Button) findViewById(R.id.dark_reset)).setTextSize(
			TypedValue.COMPLEX_UNIT_SP, textSize);
		
		// centerFrame is the one containing the board.  Change centerFrame's 
		// height and width to newBoardWidth_WithBorder:
		
		FrameLayout cFrame = (FrameLayout) findViewById(R.id.centerFrame);
		
		LinearLayout.LayoutParams cfLayoutParams = 
			new LinearLayout.LayoutParams(newBoardWidth_withBorder,
										  newBoardWidth_withBorder);
		
		cFrame.setLayoutParams(cfLayoutParams);
		
		int[] paddingAndBoardWidth = 
			Utilities.findPaddingValuesAndWidthOfBoard(
				newBoardWidth_withBorder,
				getBoardImageAdapter().getIsExpanded() );
				
		cFrame.setPadding(paddingAndBoardWidth[0],paddingAndBoardWidth[1],
						  paddingAndBoardWidth[2],paddingAndBoardWidth[3]);
				
		cFrame.setClipToPadding(true);
		
		BoardImageAdapter boardImageAdap = getBoardImageAdapter(); 
		
		boardImageAdap.setBoardWidth_inPixels(newBoardWidth_withBorder);
		
		// We need to negate here because we want to know whose turn it was
		// BEFORE the move was made:
		boolean isLightsTurn = !boardImageAdap.isLightsTurn();
		int newBoardWidth_withoutBorder = paddingAndBoardWidth[4];
		if(mIsPawnPromo && null==mPawnPromoPopup)
			displayPawnPromoPopup(isLightsTurn, newBoardWidth_withoutBorder);
	    
		// Add drag listener for border around board so that if a piece is
		// dragged there, nothing happens:
		cFrame.setOnDragListener( new OnDragListenerForDragOffBoard() );
		
		this.setUpButtons();
		this.clearHighlightsOnBoard();
	}
	
	private void displayPawnPromoPopup(boolean isLightsTurn, int boardWidth)
	{
	    LayoutInflater inflater = (LayoutInflater)
		    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		GridView gv1 = (GridView) findViewById(R.id.boardGrid);
	    	
	    // A little algebra to get piece selection buttons centered with 5dp 
		// padding:
		int popupWidth = 7*boardWidth/8;
		int popupHeight = 7*popupWidth/32+10;
		    	
		// Retrieve the appropriate layout (ie. for light or dark pieces):
		int popupLayout;
		if(isLightsTurn)
		    popupLayout = R.layout.pawn_promo_popup_light;
		else
		    popupLayout = R.layout.pawn_promo_popup_dark;
		   	
		mPawnPromoPopup = new PopupWindow(
								  inflater.inflate(popupLayout, null, false), 
								  popupWidth, 
								  popupHeight, 
								  true);
		    
		// Grey out everything behind pawn promo popup:
		float greyOut = (float) 0.5;
		((LinearLayout) findViewById(R.id.rootlayout)).setAlpha(greyOut);
		    
		mPawnPromoPopup.showAtLocation(gv1, Gravity.CENTER, 0, 0);
	}
	
	private void playSound(int soundResource)
	{
		MediaPlayer mediaPlayer;
		mediaPlayer = MediaPlayer.create(this, soundResource);
		mediaPlayer.start();
	}
	
	private void clearHighlightsOnBoard()
	{
		// Clears any highlighted squares and resets any square selected for
		// two-touch style move:
			
		GridView gv1 = (GridView) findViewById(R.id.boardGrid);
        int count = gv1.getChildCount();
            
        for (int i = 0; i < count; i++)
        	if( !getBoardImageAdapter().isSquareEmpty(i) )
        		((ImageView) gv1.getChildAt(i)).setBackgroundDrawable(null);
        
        mStartOfMoveSquare = mINVALIDSQUARE;
	}
	
	private class OnDragListenerForDragOffBoard implements View.OnDragListener
	{
		// This drag listener is simply responsible for handling the case
		// where a piece is dragged off the board, in which case we essentially
		// want nothing to happen
		
		public boolean onDrag(View v, DragEvent event) 
    	{
            boolean result = true;
            
            int action = event.getAction();
            
            switch (action)
            {
         
            case DragEvent.ACTION_DRAG_STARTED:
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DRAG_LOCATION:
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DRAG_ENTERED:
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DRAG_EXITED:
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DROP:
                if (event.getLocalState() == v) 
                {
                    result = false;
                } 
                else
                {
                	clearHighlightsOnBoard();
                	getBoardImageAdapter().notifyDataSetChanged();
                }
                break;
                
            case DragEvent.ACTION_DRAG_ENDED:
                break;
                
            default:
                result = false;
                break;
                
            }
            
            return result;
    	}
	}
	
	private class Chess960VariantsOnDragListener implements View.OnDragListener
	{
		private int mStartPos;
		
		public Chess960VariantsOnDragListener(int startPosition)
		{
			mStartPos = startPosition;
		}
		
		public boolean onDrag(View v, DragEvent event) 
    	{
            boolean result = true;
            
            int action = event.getAction();
            
            GridView gv1 = (GridView) findViewById(R.id.boardGrid);
            
            int endPosit = gv1.getPositionForView(v);
            
            BoardImageAdapter boardImageAdap = 
            	(BoardImageAdapter) gv1.getAdapter();
            
            switch (action)
            {
         
            case DragEvent.ACTION_DRAG_STARTED:
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DRAG_LOCATION:
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DRAG_ENTERED:
                if( boardImageAdap.isValidMove(mStartPos,endPosit) )
                {
                	((ImageView) gv1.getChildAt(endPosit)).
                	setBackgroundResource(R.drawable.square_border);
                	
                	mHasEnteredOtherSquare = true;
                }
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DRAG_EXITED:
                if( boardImageAdap.isValidMove(mStartPos,endPosit) )
                {
                	((ImageView) gv1.getChildAt(endPosit)).
                	setBackgroundDrawable(null);
                }
            	v.invalidate();
                break;
                
            case DragEvent.ACTION_DROP:
                if (event.getLocalState() == v) 
                {
                    result = false;
                } 
                else
                {
                	ImageView startSquareImageView = 
                		(ImageView) gv1.getChildAt(mStartPos);
                	
                	if(mStartPos == endPosit && !mHasEnteredOtherSquare)
                	{
                		// Scenario: Piece has been touched and has begun to be 
                		// dragged, but has not been dragged off its starting 
                		// square - interpret this as the first touch of a 
                		// two-touch sequence to move a piece from start square
                		// to end square.
                		
                		mStartOfMoveSquare = mStartPos;
                		startSquareImageView.setBackgroundResource(
                			R.drawable.square_border);
                		startSquareImageView.setImageResource(
                        	boardImageAdap.getImageOfPieceAtSquare(mStartPos) );
                	}
                	else if(mStartPos == endPosit && mHasEnteredOtherSquare)
                	{
                		// Scenario: Piece has been dragged off its start square
                		// and then back onto its start square where it is 
                		// dropped - interpret this as the player deciding not
                		// to move that piece and return to state that existed
                		// before the player ever touched the piece.
                		
                		mStartOfMoveSquare = mINVALIDSQUARE;
                		mHasEnteredOtherSquare = false;
                		startSquareImageView.setBackgroundDrawable(null);
                		startSquareImageView.setImageResource(
                    		boardImageAdap.getImageOfPieceAtSquare(mStartPos) );
                	}
                	else if( boardImageAdap.isValidMove(mStartPos,endPosit) )
                	{
                		// Scenario: Piece has been dragged and dropped and it's
                		// a valid move.  This is the typical case where we need
                		// to make appropriate updates after a valid move.

                		startSquareImageView.setBackgroundDrawable(null);
                		
                		// Need to do this before call to updateAfterMove():
                		boolean isLightsTurn = boardImageAdap.isLightsTurn();
                		
                		mIsPawnPromo = boardImageAdap.updateAfterMove(
                						   mStartPos,endPosit);
                		
                		String nextMove = AIEngine.getNextMove( 
                							  boardImageAdap.getGameModel() );
                		TextView lightAITextOutputBox = 
                				(TextView) findViewById(R.id.lightAITextView);
                		lightAITextOutputBox.setText(nextMove);
                		
                		setUpButtons();
                		
                		if(mIsPawnPromo)
                		{
                			int boardWidth = gv1.getWidth();
                			displayPawnPromoPopup(isLightsTurn,boardWidth);
                		}

                		// To get rid of highlighting on drop square (from 
                		// before drop):
                		View endPosView = (View) gv1.getChildAt(endPosit);
                		endPosView.setBackgroundDrawable(null);

                		if(mIsSoundEnabled)
                			playSound(R.raw.beep_move);
                		
                		// Reset for next move:
                		mStartOfMoveSquare = mINVALIDSQUARE;
                		mHasEnteredOtherSquare = false;
                	}
                	else
                	{
                		// Scenario: Don't allow move.

                		// Need to reset the image at the start square because  
                		// when we drag, we temporarily remove that image:
                		startSquareImageView.setImageResource(
                			boardImageAdap.getImageOfPieceAtSquare(mStartPos) );
                		startSquareImageView.setBackgroundDrawable(null);
                		result = false;
                	}
                	
                	boardImageAdap.notifyDataSetChanged();
                }
                break;
                
            case DragEvent.ACTION_DRAG_ENDED:
                break;
                
            default:
                result = false;
                break;
                
            }
            
            return result;
    	}
	}
	
	private class Chess960VariantsOnTouchListener 
		implements View.OnTouchListener
	{
		private int currPosit;
    	
		public boolean onTouch(View v, MotionEvent event) 
		{
    		if (event.getAction() == MotionEvent.ACTION_DOWN) 
    		{
    			GridView parent = (GridView) v;

                int x = (int) event.getX();
                int y = (int) event.getY();
                
                // NOTE: Can use ClipData to send the startPosition with 
                // the start drag event.  However, that option is not 
                // available in Android platform 2.3.3, so I've opted to 
                // do it this way to keep the implementations as similar
                // as possible:
                currPosit = parent.pointToPosition(x, y);
                
                int count = parent.getChildCount();
                
                // To protect against problems related to dragging the 
                // piece off the board, here we want to clear all the
                // backgrounds of all the squares with pieces:
                for (int i = 0; i < count; i++)
                	if( currPosit != i && 
                		!getBoardImageAdapter().isSquareEmpty(i))
                			((ImageView) parent.getChildAt(i)).
                			setBackgroundDrawable(null);
                
    			if( mStartOfMoveSquare == mINVALIDSQUARE &&
    				getBoardImageAdapter().isValidForDrag(currPosit) &&
    				currPosit > AdapterView.INVALID_POSITION ) 
    			{
    				// Drag case (as opposed to two touch case):
    				
                    for (int i = 0; i < count; i++) 
                        parent.getChildAt(i).setOnDragListener( 
                            new Chess960VariantsOnDragListener(currPosit));
                        
                    ((ImageView) parent.getChildAt(currPosit)).
                    setBackgroundResource(R.drawable.square_border);
                    
                    boolean returnVal = this.startDrag(currPosit);
                            	
                    return returnVal;
    			}
    			
    			ImageView startSquareImageView = 
        			(ImageView) parent.getChildAt(mStartOfMoveSquare);
    			
    			if( mStartOfMoveSquare != mINVALIDSQUARE && 
    				getBoardImageAdapter().isValidMove(
    					mStartOfMoveSquare,currPosit) &&
					currPosit > AdapterView.INVALID_POSITION )
    			{
    				// Two touch case (as opposed to drag case), in which
    				// a start square has been selected and we have now 
    				// selected a valid end square with which to perform 
    				// the move:
    				
    				startSquareImageView.setBackgroundDrawable(null);
    				
    				// TODO: Factor with code in private class 
    				// Chess960VariantsOnDragListener:
                		
                	// Need to do this before call to updateAfterMove():
                	boolean isLightsTurn = 
                		getBoardImageAdapter().isLightsTurn();
                		
                	mIsPawnPromo = getBoardImageAdapter().updateAfterMove(
                					   mStartOfMoveSquare,currPosit);
                	
                	//TODO: Factor this code:
                	String nextMove = 
                		AIEngine.getNextMove( 
                			getBoardImageAdapter().getGameModel() );
            		TextView lightAITextOutputBox = 
            				(TextView) findViewById(R.id.lightAITextView);
            		lightAITextOutputBox.setText(nextMove);
                	
                	setUpButtons();
                		
                	if(mIsPawnPromo)
                	{
                		int boardWidth = parent.getWidth();
                		displayPawnPromoPopup(isLightsTurn,boardWidth);
                	}

                	// To get rid of highlighting on drop square (from 
                	// before drop):
                	View currPosView = (View) parent.getChildAt(currPosit);
                	currPosView.setBackgroundDrawable(null);

                	getBoardImageAdapter().notifyDataSetChanged();
                		
                	if(mIsSoundEnabled)
                		playSound(R.raw.beep_move);
                		
                	// Reset for next move:
                	mStartOfMoveSquare = mINVALIDSQUARE;
                	mHasEnteredOtherSquare = false;
                	
                	return true;
    			}
       			else if(mStartOfMoveSquare != mINVALIDSQUARE && 
       					mStartOfMoveSquare == currPosit)
       			{
       				// In this case, we have selected one square as the
       				// start square for a two touch move and just now
       				// selected the same square again because we want to 
       				// disable it as the start square:
       				
       				// * Need to check this case BEFORE the net one.
       				
       				int imageAtStartSquare = 
 						getBoardImageAdapter().
 						getImageOfPieceAtSquare(mStartOfMoveSquare);
       				
       				startSquareImageView.setImageResource(imageAtStartSquare);
       				startSquareImageView.setBackgroundDrawable(null);
             		
             		mStartOfMoveSquare = mINVALIDSQUARE;
       			    mHasEnteredOtherSquare = false;
       			    
       			    return true;
       			}
       			else if( mStartOfMoveSquare != mINVALIDSQUARE &&
       					 !getBoardImageAdapter().isValidMove(
       						 mStartOfMoveSquare,currPosit) &&
       					 getBoardImageAdapter().isValidForDrag(currPosit) && 
       					 currPosit > AdapterView.INVALID_POSITION )
       			{
       				// Two touch case where we have already selected a start
       				// square, but we wish to change it to another square
       				// (ie. we initially wanted to move one piece, but 
       				// changed our minds and now want to move another):
       				
					int imageAtStartSquare = 
    					getBoardImageAdapter().
    					getImageOfPieceAtSquare(mStartOfMoveSquare);
					
					startSquareImageView.setImageResource(imageAtStartSquare);
					startSquareImageView.setBackgroundDrawable(null);
                		
                	((ImageView) parent.getChildAt(currPosit)).
                    setBackgroundResource(R.drawable.square_border);
                		
                	mStartOfMoveSquare = currPosit;
           			mHasEnteredOtherSquare = false;
           				
           			return true;
       			}
    			else
				{
					// TODO: Factor with code in private class 
    				// Chess960VariantsOnDragListener:
					
            		// Don't allow move:

    				if( mStartOfMoveSquare != mINVALIDSQUARE )
    				{
    					// Need to reset the image at the start square 
    					// because by dragging, we temporarily remove that 
    					// image:
    					int imageAtStartSquare = 
    						getBoardImageAdapter().
    						getImageOfPieceAtSquare(mStartOfMoveSquare);
    					
    					startSquareImageView.setImageResource(
    						imageAtStartSquare);
    					startSquareImageView.setBackgroundDrawable(null);
    				}
            		
					mStartOfMoveSquare = mINVALIDSQUARE;
					mHasEnteredOtherSquare = false;
					
					getBoardImageAdapter().notifyDataSetChanged();

					return false;
				}
    		}
    		
    		return true;
		}
		
		private boolean startDrag(int currPosit)
		{
			GridView gv1 = (GridView) findViewById(R.id.boardGrid);
			
	      // Set up dragData:  
			
	        ImageView targetView = (ImageView) gv1.getChildAt(currPosit);
	        ClipData.Item item1 = new ClipData.Item("Not Used");
	        String[] mimeTypes = {ClipDescription.MIMETYPE_TEXT_PLAIN};
	        CharSequence sPOfMove = "startPositionOfMove";
	        ClipData dragData = new ClipData(sPOfMove,mimeTypes,item1);

	      // Start the drag:
	            	
	        // *** A workaround is needed here for what appears to be a bug
	        // (or unexpected behaviour) in the android code itself.  For
	        // some reason, GridViews are created in such a way that all
	        // Views at all positions in the GridView other than position
	        // 0 contain a non-null mAttachInfo member variable which is 
	        // used in View.startDrag().  Unfortunately, this member 
	        // variable is null for the View at position 0, resulting in
	        // a NullPointerException when trying ot drag the piece at
	        // position 0 if this workaround is not used.  To get around
	        // this, we temporarily "borrow" the View on the next square 
	        // over to complete the drag:
	        
	        ImageView viewForDragging;
	        if(0 == currPosit)
	        {
	        	// "Borrowed" neighboring view:
	        	viewForDragging = (ImageView) gv1.getChildAt(currPosit+1);
	        }
	        else
	        {
	        	// In all other cases we can simply use the actual 
	        	// targetView to do the drag:
	        	viewForDragging = targetView;
	        }
	        
	        // Remove and restore the highlighted piece background (brown
	        // square border) and restore it later at an appropriate time:
	        Drawable backgroundToRestore = targetView.getBackground();
	        targetView.setBackgroundDrawable(null);
	        
	        // It's OK to use the actual targetView to create the 
	        // DragShadowBuilder (ie. we don't need to "borrow" like above):
	        View.DragShadowBuilder dsBuilder = 
	            new View.DragShadowBuilder(targetView);
	            
	        boolean returnVal = viewForDragging.startDrag( 
	                				dragData, dsBuilder, null, 0 );
	        
	        // Remove image of original piece while piece is being dragged:
	        targetView.setImageDrawable(null);
	                	
	        // Restore borrowed_target:
	        targetView.setBackgroundDrawable(backgroundToRestore);
	        
	        return returnVal;
		}
	}
}