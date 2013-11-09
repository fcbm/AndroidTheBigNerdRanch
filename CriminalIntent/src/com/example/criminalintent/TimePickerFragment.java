package com.example.criminalintent;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;

public class TimePickerFragment extends DialogFragment {

	public static final String EXTRA_TIME = "extra_time";
	
	private static final String TAG = "TimePickerFragment";
	private static final String ARG_TIME = "arg_time";
	private Date mDate;
	
	public static TimePickerFragment newInstance(Date date)
	{
		TimePickerFragment tpf = new TimePickerFragment();
		Bundle args = new Bundle();
		args.putSerializable(ARG_TIME , date);
		tpf.setArguments( args );
		
		return tpf;
	}
	
	/*
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View v = inflater.inflate( R.layout.dialog_time, container);
		mDate = (Date) getArguments().getSerializable( ARG_TIME );

		setupTimerPicker( v );
		
		return v;
	}
	*/
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstance)
	{
		View v = getActivity().getLayoutInflater().inflate( R.layout.dialog_time, null);
		mDate = (Date) getArguments().getSerializable( ARG_TIME );
		
		setupTimerPicker(v);
		
		return new AlertDialog.Builder( getActivity() ).
				setTitle( R.string.set_time ).
				setView( v ).
				setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) { sendResult(); }
					}).
				create();
	}

	private void sendResult()
	{
		if (getTargetFragment() == null)
			return;
		
		Intent i = new Intent();
		i.putExtra( TimePickerFragment.EXTRA_TIME, mDate);
		getTargetFragment().onActivityResult( getTargetRequestCode(), Activity.RESULT_OK, i); 
	}
	
	private void setupTimerPicker(View v)
	{
		TimePicker tp = (TimePicker) v.findViewById( R.id.dialog_time_timePicker);
		final Calendar c = Calendar.getInstance();
		c.setTime( mDate );

		final int year = c.get( Calendar.YEAR);
		final int month = c.get( Calendar.MONTH);
		final int day = c.get( Calendar.DAY_OF_MONTH);
		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);
		
		tp.setIs24HourView( true );
		tp.setCurrentHour( hour );
		tp.setCurrentMinute( minute );
		
		tp.setOnTimeChangedListener( new OnTimeChangedListener( ) {
			
			@Override
			public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
				mDate = new GregorianCalendar( 
						year, 
						month, 
						day, 
						hourOfDay, 
						minute).getTime();
				Log.d(TAG, "New: H " + hourOfDay + " M " + minute);
				getArguments().putSerializable(EXTRA_TIME, mDate);
			}
		});		
	}
}
