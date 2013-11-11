package com.example.criminalintent;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Window;
import android.view.WindowManager;

public class CrimeCameraActivity extends SingleFragmentActivity {

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		// The following operations must be done before the Activity's View is
		// created in Activity.setContentView()
		
		// Hide the window title
		requestWindowFeature( Window.FEATURE_NO_TITLE );
		
		// Hide the status bar and other OS-Level chrome
		getWindow().addFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected Fragment createFragment() {
		return new CrimeCameraFragment();
	}

}
