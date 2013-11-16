package com.example.criminalintent;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;

public class CrimeListActivity extends SingleFragmentActivity 
								implements CrimeListFragment.Callbacks, CrimeFragment.Callbacks {

	@Override
	protected int getLayoutResId()
	{
		return R.layout.activity_masterdetal;
	}
	
	@Override
	protected Fragment createFragment() {
		return new CrimeListFragment();
	}

	@Override
	public void onCrimeSelected(Crime c) {
		// To check if we have one or two panels we check for the
		// presence of the Fragment ID.
		// Notice: is fine that an Activity checks for the contained
		// Fragment, is not fine that a Fragment accesses sibling Fragments
		if (findViewById( R.id.detailFragmentContainer) == null)
		{
			// Start an instance of CrimePagerActivity in case of single pane
			Intent i = new Intent(this, CrimePagerActivity.class);
			i.putExtra( CrimeFragment.EXTRA_CRIME_ID, c.getId());
			startActivity(i);
		} 
		else
		{
			// Update details pane in case of dual pane
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			
			Fragment oldDetail = fm.findFragmentById( R.id.detailFragmentContainer );
			Fragment newDetail = CrimeFragment.newInstance( c.getId() );
			
			if ( oldDetail != null)
			{
				ft.remove( oldDetail );
			}
			
			ft.add( R.id.detailFragmentContainer , newDetail);
			ft.commit();
		}
	}

	@Override
	public void onCrimeUpdated(Crime c) {
		// This Callback avoids CrimeFragment to invoke directly CrimeListFragment methods
		FragmentManager fm = getSupportFragmentManager();
		CrimeListFragment listFragment =  (CrimeListFragment) fm.findFragmentById( R.id.fragmentContainer);
		listFragment.updateUI();
	}

	@Override
	public void onDeleteCrime() {
		// Remove details pane when crime is deleted from CrimePagerActivity
	}

	@Override
	public void inflateMenu(Menu menu) {
	}
}
