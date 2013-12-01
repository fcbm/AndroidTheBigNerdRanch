package com.example.photogallery;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

public class PhotoPageFragment extends VisibleFragment {
	
	private static final String TAG = "PhotoPageFragment";
	
	private String mUrl;
	private WebView mWebView;
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		Log.i(TAG, "onCreate");
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		mUrl = getActivity().getIntent().getData().toString();
		Log.i(TAG, "Url : " + mUrl);
	}
	
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		Log.i(TAG, "onCreateView");
		super.onCreateView(inflater, parent, savedInstanceState);
		
		View v = inflater.inflate( R.layout.fragment_photo_page, parent, false );
		
		final ProgressBar progressBar = (ProgressBar) v.findViewById( R.id.progressBar );
		progressBar.setMax( 100 ); // WebChromeClient reports in range 0-100
		
		final TextView titleTextView = (TextView) v.findViewById( R.id.titleTextView );
		
		mWebView = (WebView)v.findViewById( R.id.webView );
		
		
		// needs @SuppressLint("SetJavaScriptEnabled")
		mWebView.getSettings().setJavaScriptEnabled( true );
		mWebView.setWebViewClient( new WebViewClient()
		{
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url)
			{
				return false;
			}
		});
		
		mWebView.setWebChromeClient( new WebChromeClient()
		{
			@Override
			public void onProgressChanged(WebView webView, int progress)
			{
				if (progress == 100)
				{
					progressBar.setVisibility( View.INVISIBLE );
				}
				else
				{
					progressBar.setVisibility(View.VISIBLE);
					progressBar.setProgress(progress);
				}
			}
			
			@Override
			public void onReceivedTitle(WebView webView, String title)
			{
				titleTextView.setText(title);
			}
		});
		
		mWebView.loadUrl(mUrl);
		
		return v;
	}

}
