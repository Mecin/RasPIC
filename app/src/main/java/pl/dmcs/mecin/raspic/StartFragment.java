package pl.dmcs.mecin.raspic;



import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class StartFragment extends Fragment {

    private OnClickActivityAction callbackListener;


    public StartFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //recLifeCycle_with_savedInstanceState(savedInstanceState);
        super.onActivityCreated(savedInstanceState);

        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_start, container, false);


        //connect to raspberry PI button
        Button connectButton = (Button) view.findViewById(R.id.connectToPIButton);
        connectButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                //callbackListener.connectToPI();
                ConnectDialogFragment connectDialogFragment = new ConnectDialogFragment();

                connectDialogFragment.show(getFragmentManager(), "ConnectDialogFragment");
            }
        });

        //scan local network button
        Button scanButton = (Button) view.findViewById(R.id.scanner);
        scanButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                callbackListener.scanLocalNetwork();
            }
        });

        //about slide activity
        Button aboutButton = (Button) view.findViewById(R.id.aboutSlideActivity);
        aboutButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                startActivity(new Intent(v.getContext(), AboutSlideActivity.class));
            }
        });

        return view;
    }

    public interface OnClickActivityAction {
        public void connectToPI();
        public void scanLocalNetwork();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof OnClickActivityAction) {
            callbackListener = (OnClickActivityAction) activity;
        } else {
            throw new ClassCastException(activity.toString()
                    + " must implemenet OnItemSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        callbackListener = null;
    }

    @Override
    public void onResume() {
        super.onResume();

    }


}
