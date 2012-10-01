package hu.qpa.battleroyale;

import hu.qpa.battleroyale.engine.BRService;
import hu.qpa.battleroyale.engine.BRService.ServiceState;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class BRActivity extends Activity {
	public static final String EXTRA_USER_ID = "user_id";
	public static final String EXTRA_STATUS = "status";
	public static final String EXTRA_SERVICE_STATE = "service_state";
	public static final String EXTRA_MESSAGE = "message";

	BRService mService;
	ServiceConnection mServiceConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mService = null;

		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = ((BRService.BRBinder) service).getService();

		}
	};

	class ServiceStateChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context_, Intent intent) {
			handleStateChange(intent);
		}
	}

	ServiceStateChangeReceiver serviceStateChangeReceiver;
	private IntentFilter stateChangeFilter;

	void handleStateChange(Intent intent) {
		if (Prefs.debug) {
			ServiceState state = (ServiceState) intent.getExtras().get(
					EXTRA_SERVICE_STATE);
			if (state != null) {
				Log.d(getClass().getSimpleName(), "New state:" + state.name());
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// starting service
		startService(new Intent(this, BRService.class));

		// binding to Service
		Intent bindIntent = new Intent(this, BRService.class);
		bindService(bindIntent, mServiceConnection, Context.BIND_AUTO_CREATE);

		// register to SERVICE_STATE_CHANGED intent
		stateChangeFilter = new IntentFilter(BRService.SERVICE_STATE_CHANGED);

		serviceStateChangeReceiver = new ServiceStateChangeReceiver();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(serviceStateChangeReceiver);

	}

	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(serviceStateChangeReceiver, stateChangeFilter);

		if (mService != null) {
			handleStateChange(mService.getStateChangeIntent());
		}

	}

	@Override
	protected void onDestroy() {

		unbindService(mServiceConnection);
		super.onStop();

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.logout:
			if (mService != null) {
				mService.logout();
			}
			break;
		case R.id.credits:
			startActivity(new Intent(this, CreditsActivity.class));
			break;
		}
		return true;
	}

}
