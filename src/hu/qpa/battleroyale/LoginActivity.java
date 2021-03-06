package hu.qpa.battleroyale;

import hu.qpa.battleroyale.engine.BRService.ServiceState;
import hu.qpa.battleroyale.engine.BRStatus;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BRActivity {

	ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.login);

		findViewById(R.id.btn_login).setOnClickListener(
				new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						if (mService != null) {
							String username = ((EditText) findViewById(R.id.et_username))
									.getText().toString();
							String password = ((EditText) findViewById(R.id.et_password))
									.getText().toString();
							if ("".compareTo(username) != 0) {
								pd = ProgressDialog.show(LoginActivity.this,
										"login", "k�rlek v�rj...");
								mService.login(username, password);
							} else {
								Toast.makeText(LoginActivity.this, "Ki vagy?",
										Toast.LENGTH_SHORT).show();
							}

						} else {
							Toast.makeText(LoginActivity.this,
									"Hiba! Nem fut a szolg�ltat�s!",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	@Override
	void handleStateChange(Intent intent) {
		// Toast.makeText(LoginActivity.this,"handle state changed",Toast.LENGTH_SHORT).show();
		super.handleStateChange(intent);
		Bundle extras = intent.getExtras();
		if (extras != null && extras.containsKey(EXTRA_SERVICE_STATE)) {
			if (pd != null) {
				pd.dismiss();
			}
			ServiceState newState = (ServiceState) extras
					.get(EXTRA_SERVICE_STATE);
			Intent i = new Intent(this, MainActivity.class);
			switch (newState) {
			case ALIVE:

			case ZOMBIE:

				i.putExtra(EXTRA_SERVICE_STATE, newState);
				i.putExtra(EXTRA_STATUS, (BRStatus) extras.get(EXTRA_STATUS));
				startActivity(i);
				break;
			default:
				break;
			}
		}
	}
}
