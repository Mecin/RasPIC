package pl.dmcs.mecin.raspic;


import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.gstreamer.GStreamer;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;


/**
 * A simple {@link Fragment} subclass.
 */
public class RaspiControlFragmentSocket extends Fragment implements SurfaceHolder.Callback {

    //ndk sample test
    //public native String  stringFromJNI();

    private native void nativeInit();     // Initialize native code, build pipeline, etc
    private native void nativeFinalize(); // Destroy pipeline and shutdown native code
    private native void nativePlay();     // Set pipeline to PLAYING
    private native void nativePause();    // Set pipeline to PAUSED
    private static native boolean nativeClassInit(); // Initialize native class: cache Method IDs for callbacks
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private long native_custom_data;      // Native code will use this to keep private data

    private boolean is_playing_desired;   // Whether the user asked to go to PLAYING

    // 5 fps
    private long DELAY = 200;

    private String CAMERA_IMAGE = "camera.jpg";
    private String CAMERA_IMAGE_URL = "";
    private String RECEIVED_IP = "";
    private int PORT = 5000;
    private boolean STREAMING_FLAG = false;
    private boolean CHANGE_IMG_MUTEX = false;
    private long startTime;

    private GetCameraImage getCameraImage;
    private Socket socket;

    private SockConnection sockConnection;

    private DataOutputStream outToServer;

    public RaspiControlFragmentSocket() {
        // Required empty public constructor
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        //recLifeCycle_with_savedInstanceState(savedInstanceState);
        super.onActivityCreated(savedInstanceState);

        //TextView textView = (TextView) getActivity().findViewById(R.id.hello_world_socket);

        if(getArguments() != null) {
            if (getArguments().getString("IP") != null) {
                Log.d("onActivityCreated", "received " + getArguments().getString("IP"));
                RECEIVED_IP = getArguments().getString("IP");
                CAMERA_IMAGE_URL = "http://" + RECEIVED_IP + "/" + CAMERA_IMAGE;
                //textView.append(" IP: " + RECEIVED_IP);
            }
        }

        sockConnection = new SockConnection();

        sockConnection.execute();

        //setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_raspi_control_fragment_socket, container, false);

        // Initialize GStreamer and warn if it fails
        try {
            GStreamer.init(getActivity().getApplicationContext());
        } catch (Exception e) {
            Toast.makeText(getActivity().getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            //finish();
            //return;
        }

        // play gstreamer stream button
        ImageButton play = (ImageButton) view.findViewById(R.id.button_play);
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = true;
                nativePlay();

                if(outToServer != null) {
                    try {
                        // write 9 - forward
                        Log.d("gstreamer start","sending gstreamer start signal");
                        outToServer.writeByte(9);
                        Log.d("gstreamer","done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // pause gstreamer stream button
        ImageButton pause = (ImageButton) view.findViewById(R.id.button_stop);
        pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                is_playing_desired = false;
                nativePause();

                if(outToServer != null) {
                    try {
                        // write 10 - forward
                        Log.d("gstreamer stop","sending gstreamer stop signal");
                        outToServer.writeByte(10);
                        Log.d("gstreamer","done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // gstreamer surfaceview
        SurfaceView sv = (SurfaceView) view.findViewById(R.id.surface_video);
        SurfaceHolder sh = sv.getHolder();
        sh.addCallback(this);

        // forward button
        Button forwardButton = (Button) view.findViewById(R.id.forward_button);
        forwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {
                        // write 8 - forward
                        Log.d("forward","sending forward signal");
                        outToServer.writeByte(8);
                        Log.d("forward","done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // back button
        Button backButton = (Button) view.findViewById(R.id.back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {
                        // write 2 - forward
                        Log.d("back","sending back signal");
                        outToServer.writeByte(2);
                        Log.d("back","done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // left button
        Button leftButton = (Button) view.findViewById(R.id.left_button);
        leftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {
                        // write 4 - left
                        Log.d("left","sending left signal");
                        outToServer.writeByte(4);
                        Log.d("left","done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // right button
        Button rightButton = (Button) view.findViewById(R.id.right_button);
        rightButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {

                        // Change layout orientation
                        //Log.d("ORIENTATION", "to vertical");
                        //LinearLayout currentLinearLayout = (LinearLayout) v.findViewById(R.id.socket_layout);
                        //currentLinearLayout.setOrientation(LinearLayout.VERTICAL);

                        // write 6 - right
                        Log.d("right","sending right signal");
                        outToServer.writeByte(6);
                        Log.d("right","done");

                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // stop button
        Button stopButton = (Button) view.findViewById(R.id.stop_button);
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {
                        // write 5 - stop
                        Log.d("stop","sending stop signal");
                        outToServer.writeByte(5);
                        Log.d("stop", "done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // low gear button
        Button lowGearButton = (Button) view.findViewById(R.id.low_gear_button);
        lowGearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {
                        // write 1 - stop
                        Log.d("low gear","sending low gear signal");
                        outToServer.writeByte(1);
                        Log.d("low gear", "done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // mid gear button
        Button midGearButton = (Button) view.findViewById(R.id.mid_gear_button);
        midGearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {
                        // write 7 - stop
                        Log.d("mid gear","sending mid gear signal");
                        outToServer.writeByte(7);
                        Log.d("mid gear", "done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        // top gear button
        Button topGearButton = (Button) view.findViewById(R.id.max_gear_button);
        topGearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(outToServer != null) {
                    try {
                        // write 3 - stop
                        Log.d("top gear","sending top gear signal");
                        outToServer.writeByte(3);
                        Log.d("top gear", "done");
                    } catch(IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        nativeInit();

        return view;
    }

    public void onDestroy() {
        nativeFinalize();
        super.onDestroy();
    }

    // Called from native code. This sets the content of the TextView from the UI thread.
    private void setMessage(final String message) {
        final TextView tv = (TextView) getActivity().findViewById(R.id.textview_message);
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                tv.setText(message);
            }
        });
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized () {
        Log.i ("GStreamer", "Gst initialized. Restoring state, playing:" + is_playing_desired);
        // Restore previous playing state
        if (is_playing_desired) {
            nativePlay();
        } else {
            nativePause();
        }

        // Re-enable buttons, now that GStreamer is initialized
        //final Activity activity = this;
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                //activity.findViewById(R.id.button_play).setEnabled(true);
                //activity.findViewById(R.id.button_stop).setEnabled(true);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedState) {
        try {
            if(socket != null) {
                socket.close();
            }
            Log.d("onSaveInstanceStat", "socket.close()");
        } catch (IOException e) {
            e.printStackTrace();
        }

        //Log.d("========== ORIENTATION ==========", "to horizontal");
        // Change layout orientation
        //LinearLayout currentLinearLayout = (LinearLayout) getActivity().findViewById(R.id.socket_layout);
        //currentLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

        super.onSaveInstanceState(savedState);
    }


    private class SockConnection extends AsyncTask<String, Void, String> {

        public SockConnection() {

        }

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {

            Log.d("SOCKET", "trying to connect to " + RECEIVED_IP + ":" + PORT);

            try {
                InetAddress serverAddress = InetAddress.getByName(RECEIVED_IP);
                socket = new Socket(serverAddress, PORT);

                //PrintStream printStream = new PrintStream(socket.getOutputStream());
                Log.d("SOCKET", "connected to " + RECEIVED_IP + ":" + PORT);

                outToServer = new DataOutputStream(socket.getOutputStream());
                //outToServer.writeByte(8);

            } catch(UnknownHostException e) {
                e.printStackTrace();

            } catch(Exception e) {
                e.printStackTrace();
            } finally {
                /*if (socket != null) {
                    try {
                        socket.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }*/
            }

            return "DONE";
        }

        @Override
        protected void onPostExecute(String a) {
        }


    }


    private class GetCameraImage extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public GetCameraImage(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            Log.d("doInBg GetCamImg", "enter");
            String urlDisplay = urls[0];
            Bitmap mIcon11 = null;
            try {
                InputStream in = new java.net.URL(urlDisplay).openStream();
                mIcon11 = BitmapFactory.decodeStream(in);
            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();
            }

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Log.d("onPostExecute", "setImageBitmap(result)");
            bmImage.setImageBitmap(result);
        }
    }

    private class MJPEGStream extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public MJPEGStream(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected Bitmap doInBackground(String... urls) {
            startTime = System.currentTimeMillis();
            Log.d("doInBg MJPEG", "enter startTime: " + startTime);

            if(getCameraImage != null) {
                //getCameraImage.cancel(true);
            }
            //while(STREAMING_FLAG) {
                Log.d("while(true)", "before GetCamImg");

                // Get camera image from Raspberry PI
                //getCameraImage = (GetCameraImage)new GetCameraImage(bmImage).execute(urls[0]);

                Bitmap piCameraBitmap = null;
                try {
                    InputStream in = new java.net.URL(urls[0]).openStream();
                    piCameraBitmap = BitmapFactory.decodeStream(in);
                } catch (Exception e) {
                    Log.e("Error", e.getMessage());
                    e.printStackTrace();
                }

                // Sleep for 40ms = max 25 fps if possible
                //try {
                //    Thread.sleep(DELAY);
                //} catch (InterruptedException e) {
                //    e.printStackTrace();
                //}
            //}

            return piCameraBitmap;
        }

        protected void onPostExecute(Bitmap piCameraBitmap) {
            Log.d("onPostExecute", "MJPEGStream");

            //while(CHANGE_IMG_MUTEX) {

            //}

            //CHANGE_IMG_MUTEX = true;

            while((System.currentTimeMillis() - startTime) < DELAY) {
                // get stable frames
            }

            this.bmImage.setImageBitmap(piCameraBitmap);

            Log.d("STOP", "STOP TIME: " + (System.currentTimeMillis() - startTime) + " ms.");
            //CHANGE_IMG_MUTEX = false;

            // Get next image
            if(STREAMING_FLAG) {
                //getCameraImage.cancel(true);
                new MJPEGStream(this.bmImage).execute(CAMERA_IMAGE_URL);
            }
        }
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("tutorial-3");
        nativeClassInit();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Log.d("GStreamer", "Surface changed to format " + format + " width "
                + width + " height " + height);
        nativeSurfaceInit (holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamer", "Surface destroyedsss");
        nativeSurfaceFinalize ();
    }
}
