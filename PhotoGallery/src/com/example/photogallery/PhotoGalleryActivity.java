package com.example.photogallery;

import android.app.SearchManager;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;

public class PhotoGalleryActivity extends SingleFragmentActivity {

	private static final String TAG = "PhotoGalleryActivity";
	
	@Override
	protected Fragment createFragment() {
		return new PhotoGalleryFragment();
	}

	@Override
	public void onNewIntent(Intent intent)
	{
		PhotoGalleryFragment pgf = (PhotoGalleryFragment)getSupportFragmentManager().findFragmentById( R.id.fragmentContainer );
		
		if (Intent.ACTION_SEARCH.equals( intent.getAction() ))
		{
			// Remember : the search query is stored as extra in the Intent
			String query = intent.getStringExtra( SearchManager.QUERY );
			Log.i(TAG, "Received a new search query: " + query);

			// Use global unnamed SharedPreferences instead of Context.getSharedPreferences(String fname, int mode)
			// Also Activity.getPreferences(int mode) is available for data needed by the Activity itself
			PreferenceManager.getDefaultSharedPreferences( this )
				.edit()
				.putString( FlickrFetch.PREF_SEARCH_QUERY, query)
				.commit();
			
		}
		
		pgf.updateItems();
	}
}
