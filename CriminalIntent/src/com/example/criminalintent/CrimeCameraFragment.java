package com.example.criminalintent;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Notification.Action;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class CrimeCameraFragment extends Fragment {

	public static final String EXTRA_PHOTO_FILENAME = "com.example.criminalintent.photo_filename";
	
	private static final String TAG = "CrimeCameraFragment";
	
	private Camera mCamera;
	private SurfaceView mSurfaceView;
	private View mProgressContainer;
	
	// This callback is called when the image is taken but before image is processed and available
	private Camera.ShutterCallback mShutterCallback = new Camera.ShutterCallback() {
		@Override
		public void onShutter() {
			// Display the progress indicator while taking the image
			mProgressContainer.setVisibility( View.VISIBLE );
		}
	};
	
	// This callback is called when a JPEG version of the image is available
	// We could create another instance of the Callback to handle when raw image data
	// is available (typically when processing is needed and before saving the image)
	private Camera.PictureCallback mJpegCallback = new Camera.PictureCallback() {
		
		@Override
		public void onPictureTaken(byte[] data, Camera camera) {
			// Create a (random) filename
			String filename = UUID.randomUUID().toString() + ".jpg";
			
			// Save the jpeg data to disk
			FileOutputStream os = null;
			boolean success = true;
			
			try
			{
				os = getActivity().openFileOutput( filename , Context.MODE_PRIVATE);
				os.write( data );
			} catch (Exception e)
			{
				Log.e(TAG, "Error writing to file " + filename, e);
				success = false;
			} finally {
				try
				{
					if (os != null)
					{
						os.close();
					} 
				} catch (Exception e)
				{
					Log.e(TAG, "Error closing file " + filename, e);
					success = false;
				}
			}
			
			if (success)
			{
				Log.i(TAG, "JPEG saved at "+filename);
				Intent intent = new Intent();
				intent.putExtra(EXTRA_PHOTO_FILENAME, filename);
				getActivity().setResult(Activity.RESULT_OK , intent);
			}
			else
			{
				getActivity().setResult(Activity.RESULT_CANCELED);
			}
			
			getActivity().finish();
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, parent, savedInstanceState);
		
		View v = inflater.inflate( R.layout.fragment_crime_camera, parent, false);

		mProgressContainer = v.findViewById( R.id.crime_camera_progressContainer );
		mProgressContainer.setVisibility(View.INVISIBLE);
		
		Button takePictureButton = (Button) v.findViewById( R.id.crime_camera_takePictureButton);
		takePictureButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mCamera != null)
					mCamera.takePicture( mShutterCallback, null, mJpegCallback);
			}
		});
		
		mSurfaceView = (SurfaceView) v.findViewById( R.id.crime_camera_surfaceView );
		
		setupSurfaceView();
		
		return v;
	}
	
	@TargetApi(11)
	@Override
	public void onResume()
	{
		super.onResume();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
		{
			// The integer is used to distinguish the different cameras of the device
			// Here we ask for camera 0 which is the first available
			mCamera = Camera.open(0);
		}
		else
		{
			mCamera = Camera.open();
		}
	}

	@Override
	public void onPause()
	{
		super.onPause();
		
		if (mCamera != null)
		{
			mCamera.release();
			mCamera = null;
		}
	}
	
	@SuppressWarnings("deprecation")
	private void setupSurfaceView()
	{
		// SurfaceHolder offers a connection to a Surface, which represents a buffer
		// of raw pixel data
		// Surface has its own lifecycle: created when SurfaceView appears and destroyed
		// when is no longer visible. Make sure not to draw on Surface when it doesn't exists
		// Unlike other Views, nor SurfaceView nor any of its teammates draw anything into themselves.
		// This is done by its client, in this case the client is the Camera instance
		// To do the last two things: attach the Camera to SurfaceHolder when Surface is created, and
		// detach it when the Surface is destroyed. SurfaceHolder offers SurfaceHolder.Callback to
		// define this behaviour
		SurfaceHolder h = mSurfaceView.getHolder();
		
		// setType() and SURFACE_TYPE_PUSH_BUFFERS are both deprecated
		// but are required for Camera preview to work on pre-3.0 devices
		h.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		h.addCallback( new SurfaceHolder.Callback() {
			
			@Override
			public void surfaceDestroyed(SurfaceHolder holder) {
				// We can no longer display on this surface, so stop the preview
				if (mCamera != null)
				{
					// Stops drawing frames on the Surface
					mCamera.stopPreview();
				}
			}
			
			@Override
			public void surfaceCreated(SurfaceHolder holder) {
				// Tells the camera to use this surface as its preview area
				try
				{
					if (mCamera != null)
					{
						// Connects the camera with the Surface
						mCamera.setPreviewDisplay( holder );
					}
				} catch (IOException exception)
				{
					Log.e(TAG, "Error setting up preview display", exception);
				}
			}
			
			@Override
			public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
				if (mCamera == null) return;
				
				// The surface has changed size; update the camera preview size
				Camera.Parameters parameters = mCamera.getParameters();
				
				// Here we collect the best supported size for preview
				Size s = getBestSupportedSize(parameters.getSupportedPreviewSizes(), width, height);
				parameters.setPreviewSize( s.width, s.height);
				// We do the same to set the best supported size for the Picture to create
				s = getBestSupportedSize(parameters.getSupportedPictureSizes(), width, height);
				parameters.setPictureSize( s.width, s.height);
				
				mCamera.setParameters( parameters);
				
				try
				{
					// Starts drawing frames on the Surface
					mCamera.startPreview();
				} catch (Exception e)
				{
					Log.e(TAG, "Could not start preview", e);
					mCamera.release();
					mCamera = null;
				}
				
			}
		});		
	}
	
	private Size getBestSupportedSize(List<Size> sizes, int width, int height)
	{
		Size bestSize = sizes.get(0);
		
		int largestArea = bestSize.width * bestSize.height;
		
		for (Size s : sizes)
		{
			int area = s.width * s.height;
			if (area > largestArea )
			{
				bestSize = s;
				largestArea = area;
			}
		}
		return bestSize;
	}
}
