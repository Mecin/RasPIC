package pl.dmcs.mecin.raspic;



import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements ScannerFragment.OnListItemClicked,
        StartFragment.OnClickActivityAction,
        OnClickDialog.OnConnectionMethodChoose,
        ConnectDialogFragment.OnConnectDialogFragment {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Hide title bar
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);

        getActionBar().setDisplayHomeAsUpEnabled(true);


        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            Fragment newFragment = new StartFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.activity_main, newFragment).commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {
            case R.id.home:
                Log.d("HOME", "popBackStack()");
                //getFragmentManager().popBackStack();

                return true;
            case R.id.action_scanner:
            case R.id.action_scannerIcon:
                // Run scanner
                switchFragment(new ScannerFragment());
                return true;
            case R.id.action_connect:
            case R.id.action_connectIcon:
                // Connect to PI
                ConnectDialogFragment connectDialogFragment = new ConnectDialogFragment();
                connectDialogFragment.show(getFragmentManager(), "ConnectDialogFragment");
                return true;
            case R.id.action_about:
                startActivity(new Intent(this, AboutSlideActivity.class));
                return true;
            case R.id.action_exit:
                Log.d("EXIT", "System.exit(0). Bye.");
                System.exit(0);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    public void onConnectionMethodChoose(String ip, int connectMethod) {
        Bundle bundle =  new Bundle();
        switch (connectMethod) {
            case 0:
                bundle.putString("IP", ip);
                switchFragmentWithBundle(new RaspiControlFragmentSocket(), bundle);
                Log.d("onConnectionMethodChoose","Sockets " + ip);
                break;
            case 1:
                bundle.putString("IP", ip);
                switchFragmentWithBundle(new RaspiControlFragmentWeb(), bundle);
                Log.d("onConnectionMethodChoose","Webview " + ip);
                break;
            case -1:
                Log.d("onConnectionMethodChoose","negative " + ip);
                break;
            default:
                break;

        }
    }

    @Override
    public void connectToPI() {
        //Toast.makeText(getApplicationContext(), "You can't divide by zero", Toast.LENGTH_LONG).show();
        //switchFragment(new RaspiControlFragmentWeb());
    }

    @Override
    public void scanLocalNetwork() {
        //Toast.makeText(getApplicationContext(), "Run scanner!", Toast.LENGTH_LONG).show();
        switchFragment(new ScannerFragment());
    }

    @Override
    public void onItemClicked(String ip) {
        Log.d("IP", "Gets ip string " + ip);
        //Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_SHORT).show();
        Fragment raspiControlFragment = new RaspiControlFragmentWeb();
        Bundle args = new Bundle();
        args.putString("IP", ip);
        raspiControlFragment.setArguments(args);
        switchFragment(raspiControlFragment);
    }

    public void switchFragmentWithBundle(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        switchFragment(fragment);
    }

    public void switchFragment(Fragment fragment) {
        //TextView helloText = (TextView) findViewById(R.id.hello_world);
        //helloText.setText("Klik");

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.replace(R.id.activity_main,fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onConnectDialogFragment(String ip, int connectMethod) {
        onConnectionMethodChoose(ip, connectMethod);
    }
}
