package com.example.hellomoon;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

public class AudioPlayer {
	
	interface OnCompletionListener { public void onCompletion() ; };
	
	private static final String TAG = "AudioPlayer";
	private MediaPlayer mPlayer;
	private boolean mIsPaused = false;
	private OnCompletionListener mListener; 
	
	public void stop()
	{
		if (mPlayer != null && !mIsPaused)
		{
			Log.d(TAG, "STOP");
			mPlayer.release();
			mPlayer = null;
		}
	}
	
	public boolean isPlaying()
	{
		return mPlayer != null && mPlayer.isPlaying();
	}
	
	public void pause()
	{
		if (mPlayer != null && mPlayer.isPlaying())
		{
			Log.d(TAG, "PAUSE");
			mPlayer.pause();
			mIsPaused = true;
		}
		else
			Log.d(TAG, "DONT PAUSE");
	}	
	/*
	public void playVideo(Context ctx, SurfaceHolder sh)
	{
		//Uri videoUri = Uri.parse("android.resource://" + "com.example.hellomoon/res/raw/apollo_17_stroll" );
		stop();
		mPlayer = MediaPlayer.create( ctx, R.raw.apollo_17_stroll);
		mPlayer.setDisplay( sh );
		mPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				stop();
			}
		});
		mPlayer.start();
		Log.d(TAG, "PLAY VIDEO");
	}*/
	
	
	public void play(Context ctx, OnCompletionListener listener)
	{
		stop();
		mListener = listener;
		
		if (mPlayer == null)
		{ 
			mPlayer = MediaPlayer.create( ctx, R.raw.one_small_step);
			mPlayer.setOnCompletionListener( new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					stop();
					mListener.onCompletion();
				}
			});
		}
		
		Log.d(TAG, "PLAY");
		mPlayer.start();
		mIsPaused = false;
	}
}
