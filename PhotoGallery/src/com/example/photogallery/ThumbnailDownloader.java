package com.example.photogallery;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

// Using Token as parameter we don't deal with the real type (ImageView)
// so avoids knowing anything about the UI
// This is done on Fragment side by Listener implementation (this avoids
// also to use ImageView in the Runnable posted to mResponseHandler)
public class ThumbnailDownloader<Token> extends HandlerThread {

	private static final String TAG = "ThumbnailDownloader";
	private static final int MESSAGE_DOWNLOAD  = 0;
	
	private Handler mHandler;
	private Handler mResponseHandler;
	private Listener<Token> mListener;
	private Map<Token, String> requestMap = Collections.synchronizedMap( new HashMap<Token, String>());
	
	public interface Listener<Token>
	{
		void onThumbnailDownloaded(Token token, Bitmap thumbnail);
	}
	
	public void setListener(Listener<Token> listener)
	{
		mListener = listener;
	}
	
	public ThumbnailDownloader(Handler responseHandler)
	{
		// Set a name to secondary thread to better track it
		super(TAG);
		mResponseHandler = responseHandler;
	}
	
	public void queueThumbnail(Token token, String url)
	{
		Log.i(TAG, "Got an URL : " + url);
		requestMap.put(token, url);
		// in obtainMessage we set:
		// - Message.what to MESSAGE_DOWNLOAD 
		// - Message.obj to token
		// Message.target instead is set by default to mHandler
		// The msg is taken from the global message pool
		Message msg = mHandler.obtainMessage(MESSAGE_DOWNLOAD, token); 
		
		// sendToTarget will add the message to the MessageQueue owned by the
		// Looper associated to the Handler
		// Another option would be to post a Runnable through the Handler
		// calling one of the Handler.post* methods  (we do this below, in handleRequest())
		// Have a look at other Handler.send* methods
		msg.sendToTarget();
	}
	
	// The warning is raised because the Handler is kept alive by its Looper
	@SuppressLint("HandlerLeak")
	@Override
	protected void onLooperPrepared()
	{
		// TODO : check whether it's better to initialize the Handler here
		// or in the start() method (see ProAndroidApps)
		// onLooperPrepared is called before the Looper checks the queue for the
		// first time, so it's a good place to initialize the Handler
		mHandler = new Handler()
		{
			@Override
			public void handleMessage(Message msg)
			{
				if (msg.what == MESSAGE_DOWNLOAD)
				{
					@SuppressWarnings("unchecked")
					Token token = (Token) msg.obj;
					
					Log.i(TAG, "Got a request for url : " + requestMap.get(token));
					
					handleRequest(token);
				}
			}
		};
	}
	
	// This method is executed in a secondary Thread as it's
	// called from Handler.handleMessage
	// It is needed to actually download the image and setup the Bitmap
	private void handleRequest(final Token token)
	{
		try
		{
			final String url = requestMap.get(token);
			
			if (url == null) return;
			
			byte[] bitmapBytes = new FlickrFetch().getUrlBytes( url );
			
			final Bitmap bmp = BitmapFactory.decodeByteArray( bitmapBytes, 0, bitmapBytes.length);
			Log.i(TAG, "Bitmap created");
			
			mResponseHandler.post( new Runnable() {
				//TODO: try to send a Message instead of posting a Runnable
				// Runnable fills the Message.callback attribute 
				@Override
				public void run() {
					// Double check that the given token is still associated to that url.
					// By the time the download is completed, GridView may have recycled 
					// the ImageView and associated a different url to it
					if (requestMap.get(token) != url) return;
					// Notice: the request is removed only just before
					// being displayed, not when the image has been download
					// This can probably avoid downloading two times the same image
					requestMap.remove(token);
					
					// Notice: we don't have access to the whole Fragment here,
					// we just warn the Fragment that the task is completed through
					// the Listener interface
					mListener.onThumbnailDownloaded(token, bmp);
				}
			});
		} catch (IOException e) {
			Log.e(TAG, "Error downloading image", e);
		}
	}
	
	public void clearQueue()
	{
		mHandler.removeMessages( MESSAGE_DOWNLOAD );
		requestMap.clear();
	}
}
