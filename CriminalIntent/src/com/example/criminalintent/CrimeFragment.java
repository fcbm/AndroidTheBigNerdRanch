package com.example.criminalintent;

import java.util.Date;
import java.util.UUID;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

public class CrimeFragment extends Fragment {
	
	//private static final String TAG = "CrimeFragment";
	
	private Crime mCrime;
	private EditText mTitleField; 
	private Button mDateButton;
	private Button mDateTimeButton;
	private CheckBox mSolvedCheckBox;
	private ImageButton mPhotoButton;
	private ImageView mPhotoView;
	private Button mReportButton;
	private Button mSuspectButton;
	private Callbacks mCallbacks;
	
	public static final String EXTRA_CRIME_ID = "com.example.criminalintent.crime_id";
	private static final String DATE_DIALOG_TAG = "date";
	private static final String DATE_OR_TIME_DIALOG_TAG = "date_or_time";
	private static final String PHOTO_DIALOG_TAG = "photo_dialog";
	private static final int REQUEST_DATE = 0;
	private static final int REQUEST_DATE_OR_TIME = 1;
	private static final int REQUEST_PHOTO = 2;
	private static final int REQUEST_CONTACT = 3;

	public interface Callbacks
	{
		void onCrimeUpdated(Crime c);
		void onDeleteCrime();
		void inflateMenu(Menu menu);
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		mCallbacks = (Callbacks) activity;
	}
	
	@Override
	public void onDetach()
	{
		super.onDetach();
		mCallbacks = null;
	}
	
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
		
		// Notice we need to call setHasOptionsMenu in order to use the menu
		setHasOptionsMenu( true );
		
		// mCrime = new Crime();
		
		// CrimeFragment  wouldn't be a reusable building block because it expects that it 
		// will always be hosted by an activity whose  Intent  defines an extra named  EXTRA_CRIME_ID
		//UUID id = (UUID) getActivity().getIntent().getSerializableExtra(EXTRA_CRIME_ID);
		UUID id = (UUID) getArguments().getSerializable(EXTRA_CRIME_ID);
		mCrime = CrimeLab.get( getActivity() ).getCrime(id);
	}
	
	@TargetApi(11)
	private void setupAncestralNavigation()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			// This call only enables the button but doesn't define the behavior
			if (NavUtils.getParentActivityName(getActivity()) != null)
			{
				getActivity().getActionBar().setDisplayHomeAsUpEnabled( true );
			}
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		// The third parameter (boolean) tells the layoutInflater whether to add the inflated view
		// to the view's parent. We pass false because we will add the View in the Activity's code
		// TODO: check when is the case to pass true
		View v = inflater.inflate( R.layout.fragment_crime , parent, false);

		setupAncestralNavigation();
		
		mTitleField = (EditText) v.findViewById( R.id.crime_title );
		mTitleField.setText( mCrime.toString() );
		mTitleField.addTextChangedListener( new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				
				mCrime.setTitle( s.toString() );
				getActivity().setTitle( mCrime.getTitle() );
				mCallbacks.onCrimeUpdated( mCrime );
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
				mCallbacks.onCrimeUpdated( mCrime );
			}
		});
		
		mPhotoButton = (ImageButton) v.findViewById( R.id.crime_imageButton );
		mPhotoButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(getActivity(), CrimeCameraActivity.class);
				startActivityForResult(i, REQUEST_PHOTO);
			}
		});

		// If camera is not available, disable camera functionality
		// Here we check both front and back camera presence
		PackageManager pm = getActivity().getPackageManager();
		if (!pm.hasSystemFeature( PackageManager.FEATURE_CAMERA) && !pm.hasSystemFeature( PackageManager.FEATURE_CAMERA_FRONT))
		{
			mPhotoButton.setEnabled( false );
		}
		
		// Displaying the image on the ImageView requires some image handling
		// first because the file that came from the camera could be exceptionally large
		mPhotoView = (ImageView) v.findViewById( R.id.crime_imageView);
		mPhotoView.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Photo p = mCrime.getPhoto();
				if (p == null)
					return;
				// Notice : way to build absolute path from path name. 
				// We use the same in showPhoto()
				String path = getActivity().getFileStreamPath( p.getFilename() ).getAbsolutePath();
				ImageFragment imageFrag = ImageFragment.newInstance( path );
				imageFrag.show( getActivity().getSupportFragmentManager(), PHOTO_DIALOG_TAG);
			}
		});
		
		mReportButton = (Button) v.findViewById( R.id.crime_reportButton);
		mReportButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Create an implicit Intent that will start an activity
				// able to perform ACTION_SEND on text/plain mime type
				Intent i = new Intent(Intent.ACTION_SEND);
				i.setType("text/plain");
				i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
				i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
				
				// This will create a chooser dialog even if user
				// has already a default activity to handle this action
				i = Intent.createChooser( i , getString(R.string.send_report ));
				
				startActivity(i);
			}
		});
		
		mSuspectButton = (Button) v.findViewById( R.id.crime_suspectButton);
		mSuspectButton.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(Intent.ACTION_PICK, 
						ContactsContract.Contacts.CONTENT_URI);
				
				startActivityForResult(i, REQUEST_CONTACT);
			}
		});
		
		if (mCrime.getSuspect() != null)
		{
			mSuspectButton.setText( mCrime.getSuspect() );
		}
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
			mCallbacks.onCrimeUpdated( mCrime );
			updateDate();
		}
		else if (requestCode == REQUEST_DATE_OR_TIME)
		{
			Date date = (Date) data.getSerializableExtra( DateTimeFragment.EXTRA_DATE );
			mCrime.setDate(date);
			mCallbacks.onCrimeUpdated( mCrime );
			updateDate();
		}
		else if (requestCode == REQUEST_PHOTO)
		{
			// Create a new photo Object and attach it to the crime
			String fname = data.getStringExtra( CrimeCameraFragment.EXTRA_PHOTO_FILENAME );
			if (fname != null)
			{
				Photo p = new Photo(fname);
				mCrime.setPhoto(p);
				mCallbacks.onCrimeUpdated( mCrime );
				showPhoto();
			}
		}
		else if (requestCode == REQUEST_CONTACT)
		{
			Uri contactUri = data.getData();
			
			// Specify which fields you want your query to return values for
			String[] queryFields = new String[] { ContactsContract.Contacts.DISPLAY_NAME };
			
			// Perform the query - the contactUri is like a "where" clause here
			Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);
			
			// Double check that we got results
			if (c.getCount() == 0)
			{
				c.close();
				return;
			}
			
			// Pull out the first column of the first row of data - that is your suspect's name
			c.moveToFirst();
			String suspect = c.getString( 0 );
			mCrime.setSuspect( suspect );
			mCallbacks.onCrimeUpdated( mCrime );
			mSuspectButton.setText( suspect );
			c.close();
		}
	}
	
	private String getCrimeReport()
	{
		String solvedString = null;
		
		if (mCrime.isSolved())
		{
			solvedString = getString(R.string.crime_report_solved);
		}
		else
		{
			solvedString = getString(R.string.crime_report_unsolved);
		}
		
		String dateFormat = "EEE, MMM dd";
		String dateString = DateFormat.format(
									dateFormat, 
									mCrime.getDate()).toString();
		
		String suspect = mCrime.getSuspect();
		if (suspect == null)
		{
			suspect = getString(R.string.crime_report_no_suspect);
		}
		else
		{
			// Notice : here we fill a string from resources with a string from code
			suspect = getString(R.string.crime_report_suspect, suspect);
		}
		
		String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);
		
		return report;
	}
	
	private void showPhoto()
	{
		// (Re)set the image button's image based on our photo
		Photo p = mCrime.getPhoto();
		BitmapDrawable b = null;
		if (p != null)
		{
			String path = getActivity().getFileStreamPath(p.getFilename()).getAbsolutePath();
			b = PictureUtils.getScaledDrawable(getActivity(), path);
		}
		mPhotoView.setImageDrawable( b );
	}	

	@Override
	public void onStart()
	{
		super.onStart();
		showPhoto();
	}
	
	@Override
	public void onStop()
	{
		super.onStop();
		PictureUtils.cleanImageView( mPhotoView);
	}	
	@Override
	public void onPause()
	{
		super.onPause();
		CrimeLab.get( getActivity() ).saveCrimes();
	}
	
	@Override 
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		super.onCreateOptionsMenu(menu, inflater);
		// getActivity().getMenuInflater().inflate( R.menu.crime_list_item_context, menu);
		mCallbacks.inflateMenu(menu);
		return;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch(item.getItemId())
		{
		case android.R.id.home:
			/*
			Intent i = new Intent(getActivity(), CrimeListActivity.class);
			// Tells android to look for an existing instance of the Activity
			// in the stack and if there is some, pop every other activity off
			// the stack so that the activity being started will be the top-most
			i.addFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(i);
			*/
			
			// Better way to navigate to the parent : Define the parent
			// through <meta-data> in the manifest and navigate to it using
			// NavUtils methods
			// One of the advantage, is that the Fragment doesn't need to know
			// about the parent activity which is defined in the Manifest.xml
			if (NavUtils.getParentActivityName(getActivity()) != null)
			{
				NavUtils.navigateUpFromSameTask(getActivity());
			}
			
			return true;
		case R.id.menu_item_delete_crime:
			CrimeLab.get( getActivity()).deleteCrime( mCrime);
			mCallbacks.onDeleteCrime();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void updateDate()
	{
		mDateButton.setText( mCrime.getDateAsFormattedString() );
	}
}
