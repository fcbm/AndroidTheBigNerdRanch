package com.example.photogallery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.preference.PreferenceManager;
import android.util.Log;

// This BroadcastReceiver is declared in the Manifest and is active
// even when the application is not running
// Just like Activities and Services, the BroadcastReceiver needs an
// IntentFilter being declared either in code or in XML to describe
// which events it is interested
public class StartupReceiver extends BroadcastReceiver {

	private static final String TAG = "StartupReceiver";
	
	@Override
	public void onReceive(Context context, Intent intent) {

		Log.i(TAG, "Received broadcast intent : " + intent.getAction());
		
		boolean isOn = PreferenceManager.getDefaultSharedPreferences( context )
								.getBoolean( PollService.PREF_IS_ALARM_ON, false);
		
		PollService.startServiceAlarm( context, isOn);
	}

}
