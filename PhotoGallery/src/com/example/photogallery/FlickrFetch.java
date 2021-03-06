package com.example.photogallery;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.net.Uri;
import android.util.Log;

public class FlickrFetch {

	public static final String PREF_SEARCH_QUERY = "searchQuery";
	public static final String PREF_LAST_ID = "lastResultId";
	
	private static final String TAG = "FlickrFetch";
	private static final String ENDPOINT = "http://api.flickr.com/services/rest/";
	private static final String API_KEY = "0d61560f33eadb4d0fa0cca5d63c97e1";
	private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
	private static final String METHOD_SEARCH = "flickr.photos.search";
	private static final String PARAM_EXTRAS = "extras";
	private static final String PARAM_TEXT = "text";
	//private static final String PARAM_PAGE = "page";
	
	private static final String EXTRA_SMALL_URL = "url_s";
	
	private static final String XML_PHOTO = "photo";
	
	public ArrayList<GalleryItem> fetchItems(int page)
	{
		
		// Uri is a convenience class for creating properly escaped parametrized URLs
		
		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter("method", METHOD_GET_RECENT)
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
				//.appendQueryParameter(PARAM_PAGE, "" + page)
				.build().toString();
		
		return downloadGalleryItems(url);
	}
	
	public ArrayList<GalleryItem> search(String query)
	{
		
		// Uri is a convenience class for creating properly escaped parametrized URLs
		
		String url = Uri.parse(ENDPOINT).buildUpon()
				.appendQueryParameter("method", METHOD_SEARCH)
				.appendQueryParameter("api_key", API_KEY)
				.appendQueryParameter(PARAM_EXTRAS, EXTRA_SMALL_URL)
				.appendQueryParameter(PARAM_TEXT, query)
				.build().toString();
		
		return downloadGalleryItems(url);
	}	
	public ArrayList<GalleryItem> downloadGalleryItems(String url)
	{
		ArrayList<GalleryItem> items = new ArrayList<GalleryItem>();
		
		try
		{
			String xmlString = getUrl(url);
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			
			// We can also pass directly the InputStream object returned
			// by URLConnection.getInputStream()
			parser.setInput( new StringReader( xmlString) );
			
			parseItems(items, parser);
			
			Log.i(TAG, "Received xml : " + xmlString);
		} catch (IOException e) {
			Log.e(TAG, "Failed to fetch items" , e );
		} catch (XmlPullParserException e) {
			Log.e(TAG, "Failed to parse items" , e );
		}
		
		return items;
	}	
	
	private void parseItems(ArrayList<GalleryItem> items, XmlPullParser parser) throws XmlPullParserException, IOException
	{
		
		int eventType = parser.next();
		
		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			if (eventType == XmlPullParser.START_TAG && XML_PHOTO.equals( parser.getName() ))
			{
				String id = parser.getAttributeValue( null, "id");
				String caption = parser.getAttributeValue( null, "title");
				String smallUrl = parser.getAttributeValue( null, EXTRA_SMALL_URL);
				String owner = parser.getAttributeValue( null, "owner");
				
				GalleryItem item = new GalleryItem();
				item.setId(id);
				item.setCaption( caption) ;
				item.setUrl(smallUrl);
				item.setOwner(owner);
				
				items.add(item);
			}
			eventType = parser.next();
		}
	}
	
	public byte[] getUrlBytes(String urlSpec) throws IOException
	{
		URL url = new URL(urlSpec);
		
		
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			
			// Here really happens the connection to the remote HTTP server
			InputStream in = connection.getInputStream();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				return null;
			}
			
			int bytesRead = 0;
			
			byte[] buffer = new byte[1024];
			
			while ((bytesRead = in.read(buffer)) > 0)
			{
				// TODO: try to download a big file
				// and check if second parameter needs
				// to be variable
				
				out.write(buffer, 0, bytesRead);
			}
			
			out.close();
			return out.toByteArray();
			
		} finally {
			
			connection.disconnect();
		}
	}
	
	public String getUrl(String urlSpec) throws IOException
	{
		return new String(getUrlBytes(urlSpec));
	}
}
