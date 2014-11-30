package pl.dmcs.mecin.raspic;



import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A simple {@link Fragment} subclass.
 */
public class ConnectDialogFragment extends DialogFragment {

    public String IP_ADDR = "";
    OnConnectDialogFragment mCallback;

    // Interface for parent to comunicate
    public interface OnConnectDialogFragment {
        public void onConnectDialogFragment(String ip, int connectMethod);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);


        Log.d("ConnectDialog", "enter onActivityCreated");

        if (savedInstanceState != null) {
            Log.d("ConnectDialog", "savedInstanceState != null");
            IP_ADDR = savedInstanceState.getString("IP");
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallback = (OnConnectDialogFragment) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnConnectDialogFragment");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Bundle bundle = getArguments();

        if (bundle != null) {
            Log.d("ConnectDialog", "onCreateDialog bundle != null");
            IP_ADDR = bundle.getString("IP");
        }

        LayoutInflater li = LayoutInflater.from(getActivity().getApplicationContext());
        View connectView = li.inflate(R.layout.fragment_connect_dialog, null);

        final EditText inputIpEditText = (EditText) connectView.findViewById(R.id.inputIp);

        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.connect_method)
                .setView(connectView)
                .setSingleChoiceItems(new String[]{"Sockets", "Webview"}, 1, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int id) {

                        //dialog.dismiss();
                        //int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                        //Toast.makeText(getActivity().getApplicationContext(), " id: " + id, Toast.LENGTH_SHORT).show();

                    }
                })
                .setPositiveButton(R.string.connect_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                        LayoutInflater li = LayoutInflater.from(getActivity().getApplicationContext());
                        View connectView = li.inflate(R.layout.fragment_connect_dialog, null);


                        IP_ADDR = inputIpEditText.getText().toString();

                        Log.d("IP connDial","IP: " + IP_ADDR);
                        if (!inputIpEditText.getText().toString().equals("")) {

                            int selectedPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();

                            mCallback.onConnectDialogFragment(IP_ADDR, selectedPosition);
                        } else {
                            Toast.makeText(getActivity().getApplicationContext(), "Input IP address!", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
                });

        // Create the AlertDialog object and return it
        return builder.create();
    }

}
