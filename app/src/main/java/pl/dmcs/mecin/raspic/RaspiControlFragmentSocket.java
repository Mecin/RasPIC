package pl.dmcs.mecin.raspic;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
public class RaspiControlFragmentSocket extends Fragment {

    private long DELAY = 300;
    private String CAMERA_IMAGE = "camera.jpg";
    private String CAMERA_IMAGE_URL = "";
    private String RECEIVED_IP = "";
    private int PORT = 5000;
    private boolean STREAMING_FLAG = false;

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

        // Camera image
        ImageView cameraImageView = (ImageView) view.findViewById(R.id.camera_image);
        cameraImageView.setImageResource(R.drawable.iconpi);

        // Set flag to start stream
        STREAMING_FLAG = true;

        // Get camera image from Raspberry PI
        new MJPEGStream(cameraImageView).execute(CAMERA_IMAGE_URL);

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

                        //Log.d("ORIENTATION", "to horizontal");
                        // Change layout orientation
                        //LinearLayout currentLinearLayout = (LinearLayout) v.findViewById(R.id.socket_layout);
                        //currentLinearLayout.setOrientation(LinearLayout.HORIZONTAL);

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

        return view;
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

            //bmImage.setImageBitmap(mIcon11);

            return mIcon11;
        }

        protected void onPostExecute(Bitmap result) {
            Log.d("onPostExecute", "setImageBitmap(result)");
            bmImage.setImageBitmap(result);
        }
    }

    private class MJPEGStream extends AsyncTask<String, Void, ImageView> {
        ImageView bmImage;

        public MJPEGStream(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected ImageView doInBackground(String... urls) {
            Log.d("doInBg MJPEG", "enter");
            if(getCameraImage != null) {
                getCameraImage.cancel(true);
            }
            //while(STREAMING_FLAG) {
                Log.d("while(true)", "before GetCamImg");
                // Get camera image from Raspberry PI
                getCameraImage = (GetCameraImage)new GetCameraImage(bmImage).execute(urls[0]);

                // Sleep for debug 5s
                try {
                    Thread.sleep(DELAY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            //}

            return bmImage;
        }

        protected void onPostExecute(ImageView bmImage) {
            Log.d("onPostExecute", "MJPEGStream");

            // Get next image
            if(STREAMING_FLAG) {
                //getCameraImage.cancel(true);
                new MJPEGStream(bmImage).execute(CAMERA_IMAGE_URL);
            }

            //bmImage.setImageBitmap(result);
        }
    }
}
