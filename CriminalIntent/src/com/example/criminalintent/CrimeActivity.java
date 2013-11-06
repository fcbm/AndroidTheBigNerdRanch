package com.example.criminalintent;

import java.util.UUID;

import android.support.v4.app.Fragment;
import android.view.Menu;

public class CrimeActivity extends SingleFragmentActivity {

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.crime, menu);
		return true;
	}

	@Override
	protected Fragment createFragment() {
		UUID id = (UUID) getIntent().getSerializableExtra(CrimeFragment.EXTRA_CRIME_ID);
		return CrimeFragment.newInstance(id);
	}

}
