package pl.dmcs.mecin.raspic;



import android.app.Activity;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.app.ListFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
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
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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


/**
 * A simple {@link Fragment} subclass.
 *
 */



public class ScannerFragment extends ListFragment {

    OnListItemClicked mCallback;
    protected ProgressDialog dialogScanning;
    protected int REACH_TIME = 100;
    protected ArrayAdapter<String> adapter;
    public ArrayList<String> arrayListDeviceNetworkDetails = null;


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
        if(adapter != null) {
            String[] storedAdapter = new String[adapter.getCount()];
            for (int i = 0; i < adapter.getCount(); i++) {
                storedAdapter[i] = adapter.getItem(i);
            }

            savedState.putStringArray("storedAdapter", storedAdapter);
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

        adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, arrayListDeviceNetworkDetails);
        setListAdapter(adapter);
        final ListView scannerListView = getListView();


        scannerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {

                String item = ((TextView)view).getText().toString();

                //Toast.makeText(getActivity().getApplicationContext(), item, Toast.LENGTH_SHORT).show();

                //mCallback.onItemClicked(item);

                Bundle args = new Bundle();
                args.putString("IP", item);

                OnClickDialog onClickDialog = new OnClickDialog();
                onClickDialog.setArguments(args);

                onClickDialog.show(getFragmentManager(), "OnClickDialog");
            }
        });

        Log.d("onActivityCreated", "enter onActivityCreated");

        if (savedInstanceState != null) {
            Log.d("onActivityCreated", "savedInstanceState != null");
            String[] values = savedInstanceState.getStringArray("storedAdapter");
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

        arrayListDeviceNetworkDetails = new ArrayList<String>();



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
        WifiManager wifiMan = (WifiManager)getActivity().getSystemService(Context.WIFI_SERVICE);
        // DHCP information
        DhcpInfo dhcpInfo = wifiMan.getDhcpInfo();

        // List of scanned ip
        ArrayList<String> ipAddresses = new ArrayList<String>();

        // Convert DHCP integer ip address to human readable format
        final InetAddress ipAddr = intToInetAddress(dhcpInfo.ipAddress);

        // Set your ip text
        TextView yourIpText = (TextView) view.findViewById(R.id.your_ip_addr);
        if(isWifiConnected()) {
            yourIpText.append(ipAddr.toString().replace("/", ""));
        } else {
            yourIpText.append("Not connected!");
        }
        // Scann button
        Button scanButton = (Button) view.findViewById(R.id.start_scan_button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "Starting scanning!", Toast.LENGTH_SHORT).show();
                dialogScanning = ProgressDialog.show(getActivity(), "Scanning", "Please wait...", true, true);
                new Scanner().execute(ipAddr.toString());
            }
        });

        Button refreshButton = (Button) view.findViewById(R.id.refresh_button);
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity().getApplicationContext(), "Refresh", Toast.LENGTH_SHORT).show();
                Fragment newFragInstance = new ScannerFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();

                // Get current fragment and remove
                // transaction.remove(getFragmentManager().findFragmentById(R.id.fragment_scanner));

                transaction.replace(R.id.activity_main, newFragInstance);
                // To store prevous fragment keep null
                //transaction.addToBackStack(null);

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
                        arrayListDeviceNetworkDetails.add(tmpDataString);
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

}
