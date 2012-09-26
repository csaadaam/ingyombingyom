package hu.qpa.battleroyale;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.OverlayItem;

public class BRMapActivity extends com.google.android.maps.MapActivity {
	MapView mMapView;
	MyLocationOverlay mmyLocationOverlay;

	public static final String INTENT_KEY_SPELL_SHOW_EVERYBODY = "spell_show_everybody";

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);

		mMapView = (MapView) findViewById(R.id.mapView);
		mmyLocationOverlay = new MyLocationOverlay(this, mMapView);
		mmyLocationOverlay.enableCompass();

		mMapView.getOverlays().add(mmyLocationOverlay);
		mMapView.postInvalidate();

		handleIntent(getIntent());
	}

	private void handleIntent(Intent intent) {
		if (intent == null) {
			return;
		}

		Bundle extras = intent.getExtras();

		if (extras == null) {
			return;
		}
		if (extras.containsKey(INTENT_KEY_SPELL_SHOW_EVERYBODY)) {

			// create the overlay of enemies
			MyItemizedOverlay itemizedOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(
							android.R.drawable.arrow_up_float), this);

			// add the enemies
			itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(47501000,
					19040000), "item1", "ITEM1"));

			mMapView.getOverlays().add(itemizedOverlay);
			extras.remove(INTENT_KEY_SPELL_SHOW_EVERYBODY);

			final MyItemizedOverlay itemizedOverlay_ = itemizedOverlay;

			// remove markers after 10 seconds
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					mMapView.getOverlays().remove(itemizedOverlay_);

				}
			}, 10000);
		}

	}

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void onResume() {
		super.onResume();
		mmyLocationOverlay.enableMyLocation();
	}

	@Override
	protected void onPause() {
		super.onPause();
		mmyLocationOverlay.disableMyLocation();
	}

	class MyItemizedOverlay extends ItemizedOverlay<OverlayItem> {
		private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
		private Context mContext;

		public MyItemizedOverlay(Drawable arg0) {
			super(boundCenterBottom(arg0));
		}

		public MyItemizedOverlay(Drawable defaultMarker, Context context) {
			super(boundCenterBottom(defaultMarker));
			mContext = context;
		}

		@Override
		protected OverlayItem createItem(int arg0) {
			return mOverlays.get(arg0);
		}

		@Override
		public int size() {
			return mOverlays.size();
		}

		public void addOverlay(OverlayItem overlay) {
			mOverlays.add(overlay);
			populate();
		}

		@Override
		protected boolean onTap(int index) {
			OverlayItem item = mOverlays.get(index);
			AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
			dialog.setTitle(item.getTitle());
			dialog.setMessage(item.getSnippet());
			dialog.show();
			return true;
		}

	}
}
