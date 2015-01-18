package com.example.multihopwithsingleservice;

import java.util.ArrayList;
import java.util.HashMap;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;


public class MainActivity extends ActionBarActivity {
	
	/* (non-Javadoc)
	 * @see android.support.v4.app.FragmentActivity#onDestroy()
	 */
	

	SimpleAdapter outboxAdap;
	ArrayList<HashMap<String, String>> outboxArrList = new ArrayList<HashMap<String, String>>();
	ArrayList<String> outboxList ;
	ListView outboxListView;
	
	SimpleAdapter inboxAdap;
	ArrayList<HashMap<String, String>> inboxArrList = new ArrayList<HashMap<String, String>>();
	ArrayList<String> inboxList ;
	ListView inboxListView;
	IntentFilter intentFilter;
	Channel channel;
	WifiP2pManager wifiP2pManager;
	String username;

    Button sendBtn;
	public MessageManager messageManager;
	 EditText message,receiver;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intentFilter = new IntentFilter();
    	intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
    	intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
    	intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
    	intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    	
    	Bundle extras = getIntent().getExtras();
        if (extras != null) {
            username = extras.getString("username");

        }


    	wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
    	channel = wifiP2pManager.initialize(this, getMainLooper(), null);

        Receiver wifiBroadcastReceiver = new Receiver(wifiP2pManager, channel, this);
        registerReceiver(wifiBroadcastReceiver, intentFilter);

    	messageManager= new MessageManager(this,wifiP2pManager,channel,getApplicationContext(),username);


    	 outboxListView = (ListView) findViewById(R.id.outboxList);
         outboxList = new ArrayList<String>();
         outboxAdap = new SimpleAdapter(MainActivity.this, outboxArrList, R.layout.listview_column,
                 new String[] {"Receiver","Sender", "ID", "Message", "PN"}, new int[] {R.id.colReceiver, R.id.colSender,R.id.colId, R.id.colMessage, R.id.colPN});      
         outboxListView.setAdapter(outboxAdap);
         
         inboxListView = (ListView) findViewById(R.id.inboxList);
         inboxList = new ArrayList<String>();
         inboxAdap = new SimpleAdapter(MainActivity.this, inboxArrList, R.layout.listview_column,
                 new String[] {"Receiver","Sender", "ID", "Message", "PN"}, new int[] {R.id.colReceiver, R.id.colSender,R.id.colId, R.id.colMessage, R.id.colPN});      
         inboxListView.setAdapter(inboxAdap);
         
         HashMap<String, String> map;
 			map = new HashMap<String, String>();
 			map.put("Receiver", "Receiver");
 			map.put("Message", "Message");
 			map.put("Sender", "Sender");
 			map.put("ID", "ID");
 			map.put("PN", "PN");
 			outboxArrList.add(map);
 			inboxArrList.add(map);
 		inboxAdap.notifyDataSetChanged();
 		outboxAdap.notifyDataSetChanged();
 			
			
         
        

         sendBtn = (Button) findViewById(R.id.sendButton);
        message = (EditText) findViewById(R.id.messageField);
        receiver = (EditText) findViewById(R.id.receiverField);
        Toast.makeText(getApplicationContext(), "username:" +username,
                Toast.LENGTH_SHORT).show();
        
        sendBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
				messageManager.addMessage(username,
						message.getText().toString(), 
						receiver.getText().toString());
				/*messageManager.addMessageLong("1234567890123456789012345678901234567890123456789012345678901234567890" +
						"1234567890123456789012345678901234567890123456789012345678901234567890" +
						"1234567890123456789012345678901234567890123456789012345678901234567890", "123456789012345678901234567890" +
						"1234567890123456789012345678901234567890123456789012345678901234567890" +
						"1234567890123456789012345678901234567890123456789012345678901234567890" +
						"123456789012345678901234567890123456789012345678901234567890",
						"1234567890123456789012345678901234567890123456789012345678901234567890" +
						"1234567890123456789012345678901234567890123456789012345678901234567890" +
						"1234567890123456789012345678901234567890123456789012345678901234567890");*/
				
				 
			}
		});
        
        
        
        
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		wifiP2pManager.clearLocalServices(channel, new ActionListener(){

			@Override
			public void onSuccess() {
				Toast.makeText(getApplicationContext(), "Clear service.",  
						Toast.LENGTH_SHORT).show();					
			}

			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
			
		});
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
