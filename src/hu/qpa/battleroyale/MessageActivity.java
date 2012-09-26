package hu.qpa.battleroyale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Simple activity for showing messages
 * @author Csaadaam
 *
 */
public class MessageActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String message = extras.getString(BRActivity.EXTRA_MESSAGE);
			if (message != null) {
				new AlertDialog.Builder(this)
						.setCancelable(false)
						.setPositiveButton("OK",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										finish();
									}
								}).setMessage(message).show();
			} else {
				finish();
			}
		} else {
			finish();
		}
	}
}
