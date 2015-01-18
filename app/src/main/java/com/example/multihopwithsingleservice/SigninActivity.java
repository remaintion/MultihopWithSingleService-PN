package com.example.multihopwithsingleservice;

import java.util.ArrayList;
import java.util.HashMap;

import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

public class SigninActivity extends ActionBarActivity {
	private EditText usernameField;
	private Button submitButton;
	WifiManager wifiManager;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_signin);
		
		//Toast.makeText(getApplicationContext(), ""+Integer.MAX_VALUE,
            //    Toast.LENGTH_SHORT).show();
		

		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		
		wifiManager.setWifiEnabled(true);
		usernameField = (EditText) findViewById(R.id.usernameField);
		submitButton = (Button) findViewById(R.id.submitButton);
		
		submitButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				String username = usernameField.getText().toString();
				Intent intent = new Intent(getBaseContext(), MainActivity.class);
				intent.putExtra("username", username);
				startActivity(intent);
			}
			
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.signin, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
