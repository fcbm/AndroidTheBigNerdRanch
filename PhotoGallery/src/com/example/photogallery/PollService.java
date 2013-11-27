package com.example.photogallery;

import java.util.ArrayList;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class PollService extends IntentService {

	private static final String TAG = "PollService";
	
	private static final int POLL_INTERVAL = 1000 * 15; // 15sec
	
	public PollService() {
		super(TAG);
	}

	public static void startServiceAlarm(Context ctx, boolean isOn)
	{
		Intent i = new Intent(ctx, PollService.class);
		
		int requestCode = 0;
		int flags = 0;
		// This packages an invocation of Context.startService(i);
		PendingIntent pi = PendingIntent.getService( ctx, requestCode, i, flags);
		
		// pi.send(requestCode) performs the operation associated to the PendingIntent
		
		// AlarmManager is a system service that *can send Intent for you*
		// It works also when the application is not started
		AlarmManager am = (AlarmManager) ctx.getSystemService( Context.ALARM_SERVICE );
		
		if (isOn)
		{
			int type = AlarmManager.RTC;
			// We can only register one Alarm for each PendingIntent
			// set() is used for not repeating alarms, no need to call cancel() in this case
			// setInexactRepeating() is more efficient when precision is not required
			// If there is already an alarm scheduled for the same IntentSender, it will first be canceled
			// Note: for timing operations (ticks, timeouts, etc) it is easier and much more efficient to use Handler
			// If your application wants to allow the delivery times to drift in order to guarantee that at least a 
			// certain time interval always elapses between alarms, then the approach to take is to use one-time alarms, 
			// scheduling the next one yourself when handling each alarm delivery. 
			am.setRepeating(type, System.currentTimeMillis(), POLL_INTERVAL, pi);
		}
		else
		{
			// This cancels the alarm
			am.cancel( pi );
			// We also want to cancel the PendingIntent (TODO: check why)
			// We cancel the PendingIntent so that isServiceAlarmOn will
			// return null in case the alarm is not active (in combination with PendingIntent.FLAG_NO_CREATE)
			// Notice : it seems there is no way to do the same using AlarmManager
			pi.cancel();
		}
	}
	
	public static boolean isServiceAlarmOn(Context ctx)
	{
		Intent i = new Intent(ctx, PollService.class);
		
		int requestCode = 0;
		// This flag says if the PI does not already exist, return null instead of
		// creating it
		int flags = PendingIntent.FLAG_NO_CREATE;

		// Notice : requesting the same PendingIntent twice will get the same PendingIntent
		// This is useful to test whether a  PendingIntent already exists or to cancel
		// the previously issued PendingIntent
		// TODO: check the documentation to see when two PendingIntents are considered equal
		PendingIntent pi = PendingIntent.getService( ctx, requestCode, i, flags);
		
		return pi != null;
				
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		// Service's intents are called "commands"
		Log.i(TAG, "Intent received "+ intent);
		
		// When doing network work in the background checking for
		// connection availability to improve resource usage
		ConnectivityManager cm = (ConnectivityManager) getSystemService( Context.CONNECTIVITY_SERVICE);
		
		// We call getBackgroundDataSetting for compatibility with old devices
		// To check getActiveNetworkInfo we need ACCESS_NETWORK_STATE
		@SuppressWarnings("deprecation")
		boolean isNetworkAvailable = cm.getBackgroundDataSetting() &&
			cm.getActiveNetworkInfo( ) != null;
		
		if (!isNetworkAvailable)
		{
			return;
		}
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences( this );
		String query = sp.getString( FlickrFetch.PREF_SEARCH_QUERY, null);
		String lastItemId = sp.getString( FlickrFetch.PREF_LAST_ID, null);
		
		ArrayList<GalleryItem> items;
		
		if (query != null)
		{
			items = new FlickrFetch().search(query);
		}
		else
		{
			items = new FlickrFetch().fetchItems(0);
		}
		
		if (items.isEmpty())
		{
			return;
		}
		
		String resultId = items.get(0).getId();
		
		if (!resultId.equals( lastItemId ))
		{
			Log.i(TAG, "Got a new result: " + resultId);
			
			// Show notification
			Resources r = getResources();
			int requestCode = 0;
			int flags = 0;
			PendingIntent pi = PendingIntent.getActivity(this, requestCode, new Intent(this, PhotoGalleryActivity.class), flags);
			
			Notification n = new NotificationCompat.Builder(this)
					.setTicker( r.getString( R.string.new_pictures_title))
					.setSmallIcon( android.R.drawable.ic_menu_report_image)
					.setContentTitle( r.getString( R.string.new_pictures_title))
					.setContentText( r.getString( R.string.new_pictures_text))
					.setContentIntent(pi)
					.setAutoCancel( true )
					.build();
			
			NotificationManager nm = (NotificationManager)getSystemService( Context.NOTIFICATION_SERVICE );
			
			int id = 0;
			nm.notify( id, n);
		}
		else
		{
			Log.i(TAG, "Got an old result: " + resultId);
		}
		
		sp.edit().putString( FlickrFetch.PREF_LAST_ID, resultId).commit();
	}

}
