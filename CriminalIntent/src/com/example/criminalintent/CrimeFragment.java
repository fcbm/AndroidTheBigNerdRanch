package com.example.criminalintent;

import java.util.UUID;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class CrimeFragment extends Fragment {
	
	private Crime mCrime;
	private EditText mTitleField; 
	private Button mDateButton;
	private CheckBox mSolvedCheckBox;
	
	public static final String EXTRA_CRIME_ID = "com.example.criminalintent.crime_id";

	public static Fragment newInstance(UUID crimeId)
	{
		Bundle args = new Bundle();
		args.putSerializable(EXTRA_CRIME_ID, crimeId);
		CrimeFragment fragment = new CrimeFragment();
		fragment.setArguments(args);
		return fragment;
	}
	
	// Notice : Fragment.onCreate must be public because they will be called by
	// whatever Activity is hosting them. Whereas Activity.onCreate is protected
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		// mCrime = new Crime();
		
		// CrimeFragment  wouldn't be a reusable building block because it expects that it 
		// will always be hosted by an activity whose  Intent  defines an extra named  EXTRA_CRIME_ID
		//UUID id = (UUID) getActivity().getIntent().getSerializableExtra(EXTRA_CRIME_ID);
		UUID id = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
		mCrime = CrimeLab.get( getActivity() ).getCrime(id);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		// The third parameter (boolean) tells the layoutInflater whether to add the inflated view
		// to the view's parent. We pass false because we will add the View in the Activity's code
		View v = inflater.inflate( R.layout.fragment_crime , parent, false);
		
		mTitleField = (EditText) v.findViewById( R.id.crime_title );
		mTitleField.setText( mCrime.toString() );
		mTitleField.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				mCrime.setTitle( s.toString() );
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}
			
			@Override
			public void afterTextChanged(Editable s) {
			}
		});

		mDateButton = (Button) v.findViewById(R.id.crime_data);
		mDateButton.setText( DateFormat.format( "EEEE, MMM d, yyyy", mCrime.getDate()).toString() );
		mDateButton.setEnabled(false);
		
		mSolvedCheckBox = (CheckBox) v.findViewById( R.id.crime_solved );
		mSolvedCheckBox.setChecked( mCrime.isSolved() );
		mSolvedCheckBox.setOnCheckedChangeListener( new OnCheckedChangeListener() {
			
			// CheckBox is subclass of CompoundButton
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				mCrime.setSolved( isChecked );
			}
		});
		
		return v;
	}

}