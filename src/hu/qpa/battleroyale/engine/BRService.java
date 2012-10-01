package hu.qpa.battleroyale.engine;

import hu.qpa.battleroyale.BRActivity;
import hu.qpa.battleroyale.BRMapActivity;
import hu.qpa.battleroyale.MessageActivity;
import hu.qpa.battleroyale.Prefs;
import hu.qpa.battleroyale.R;
import hu.qpa.battleroyale.engine.types.Spell;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
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
import com.google.gson.reflect.TypeToken;

public class BRService extends Service implements LocationListener {
	private static final String TAG = "BRService";
	public static final String SERVICE_STATE_CHANGED = "Service state is changed.";

	// private static final int NOTIFICATION_ID = 876545689;

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
				.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
//		sendLocation(mLocationManager
//				.getLastKnownLocation(LocationManager.GPS_PROVIDER));

		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
				5000, 0, this);
	}

//	private void sendLocation(Location location) {
//		// TODO
//		if (location != null)
//			Log.d(TAG, "location updated:" + location.getLatitude() + ","
//					+ location.getLongitude());
//	}

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
		// TODO típus
		Intent intent = new Intent(this, MessageActivity.class);
		intent.putExtra(BRActivity.EXTRA_MESSAGE, message);

		fireNotification(getString(R.string.notif_warning_title),
				getString(R.string.notif_warning_title), message, intent,
				android.R.drawable.ic_dialog_alert);
	}

//	@Deprecated
//	public void showEnemies(String enemiesJSON) {
//		// TODO
//
//		Intent intent = new Intent(this, BRMapActivity.class);
//		intent.putExtra(BRMapActivity.INTENT_KEY_SPELL_SHOW_EVERYBODY,
//				"enemies 10.000 48.0000");
//		String spellText = "Láthatod az ellenfeleket 10 mp-ig";
//		String spellTitle = "Varázslat";
//		fireNotification(spellText, spellTitle, spellText, intent,
//				android.R.drawable.ic_dialog_map);
//
//	}

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
		if (mUpdateTimer != null) {
			mUpdateTimer.cancel();
			mUpdateTimer.purge();
			mUpdateTimer = null;
		}
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
		if (Prefs.debug) {
			Log.d(TAG, "response:" + responseString);
		}
		if ("".compareTo(responseString) == 0) {
			Toast.makeText(getApplicationContext(), "Rossz felhasználónév vagy jelszó, vagy nincs net, vagy valami más hiba :)", Toast.LENGTH_LONG).show();
			newState(ServiceState.STARTED, null);
			return;
		}
		if (responseString.length() == 10) {
			// login válasz
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
					"Hibás válasz a szervertõl!", Toast.LENGTH_SHORT).show();
			return;
		}
		boolean isAlive_ = response.alive == 1;
		Date date_ = new Date(1970, 1, 1);
		try {

			if (response.lastupdate == null)
				response.lastupdate = "1970-01-01 1:00:00";
			date_ = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
					.parse(response.lastupdate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// teszt
		// ArrayList<double[]>borders = new ArrayList<double[]>();
		// borders.add(new double[]{47.5513, 18.9934});
		// borders.add(new double[]{47.55228, 18.9991});
		// borders.add(new double[]{47.55118, 19.0055});
		// borders.add(new double[]{47.54791, 19.0036});
		// borders.add(new double[]{47.54736, 18.9964});

		status = new BRStatus(response.username, response.team,
				isAlive_, response.score, response.lastupdate,
				response.nearestserum, response.code, response.borders, response.cspeed);
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

		if (response.alive == 1) {
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
		} else if ("killed".compareTo(eventType) == 0) {
			handleMessage("Megölted " + message + "t");
		} else if ("killed by".compareTo(eventType) == 0) {
			if (message.contains(":")) {
				handleWarning("killed by",
						"Megölt: " + message.substring(0, message.indexOf(":")));
				handleMessage("Megölt: "
						+ message.substring(0, message.indexOf(":")));
			} else {
				handleWarning("killed by", "Megölt: " + message);
				handleMessage("Megölt: " + message);
			}
		}

		processedEvents.add(eventID);
	}

	private void handleSpell(String message) {
		// Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT)
		// .show();
		Spell spell = null;
		try {
			spell = mGson.fromJson(message, Spell.class);
		} catch (JsonParseException e) {
			Log.w(TAG, "", e);
		}

		Intent intent = new Intent(this, BRMapActivity.class);
		if (spell != null) {// spell
			intent.putExtra(BRMapActivity.INTENT_KEY_SPELL, spell);
			intent.putExtra(BRMapActivity.INTENT_KEY_BORDERS, status.getBorders());
			String spellMessage = "";
			switch (spell.getID()) {
			case 2:
				spellMessage = "Radar: " + Prefs.spellTimeout
						+ " másodpercig láthatsz mindenkit";
				break;
			case 3:
				spellMessage = "Kilátó: Láthatod a csapattársad";
				break;
			case 4:
				spellMessage = "Kincskeresõ: Láthatod a legközelebbi tárgyat";
				break;
			}

			fireNotification(getString(R.string.notif_spell_title),
					getString(R.string.notif_spell_title), spellMessage,
					intent, android.R.drawable.ic_dialog_map);
		} else {// üzenet a csapattól
			// Log.d(TAG, message);
			// Toast.makeText(getApplicationContext(), message,
			// Toast.LENGTH_SHORT).show();
			Map<String, String> messageMap = mGson.fromJson(message,
					new TypeToken<Map<String, String>>() {
					}.getType());

			handleMessage("Üzenet a csapattól: " + messageMap.get("Parameter"));

		}

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
		if (mUpdateTimer == null)
			mUpdateTimer = new Timer();
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
