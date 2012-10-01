package hu.qpa.battleroyale;

import hu.qpa.battleroyale.engine.types.Point;
import hu.qpa.battleroyale.engine.types.Spell;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.google.android.maps.Projection;

public class BRMapActivity extends com.google.android.maps.MapActivity {
	MapView mMapView;
	MyLocationOverlay mmyLocationOverlay;

	public static final String INTENT_KEY_SPELL = "spell";
	public static final String INTENT_KEY_SPELL_SHOW_EVERYBODY = "spell_show_everybody";
	public static final String INTENT_KEY_NEAREST_SERUM = "nearest_serum";
	public static final String INTENT_KEY_BORDERS = "borders";

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();

	ArrayList<double[]> borders;

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

		// ez már nincs
		// if (extras.containsKey(INTENT_KEY_SPELL_SHOW_EVERYBODY)) {
		//
		// // create the overlay of enemies
		// MyItemizedOverlay enemisOverlay = new MyItemizedOverlay(this
		// .getResources().getDrawable(
		// android.R.drawable.arrow_up_float), this); // TODO
		// // kép
		//
		// // add the enemies
		// enemisOverlay.addOverlay(new OverlayItem(new GeoPoint(47501000,
		// 19040000), "item1", "ITEM1"));
		//
		// mMapView.getOverlays().add(enemisOverlay);
		// extras.remove(INTENT_KEY_SPELL_SHOW_EVERYBODY);
		//
		// final MyItemizedOverlay itemizedOverlay_ = enemisOverlay;
		//
		// // remove markers after 10 seconds
		// new Timer().schedule(new TimerTask() {
		//
		// @Override
		// public void run() {
		// mMapView.getOverlays().remove(itemizedOverlay_);
		//
		// }
		// }, 10000);
		// }
		if (extras.containsKey(INTENT_KEY_SPELL)) {
			Spell spell = (Spell) extras.getSerializable(INTENT_KEY_SPELL);
			final MyItemizedOverlay friendOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(R.drawable.ally), this);
			final MyItemizedOverlay enemyOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(R.drawable.foe), this);
			final MyItemizedOverlay itemOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(R.drawable.item), this);

			boolean friendsEmpty = true;
			boolean enemiesEmpty = true;
			boolean itemsEmpty = true;

			for (Point p : spell.getParameter()) {
				if ("friend".compareTo(p.getType()) == 0) {
					friendOverlay.addOverlay(new OverlayItem(new GeoPoint(
							(int) (p.getLatitude() * 1e6), (int) (p
									.getLongitude() * 1e6)), p.getName(),
							"barát"));
					friendsEmpty = false;
				} else if ("enemy".compareTo(p.getType()) == 0) {
					enemyOverlay.addOverlay(new OverlayItem(new GeoPoint(
							(int) (p.getLatitude() * 1e6), (int) (p
									.getLongitude() * 1e6)), p.getName(),
							"ellenség"));
					enemiesEmpty = false;
				} else if ("item".compareTo(p.getType()) == 0) {
					itemOverlay.addOverlay(new OverlayItem(new GeoPoint(
							(int) (p.getLatitude() * 1e6), (int) (p
									.getLongitude() * 1e6)), "", "tárgy"));
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

			if (spell.getID() == 2) { // csak ha radar
				// remove markers after n seconds
				new Timer().schedule(new TimerTask() {

					@Override
					public void run() {
						mMapView.getOverlays().remove(friendOverlay);
						mMapView.getOverlays().remove(enemyOverlay);
						mMapView.getOverlays().remove(itemOverlay);

					}
				}, Prefs.spellTimeout * 1000);
			}

		}
		if (extras.containsKey(INTENT_KEY_NEAREST_SERUM)) {
			MyItemizedOverlay serumOverlay = new MyItemizedOverlay(this
					.getResources().getDrawable(R.drawable.serum), this);
			double[] nearestSerum = extras
					.getDoubleArray(INTENT_KEY_NEAREST_SERUM);
			serumOverlay.addOverlay(new OverlayItem(new GeoPoint(
					(int) (nearestSerum[0] * 1e6),
					(int) (nearestSerum[1] * 1e6)), "", "Legközelebbi szérum"));
			mMapView.getOverlays().add(serumOverlay);
		}
		if (extras.containsKey(INTENT_KEY_BORDERS)) {
			borders = (ArrayList<double[]>) extras.get(INTENT_KEY_BORDERS);
		
			mMapView.getOverlays().add(new BorderOverlay());
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

	class BorderOverlay extends Overlay {
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			super.draw(canvas, mapView, shadow);
			if (borders != null) {
				Path path = new Path();

				for (int j = 0; j < borders.size(); j++) {

					GeoPoint gP1 = new GeoPoint(
							(int) (borders.get(j)[0] * 1e6),
							(int) (borders.get(j)[1] * 1e6));
					android.graphics.Point currentScreenPoint = new android.graphics.Point();

					Projection projection = mapView.getProjection();
					projection.toPixels(gP1, currentScreenPoint);

					if (j == 0)
						path.moveTo(currentScreenPoint.x, currentScreenPoint.y);
					else
						path.lineTo(currentScreenPoint.x, currentScreenPoint.y);
				}

				//utolsó pontot elsõvel összekötni
				GeoPoint gP1 = new GeoPoint(
						(int) (borders.get(0)[0] * 1e6),
						(int) (borders.get(0)[1] * 1e6));
				android.graphics.Point currentScreenPoint = new android.graphics.Point();

				Projection projection = mapView.getProjection();
				projection.toPixels(gP1, currentScreenPoint);
				path.lineTo(currentScreenPoint.x, currentScreenPoint.y);
				
				
				Paint paint = new Paint();
				  paint.setDither(true);
				  paint.setColor(Color.RED);
				  paint.setStyle(Paint.Style.STROKE);
				  paint.setStrokeJoin(Paint.Join.ROUND);
				  paint.setStrokeCap(Paint.Cap.ROUND);
				  paint.setStrokeWidth(6);
				  paint.setAntiAlias(true);
				canvas.drawPath(path, paint);
			}

		}

	}
}
