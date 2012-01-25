package dj.chess960var;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class TitlePageActivity extends Activity
{
	@Override public void onCreate(Bundle savedInstanceState) 
	{
	    super.onCreate(savedInstanceState);
	      
		// Note: These need to be done BEFORE setContentView(R.layout.main):
		//
		// Remove title bar:
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		// Remove notification bar:
		this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
								  WindowManager.LayoutParams.FLAG_FULLSCREEN);
	      
	    setContentView(R.layout.title_page);
	      
	    Thread splashThread = new Thread() {
	        @Override public void run() {
	            try {
	               int waited = 0;
	               // Set wait time here:
	               while (waited < 4000) {
	                  sleep(100);
	                  waited += 100;
	               }
	            } catch (InterruptedException e) {
	               // do nothing
	            } finally {
	               finish();
	               Intent i = new Intent();
	               i.setClassName("dj.chess960var",
	                              "dj.chess960var.Chess960VariantsActivity");
	               startActivity(i);
	            }
	        }
	    };
	      
	    splashThread.start();
	}
	
	@Override public void onWindowFocusChanged(boolean hasFocus)
	{
		super.onWindowFocusChanged(hasFocus);
		
		// Do some calculations here to get image displayed with correct
		// dimensions:
		
		LinearLayout rootLayout = 
			(LinearLayout) findViewById(R.id.titleRootLayout);
		
		int width = rootLayout.getWidth();
		int height = rootLayout.getHeight();
			
		LinearLayout.LayoutParams theLPs;
		
		// TODO: Try to do these calculations without hard-coding the
		// dimensions of the logo (ie. it would be safer to somehow read 
		// these values directly from the image, in case that image were
		// ever to change, for example):
		
		// The image we are using is 1046 by 603 pixels:
		double logoRatio = 603.0/1046.0;
		
		if(height >= width)
		{
			// Portrait or square; limiting factor will always be the width,
			// since we always want to display the title logo upright:
			int logoHeight = (int)(logoRatio*(double)width);
			theLPs = new LinearLayout.LayoutParams(width, logoHeight);
		}
		else
		{
			// Landscape; in this case, depending on the screen size, the logo
			// may have extra space either (1) above and below or (2) on the
			// left and on the right.  This case is slightly more complicated:
			
			double screenRatio = ((double)height)/((double)width);
			
			if(screenRatio > logoRatio)
			{
				// Case (1):
				int logoHeight = (int)(logoRatio*((double)width));
				theLPs = new LinearLayout.LayoutParams(width, logoHeight);
			}
			else
			{
				// Case (2):
				int logoWidth = (int)(((double)height)/logoRatio);
				theLPs = new LinearLayout.LayoutParams(logoWidth, height);
			}
		}
		
		ImageView theImage = (ImageView) findViewById(R.id.titleImage);
		theImage.setLayoutParams(theLPs);
	}
}
