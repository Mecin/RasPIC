package pl.dmcs.mecin.raspic;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;


public class RaspiControlFragmentWeb extends Fragment {

    private String RECEIVED_IP = "";

    public RaspiControlFragmentWeb() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setRetainInstance(true);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //recLifeCycle_with_savedInstanceState(savedInstanceState);
        super.onActivityCreated(savedInstanceState);

        TextView receivedIp = (TextView)getActivity().findViewById(R.id.receivedIp);
        if(getArguments() != null) {
            if(getArguments().getString("IP") != null) {
                Log.d("onActivityCreated", "received " + getArguments().getString("IP"));
                receivedIp.setText(getArguments().getString("IP"));
                RECEIVED_IP = getArguments().getString("IP");
            }
        }

        WebView webView = (WebView) getActivity().findViewById(R.id.webview);
        Log.d("URL", "loadUrl " + RECEIVED_IP);
        webView.loadUrl("http://" + RECEIVED_IP);

        // To open webview inside my fragment not as intent (eg in Chrome)
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                view.loadUrl(url);
                return true;
            }
        });

        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_raspi_control, container, false);




        return view;
    }


}
