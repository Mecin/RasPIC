package pl.dmcs.mecin.raspic;

import android.app.Activity;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


public class AboutSlideActivity extends FragmentActivity implements SensorEventListener{

    private float x ,y ,z;

    private float last_x, last_y, last_z;

    private static final int SHAKE_SENSIBLE = 800;

    MediaPlayer player;

    private SensorManager sensorManager;

    private long lastUpdate = -1;

    private View view;

    private boolean color = false;

    // The number of pages (wizard steps) to show.

    private static final int NUM_PAGES = 2;

    private ViewPager mPager;

    private PagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_slide);

        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);

        view = findViewById(R.id.pager);
        view.setBackgroundColor(Color.BLACK);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lastUpdate = System.currentTimeMillis();
    }

    @Override
    public void onBackPressed() {
        if (mPager.getCurrentItem() == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed();
        } else {
            // Otherwise, select the previous step.
            mPager.setCurrentItem(mPager.getCurrentItem() - 1);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(sensorEvent);
        }
    }

    private void getAccelerometer(SensorEvent event) {

        long curTime = System.currentTimeMillis();
        // only allow one update every 100ms.
        if ((curTime - lastUpdate) > 100) {
            long diffTime = (curTime - lastUpdate);
            lastUpdate = curTime;

        float[] values = event.values;
        // Movement
        float x = values[0];
        float y = values[1];
        float z = values[2];

        if (Round(x,4) > 10.0000) {
            Log.d("sensor", "X Right axis: " + x);
            //Toast.makeText(this, "Right shake detected", Toast.LENGTH_SHORT).show();

            if (mPager.getCurrentItem() == (NUM_PAGES - 1)) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                //super.onBackPressed();
            } else {
                // Otherwise, select the previous step.
                mPager.setCurrentItem(mPager.getCurrentItem() + 1);
            }

        }
        else if (Round(x,4) < -10.0000) {
            Log.d("sensor", "X Left axis: " + x);
            //Toast.makeText(this, "Left shake detected", Toast.LENGTH_SHORT).show();

            if (mPager.getCurrentItem() == 0) {
                // If the user is currently looking at the first step, allow the system to handle the
                // Back button. This calls finish() on this activity and pops the back stack.
                //super.onBackPressed();
            } else {
                // Otherwise, select the previous step.
                mPager.setCurrentItem(mPager.getCurrentItem() - 1);
            }
        }

        float speed = Math.abs(x+y+z - last_x - last_y - last_z) / diffTime * 10000;

        // Log.d("sensor", "diff: " + diffTime + " - speed: " + speed);
        if (speed > SHAKE_SENSIBLE) {
            Log.d("sensor", "shake detected w/ speed: " + speed);
            //Toast.makeText(this, "shake detected w/ speed: " + speed, Toast.LENGTH_SHORT).show();
        }

        last_x = x;
        last_y = y;
        last_z = z;
    }

        //Log.d("accel values", "x: " + x + " y: " + y + " z: " + z);


    }

    public static float Round(float Rval, int Rpl) {
        float p = (float)Math.pow(10,Rpl);
        Rval = Rval * p;
        float tmp = Math.round(Rval);
        return (float)tmp/p;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {
        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d("getItem", "position: " + position);
            switch (position) {
                case 0:
                    return new AboutFragment();
                case 1:
                    return new VersionFragment();
                default:
                    return new AboutFragment();
            }
            //return new AboutFragment();
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // register this class as a listener for the orientation and
        // accelerometer sensors
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        // unregister listener
        super.onPause();
        sensorManager.unregisterListener(this);
    }
}