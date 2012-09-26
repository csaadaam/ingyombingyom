package hu.qpa.battleroyale;

import hu.qpa.battleroyale.engine.BRService.ServiceState;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends BRActivity {

//	private TextView tvUserID;
	private TextView tvStatus;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

//		tvUserID = (TextView) findViewById(R.id.tv_user_id);
		tvStatus = (TextView) findViewById(R.id.tv_status);

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
		if (extras != null) {
			String message = extras.getString(EXTRA_MESSAGE);
			if (message != null) {
				new AlertDialog.Builder(this)
						.setCancelable(true)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										return;
									}
								}).setMessage(message).show();
				if (mService != null) {
//					tvUserID.setText(mService.getUserID());
					updateStatusLabel((ServiceState) mService
							.getStateChangeIntent().getExtras()
							.get(EXTRA_STATUS));
				}
			}
			if (extras.containsKey(EXTRA_USER_ID)) {
				int userID = extras.getInt(EXTRA_USER_ID);
//				tvUserID.setText(String.valueOf(userID));
			}
			if (extras.containsKey(EXTRA_STATUS)) {
				ServiceState state = (ServiceState) extras.get(EXTRA_STATUS);
				if (state != null) {
					updateStatusLabel(state);
				}

			}

		}

	}

	private void updateStatusLabel(ServiceState state) {
		String label = "élõ";
		switch (state) {
		case ALIVE:
			label = "élõ";
			break;
		case ZOMBIE:
			label = "zombi";
			break;
		}
		tvStatus.setText(label);
	}

	@Override
	void handleStateChange(Intent intent) {
		super.handleStateChange(intent);
		Bundle extras = intent.getExtras();
		if (extras != null) {
			ServiceState state = (ServiceState) extras.get(EXTRA_STATUS);
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
