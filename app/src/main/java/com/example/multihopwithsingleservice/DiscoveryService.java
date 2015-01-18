package com.example.multihopwithsingleservice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.WifiP2pManager.ServiceResponseListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.util.Log;
import android.widget.Toast;


public class DiscoveryService implements Runnable{
	

	/* (non-Javadoc)
	 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
	 */
	 WifiP2pManager manager;
	private Channel channel;
	private Context context;

	
	ArrayList<WifiP2pDnsSdServiceInfo> serviceList = new ArrayList<WifiP2pDnsSdServiceInfo> ();
	

	public DiscoveryService(WifiP2pManager manager, Channel channel, Context context) {
		super();
		this.manager = manager;
		this.channel = channel;
		this.context = context;
	}


	@Override
	public void run() {
		// TODO Auto-generated method stub
		while(true){
			/*
			manager.discoverPeers(channel, new ActionListener(){

				@Override
				public void onSuccess() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onFailure(int reason) {
					// TODO Auto-generated method stub
					
				}
				
			});
			*/
            manager.discoverPeers(channel,new ActionListener() {
                @Override
                public void onSuccess() {


                }

                @Override
                public void onFailure(int i) {

                }
            });

			 manager.discoverServices(channel, new ActionListener() {

		            @Override
		            public void onSuccess() {
		            	
		            }

		            @Override
		            public void onFailure(int arg0) {
		            	//Toast.makeText(context, "Discovery Failed : "+arg0,
				          //       Toast.LENGTH_SHORT).show();
		                

		            }
		        });
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}

	}


}
