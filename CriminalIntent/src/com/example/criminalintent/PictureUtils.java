package com.example.criminalintent;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.view.Display;
import android.widget.ImageView;

public class PictureUtils {

	// Get a BitmapDrawable from a local file that is scaled down to fit
	// the current Window size
	
	@SuppressWarnings("deprecation")
	public static BitmapDrawable getScaledDrawable(Activity a, String path)
	{
		Display display = a.getWindowManager().getDefaultDisplay();
		// It would be best to scale the image so that it fits the ImageView perfectly
		// However, the size of the view in which the image will be displayed is often not
		// available when you need it. For example, inside onCreateView() you can't gwt the
		// size of the ImageView. As a safe guess, scale the image to the size of the default
		// display for the device
		float destWidth = display.getWidth();
		float destHeight = display.getHeight();
		
		// Read in the dimensions of the image on the disk
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, options);
		
		float srcWidth = options.outWidth;
		float srcHeight = options.outHeight;
		
		int inSampleSize = 1;
		
		if (srcHeight > destHeight || srcWidth > destWidth)
		{
			if (srcWidth > srcHeight)
			{
				inSampleSize = Math.round( srcHeight / destHeight);
			}
			else
			{
				inSampleSize = Math.round( srcWidth / destWidth);
			}
		}
		
		options = new BitmapFactory.Options();
		options.inSampleSize = inSampleSize;
		
		Bitmap bitmap = BitmapFactory.decodeFile( path, options);
		
		return new BitmapDrawable(a.getResources(), bitmap); 
	}
	
	public static void cleanImageView(ImageView imageView)
	{
		if (!(imageView.getDrawable() instanceof BitmapDrawable))
			return;
		
		// Clean up the view's image for the sake of memory
		BitmapDrawable b = (BitmapDrawable) imageView.getDrawable();

		// Calling recycle is necessary to free the native storage for the bitmap
		b.getBitmap().recycle();
		
		imageView.setImageDrawable(null);
	}
}
