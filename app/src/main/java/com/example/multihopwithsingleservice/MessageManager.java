package com.example.multihopwithsingleservice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.ServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.UpnpServiceResponseListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pServiceRequest;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MessageManager {
	private MainActivity activity;
	private ArrayList<ArrayList<String>> receivedMessage = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> sendedMessage = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> allMessage = new ArrayList<ArrayList<String>>();
	private ArrayList<ArrayList<String>> messageInService = new ArrayList<ArrayList<String>>();
    private ArrayList<WifiP2pDnsSdServiceInfo> serviceList = new ArrayList<WifiP2pDnsSdServiceInfo>();
	DiscoveryService discoveryService;
	private String SERVICE_INSTANCE = "MultiHopWithService";
	private String SERVICE_REG_TYPE = "MultiHopWithService";
	WifiP2pDnsSdServiceInfo currentService;
	String username;
	int messageCount =1 ;
		
	//status :: 0== Sending, 1 = on relay node, 2= arrived
	ArrayList<String> tempMessage;
	WifiP2pManager wifiP2pManager;
	Channel channel;
	IntentFilter intentFilter;
	Thread discoveryServiceThread;
	WifiManager wifiManager;
	String macAddress;
	MessageManager messageManager;
	Context context;
    int n = 2; //n-Epidermic
	
	public MessageManager(MainActivity mainActivity, WifiP2pManager wifiP2pManager,Channel channel, Context context,String username) {
		activity = mainActivity;
		this.wifiP2pManager = wifiP2pManager;
		this.channel= channel;
		this.context = context;
		this.username=username;
		discoveryService = new DiscoveryService(wifiP2pManager, channel,context);
		discoveryServiceThread = new Thread(discoveryService);
		discoveryServiceThread.start();
		
		initial();
		TextView user;
        user = (TextView) mainActivity.findViewById(R.id.userName);
        user.setText("My Name Is : "+username);
		
	}
	
	public void addMessage(final String src, final String Message, final String des){ //Call by activity
		ArrayList<String> tempMessage = new ArrayList<String>();
		tempMessage.add(src);
		tempMessage.add(Message);
		tempMessage.add(des);
		tempMessage.add(messageCount+"");
		tempMessage.add(username);
		allMessage.add(tempMessage);
		
		sendedMessage.add(tempMessage);
		clearMessageAdvertisement();
		
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();

		for(int i=0 ; i<allMessage.size(); i++){
			if(i==4 || i==9){
				temp.add(allMessage.get(allMessage.size()-i-1));
				ArrayList<String> zipped = zipMessage(temp);
				WifiP2pDnsSdServiceInfo service = createMessageAdvertisement(zipped);
				//advertisement(service);
                serviceList.add(service);
				temp.clear();
				zipped.clear();
				if(i==9){
					break;
				}
			}else{
				temp.add(allMessage.get(allMessage.size()-i-1));
			}
		}
		
		showOnUiOutboxList(tempMessage);
		ArrayList<String> zipped = zipMessage(temp);
		WifiP2pDnsSdServiceInfo service = createMessageAdvertisement(zipped);
		//advertisement(service);
        serviceList.add(service);
		temp.clear();
		
		
		messageCount++;
		
	}
	
	
	private void initial() {
		wifiP2pManager.setServiceResponseListener(channel, new ServiceResponseListener(){

			@Override
			public void onServiceAvailable(int protocolType,
					byte[] responseData, WifiP2pDevice srcDevice) {
				//Toast.makeText(context, "Hey onServiceAvailable",
		          //       Toast.LENGTH_SHORT).show();
				
			}
			
		});
		 wifiP2pManager.setDnsSdResponseListeners(channel, new DnsSdServiceResponseListener(){

				@Override
				public void onDnsSdServiceAvailable(String instanceName,
						String registrationType, WifiP2pDevice srcDevice) {
					// TODO Auto-generated method stub
					
				}
	        	
	        }, new DnsSdTxtRecordListener(){

				@Override
				public void onDnsSdTxtRecordAvailable(String fullDomainName,
						Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
					//Log.d("Bug", "Get Message!!!!!!!");
					//Toast.makeText(context, txtRecordMap.get("Sender")+"|"+ txtRecordMap.get("Message")+"|"+
						//	txtRecordMap.get("Receiver"),
			                 //Toast.LENGTH_SHORT).show();		
					ArrayList<ArrayList<String>> newMessage = unzipMessage(txtRecordMap.get("Sender"), txtRecordMap.get("Message"), 
							txtRecordMap.get("Receiver"), txtRecordMap.get("ID"), txtRecordMap.get("PN"));
					newMessageAvailable(newMessage,srcDevice);
				}
	        	
	        });
		 
		 
		 
		 discoverService();
	}
	

    private void discoverService() {
    	wifiP2pManager.clearServiceRequests(channel, new ActionListener(){

    		
			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onFailure(int reason) {
				// TODO Auto-generated method stub
				
			}
    		
    	});
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_INSTANCE, SERVICE_REG_TYPE);
        //WifiP2pServiceRequest serviceRequest =  WifiP2pServiceRequest .newInstance(0);
        wifiP2pManager.addServiceRequest(channel, serviceRequest,
                new ActionListener() {

                    @Override
                    public void onSuccess() {
                    	//Log.d("BUG", "Add Request Service");
                    }

                    @Override
                    public void onFailure(int arg0) {
                    	//Log.d("BUG", "Add Request Service fail :" +arg0);
                    	//Toast.makeText(context, "Add Request Service fail :" +arg0,
				                 //Toast.LENGTH_SHORT).show();
                       
                    }
                });
        

    }
    
    private void advertisement(WifiP2pDnsSdServiceInfo service){
		wifiP2pManager.addLocalService(channel, service, new ActionListener() {

            @Override
            public void onSuccess() {
            	//Log.d("BUG", "Add Service");
            }

            @Override
            public void onFailure(int error) {
            	//Log.d("BUG", "Fail to addService : "+error+"");
            	//Toast.makeText(context, "FAIL!!!!!!",
		                 //Toast.LENGTH_SHORT).show();
            }
        });
	}
	
    private WifiP2pDnsSdServiceInfo createMessageAdvertisement(ArrayList<String> temp){
		
		Map<String, String> record = new HashMap<String, String>();
        record.put("Sender", temp.get(0));
        record.put("Message",  temp.get(1));
        record.put("Receiver",  temp.get(2));
        record.put("ID", temp.get(3));
        record.put("PN", temp.get(4));
        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        Log.d("BUG", temp.get(1).length()+"");

        return service;
       
	}
	
	private void newMessageAvailable(ArrayList<ArrayList<String>> message,WifiP2pDevice srcDevice){ //Call by activity
		if(messageInService.containsAll(message)){
			//Log.d("Bug", "ContainAllllllllllll");
		}else{
		allMessage = checkDuplicate(allMessage , message);
		clearMessageAdvertisement();
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		

		/*
		if(allMessage.size()>=10){
		for(int i=allMessage.size()-1;i>allMessage.size()-10;i--){
			if(i%4==0 && i!=allMessage.size()){
				//Log.d("BUG", i+"");
				temp.add(allMessage.get(i));
			}else{
				temp.add(allMessage.get(i));
			}
		}
		}else{
			for(int i=0;i<allMessage.size();i++){
				if(i%4==0 && i!=0){
					//Log.d("BUG", i+"");
					temp.add(allMessage.get(i));
				}else{
					temp.add(allMessage.get(i));
				}
			}
		}*/
		
		for(int i=0 ; i<allMessage.size(); i++){
			if(i==4 || i==9){
				temp.add(allMessage.get(allMessage.size()-i-1));
				ArrayList<String> zipped = zipMessage(temp);
				WifiP2pDnsSdServiceInfo service = createMessageAdvertisement(zipped);
				//advertisement(service);
                serviceList.add(service);
				temp.clear();
				zipped.clear();
				if(i==9){
					break;
				}
			}else{
				temp.add(allMessage.get(allMessage.size()-i-1));
			}
		}
		
		
		ArrayList<String> zipped = zipMessage(temp);
		WifiP2pDnsSdServiceInfo service = createMessageAdvertisement(zipped);
		//advertisement(service);
            serviceList.add(service);
		temp.clear();
		}
		
	}
	
	private ArrayList<String> zipMessage(ArrayList<ArrayList<String>> messageList){
		ArrayList<String> temp = new ArrayList<String>();
		String Sender ="";
		String message ="";
		String Receiver ="";
		String ID ="";
		String PN="";
		for(int i=0;i<messageList.size();i++){
			
			messageInService.add(messageList.get(i));
			Sender+=messageList.get(i).get(0);
			message+=messageList.get(i).get(1);
			Receiver+=messageList.get(i).get(2);
			ID+=messageList.get(i).get(3);
			PN+=username;
			if(i != messageList.size()-1){
				Sender+=":";
				message+=":";
				Receiver+=":";
				ID+=":";
				PN+=":";
			}
		}
		temp.add(Sender);
		temp.add(message);
		temp.add(Receiver);
		temp.add(ID);
		temp.add(PN);
		//Log.d("BUG", temp.get(0)+"|"+temp.get(1)+"|"+temp.get(2)+"|"+temp.get(3));
    	
		return temp;
	}
	
	private ArrayList<ArrayList<String>> unzipMessage(String Sender, String message,  String Receiver, String ID, String PN){
		ArrayList<ArrayList<String>> temp = new ArrayList<ArrayList<String>>();
		List<String> SenderList = new ArrayList<String>(Arrays.asList(Sender.split(":")));
		List<String> messageList = new ArrayList<String>(Arrays.asList(message.split(":")));
		List<String> ReceiverList = new ArrayList<String>(Arrays.asList(Receiver.split(":")));
		List<String> IDList = new ArrayList<String>(Arrays.asList(ID.split(":")));
		List<String> PNList = new ArrayList<String>(Arrays.asList(PN.split(":")));

		for(int i=0;i<SenderList.size();i++){
			ArrayList<String> t = new ArrayList<String>();
			t.add(SenderList.get(i));
			t.add(messageList.get(i));
			t.add(ReceiverList.get(i));
			t.add(IDList.get(i));
			t.add(PNList.get(i));
			temp.add(t);
		}
		return temp;
	}
	
	private ArrayList<ArrayList<String>> checkDuplicate(ArrayList<ArrayList<String>> mainList, ArrayList<ArrayList<String>> addedList){
		for(int i=0;i<addedList.size();i++){
			if(! mainList.contains(addedList.get(i))){
				if( ! receivedMessage.contains(addedList.get(i))){
					if(addedList.get(i).get(2).equalsIgnoreCase(username)){
						receivedMessage.add(addedList.get(i));
						showOnUiInboxList(addedList.get(i));
					}else if(addedList.get(i).get(0).equalsIgnoreCase(username)) {
						
					}else{
						mainList.add(addedList.get(i));
						showOnUiInboxList(addedList.get(i));
					}
					
				}
				
				
			}
		}
		return mainList;
	}
	

	private void showOnUiInboxList(final ArrayList<String> message){
		activity.runOnUiThread(new Runnable() {
		    public void run() {
		if(message.get(0) != username){
		    		HashMap<String, String> map;
		    		map = new HashMap<String, String>();
		    		map.put("Receiver", message.get(2));
					
					map.put("Sender", message.get(0));
					map.put("ID", message.get(3));
					map.put("PN", message.get(4));
					
					if(message.get(2).toString().equalsIgnoreCase(username)){
						map.put("Message", message.get(1));

					}else{
						map.put("Message", "Not for you");

					}
					activity.inboxArrList.add(map);
					activity.inboxAdap.notifyDataSetChanged();
		    }
		    }
		});
		
	}
	
	private void showOnUiOutboxList(final ArrayList<String> message){
		activity.runOnUiThread(new Runnable() {
		    public void run() {
		    	HashMap<String, String> map;
		    	map = new HashMap<String, String>();
				map.put("Receiver", message.get(2));
				map.put("Message", message.get(1));
				map.put("Sender", message.get(0));
				map.put("ID", message.get(3));
				map.put("PN", message.get(4));
				activity.outboxArrList.add(map);
				activity.outboxAdap.notifyDataSetChanged();
		    }
		});
		
	}
	
	private void clearMessageAdvertisement(){
		wifiP2pManager.clearLocalServices(channel, new ActionListener(){

			@Override
			public void onSuccess() {
				// TODO Auto-generated method stub
				//Log.d("BUG", "Clear service");
			}

			@Override
			public void onFailure(int reason) {
				//Toast.makeText(context, "Clear service fail :" +reason,
		                 //Toast.LENGTH_SHORT).show();			hg
			}
			
		});
	}
    private List peers = new ArrayList();

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener(){


        @Override
        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            peers.clear();
            peers.addAll(wifiP2pDeviceList.getDeviceList());

            Toast.makeText(context, "Peers count : "+peers.size(),
            Toast.LENGTH_SHORT).show();
            if(peers.size()>=n){
                //clearMessageAdvertisement();
                for (int i=0; i<serviceList.size();i++){
                    advertisement(serviceList.get(i));
                }

                serviceList.clear();
            }
        }
    };
}



