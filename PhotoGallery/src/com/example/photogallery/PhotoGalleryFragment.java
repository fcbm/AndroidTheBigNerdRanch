package com.example.photogallery;

import java.util.ArrayList;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class PhotoGalleryFragment extends Fragment {

	private final static String TAG = "PhotoGalleryFragment";
	
	private GridView mGridView;
	private ArrayList<GalleryItem> mItems;
	private ThumbnailDownloader<ImageView> mThumbnailThread;
	
	private int mPageCount = 1;
	
	private class FetchItemTask extends AsyncTask<Void, Void, ArrayList< GalleryItem >>
	{
		@Override
		protected ArrayList< GalleryItem > doInBackground(Void... params) {
			return new FlickrFetch().fetchItems(mPageCount);
		}
		@Override
		protected void onPostExecute(ArrayList< GalleryItem > items)
		{
			if (mItems == null)
			{
				mItems = items;
			}
			else
			{
				mItems.addAll( items );
			}
			setupAdapter();
			mPageCount++;			
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		setRetainInstance( true );
		// Notice: in this example AsyncTask was carefully structured so that
		// we don't need to keep track of it. In other situations, we may need
		// to keep track of it. For these more complicated cases AsyncTask will
		// be assigned to an instance variable - TODO: check cases where this may
		// lead to memory leaks
		new FetchItemTask().execute();
		
		mThumbnailThread = new ThumbnailDownloader<ImageView>();
		// Creates the Thread
		mThumbnailThread.start();
		
		// HanldlerThread is needed to prepare a Looper
		mThumbnailThread.getLooper();
		
		Log.i(TAG, "Background thread started");
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		mThumbnailThread.quit();
		Log.i(TAG, "Background thread stopped");
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, parent, savedInstanceState);
		
		View v = inflater.inflate(R.layout.fragment_photo_gallery, parent , false);
		
		mGridView = (GridView) v.findViewById( R.id.gridView );
		
		setupAdapter();
		
		return v;
	}
	
	private class GalleryAdapter extends ArrayAdapter<GalleryItem>
	{
		public GalleryAdapter(ArrayList<GalleryItem> items) {
			super(getActivity(), 0, items);
			 
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			//super.getView(position, convertView, parent);
			
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate(R.layout.gallery_item , parent, false);
			}
			
			ImageView iv = (ImageView) convertView.findViewById( R.id.gallery_item_imageView );
			iv.setImageResource( R.drawable.brian_up_close );
			
			GalleryItem item = getItem(position);
			mThumbnailThread.queueThumbnail(iv, item.getUrl());
			
			if (position == (getCount() - 1) )
			{
				Log.i(TAG, "We are close to the end of the list - pos " + position);
				Toast.makeText( getContext(), "We are close to the end of the list - pos " + position + " request page " + mPageCount, Toast.LENGTH_SHORT).show();
				new FetchItemTask().execute();
			}
			
			return convertView;
		}
	}
	
	private void setupAdapter()
	{
		// We do these checks in case the Fragment is not attached
		// to an Activity. Since we're using an AsyncTask to do something asynchronously
		//  we can't assume that the fragment is attached to an Activity
		if (getActivity() == null || mGridView == null) return;
		
		if (mItems != null)
		{
			if (mGridView.getAdapter() == null)
			{
				mGridView.setAdapter( new GalleryAdapter(mItems));
			}
			else
			{
				Toast.makeText( getActivity(), "items size " + mItems.size(), Toast.LENGTH_SHORT).show();
				((GalleryAdapter)mGridView.getAdapter()).notifyDataSetChanged();
			}
		} 
		else
		{
			mGridView.setAdapter( null );
		}
	}
	
}
