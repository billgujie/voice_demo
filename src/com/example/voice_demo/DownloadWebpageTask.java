package com.example.voice_demo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import android.os.AsyncTask;

public class DownloadWebpageTask extends AsyncTask<String, Void, String> {

	@Override
	protected String doInBackground(String... urls) {
		try {
			return downloadUrl(urls[0]);
		} catch (IOException e) {
			return "Unable to retrieve web page. URL may be invalid.";
		}
	}

	private String downloadUrl(String myurl) throws IOException {
		InputStream is = null;
		String contentAsString="";
		int len = 2048;

		try {
			URI uri = new URI(myurl);
				URL url = uri.toURL();
			//URL url = new URL(myurl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("GET");
			conn.setDoInput(true);
			// Starts the query
			conn.connect();
			is = conn.getInputStream();

			// Convert the InputStream into a string
			contentAsString = readIt(is, len);
			// Makes sure that the InputStream is closed after the app is
			// finished using it.
		} catch (URISyntaxException e) {
			
			e.printStackTrace();
		} finally {
			if (is != null) {
				is.close();
			}
		}
		return contentAsString;
	}

	public String readIt(InputStream stream, int len) throws IOException,
			UnsupportedEncodingException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}

}
