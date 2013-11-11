package com.example.criminalintent;

import java.util.ArrayList;
import java.util.UUID;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

// This class will be a Singleton:
// A singleton exists as long as the application stays in memory, so storing 
// the list in a singleton will keep the crime data available no matter what 
// happens with activities, fragments, and their life-cycles. 
public class CrimeLab {

	private static final String TAG = "CrimeLab";
	private static final String FILENAME = "crimes.json";
	
	private static CrimeLab sCrimeLab;
	private Context mAppContex;
	private ArrayList<Crime> mCrimes;
	private CriminalIntentJSONSerializer mSerializer;
	
	private CrimeLab(Context appContext)
	{
		mAppContex = appContext;
		mSerializer = new CriminalIntentJSONSerializer(mAppContex, FILENAME);
		
		try
		{
			mCrimes = mSerializer.loadCrimes();
		} catch (Exception e) 
		{
			mCrimes = new ArrayList<Crime>();
			Log.e(TAG, "Error loading crimes: ", e);
		}
		
		/*
		mCrimes = new ArrayList<Crime>();
		
		for (int i = 0; i < 100; i++)
		{
			Crime c = new Crime();
			c.setTitle( "Crime #" + i);
			c.setSolved( i%2 == 0);
			mCrimes.add(c);
		}
		*/
	}
	
	// You cannot be sure that just any  Context  will exist as long as  
	// CrimeLab  needs it, which is for the life of the application. 
	// To ensure that your singleton has a long-term  Context  to work with, 
	// you call  getApplicationContext() and trade the passed-in Context for the  application context. 
	// The  application context  is a  Context that is global to your application. 
	// Whenever you have an application-wide singleton, it should always use the application context. 	
	public static CrimeLab get(Context appContext)
	{
		if (sCrimeLab == null)
		{
			sCrimeLab = new CrimeLab( appContext );
		}
		return sCrimeLab;
	}
	
	public ArrayList<Crime> getCrimes()
	{
		return mCrimes;
	}
	
	public void addCrime(Crime c)
	{
		mCrimes.add(c);
	}
	
	// TODO: handle drop photo when deleting a Crime
	public void deleteCrime(Crime c)
	{
		mCrimes.remove( c );
	}
	
	public Crime getCrime(UUID id)
	{
		for (Crime c : mCrimes)
		{
			if (c.getId().equals( id ) )
			{
				return c;
			}
		}
		return null;
	}
	
	public boolean saveCrimes()
	{
		try
		{
			mSerializer.saveCrimes(mCrimes);
			Log.d(TAG, "crimes saved to file");
			Toast.makeText(mAppContex, "Crimes saved to file", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Log.e(TAG, "Error saving crimes: ", e);
			Toast.makeText(mAppContex, "Error saving crimes: " + e.toString(), Toast.LENGTH_LONG).show();
			return false;
		}
		
		return true;
	}
}
