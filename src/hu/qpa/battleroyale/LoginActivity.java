package hu.qpa.battleroyale;

import hu.qpa.battleroyale.engine.BRService.ServiceState;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends BRActivity {

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
							mService.login(username, password);
						} else {
							Toast.makeText(LoginActivity.this,
									"Hiba! Nem fut a szolgáltatás!",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
	}

	@Override
	void handleStateChange(Intent intent) {
		super.handleStateChange(intent);
		Bundle extras = intent.getExtras();
		if (extras != null && extras.containsKey(EXTRA_STATUS)) {
			ServiceState newState = (ServiceState) extras.get(EXTRA_STATUS);
			switch (newState) {
			case ALIVE:
			case ZOMBIE:
				Intent i = new Intent(this, MainActivity.class);
				i.putExtra(EXTRA_USER_ID, mService.getUserID());
				i.putExtra(EXTRA_STATUS, newState);
				startActivity(i);
				break;
			default:
				break;
			}
		}
	}
}
