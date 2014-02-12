package com.example.voice_demo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

public class VoiceActivity extends Activity implements OnInitListener {

	private int MY_DATA_CHECK_CODE = 0;
	private TextToSpeech myTTS;
	private static final int REQUEST_CODE = 1234;
	private ListView resultList;
	private ArrayList<String> matches;
	Button speakButton;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_demo);

		speakButton = (Button) findViewById(R.id.speakButton);

		resultList = (ListView) findViewById(R.id.list);

		// Disable button if no recognition service is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		if (activities.size() == 0) {
			speakButton.setEnabled(false);
			Toast.makeText(getApplicationContext(), "Recognizer Not Found",
					Toast.LENGTH_LONG).show();
		}
		// TTS check
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

		speakButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				startVoiceRecognitionActivity();

			}
		});
	}

	private void promt(String word) {
		util a = new util();
		String sentence = a.nextword(word);
		String parameters = "&maxRows=1&username=billgujie&style=SHORT&type=xml";
		//word = URLEncoder.encode(name);
		String urlString = "http://api.geonames.org/search?";
		urlString = urlString + "name_equals=" + URLEncoder.encode(word) + parameters;
		String buf = null;
		new DownloadWebpageTask().execute(urlString);
		speakWords(sentence);
		
	}

	public void speakWords(String word) {
		myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
	}

	private void startVoiceRecognitionActivity() {
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
				"AndroidBite Voice Recognition...");
		startActivityForResult(intent, REQUEST_CODE);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			float[] confidence = data
					.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
			ArrayList<String> results = new ArrayList<String>();
			for (int i = 0; i < matches.size(); i++) {
				results.add(matches.get(i) + ": " + confidence[i]);
			}
			resultList.setAdapter(new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, results));
			promt(matches.get(0));

		}
		if (requestCode == MY_DATA_CHECK_CODE) {
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
				myTTS = new TextToSpeech(this, this);
			} else {
				Intent installTTSIntent = new Intent();
				installTTSIntent
						.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
				startActivity(installTTSIntent);
			}
		}

		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			int result = myTTS.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				Log.e("TTS", "This Language is not supported");
			}
		} else if (status == TextToSpeech.ERROR) {
			Log.e("TTS", "Initilization Failed!");
		}

	}
}
