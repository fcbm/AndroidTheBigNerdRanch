package com.example.photogallery;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.Toast;

// TODO : it seems when the app crashes when it is idle for long time
public class PhotoGalleryFragment extends VisibleFragment {

	private final static String TAG = "PhotoGalleryFragment";
	
	private GridView mGridView;
	private ArrayList<GalleryItem> mItems;
	private ThumbnailDownloader<ImageView> mThumbnailThread;
	
	private int mPageCount = 1;
	
	private class FetchItemTask extends AsyncTask<Void, Void, ArrayList< GalleryItem >>
	{
		@Override
		protected ArrayList< GalleryItem > doInBackground(Void... params) {
			
			Activity activity = getActivity();
			if (activity == null)
				return new ArrayList<GalleryItem>();
			
			String query = PreferenceManager.getDefaultSharedPreferences( activity )
					.getString(FlickrFetch.PREF_SEARCH_QUERY, null);

			if (query != null)
			{
				return new FlickrFetch().search(query);
			}
			else
			{
				return new FlickrFetch().fetchItems(mPageCount);
			}
		}
		@Override
		protected void onPostExecute(ArrayList< GalleryItem > items)
		{
			//if (mItems == null)
			{
				mItems = items;
			}
			//else
			{
				//mItems.addAll( items );
			}
			setupAdapter();
			mPageCount++;			
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// Needed to simplify handling of rotation
		setRetainInstance( true );
		
		// Needed to have menu in a Fragment
		setHasOptionsMenu( true );
		
		// Notice: in this example AsyncTask was carefully structured so that
		// we don't need to keep track of it. In other situations, we may need
		// to keep track of it. For these more complicated cases AsyncTask will
		// be assigned to an instance variable - TODO: check cases where this may
		// lead to memory leaks
		// new FetchItemTask().execute();
		updateItems();

		// Debug Code:
		//Intent i = new Intent(getActivity(), PollService.class);
		//getActivity().startService(i);
		// Debug Code:
		//PollService.startServiceAlarm( getActivity(), true );
		
		mThumbnailThread = new ThumbnailDownloader<ImageView>(new Handler());
		mThumbnailThread.setListener( new ThumbnailDownloader.Listener<ImageView> () {

			@Override
			public void onThumbnailDownloaded(ImageView imageView, Bitmap thumbnail) {
				if (isVisible())
				{
					imageView.setImageBitmap( thumbnail );
				}
			}
		});
		// Creates the Thread
		mThumbnailThread.start();
		
		// HanldlerThread is needed to prepare a Looper
		// This method returns the Looper associated with this thread. 
		// If this thread not been started or for any reason is isAlive() returns false, 
		// this method will return null. 
		// If this thread has been started, this method will *block* until the looper has been initialized.
		mThumbnailThread.getLooper();
		
		Log.i(TAG, "Background thread started");
	}
	
	public void updateItems()
	{
		new FetchItemTask().execute();
	}
	
	@Override
	public void onDestroy()
	{
		super.onDestroy();
		// This is critical, if we don't call quit the Thread will stay alive
		// TODO: check what happens if the process is killed by the system and
		// onDestroy is not called
		mThumbnailThread.quit();
		Log.i(TAG, "Background thread stopped");
	}
	
	@Override
	public void onDestroyView()
	{
		// Clear the enqueued requests in case user rotates the screen
		// because all the ImageView in the map become invalid in that case
		super.onDestroyView();
		mThumbnailThread.clearQueue();
	}	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, parent, savedInstanceState);
		
		View v = inflater.inflate(R.layout.fragment_photo_gallery, parent , false);
		
		mGridView = (GridView) v.findViewById( R.id.gridView );
		
		Log.d(TAG, "Test Log onCreateView");
		
		
		setupAdapter();
		
		mGridView.setOnItemClickListener( new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> gridView, View view, int pos, long id) {
				
				GalleryItem item = mItems.get(pos);
				
				Uri photoPageUri = Uri.parse(item.getPhotoPageUrl());

				// This will start a browser
				//Intent i = new Intent(Intent.ACTION_VIEW, photoPageUri);
				Log.i(TAG, "url " + photoPageUri);
				Intent i = new Intent(getActivity(), PhotoPageActivity.class);
				i.setData(photoPageUri);
				
				startActivity(i);
			}
		});
		
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
			
			// This step allows to download only the images that are required
			// by GridView, and not all the URLs fetched by FlickrFetch
			// AsyncTask is ill-suited for repetitive and long running tasks
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
			//if (mGridView.getAdapter() == null)
			{
				mGridView.setAdapter( new GalleryAdapter(mItems));
			}
			//else
			{
				//Toast.makeText( getActivity(), "items size " + mItems.size(), Toast.LENGTH_SHORT).show();
				//((GalleryAdapter)mGridView.getAdapter()).notifyDataSetChanged();
			}
		} 
		else
		{
			mGridView.setAdapter( null );
		}
	}
	
	@TargetApi(11)
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu( menu , inflater);
		inflater.inflate( R.menu.fragment_photo_gallery, menu);
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			// Configure SearchView in case we're post Android3.0
			// To do this we ask SarchManager to build a SerchableInfo reading
			// the info from the manifest, then we pass SearchableInfo to SearchView
			// This is needed to have a SearchView that is able to forward the
			// search Intent properly
			
			MenuItem searchItem = menu.findItem( R.id.menu_item_search );
			
			SearchView searchView = (SearchView) searchItem.getActionView();
			
			// Get data from searchable.xml as SearchableInfo
			SearchManager sm = (SearchManager) getActivity().getSystemService( Context.SEARCH_SERVICE );
			ComponentName name = getActivity().getComponentName();
			SearchableInfo si = sm.getSearchableInfo( name );
			
			searchView.setSearchableInfo( si );
		}
	}
	
	@TargetApi(11) // needed for invalidateOptionsMenu
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		if (item.getItemId() == R.id.menu_item_search)
		{
			// To make the search work PhotoGalleryActivity we should 
			// turn it into a "searchable Activity". Two things are needed:
			// - <searchable> tag in res/xml/searchable.xml 
			//     -> this is the search configuration and describes how the search dialog should appear
			// - in the manifest : 
			//     -> change the launch mode of the Activity to singleTop
			//     -> define a new intent-filter to tell that the activity listens to search intents
			//     -> define metadata to attach the searchable.xml config to the Activity
			// These two things will tell SearchManager that the Activity is capable to handle onSearchRequested
			getActivity().onSearchRequested();
			return true;
		}
		else if (item.getItemId() == R.id.menu_item_clear)
		{
			PreferenceManager.getDefaultSharedPreferences( getActivity() )
				.edit()
				.putString( FlickrFetch.PREF_SEARCH_QUERY, null)
				.commit();
			updateItems();
			return true;
		}
		else if (item.getItemId() == R.id.menu_item_toggle_polling)
		{
			boolean shouldStartAlarm = !PollService.isServiceAlarmOn( getActivity() );
			PollService.startServiceAlarm( getActivity(), shouldStartAlarm);
			
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				// After Android 3.0 this is needed to tell the ActionBar
				// to call onPrepareOptionsMenu and to refresh the items
				getActivity().invalidateOptionsMenu();
			}
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
	
	// This method is needed to update dynamically the
	// content of the options menu
	@Override
	public void onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);
		
		MenuItem toggleItem = menu.findItem( R.id.menu_item_toggle_polling );
		
		// Notice we don't do the test on the menu item title
		// but on the real status of the alarm
		if (PollService.isServiceAlarmOn( getActivity() ))
		{
			toggleItem.setTitle( R.string.stop_polling);
		}
		else
		{
			toggleItem.setTitle( R.string.start_polling);
		}
		
	}
	
}
