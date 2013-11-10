package com.example.hellomoon;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class HelloMoonFragment extends Fragment implements AudioPlayer.OnCompletionListener {
	
	private AudioPlayer mPlayer = new AudioPlayer();
	private Button mPlayButton;
	private Button mStopButton;
	//private Button mPlayVideoButton;
	//private SurfaceView mSurfaceView;

	// TODO: check http://developer.android.com/guide/topics/media/mediaplayer.html
	// to implement a more civilized audio player
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// This method and onDestroy won't be called
		// upon configuration changes because we call setRetainInstance(true)
		super.onCreate(savedInstanceState);
		setRetainInstance( true );
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		// onCreateView is called even if we've set retain instance option
		super.onCreateView(inflater, container, savedInstanceState);
		
		View v = inflater.inflate( R.layout.fragment_hello_moon,  container, false);
		
		/*
		TODO: setup video player
		mSurfaceView = (SurfaceView) v.findViewById( R.id.surfaceView );
		
		mPlayVideoButton = (Button) v.findViewById( R.id.hellomoon_playVideo);
		mPlayVideoButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPlayer.playVideo( getActivity(),  mSurfaceView.getHolder());
			}
		});
		*/
		
		mPlayButton = (Button) v.findViewById( R.id.hellomoon_playButton );
		mPlayButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (!mPlayer.isPlaying())
				{
					mPlayer.play( getActivity(), HelloMoonFragment.this );
				}
				else
				{
					mPlayer.pause( );
				}
				updateButtons();
			}
		});
		
		mStopButton = (Button) v.findViewById( R.id.hellomoon_stopButton );
		mStopButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mPlayer.stop();
				updateButtons();
			}
		});
		
		updateButtons();
		
		return v;
	}
	
	private void updateButtons()
	{
		if (mPlayer !=null && mPlayer.isPlaying())
		{
			mPlayButton.setText( R.string.hellomoon_pause );
		}
		else
		{
			mPlayButton.setText( R.string.hellomoon_play );
		}
	}
	
	
	@Override
	public void onDestroy()
	{
		// This method won't be called when rotating the
		// screen because we have set the retain instance option
		// It will be called when the app is in the background
		// and the system needs resources
		super.onDestroy();
		mPlayer.stop();
	}


	@Override
	public void onCompletion() {
		updateButtons();
	}
}
