package com.example.criminalintent;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class CrimeListFragment extends ListFragment {

	private static final String TAG = "CrimeListFragment";
	private ArrayList<Crime> mCrimes;
	private ArrayAdapter<Crime> mAdapter;
	
	@Override
	public void onResume()
	{
		super.onResume();
		// list view’s adapter needs to be informed that the data set 
		// has changed (or may have changed) 
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		getActivity().setTitle( R.string.crimes_title );
		mCrimes = CrimeLab.get( getActivity() ).getCrimes();
		//mAdapter = new ArrayAdapter<Crime>( getActivity(), android.R.layout.simple_list_item_1, mCrimes);
		mAdapter = new CrimeAdapter( mCrimes );
		
		setListAdapter(mAdapter);
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		// Crime c = (Crime)l.getItemAtPosition(position);
		// Crime c = (Crime) getListAdapter().getItem(position);
		Crime c = ((CrimeAdapter) getListAdapter()).getItem(position);
		Log.d(TAG, c.getTitle() + " was clicked");
		
		//Intent i = new Intent(getActivity(), CrimeActivity.class);
		Intent i = new Intent(getActivity(), CrimePagerActivity.class);
		i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
		startActivity(i);
	}
	
	private class CrimeAdapter extends ArrayAdapter<Crime>
	{
		public CrimeAdapter(ArrayList<Crime> crimes)
		{
			super(getActivity(), 0, crimes);
		}
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				convertView = getActivity().getLayoutInflater().inflate(R.layout.list_item_crime, null);
			}
			
			// Configure the view for this crime
			Crime c = getItem(position);
			
			TextView titleTextView = (TextView) convertView.findViewById( R.id.crime_list_item_titleTextView );
			titleTextView.setText( c.getTitle().toString() );
			
			TextView dateTextView = (TextView) convertView.findViewById( R.id.crime_list_item_dateTextView );
			dateTextView.setText( c.getDate().toString() );
			
			// A  CheckBox  is  focusable  by default. This means that a click on a list item will be interpreted as 
			// toggling the  CheckBox  and will not reach your  onListItemClick(…)  method. 
			// Any focusable widget that appears in a list item layout (like a  CheckBox  or a 
			// Button ) should be made non-focusable to ensure that a click on a list item will work as you expect. 
			CheckBox solvedCheckBox = (CheckBox) convertView.findViewById( R.id.crime_list_item_solvedCheckBox );
			solvedCheckBox.setChecked( c.isSolved() );
			return convertView;
		}
		
	}
}
