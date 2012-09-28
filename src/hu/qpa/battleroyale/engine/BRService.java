package hu.qpa.battleroyale.engine;

import hu.qpa.battleroyale.BRActivity;
import hu.qpa.battleroyale.BRMapActivity;
import hu.qpa.battleroyale.MessageActivity;
import hu.qpa.battleroyale.Prefs;
import hu.qpa.battleroyale.R;
import hu.qpa.battleroyale.engine.types.Spell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;

public class BRService extends Service implements LocationListener {
	private static final String TAG = "BRService";
	public static final String SERVICE_STATE_CHANGED = "Service state is changed.";

	private static final int NOTIFICATION_ID = 876545689;

	private Intent stateChangeIntent;

	LocationManager mLocationManager;
	NotificationManager mNotificationManager;
	BRClient mClient;

	Gson mGson;

	private boolean isInitialized = false;

	private IBinder mBinder = new BRBinder();

	// status
	private BRStatus status;
	private String token;
	private Location mLocation;

	private ArrayList<Integer> processedEvents = new ArrayList<Integer>();

	private Timer mUpdateTimer = new Timer();
	private TimerTask mUpdateTask;

	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	public class BRBinder extends Binder {
		public BRService getService() {
			return BRService.this;
		}
	}

	// vibrate pattern

	// This example will cause the phone to vibrate "SOS" in Morse Code
	// In Morse Code, "s" = "dot-dot-dot", "o" = "dash-dash-dash"
	// There are pauses to separate dots/dashes, letters, and words
	// The following numbers represent millisecond lengths
	int dot = 200; // Length of a Morse Code "dot" in milliseconds
	int dash = 500; // Length of a Morse Code "dash" in milliseconds
	int short_gap = 200; // Length of Gap Between dots/dashes
	int medium_gap = 500; // Length of Gap Between Letters
	int long_gap = 1000; // Length of Gap Between Words
	// long[] pattern = {
	// 0, // Start immediately
	// dot, short_gap, dot, short_gap, dot, // s
	// medium_gap,
	// dash, short_gap, dash, short_gap, dash, // o
	// medium_gap,
	// dot, short_gap, dot, short_gap, dot, // s
	// long_gap
	// };
	long[] pattern = {
			0, // Start immediately
			dot, short_gap, dot, short_gap, dot, short_gap, dot, short_gap,
			dot, long_gap };

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (!isInitialized) {
			stateChangeIntent = new Intent(SERVICE_STATE_CHANGED).putExtra(
					"newState", ServiceState.STARTED);
			mClient = new BRClient();
			mGson = new Gson();
			isInitialized = true;
		}
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
	}

	@Override
	public void onDestroy() {
		mLocationManager.removeUpdates(this);

		super.onDestroy();
	}

	public void fireNotification(CharSequence ticker, CharSequence title,
			CharSequence message, Intent intent, int smallIcon) {
		PendingIntent contentIntent = PendingIntent.getActivity(this,
				(int) System.currentTimeMillis(), intent, 0);

		Notification not = new Notification(smallIcon, ticker,
				System.currentTimeMillis());
		not.flags |= Notification.FLAG_AUTO_CANCEL;
		not.number += 1;
		not.setLatestEventInfo(this, title, message, contentIntent);

		mNotificationManager.notify((int) System.currentTimeMillis(), not);
	}

	private void startPositioning() {
		mLocation = mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		sendLocation(mLocationManager
				.getLastKnownLocation(LocationManager.GPS_PROVIDER));

		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				5000, 0, this);
	}

	private void sendLocation(Location location) {
		// TODO
		if (location != null)
			Log.d(TAG, "location updated:" + location.getLatitude() + ","
					+ location.getLongitude());
	}

	private void newState(ServiceState state, BRStatus status) {
		stateChangeIntent = new Intent(SERVICE_STATE_CHANGED).putExtra(
				BRActivity.EXTRA_SERVICE_STATE, state);
		if (status != null) {
			stateChangeIntent.putExtra(BRActivity.EXTRA_STATUS, status);
		}
		sendBroadcast(stateChangeIntent);
	}

	public void login(String username, String password) {
		String passwordSHA1 = Sha1.getHash(password);
		new callWSMethodTask().execute(
				new BasicNameValuePair("action", "auth"),
				new BasicNameValuePair("user", username),
				new BasicNameValuePair("pass", passwordSHA1));
	}

	public void codeEntry(String entry) {
		new callWSMethodTask().execute(
				new BasicNameValuePair("action", "entry"),
				new BasicNameValuePair("token", token), new BasicNameValuePair(
						"code", entry));
	}

	/**
	 * creates a notification with the given message, which will open a message
	 * dialog
	 * 
	 * @param message
	 *            the message to show
	 * @see MessageActivity
	 */
	public void handleMessage(String message) {
		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(BRActivity.EXTRA_MESSAGE, message);

		fireNotification(getString(R.string.notif_message_title),
				getString(R.string.notif_message_title), message, intent,
				android.R.drawable.ic_dialog_email);

	}

	public void handleWarning(String type, String message) {
		// TODO t�pus
		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(BRActivity.EXTRA_MESSAGE, message);

		fireNotification(getString(R.string.notif_warning_title),
				getString(R.string.notif_warning_title), message, intent,
				android.R.drawable.ic_dialog_alert);
	}

	public void showEnemies(String enemiesJSON) {
		// TODO

		Intent intent = new Intent(this, BRMapActivity.class);
		intent.putExtra(BRMapActivity.INTENT_KEY_SPELL_SHOW_EVERYBODY,
				"enemies 10.000 48.0000");
		String spellText = "L�thatod az ellenfeleket 10 mp-ig";
		String spellTitle = "Var�zslat";
		fireNotification(spellText, spellTitle, spellText, intent,
				android.R.drawable.ic_dialog_map);

	}

	@Override
	public void onLocationChanged(Location location) {
		Log.d(TAG, "location changed");
		this.mLocation = location;

	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	public static enum ServiceState {
		STARTED, ALIVE, ZOMBIE
	}

	public Intent getStateChangeIntent() {
		return stateChangeIntent;
	}

	public void logout() {
		mUpdateTimer.cancel();
		mLocationManager.removeUpdates(this);
		newState(ServiceState.STARTED, null);

	}

	class callWSMethodTask extends AsyncTask<NameValuePair, Void, String> {
		@Override
		protected String doInBackground(NameValuePair... params) {
			try {
				return mClient.callWSMethod(Prefs.wsUrl, Arrays.asList(params));
			} catch (IOException e) {
				Log.e(TAG, "Error calling server", e);
				return "";
			}
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			handleResponse(result);
		}

	}

	private void handleResponse(String responseString) {
		if (responseString.endsWith("\n")) {
			responseString = responseString.split("\n")[0];
		}
		Log.d(TAG, "response:" + responseString);
		if ("".compareTo(responseString) == 0) {
			return;
		}
		if (responseString.length() == 10) {
			// login v�lasz
			startPositioning();
			this.token = responseString;

			// new callWSMethodTask()
			// .execute(
			// new BasicNameValuePair("action", "status"),
			// new BasicNameValuePair("token", this.token),
			// new BasicNameValuePair("status",
			// "{\"lat\":47.12,\"lon\":35.31,\"eventid\":[0,1,2,3,4]}"));
			// TODO start status updater timer
			startUpdateTimer();

			return;
		}
		WSResponse response;
		try {
			response = mGson.fromJson(responseString, WSResponse.class);
		} catch (JsonParseException e) {
			Log.e(TAG, "Error parsing response", e);
			Toast.makeText(getApplicationContext(),
					"Hib�s v�lasz a szervert�l!", Toast.LENGTH_SHORT).show();
			return;
		}
		BRStatus status = new BRStatus(response.username, response.team,
				response.alive, response.score, response.warnsince,
				response.nearestserum, response.code);
		this.token = response.token;
		if (response.events != null) {
			for (String[] event : response.events) {
				handleEvent(event);
			}
		}
		if (response.warnings != null) {
			for (String[] warning : response.warnings) {
				handleWarning(warning[0], warning[1]);
			}
		}

		if (response.alive) {
			newState(ServiceState.ALIVE, status);
		} else {
			newState(ServiceState.ZOMBIE, status);
		}
	}

	private void handleEvent(String... args) {
		int eventID = Integer.valueOf(args[0]);
		String eventType = args[1];
		String message = args[2];

		if ("message".compareTo(eventType) == 0) {
			handleMessage(message);
		} else if ("spell".compareTo(eventType) == 0) {
			handleSpell(message);
		}

		processedEvents.add(eventID);
	}

	private void handleSpell(String message) {
		Spell spell = mGson.fromJson(message, Spell.class);
		
		Intent intent = new Intent(this, BRMapActivity.class);
		intent.putExtra(BRMapActivity.INTENT_KEY_SPELL, spell);

		fireNotification(getString(R.string.notif_spell_title),
				getString(R.string.notif_spell_title), getString(R.string.notif_spell_content), intent,
				android.R.drawable.ic_dialog_map); //TODO rendes ikon
	}
	
	
	

	private void startUpdateTimer() {
		mUpdateTask = new TimerTask() {

			@Override
			public void run() {
				if (mLocation != null) {
					StatusUpdate status = new StatusUpdate(
							mLocation.getLatitude(), mLocation.getLongitude(),
							processedEvents.toArray(new Integer[0]));
					processedEvents.clear();
					new callWSMethodTask().execute(new BasicNameValuePair(
							"action", "status"), new BasicNameValuePair(
							"token", token), new BasicNameValuePair("status",
							mGson.toJson(status)));
				}
			}
		};
		mUpdateTimer.scheduleAtFixedRate(mUpdateTask, 0l,
				Prefs.refreshInterval * 1000);

	}

	private class StatusUpdate {
		double lat;
		double lon;
		Integer[] eventid;

		public StatusUpdate(double lat, double lon, Integer[] eventid) {
			super();
			this.lat = lat;
			this.lon = lon;
			this.eventid = eventid;
		}

	}

}
