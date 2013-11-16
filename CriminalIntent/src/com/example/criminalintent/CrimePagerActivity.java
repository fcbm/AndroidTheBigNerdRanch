package com.example.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;

public class CrimePagerActivity extends FragmentActivity implements CrimeFragment.Callbacks {

	// ViewPager  is a fragment container (like FrameLayout)
	// Just like a AdapterView (such as ListView) requires an Adapter
	// ViewPager needs a PagerAdapter (like FragmentStatePagerAdapter)
	private ViewPager mViewPager;
	private ArrayList<Crime> mCrimes;
	
	@Override
	protected void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);
		mViewPager = new ViewPager(this);
		mViewPager.setId( R.id.viewPager );
		
		setContentView( mViewPager );
		
		mCrimes = CrimeLab.get( this ).getCrimes();
		
		FragmentManager fm = getSupportFragmentManager();
		// FragmentStatePagerAdapter vs. FragmentPagerAdapter
		mViewPager.setAdapter( new FragmentStatePagerAdapter( fm ) {
			
			@Override
			public int getCount() {
				return mCrimes.size() ;
			}
			
			@Override
			public Fragment getItem(int position) {
				Crime c = mCrimes.get(position);
				return CrimeFragment.newInstance( c.getId() );
			}
		});
		
		// By default,  ViewPager  loads the item currently on screen plus one 
		// neighboring page in each direction so that the response to a swipe is immediate. 
		// You can tweak how many neighboring pages are loaded by calling setOffscreenPageLimit(int)
		// mViewPager.setOffscreenPageLimit( 3 );
		
		UUID crimeId = (UUID)getIntent().getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
		for (int i = 0; i < mCrimes.size(); i++)
		{
			Crime c = mCrimes.get(i); 
			if (c.getId().equals(crimeId))
			{
				mViewPager.setCurrentItem(i);
				if (c.getTitle() != null)
				{
					setTitle(c.getTitle());
				}
				break;
			}
		}
		
		
		mViewPager.setOnPageChangeListener( new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int pos) {
				Crime c = mCrimes.get(pos);
				if (c.getTitle() != null)
				{
					setTitle(c.getTitle());
				}
			}
			
			@Override
			public void onPageScrolled(int pos, float posOffset, int posOffsetPixels) { }
			
			@Override
			public void onPageScrollStateChanged(int state) { }
		});
	}

	@Override
	public void onCrimeUpdated(Crime c) {
		// This method is empty. Nothing to update in case of single pane
	}

	@Override
	public void onDeleteCrime() {
		// In case of single pane: close Activity and return to CrimeListActivity
		finish();
	}

	@Override
	public void inflateMenu(Menu menu) {
		getMenuInflater().inflate( R.menu.crime_list_item_context, menu);
	}
}
