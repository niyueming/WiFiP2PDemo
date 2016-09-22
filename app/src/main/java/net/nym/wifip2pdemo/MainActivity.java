package net.nym.wifip2pdemo;

import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ListViewCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private final IntentFilter intentFilter = new IntentFilter();
    private WifiP2pManager.Channel mChannel;
    private WifiP2pManager mManager;
    private WiFiP2PBroadcastReceiver receiver;
    private ListViewCompat listView;
    public static ArrayList<WifiP2pDevice> peers = new ArrayList<>();
    private WifiP2pDeviceAdapter adapter;
    private TextView thisDevice;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initFilter();
        mManager = (WifiP2pManager) getSystemService(WIFI_P2P_SERVICE);
        mChannel = mManager.initialize(this, getMainLooper(), new WifiP2pManager.ChannelListener() {
            @Override
            public void onChannelDisconnected() {
                Log.e("initialize","onChannelDisconnected");
                mManager.initialize(MainActivity.this,getMainLooper(),this);
            }
        });


        thisDevice = (TextView) findViewById(R.id.thisD);

        listView = (ListViewCompat) findViewById(R.id.listView);
        adapter = new WifiP2pDeviceAdapter(this,peers);
        listView.setAdapter(adapter);
    }

    private void initFilter() {
        //  Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
    }

    private void discoverPeers(){
        mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("discover","onSuccess");
            }

            @Override
            public void onFailure(int reason) {

            }
        });
    }

    private void stopPeerDiscovery(){

        mManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("stopPeerDiscovery","onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason){
                    case WifiP2pManager.ERROR:
                        Log.e("stopPeerDiscovery","ERROR");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.e("stopPeerDiscovery","P2P_UNSUPPORTED");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.e("stopPeerDiscovery","BUSY");
                        break;
                    case WifiP2pManager.NO_SERVICE_REQUESTS:
                        Log.e("stopPeerDiscovery","NO_SERVICE_REQUESTS");
                        break;
                }
            }
        });
    }

    public void setIsWifiP2pEnabled(boolean enabled){
        Log.e("IsWifiP2pEnabled",String.valueOf(enabled));
    }

    public void notifyDataSetChanged(){
        adapter.notifyDataSetChanged();
        if (peers.size() == 0) {
            Log.d("notifyDataSetChanged", "No devices found");
        }
    }

    public void updateThisDevice(WifiP2pDevice device){
        thisDevice.setText(device.toString());
    }

    public void connect(WifiP2pDevice device) {
        // Picking the first device found on the network.

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = device.deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
                Log.e("connect","onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason){
                    case WifiP2pManager.ERROR:
                        Log.e("connect","ERROR");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.e("connect","P2P_UNSUPPORTED");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.e("connect","BUSY");
                        break;
                    case WifiP2pManager.NO_SERVICE_REQUESTS:
                        Log.e("connect","NO_SERVICE_REQUESTS");
                        break;
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        receiver = new WiFiP2PBroadcastReceiver(mManager,mChannel,this);
        registerReceiver(receiver,intentFilter);
        discoverPeers();
    }

    @Override
    public void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
        stopPeerDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mManager.cancelConnect(mChannel,null);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.add:
                startRegistration();
                break;
            case R.id.discover:
                discoverService();
                break;
        }
    }


    /****************************Wi-Fi P2P for Service Discovery******************************/
    private void startRegistration() {
        //  Create a string map containing information about your service.
        Map<String,String> record = new HashMap<>();
        record.put("listenport", String.valueOf("8080"));
        record.put("buddyname", Build.MODEL);
        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        mManager.addLocalService(mChannel, serviceInfo, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("addLocalService","onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason){
                    case WifiP2pManager.ERROR:
                        Log.e("addLocalService","ERROR");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.e("addLocalService","P2P_UNSUPPORTED");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.e("addLocalService","BUSY");
                        break;
                    case WifiP2pManager.NO_SERVICE_REQUESTS:
                        Log.e("addLocalService","NO_SERVICE_REQUESTS");
                        break;
                }
            }
        });
    }

    final HashMap<String, String> buddies = new HashMap<String, String>();
    private void discoverService(){
        WifiP2pManager.DnsSdTxtRecordListener txtListener = new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
        /* Callback includes:
         * fullDomain: full domain name: e.g "printer._ipp._tcp.local."
         * record: TXT record dta as a map of key/value pairs.
         * device: The device running the advertised service.
         */

            public void onDnsSdTxtRecordAvailable(
                    String fullDomain, Map record, WifiP2pDevice device) {
                Log.e("DnsSdTxtRecordListener", "DnsSdTxtRecord available -" + record.toString());
                buddies.put(device.deviceAddress, record.get("buddyname").toString());
            }
        };

        WifiP2pManager.DnsSdServiceResponseListener servListener = new WifiP2pManager.DnsSdServiceResponseListener() {
            @Override
            public void onDnsSdServiceAvailable(String instanceName, String registrationType,
                                                WifiP2pDevice resourceType) {

                // Update the device name with the human-friendly version from
                // the DnsTxtRecord, assuming one arrived.
                resourceType.deviceName = buddies
                        .containsKey(resourceType.deviceAddress) ? buddies
                        .get(resourceType.deviceAddress) : resourceType.deviceName;

                // Add to the custom adapter defined specifically for showing
//                // wifi devices.
//                WiFiDirectServicesList fragment = (WiFiDirectServicesList) getFragmentManager()
//                        .findFragmentById(R.id.frag_peerlist);
//                WiFiDevicesAdapter adapter = ((WiFiDevicesAdapter) fragment
//                        .getListAdapter());
//
//                adapter.add(resourceType);
//                adapter.notifyDataSetChanged();
                Log.e("DnsSdService", "onBonjourServiceAvailable " + instanceName);
            }
        };

        mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        mManager.addServiceRequest(mChannel,
                serviceRequest,
                new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Log.e("addServiceRequest","onSuccess");
                    }

                    @Override
                    public void onFailure(int reason) {
                        switch (reason){
                            case WifiP2pManager.ERROR:
                                Log.e("addServiceRequest","ERROR");
                                break;
                            case WifiP2pManager.P2P_UNSUPPORTED:
                                Log.e("addServiceRequest","P2P_UNSUPPORTED");
                                break;
                            case WifiP2pManager.BUSY:
                                Log.e("addServiceRequest","BUSY");
                                break;
                            case WifiP2pManager.NO_SERVICE_REQUESTS:
                                Log.e("addServiceRequest","NO_SERVICE_REQUESTS");
                                break;
                        }
                    }
                });

        mManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Log.e("discoverServices", "onSuccess");
            }

            @Override
            public void onFailure(int reason) {
                switch (reason) {
                    case WifiP2pManager.ERROR:
                        Log.e("discoverServices", "ERROR");
                        break;
                    case WifiP2pManager.P2P_UNSUPPORTED:
                        Log.e("discoverServices", "P2P_UNSUPPORTED");
                        break;
                    case WifiP2pManager.BUSY:
                        Log.e("discoverServices", "BUSY");
                        break;
                    case WifiP2pManager.NO_SERVICE_REQUESTS:
                        Log.e("discoverServices", "NO_SERVICE_REQUESTS");
                        break;
                }
            }
        });
    }
}
