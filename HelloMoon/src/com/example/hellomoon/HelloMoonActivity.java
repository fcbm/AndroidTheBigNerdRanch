package com.example.hellomoon;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class HelloMoonActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// When the <fragment> is found in the layout, FragmentManager
		// creates an instance of the class specified in android:name
		// and adds it to its list. Then calls HelloMoonFragment.onCreateView
		// and places the View that this method creates in the spot kept by <fragment>
		
		setContentView(R.layout.activity_hello_moon);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.hello_moon, menu);
		return true;
	}

}
