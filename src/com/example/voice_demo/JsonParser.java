package com.example.voice_demo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonParser {
	JSONObject myjson;
	String place;
	
	public String getResult(){
		return place;
	}
	
	public boolean verify(String input){
		try {
			myjson = new JSONObject(input);
			if (myjson.has("totalResultsCount")){
				//JSONObject temp=myjson.getJSONObject("totalResultsCount");
				String count = myjson.getString("totalResultsCount");
				int value = Integer.parseInt(count);
				if (value>0){
					JSONArray geonames = myjson.getJSONArray("geonames");
					String geoname_s = geonames.get(0).toString();
					JSONObject geoname = new JSONObject(geoname_s);
					String toponymName = geoname.getString("toponymName");
					String countryCode = geoname.getString("countryCode");
					StringBuilder sb = new StringBuilder();
					sb.append(toponymName);
					sb.append(" ,");
					sb.append(countryCode);
					place=sb.toString();
					return true;
				}else{
					return false;
				}
			}else{
				return false;
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}

	}
}
