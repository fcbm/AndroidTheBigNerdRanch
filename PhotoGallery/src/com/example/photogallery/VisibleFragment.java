package com.example.photogallery;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.widget.Toast;

public class VisibleFragment extends Fragment {
	
	//private final static String TAG = "VisibleFragment";
	
	private BroadcastReceiver mShowOnNotification = new BroadcastReceiver() {
		
		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO: check why we don't use input Context
			Toast.makeText( getActivity(), "Got a broadcast " + intent.getAction(), Toast.LENGTH_LONG).show();
			
			setResultCode( Activity.RESULT_CANCELED );
		}
	};
	
	@Override
	public void onResume()
	{
		super.onResume();
		IntentFilter filter = new IntentFilter(PollService.ACTION_SHOW_NOTIFICATION);
		// TODO: check why Handler is needed when passing the permission string
		Handler scheduler = null;
		getActivity().registerReceiver(mShowOnNotification, filter, PollService.PERM_PRIVATE, scheduler);
		
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		getActivity().unregisterReceiver(mShowOnNotification);
	}

}

