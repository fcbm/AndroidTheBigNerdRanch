package com.example.criminalintent;

import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

public class CrimeListFragment extends ListFragment {

	private static final String TAG = "CrimeListFragment";
	private ArrayList<Crime> mCrimes;
	private ArrayAdapter<Crime> mAdapter;
	private boolean mSubtitleVisible;
	private Callbacks mCallbacks;
	
	public interface Callbacks 
	{
		void onCrimeSelected(Crime c);
	}
	
	public void updateUI()
	{
		((CrimeAdapter)getListAdapter()).notifyDataSetChanged();
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
	
	@Override
	public void onResume()
	{
		super.onResume();
		// list view’s adapter needs to be informed that the data set 
		// has changed (or may have changed) 
		updateUI();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		// Needed to preserve the state of subtitle visibility
		//setRetainInstance( true );
		mSubtitleVisible = false;
		
		// Tells FragmentManager that this Fragment has an OptionsMenu associated
		// TODO: test multiple Fragments with different OptionsMenus
		setHasOptionsMenu(true);
		
		getActivity().setTitle( R.string.crimes_title );
		mCrimes = CrimeLab.get( getActivity() ).getCrimes();
		//mAdapter = new ArrayAdapter<Crime>( getActivity(), android.R.layout.simple_list_item_1, mCrimes);
		mAdapter = new CrimeAdapter( mCrimes );
		
		setListAdapter(mAdapter);
	}
	
	@TargetApi(11)
	private void updateActionBarSubtitle()
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
		{
			if (mSubtitleVisible)
			{
				getActivity().getActionBar().setSubtitle( R.string.subtitle );
			}
		}
	}
	
	@TargetApi(11)
	private void registerDeleteContextMenu(View v)
	{
		// Get the ListView to register for context menu
		// Notice : the list will register in turn each of its child Views
		// ListFragment.getListView() is only usable after onCreateView
		ListView l = (ListView) v.findViewById( android.R.id.list );

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB)
		{
			// Use floating context menus on Froyo and Gingerbread
			registerForContextMenu( l );
		}
		else
		{
			// The following implementation works with AdapterViews such as ListView and GridView
			// To use it with other Views set a listener in OnLongClickListener and within its implementation
			// create an instance of ActionMode by calling Activity.startActionMode and implement ActionMode.Callback
			
			// Use contextual action bar on Honeycomb or higher
			l.setChoiceMode( ListView.CHOICE_MODE_MULTIPLE_MODAL );

			// When the screen is put into contextual action mode, an instance of 
			// the ActionMode class is created and the methods in ActionMode.Callback
			// are called at different points of ActionMode's lifecycle
			
			// Here we implement AbsListView.MultiChoiceModeListener which inherits from ActionMode.Callback
			// and is needed for multiChoice mode. It adds onItemCheckedStateChanged, that is triggered
			// when an item's selection state changes
			l.setMultiChoiceModeListener( new AbsListView.MultiChoiceModeListener() {
				
				@Override
				public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
					// Required but not used in this implementation
				}
				
				// The following methods are ActionMode.Callback methods
				
				@Override
				public boolean onCreateActionMode(ActionMode mode, Menu menu) {
					// Called when the ActionMode is created. Here we inflate the ContextMenu resource
					// more or less like in onCreateContextMenu
					
					// Notice: we get the menuInflater from ActionMode not from the Activity.
					// ActionMode knows details for configuring the contextual action bar
					// such as the title which activity's menu inflater doesn't have
					MenuInflater inflater = mode.getMenuInflater();
					inflater.inflate(R.menu.crime_list_item_context, menu);
					
					// Notice : change return value, Eclipse returns by default false
					return true;
				}
				
				@Override
				public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
					// Called after onCreateActionMode and whenever contextual action bar
					// needs to be refreshed
					return false;
				}
				
				@Override
				public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
					// called when the user selects an action to respond to contextual actions
					// more or less like in onContextItemSelected. Here we must handle multiple delete
					switch (item.getItemId() )
					{
					case R.id.menu_item_delete_crime:
						CrimeAdapter adapter = (CrimeAdapter) getListAdapter();
						CrimeLab crimeLab = CrimeLab.get(getActivity());
						for (int i = adapter.getCount() - 1; i >= 0; i--)
						{
							if (getListView().isItemChecked(i))
							{
								crimeLab.deleteCrime( adapter.getItem( i ));
							}
						}
						
						// Important to prepare the ActionMode to be destroyed
						mode.finish();
						
						// Notify the adapter to update itself
						updateUI();
						return true;
					}
					
					return false;
				}				
				
				@Override
				public void onDestroyActionMode(ActionMode mode) {
					// called when the ActionMode is about to be destroyed.
					// The default behavior unselects the Views 
				}
			});
		}
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState)
	{
		// Notice: since this is a ListFragment with a default layout, return the View as it
		// is build by the parent version 
		//View v = 
		super.onCreateView(inflater, parent, savedInstanceState);
		View v = inflater.inflate( R.layout.fragment_crime_list, null);
		
		Button b = (Button) v.findViewById( R.id.btn_newCrime );
		b.setOnClickListener( new View.OnClickListener() {
			@Override
			public void onClick(View v)
			{
				startNewCrimeActivity();
			}
		});
		
		updateActionBarSubtitle();
		
		registerDeleteContextMenu(v);
		
		return v;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id)
	{
		// Crime c = (Crime)l.getItemAtPosition(position);
		// Crime c = (Crime) getListAdapter().getItem(position);
		Crime c = ((CrimeAdapter) getListAdapter()).getItem(position);
		Log.d(TAG, c.getTitle() + " was clicked");
		
		//Intent i = new Intent(getActivity(), CrimeActivity.class);
		
		//Intent i = new Intent(getActivity(), CrimePagerActivity.class);
		//i.putExtra(CrimeFragment.EXTRA_CRIME_ID, c.getId());
		//startActivity(i);
		mCallbacks.onCrimeSelected(c);
	}
	
	// Notice : Fragment (as Activity) comes with its own set of options menu callbacks
	// Notice : Argument types: Menu, MenuInflater, MenuItem
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
	{
		// - This method just calls super and inflates the menu through MenuInflater
		// - This method is called by FragmentManager, so we need to inform FragmentManager
		//   that this fragment has an options menu by calling setHasOptionsMenu(boolean)
		//   Not needed for Activity
		// - On Honeycomb and later called when the Activity is started, on older devices
		//   is called the first time the menu is accessed
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate( R.menu.fragment_crime_list , menu);
		
		MenuItem itemShowSubtitle = menu.findItem( R.id.menu_item_show_subtitle );
		if (mSubtitleVisible && itemShowSubtitle != null)
		{
			itemShowSubtitle.setTitle( R.string.hide_subtitle );
		}
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_item_new_crime :
			startNewCrimeActivity();
			return true;
		case R.id.menu_item_show_subtitle:
			showSubtitle(item);
			return true;
		default :
			// Notice: default behavior is: return the parent's version return value
			return super.onOptionsItemSelected(item);
		}
	}	
	
	@TargetApi(11)
	private void showSubtitle(MenuItem item)
	{
		// Wrapping code within Build.VERSION.SDK_INT check is not
		// needed here because R.id.menu_item_show_subtitle is defined
		// in an alternative resource specific for API >= v11 
		if (getActivity().getActionBar().getSubtitle() == null)
		{
			getActivity().getActionBar().setSubtitle( R.string.subtitle );
			item.setTitle( R.string.hide_subtitle );
			mSubtitleVisible = true;
		}
		else
		{
			getActivity().getActionBar().setSubtitle( null );
			item.setTitle( R.string.show_subtitle );
			mSubtitleVisible = false;
		}
		
		// Notice: we are going to need a state variable and a call
		// to setRetainInstance to have this setting survive over
		// the screen rotation
	}
	
	private void startNewCrimeActivity()
	{
		Crime crime = new Crime();
		CrimeLab.get(getActivity()).addCrime( crime );
		
		//Intent i = new Intent(getActivity(), CrimePagerActivity.class);
		//i.putExtra(CrimeFragment.EXTRA_CRIME_ID, crime.getId());
		//startActivityForResult(i, REQUEST_CODE_NEW_CRIME);
		
		// We need to reload the list immediately in case of dual pane upon
		// adding a new crime, because the list is always visible in this case
		updateUI();
		mCallbacks.onCrimeSelected(crime);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		// Notice : in onCreateOptionsMenu we get a MenuInflater as input parameter,
		// here we must get it from the hosting Activity
		// In case of multiple ContextMenu, the View could be used to check which 
		// ContextMenu to inflate by checking its ID (the View must have been registered
		// for context menu by calling Fragment.registerForContextMenu(View))
		getActivity().getMenuInflater().inflate( R.menu.crime_list_item_context, menu);
		
		// ContextMenu.ContextMenuInfo is an EMPTY-INTERFACE which provides additional info
		// regarding the creation of the context menu.
		// Notable subclass AdapterView.AdapterContextMenuInfo which allows to retrieve 
		// the position within the Adapter. This Object can be retrieved from each MenuInfo, see below
		
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		// Get an adapter reference to get the Crime to delete and to request to refresh itself
		CrimeAdapter adapter = (CrimeAdapter)getListAdapter();
		
		switch (item.getItemId())
		{
		case R.id.menu_item_delete_crime:
			// Obtain the position of the item in the list through AdapterContextMenuInfo
			AdapterView.AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			int position = info.position;
			
			// Get the data at that position
			Crime c = adapter.getItem(position);
			
			// Delete
			CrimeLab.get(getActivity()).deleteCrime(c);
			
			// Notify the adapter to update the list
			updateUI();
			return true;
		}
		
		return false;
	}
	
	private class CrimeAdapter extends ArrayAdapter<Crime>
	{
		public CrimeAdapter(ArrayList<Crime> crimes)
		{
			// The call to the superclass constructor is required to properly
			// hook up the dataset of Crime. 
			// We'll not use a pre-defined layout so we can pass 0 for the Layout ID
			// TODO: try to pass a default layout
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
