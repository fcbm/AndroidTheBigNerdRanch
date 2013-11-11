package com.example.criminalintent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONTokener;

import android.content.Context;

public class CriminalIntentJSONSerializer {

	private Context mContext;
	private String mFilename;
	
	// This class could be reused everywhere to write a file
	// to the local sandbox. 
	// Notice : we save the entire content even if a bit of info is
	// changed. a DB would be a better alternative in case of bigger dataset
	public CriminalIntentJSONSerializer(Context c, String f)
	{
		mContext = c;
		mFilename = f;
	}
	
	public void saveCrimes(ArrayList<Crime> crimes) throws JSONException, IOException
	{
		JSONArray array = new JSONArray();
		
		for (Crime c : crimes)
		{
			array.put(c.toJSON());
		}
		
		// Write the file to disk
		Writer writer = null;
		
		try
		{
			// openFileOutput prepends the filename with the path to the file's folder
			// in the app's sandbox, creates the file and opens it for writing.
			// Use Context.getFilesDir() to create the file paths by hand
			OutputStream out = mContext.openFileOutput( mFilename, Context.MODE_PRIVATE);
			writer = new OutputStreamWriter(out);
			
			writer.write(array.toString());
		} finally {
			if (writer != null)
				writer.close();
		}
	}
	
	public ArrayList<Crime> loadCrimes() throws IOException, JSONException
	{
		ArrayList<Crime> crimes = new ArrayList<Crime>();
		BufferedReader reader = null;
		
		try
		{
			// Open and read the file into a StringBuilder
			InputStream in = mContext.openFileInput(mFilename);
			
			reader = new BufferedReader( new InputStreamReader( in ));
			
			StringBuilder jsonString = new StringBuilder();
			
			String line = null;
			
			while ((line = reader.readLine()) != null)
			{
				// Line breaks are omitted and irrelevant
				jsonString.append(line);
			}
			
			// JSONTokener : Parses a JSON (RFC 4627) encoded string into the corresponding object.
			JSONArray array = (JSONArray) new JSONTokener( jsonString.toString() ).nextValue();
			
			// Build the array of crimes from JSONObject
			for (int i = 0; i < array.length(); i++)
			{
				crimes.add(new Crime(array.getJSONObject(i)));
			}
		} catch (FileNotFoundException e) {
			// Ignore this one; it happens when starting fresh
		} finally {
			if (reader != null)
				reader.close();
		}
		
		return crimes;
	}
}
