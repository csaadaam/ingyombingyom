package hu.qpa.battleroyale;

import hu.qpa.battleroyale.engine.BRStatus;
import hu.qpa.battleroyale.engine.BRService.ServiceState;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends BRActivity {

	// private TextView tvUserID;
	private TextView tvStatus;
	private TextView tvUsernameTeam;
	private TextView tvScore;
	private TextView tvWarn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// tvUserID = (TextView) findViewById(R.id.tv_user_id);
		tvStatus = (TextView) findViewById(R.id.tv_status);
		tvUsernameTeam = (TextView) findViewById(R.id.tv_user_team);
		tvScore = (TextView) findViewById(R.id.tv_score);
		tvWarn = (TextView) findViewById(R.id.tv_last_warn);

		findViewById(R.id.btn_entry).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						EditText etEntry = (EditText) findViewById(R.id.et_entry);
						String entry = etEntry.getText().toString();
						if (mService != null) {
							mService.codeEntry(entry);
							etEntry.setText("");
						}

					}
				});

		findViewById(R.id.btn_map).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// TODO zombi esetén legközelebbi izé
						Intent i = new Intent(MainActivity.this,
								BRMapActivity.class);
						startActivity(i);

					}
				});

		Intent intent = getIntent();
		if (intent != null) {
			updateUI(intent);
		}

	}

	private void updateUI(Intent intent) {
		Bundle extras = intent.getExtras();
		if (extras == null) {
			return;
		}
		if (extras.containsKey(EXTRA_STATUS)) {
			BRStatus status = (BRStatus) extras.get(EXTRA_STATUS);
			tvScore.setText(status.getScore() + " pont");
			tvUsernameTeam.setText(status.getUsername() + " ("
					+ status.getTeam() + ")");
			Date warnSince = new Date(status.getWarnsince());
			if (warnSince != null) { // TODO mikor kell ez?
				tvWarn.setText("Utolsó warn: "
						+ SimpleDateFormat.getDateTimeInstance().format(
								warnSince));
			}
			updateStatusLabel(status.isAlive());
		}

		// Bundle extras = intent.getExtras();
		// if (extras != null) {
		// String message = extras.getString(EXTRA_MESSAGE);
		// if (message != null) {
		// new AlertDialog.Builder(this)
		// .setCancelable(true)
		// .setPositiveButton("OK",
		// new DialogInterface.OnClickListener() {
		//
		// @Override
		// public void onClick(DialogInterface dialog,
		// int which) {
		// return;
		// }
		// }).setMessage(message).show();
		//
		// }
		// if (extras.containsKey(EXTRA_USER_ID)) {
		// int userID = extras.getInt(EXTRA_USER_ID);
		// // tvUserID.setText(String.valueOf(userID));
		// }
		// if (extras.containsKey(EXTRA_STATUS)) {
		// ServiceState state = (ServiceState) extras.get(EXTRA_STATUS);
		// if (state != null) {
		// updateStatusLabel(state);
		// }
		//
		// }
		//
		// }

	}

	private void updateStatusLabel(boolean alive) {
		String label = "élõ";
		if (alive) {
			label = "élõ";
		} else {
			label = "zombi";
		}
		tvStatus.setText(label);
	}

	@Override
	void handleStateChange(Intent intent) {
		super.handleStateChange(intent);
		Bundle extras = intent.getExtras();
		if (extras != null) {
			ServiceState state = (ServiceState) extras.get(EXTRA_SERVICE_STATE);
			switch (state) {
			case ALIVE:
				break;
			case ZOMBIE:
				break;
			case STARTED:
				finish();
				break;

			default:
				break;
			}
		}
	}
}
