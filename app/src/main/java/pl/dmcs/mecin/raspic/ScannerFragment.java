package pl.dmcs.mecin.raspic;



import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.text.format.Formatter;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.w3c.dom.Text;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;




public class ScannerFragment extends ListFragment {

    OnListItemClicked mCallback;
    protected ProgressDialog dialogScanning;
    protected int REACH_TIME = 100;
    protected ScannerArrayAdapter adapter;
    public ArrayList<DeviceNetworkDetails> arrayListDeviceNetworkDetails = null;
    WifiManager wifiMan;
    WifiScanReceiver wifiReciever;
    private boolean ipScanner = true;

    public ScannerFragment() {
        // Required empty public constructor
    }

    // Interface for parent to comunicate
    public interface OnListItemClicked {
        public void onItemClicked(String ip);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnListItemClicked) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnArticleSelectedListener");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        // To avoid null pointer exception when fragment is shadowed
        if(adapter != null && arrayListDeviceNetworkDetails != null) {
            //String[] storedAdapter = new String[adapter.getCount()];
            //for (int i = 0; i < adapter.getCount(); i++) {
            //    storedAdapter[i] = adapter.getItem(i);
            //}

            savedState.putParcelableArrayList("storedAdapter", arrayListDeviceNetworkDetails);
        }

        if(dialogScanning != null) {
            if (dialogScanning.isShowing()) {
                dialogScanning.dismiss();
            }
        }
        super.onSaveInstanceState(savedState);
        Log.d("onSaveInstanceState", "savedState put data");
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new ScannerArrayAdapter(getActivity(), R.layout.scanner_row, arrayListDeviceNetworkDetails);
        setListAdapter(adapter);
        final ListView scannerListView = getListView();



        scannerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                if(ipScanner) {

                    Log.d("onItemListClick", "ip mode");

                    DeviceNetworkDetails selectedDeviceNetworkDetails = (DeviceNetworkDetails) getListAdapter().getItem(position);

                    //Toast.makeText(getActivity().getApplicationContext(), item, Toast.LENGTH_SHORT).show();

                    //mCallback.onItemClicked(item);

                    Log.d("selected", "IP: " + selectedDeviceNetworkDetails.getIP() + " SSID: " + selectedDeviceNetworkDetails.getSSID());

                    Bundle args = new Bundle();
                    args.putString("IP", selectedDeviceNetworkDetails.getIP());

                    OnClickDialog onClickDialog = new OnClickDialog();
                    onClickDialog.setArguments(args);

                    onClickDialog.show(getFragmentManager(), "OnClickDialog");

                } else {

                    Log.d("onItemListClick", "network mode");

                    DeviceNetworkDetails selectedDeviceNetworkDetails = (DeviceNetworkDetails) getListAdapter().getItem(position);

                    // Configuration of new wi-fi connection
                    WifiConfiguration wifiConfiguration = new WifiConfiguration();

                    // Set SSID for new connection
                    wifiConfiguration.SSID = "\"" + selectedDeviceNetworkDetails.getSSID() + "\"";

                    // For open network only
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);

                    // Add prepared network
                    wifiMan.addNetwork(wifiConfiguration);

                    // Get all configured wi-fi connections
                    List<WifiConfiguration> list = wifiMan.getConfiguredNetworks();

                    Log.d("WIFI", "Before connecting.");
                    // Search for added configuration and connect if found
                    for( WifiConfiguration i : list ) {
                        if(i.SSID != null && i.SSID.equals("\"" + selectedDeviceNetworkDetails.getSSID() + "\"")) {
                            wifiMan.disconnect();
                            wifiMan.enableNetwork(i.networkId, true);
                            wifiMan.reconnect();

                            Toast.makeText(getActivity().getApplicationContext(), selectedDeviceNetworkDetails.getSSID() + " connected", Toast.LENGTH_SHORT).show();

                            break;
                        }
                    }
                    Log.d("WIFI", "After connecting.");
                }
            }
        });

        Log.d("onActivityCreated", "enter onActivityCreated");

        if (savedInstanceState != null) {
            Log.d("onActivityCreated", "savedInstanceState != null");
            ArrayList<DeviceNetworkDetails> values = savedInstanceState.getParcelableArrayList("storedAdapter");
            if (values != null) {
                Log.d("onActivityCreated", "values != null");
                adapter.addAll(values);
                adapter.notifyDataSetChanged();
                //setListAdapter(adapter);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        //super.onCreateView(savedInstanceState);
        //setRetainInstance(true);
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_scanner, container, false);

        arrayListDeviceNetworkDetails = new ArrayList<DeviceNetworkDetails>();


        // Reach time field
        final EditText reachTextEditText = (EditText) view.findViewById(R.id.reachTimeEditText);
        reachTextEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                boolean handled = false;
                if(actionId == EditorInfo.IME_ACTION_DONE) {
                    // To hide virtual keyboard
                    InputMethodManager inputManager = (InputMethodManager)
                            getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                    if(reachTextEditText.getText().toString() != null && !reachTextEditText.getText().toString().equals("")) {
                        REACH_TIME = Integer.valueOf(reachTextEditText.getText().toString());

                        Toast.makeText(getActivity().getApplicationContext(), "Reach time: " + REACH_TIME, Toast.LENGTH_SHORT).show();
                    }
                    handled = true;
                }
                return handled;
            }
        });

        // Wi-fi manager
        wifiMan = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);

        // Wi-fi reciever
        wifiReciever = new WifiScanReceiver();

        // DHCP information
        DhcpInfo dhcpInfo = wifiMan.getDhcpInfo();

        // List of scanned ip
        ArrayList<String> ipAddresses = new ArrayList<String>();

        // Convert DHCP integer ip address to human readable format
        final InetAddress ipAddr = intToInetAddress(dhcpInfo.ipAddress);

        // Set your ip text
        TextView yourIpText = (TextView) view.findViewById(R.id.your_ip_addr);
        if(isWifiConnected()) {
            yourIpText.append(" " + ipAddr.toString().replace("/", ""));
        } else {
            yourIpText.append(" Not connected!");
        }

        // Toggle IP/Wi-Fi scan button
        ToggleButton ipWifiToggleButton = (ToggleButton) view.findViewById(R.id.toggle_ip_wifi);
        ipWifiToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    Toast.makeText(getActivity().getApplicationContext(), "Network scanner", Toast.LENGTH_SHORT).show();
                    // Set network scanner mode
                    ipScanner = false;
                } else {
                    // The toggle is disabled
                    Toast.makeText(getActivity().getApplicationContext(), "IP scanner", Toast.LENGTH_SHORT).show();
                    // Set ip scanner mode
                    ipScanner = true;
                }
            }
        });

        // Scann button
        Button scanButton = (Button) view.findViewById(R.id.start_scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ipScanner) {
                    Toast.makeText(getActivity().getApplicationContext(), "IP scanning!", Toast.LENGTH_SHORT).show();
                    dialogScanning = ProgressDialog.show(getActivity(), "Scanning", "Please wait...", true, true);
                    new Scanner().execute(ipAddr.toString());
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Network scanning!", Toast.LENGTH_SHORT).show();
                    // TODO
                    wifiMan.setWifiEnabled(true);

                    // Wi-fi scanner brodcast receiver
                    getActivity().getApplicationContext().registerReceiver(wifiReciever, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

                    if(wifiMan.startScan()) {
                        Log.d("SR", "before for each");

                    }
                }
            }
        });

        Button refreshButton = (Button) view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "Refresh", Toast.LENGTH_SHORT).show();

                // Pop current fragment
                getFragmentManager().popBackStack();

                // And create new instance
                Fragment newFragInstance = new ScannerFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                transaction.replace(R.id.activity_main, newFragInstance);

                // To store prevous fragment keep null
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }
        });



        return view;
    }

    public void playNotificationSound() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            Ringtone ring = RingtoneManager.getRingtone(getActivity().getApplicationContext(), notification);
            ring.play();
        } catch(Exception e) {
            Log.d("RING", "playNotificationSonud : " + e.getMessage() );
        }
    }

    public boolean isWifiConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }

    }

    public static InetAddress intToInetAddress(int hostAddress) {
        byte[] addressBytes = { (byte)(0xff & hostAddress),
                (byte)(0xff & (hostAddress >> 8)),
                (byte)(0xff & (hostAddress >> 16)),
                (byte)(0xff & (hostAddress >> 24)) };

        try {
            return InetAddress.getByAddress(addressBytes);
        } catch (UnknownHostException e) {
            throw new AssertionError();
        }
    }


    private class Scanner extends AsyncTask<String, Void, String> {

        public Scanner() {

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            Log.d("ASYNC", "ENTER");

            // Clear list
            arrayListDeviceNetworkDetails.clear();

            // InetAddress
            InetAddress ipAddress = null;

            // Array of splitted ip address strings
            String[] ipAddrArray = params[0].replace("/", "").split("\\.");

            String currentPingedIp = "";
            String tmpDataString;
            Runtime runtime = Runtime.getRuntime();
            for(int i = 1; i < 255; i++) {
                tmpDataString = "";
                currentPingedIp = ipAddrArray[0] + "." + ipAddrArray[1] + "." + ipAddrArray[2] + "." + i;
                try {
                    ipAddress = InetAddress.getByName(currentPingedIp);
                } catch (UnknownHostException e) {
                    Log.e("IP", "Unknown Host Exception " + e.getMessage());
                }

                try {
                    if(ipAddress.isReachable(REACH_TIME)) {
                        //textField.append("\n" + currentPingedIp + " - Respond OK");
                        Log.d("IP", "\n" + currentPingedIp + " - Respond OK");
                        Log.d("IP", "\n" + ipAddress.getCanonicalHostName());
                        // Detailed scanner result
                        //tmpDataString = "Hostname: " + ipAddress.getCanonicalHostName() + "\nIP: " + currentPingedIp;

                        // IP only result
                        tmpDataString = currentPingedIp;
                        arrayListDeviceNetworkDetails.add(new DeviceNetworkDetails(ipAddress.getCanonicalHostName(), currentPingedIp));
                    } else {
                        Log.d("IP", "\n" + currentPingedIp + " - Not responding");
                        //textField.append("\n" + currentPingedIp + " - Not responding");
                    }
                } catch (IOException e) {

                }

            }
            return "DONE";
        }

        @Override
        protected void onPostExecute(String a) {
            //Toast.makeText(getActivity().getApplicationContext(), "Scanning completed! a: " + a, Toast.LENGTH_SHORT).show();
            if(dialogScanning != null) {
                if (dialogScanning.isShowing()) {
                    dialogScanning.dismiss();
                }
            }
            adapter.notifyDataSetChanged();
            playNotificationSound();
        }


    }


    private class ScannerArrayAdapter extends ArrayAdapter<DeviceNetworkDetails> {

        private ArrayList<DeviceNetworkDetails> items;

        public ScannerArrayAdapter(Context context, int textViewResourceId,
                         ArrayList<DeviceNetworkDetails> items) {
            super(context, textViewResourceId, items);
            this.items = items;

        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                LayoutInflater li = LayoutInflater.from(getActivity().getApplicationContext());
                v = li.inflate(R.layout.scanner_row, null);
            }

            DeviceNetworkDetails o = items.get(position);
            if (o != null) {

                TextView adapterSsidTextView = (TextView) v.findViewById(R.id.adapter_ssid);
                TextView adapterIpTextView = (TextView) v.findViewById(R.id.adapter_ip);
                //TextView kursSr = (TextView) v.findViewById(R.id.kursSr);

                if (adapterSsidTextView != null) {
                    adapterSsidTextView.setText("SSID: " + o.getSSID());
                }
                if (adapterIpTextView != null) {
                    adapterIpTextView.setText("IP: " + o.getIP());
                }

            }
            return v;
        }
    }


    private class WifiScanReceiver extends BroadcastReceiver {

        public void onReceive(Context c, Intent intent) {

            Log.d("BR", "enter onReceive");

            List<ScanResult> wifiScanList = wifiMan.getScanResults();

            arrayListDeviceNetworkDetails.clear();

            for(ScanResult sr : wifiScanList) {
                Log.d("FE SR", "" + sr.SSID + " " + sr.BSSID);
                arrayListDeviceNetworkDetails.add(new DeviceNetworkDetails(sr.SSID, sr.BSSID));
            }

            // Unregister brodcast receiver
            getActivity().getApplicationContext().unregisterReceiver(wifiReciever);

            adapter.notifyDataSetChanged();

        }
    }

}
