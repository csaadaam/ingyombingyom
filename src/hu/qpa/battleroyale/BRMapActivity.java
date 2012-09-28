package hu.qpa.battleroyale;

import hu.qpa.battleroyale.engine.types.Point;
import hu.qpa.battleroyale.engine.types.Spell;

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

	public static final String INTENT_KEY_SPELL = "spell";
	public static final String INTENT_KEY_SPELL_SHOW_EVERYBODY = "spell_show_everybody";
	public static final String INTENT_KEY_NEAREST_SERUM = "nearest_serum";

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_map);

		mMapView = (MapView) findViewById(R.id.mapView);
		mMapView.setBuiltInZoomControls(true);
		mmyLocationOverlay = new MyLocationOverlay(this, mMapView);
		mmyLocationOverlay.enableCompass();

		mMapView.getOverlays().add(mmyLocationOverlay);
		mMapView.postInvalidate();

		mmyLocationOverlay.runOnFirstFix(new Runnable() {
			public void run() {
				mMapView.getController().setZoom(18);
				mMapView.getController().animateTo(
						mmyLocationOverlay.getMyLocation());
			}
		});
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
			MyItemizedOverlay enemisOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(
							android.R.drawable.arrow_up_float), this); // TODO
																		// kép

			// add the enemies
			enemisOverlay.addOverlay(new OverlayItem(new GeoPoint(47501000,
					19040000), "item1", "ITEM1"));

			mMapView.getOverlays().add(enemisOverlay);
			extras.remove(INTENT_KEY_SPELL_SHOW_EVERYBODY);

			final MyItemizedOverlay itemizedOverlay_ = enemisOverlay;

			// remove markers after 10 seconds
			new Timer().schedule(new TimerTask() {

				@Override
				public void run() {
					mMapView.getOverlays().remove(itemizedOverlay_);

				}
			}, 10000);
		}
		if (extras.containsKey(INTENT_KEY_SPELL)) {
			Spell spell = (Spell) extras.getSerializable(INTENT_KEY_SPELL);
			final MyItemizedOverlay friendOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(
							android.R.drawable.btn_star_big_on), this);
			final MyItemizedOverlay enemyOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(
							android.R.drawable.ic_dialog_alert), this);
			final MyItemizedOverlay itemOverlay = new MyItemizedOverlay(this
					.getResources()
					.getDrawable(android.R.drawable.ic_input_add), this);

			boolean friendsEmpty = true;
			boolean enemiesEmpty = true;
			boolean itemsEmpty = true;

			for (Point p : spell.getParameter()) {
				if ("friend".compareTo(p.getType()) == 0) {
					friendOverlay.addOverlay(new OverlayItem(new GeoPoint(
							(int) (p.getLatitude() * 1e6), (int) (p
									.getLongitude() * 1e6)), p.getName(), p
							.getType()));
					friendsEmpty = false;
				} else if ("enemy".compareTo(p.getType()) == 0) {
					enemyOverlay.addOverlay(new OverlayItem(new GeoPoint(
							(int) (p.getLatitude() * 1e6), (int) (p
									.getLongitude() * 1e6)), p.getName(), p
							.getType()));
					enemiesEmpty = false;
				} else if ("item".compareTo(p.getType()) == 0) {
					itemOverlay.addOverlay(new OverlayItem(new GeoPoint(
							(int) (p.getLatitude() * 1e6), (int) (p
									.getLongitude() * 1e6)), p.getName(), p
							.getType()));
					itemsEmpty = false;
				}
			}
			if (!friendsEmpty) {
				mMapView.getOverlays().add(friendOverlay);
			}
			if (!enemiesEmpty) {
				mMapView.getOverlays().add(enemyOverlay);
			}
			if (!itemsEmpty) {
				mMapView.getOverlays().add(itemOverlay);
			}
			extras.remove(INTENT_KEY_SPELL);

			// remove markers after 10 seconds
			 new Timer().schedule(new TimerTask() {
			
			 @Override
			 public void run() {
			 mMapView.getOverlays().remove(friendOverlay);
			 mMapView.getOverlays().remove(enemyOverlay);
			 mMapView.getOverlays().remove(itemOverlay);
			
			 }
			 }, 10000);

		}
		if (extras.containsKey(INTENT_KEY_NEAREST_SERUM)) {
			MyItemizedOverlay serumOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(
							android.R.drawable.ic_menu_directions), this); // TODO
																			// kép
			double[] nearestSerum = extras
					.getDoubleArray(INTENT_KEY_NEAREST_SERUM);
			serumOverlay.addOverlay(new OverlayItem(new GeoPoint(
					(int) (nearestSerum[0] * 1e6),
					(int) (nearestSerum[1] * 1e6)), "", "Legközelebbi szérum"));
			mMapView.getOverlays().add(serumOverlay);
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
