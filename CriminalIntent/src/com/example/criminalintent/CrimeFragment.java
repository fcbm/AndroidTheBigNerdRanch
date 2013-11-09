package com.example.criminalintent;

import java.util.Date;
import java.util.UUID;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
	private Button mDateTimeButton;
	private CheckBox mSolvedCheckBox;
	
	public static final String EXTRA_CRIME_ID = "com.example.criminalintent.crime_id";
	private static final String DATE_DIALOG_TAG = "date";
	private static final String DATE_OR_TIME_DIALOG_TAG = "date_or_time";
	private static final int REQUEST_DATE = 0;
	private static final int REQUEST_DATE_OR_TIME = 1;

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
		// TODO: check when is the case to pass true
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
		updateDate();
		mDateButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				FragmentManager fm = getActivity().getSupportFragmentManager();
				DatePickerFragment dpf = DatePickerFragment.newInstance( mCrime.getDate() );
				// This creates a connection similar to the one kept by ActivityManager between
				// the Activity calling setActivityForeResult and the one calling setResult
				// NOTE: Fragment doesn't have setResult()
				// The connection here is kept by FragmentManager and says that the target for
				// result of DatePickerFragment is CrimeFragment
				dpf.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
				dpf.show(fm, DATE_DIALOG_TAG);
			}
		});
		
		mDateTimeButton = (Button) v.findViewById( R.id.crime_datetime );
		mDateTimeButton.setOnClickListener( new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				DateTimeFragment dtf = DateTimeFragment.newInstance( mCrime.getDate() );
				dtf.setTargetFragment( CrimeFragment.this, REQUEST_DATE_OR_TIME);
				dtf.show( getActivity().getSupportFragmentManager(), DATE_OR_TIME_DIALOG_TAG);
			}
		});
		
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

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode != Activity.RESULT_OK) return;
		
		if (requestCode == REQUEST_DATE)
		{
			Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
			mCrime.setDate(date);
			updateDate();
		}
		else if (requestCode == REQUEST_DATE_OR_TIME)
		{
			Date date = (Date) data.getSerializableExtra( DateTimeFragment.EXTRA_DATE );
			mCrime.setDate(date);
			updateDate();
		}
	}
	
	private void updateDate()
	{
		mDateButton.setText( DateFormat.format( "EEEE, MMM d, yyyy HH:mm", mCrime.getDate()).toString() );
	}
}
