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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class VoiceActivity extends Activity implements OnInitListener {

	private int MY_DATA_CHECK_CODE = 0;
	private TextToSpeech myTTS;
	private static final int REQUEST_CODE = 1234;
	private ListView resultList;
	private ArrayList<String> matches;
	Button speakButton;
	public TextView timertext;
	private HashSet<String> geoSet = new HashSet<String>();
	//private CountDownTimer timer;
	//private static long total = 10000;
	// private boolean timer_flag = false;
	private boolean first_time = true;
	private static String currentname;
	private static String previousname;
	private static ArrayList<String> results = new ArrayList<String>();
	private static ArrayAdapter<String> resultadapter;
	CountDownTimerPausable mycounter;
	private WebView mapview;
	private String mapq_prefix;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.voice_demo);
		mycounter=new CountDownTimerPausable(10000,1000);
		timertext = (TextView) findViewById(R.id.timer);
		speakButton = (Button) findViewById(R.id.speakButton);
		resultList = (ListView) findViewById(R.id.list);
		resultadapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, results);
		mapview=(WebView)findViewById(R.id.map);
		// Disable button if no recognition service is present
		PackageManager pm = getPackageManager();
		List<ResolveInfo> activities = pm.queryIntentActivities(new Intent(
				RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
		
		WebSettings webViewSettings = mapview.getSettings();
		webViewSettings.setJavaScriptCanOpenWindowsAutomatically(true);
		webViewSettings.setJavaScriptEnabled(true);
		//webViewSettings.setPluginsEnabled(true);
		//webViewSettings.setBuiltInZoomControls(true);
		//webViewSettings.setPluginState(PluginState.ON);
		mapq_prefix="<iframe height=\"200\" width=\"100%\" frameborder=\"0\" style=\"border:0\" src=\"https://www.google.com/maps/embed/v1/place?key=AIzaSyA3JmPmuWf9qqPP5G7sa4mxJ70pHLkiW6U&q=";
		//mapview.loadData("<iframe height=\"100%\" width=\"100%\" src=\"https://www.google.com/maps/embed/v1/place?key=AIzaSyA3JmPmuWf9qqPP5G7sa4mxJ70pHLkiW6U&q=Space+Needle,Seattle+WA\"> </iframe>", "text/html","utf-8");
		
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

	//@SuppressWarnings("deprecation")
	private void newGeoCheck(String word) {

		if (!first_time) {
			char end = previousname.charAt(previousname.length() - 1);
			if (end != word.toLowerCase().charAt(0)) {
				// startCountDown();
				Toast.makeText(getApplicationContext(),
						"say a place begin with '" + end + "'!",
						Toast.LENGTH_LONG).show();
			} else {
				String parameters = "&maxRows=1&username=billgujie&style=SHORT&type=JSON&featureClass=P&featureClass=A";
				String urlString = "http://api.geonames.org/search?";
				try {
					urlString = urlString + "name_equals="
							+ URLEncoder.encode(word, "UTF-8") + parameters;
				} catch (UnsupportedEncodingException e) {
					//
					e.printStackTrace();
				}
				new DownloadWebpageTask().execute(urlString);
			}
		} else {

			String parameters = "&maxRows=1&username=billgujie&style=SHORT&type=JSON&featureClass=P&featureClass=A";
			String urlString = "http://api.geonames.org/search?";
			try {
				urlString = urlString + "name_equals=" + URLEncoder.encode(word,"UTF-8")
						+ parameters;
			} catch (UnsupportedEncodingException e) {
				
				e.printStackTrace();
			}
			new DownloadWebpageTask().execute(urlString);
		}
	}

	public void speakWords(String word) {
		myTTS.speak(word, TextToSpeech.QUEUE_FLUSH, null);
	}

	private void startVoiceRecognitionActivity() {
		if (!first_time) {
			//mycounter.pause();
		}
		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say a location...");
		startActivityForResult(intent, REQUEST_CODE);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (!first_time) {
			//mycounter.start();
		}
		if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
			matches = data
					.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
			//float[] confidence = data.getFloatArrayExtra(RecognizerIntent.EXTRA_CONFIDENCE_SCORES);
			// ArrayList<String> results = new ArrayList<String>();
			// for (int i = 0; i < matches.size(); i++) {
			// results.add(matches.get(i) + ": " + confidence[i]);
			// }

			currentname = matches.get(0);
			newGeoCheck(currentname);

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

	public class CountDownTimerPausable {
	    long millisInFuture = 0;
	    long countDownInterval = 0;
	    long millisRemaining =  0;

	    CountDownTimer countDownTimer = null;

	    boolean isPaused = true;

	    public CountDownTimerPausable(long millisInFuture, long countDownInterval) {
	        super();
	        this.millisInFuture = millisInFuture;
	        this.countDownInterval = countDownInterval;
	        this.millisRemaining = this.millisInFuture;
	    }
	    private void createCountDownTimer(){
	        countDownTimer = new CountDownTimer(millisRemaining,countDownInterval) {

	            @Override
	            public void onTick(long millisUntilFinished) {
	                millisRemaining = millisUntilFinished;
	                timertext.setText(millisUntilFinished/1000+" seconds remain");

	            }

	            @Override
	            public void onFinish() {
	            	millisRemaining=0;
	            	myTTS.speak("time's up!", TextToSpeech.QUEUE_FLUSH, null);
	            	timertext.setText("");
	            	gameRestart();

	            }
	        };
	    }
	    /**
	     * Cancel the countdown.
	     */
	    public final void cancel(){
	        if(countDownTimer!=null){
	            countDownTimer.cancel();
	        }
	        this.millisRemaining = 0;
	    }
	    /**
	     * Start or Resume the countdown. 
	     * @return CountDownTimerPausable current instance
	     */
	    public synchronized final CountDownTimerPausable start(){
	        if(isPaused){
	            createCountDownTimer();
	            countDownTimer.start();
	            isPaused = false;
	        }
	        return this;
	    }
	    /**
	     * Pauses the CountDownTimerPausable, so it could be resumed(start)
	     * later from the same point where it was paused.
	     */
	    public void pause()throws IllegalStateException{
	        if(isPaused==false){
	            countDownTimer.cancel();
	        } else{
	            throw new IllegalStateException("CountDownTimerPausable is already in pause state, start counter before pausing it.");
	        }
	        isPaused = true;
	    }
	    public boolean isPaused() {
	        return isPaused;
	    }
	}

	private class DownloadWebpageTask extends AsyncTask<String, Void, String> {

		@Override
		protected String doInBackground(String... urls) {
			try {
				return downloadUrl(urls[0]);
			} catch (IOException e) {
				return "Unable to retrieve web page. URL may be invalid.";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			JsonParser checker = new JsonParser();
			if (checker.verify(result)) {
				if (geoSet.add(currentname)) {

					// insert the return string for display
					results.add(0, checker.getResult());
					resultList.setAdapter(resultadapter);
					StringBuilder sb = new StringBuilder();
					sb.append(mapq_prefix);
					try {
						sb.append(URLEncoder.encode(checker.getResult(), "UTF-8"));
					} catch (UnsupportedEncodingException e) {
						// 
						e.printStackTrace();
					}
					sb.append("\" > </iframe>");
					mapview.loadData(sb.toString(),"text/html","utf-8");
					previousname = currentname;
					util a = new util();
					String sentence = a.nextword(currentname);
					first_time = false;
					//mycounter.cancel();
					//mycounter=new CountDownTimerPausable(10000,1000);
					//mycounter.start();
					speakWords(sentence);
				} else {
					Toast.makeText(getApplicationContext(),
							"name already used, try another", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				Toast.makeText(getApplicationContext(), "not a valid name",
						Toast.LENGTH_LONG).show();
				//mycounter.start();
			}
		}

		private String downloadUrl(String myurl) throws IOException {
			InputStream is = null;
			String contentAsString = "";
			int len = 2048;

			try {
				URI uri = new URI(myurl);
				URL url = uri.toURL();
				// URL url = new URL(myurl);
				HttpURLConnection conn = (HttpURLConnection) url
						.openConnection();
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
	
	public void gameRestart() {
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
		// set title
		alertDialogBuilder.setTitle("Restart Game?");
		// set dialog message
		alertDialogBuilder
				.setCancelable(false)
				.setPositiveButton("RESTART", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						recreate();
					}
				})
				.setNegativeButton("CANCEL",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		// create alert dialog
		AlertDialog alertDialog = alertDialogBuilder.create();
		// show it
		alertDialog.show();
	}

}
