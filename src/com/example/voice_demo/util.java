package com.example.voice_demo;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class util {
	String nextword(String word) {
		char end = word.charAt(word.length() - 1);
		String result = "say a place begin with letter ";
		return (result + end);
	}

	Boolean geocheck(String name) throws IOException {
		// int len = 2048;
		String parameters = "&maxRows=1&username=billgujie&style=SHORT&type=json";
		name = URLEncoder.encode(name);
		String urlString = "http://api.geonames.org/search?";
		urlString = urlString + "name_equals" + name + parameters;
		String buf = null;

		URL url = new URL(urlString);
		HttpURLConnection urlConnection = (HttpURLConnection) url
				.openConnection();
		try {
			InputStream in = new BufferedInputStream(
					urlConnection.getInputStream());
			readIt(in, 1024);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			urlConnection.disconnect();
		}

		if (buf.length() == 0) {
			return false;
		}
		return true;
	}

	private String readIt(InputStream stream, int len) throws IOException {
		Reader reader = null;
		reader = new InputStreamReader(stream, "UTF-8");
		char[] buffer = new char[len];
		reader.read(buffer);
		return new String(buffer);
	}
}