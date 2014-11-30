package com.adriansoghoian.wearexperiments;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class WearActivity extends Activity implements SensorEventListener {

    private GoogleApiClient mGoogleApiClient;
    Button authenticate;
    SensorManager senSensorManager;
    Sensor senAccelerometer;
    private long lastUpdate = 0;
    private float last_x, last_y, last_z;
    private static final int SHAKE_THRESHOLD = 300;
    public float[] data;
    private boolean recording = false;
    private ArrayList<float[]> measurements = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        System.out.println("onCreate: about to init sensor manager.");
        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_NORMAL);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                authenticate = (Button) findViewById(R.id.authenticate);
                System.out.println("onCreate: about to create button");

                authenticate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        recording = ! recording;
                        if (! recording) {
                            pingPhone();
                        }
                    }
                });
            }
        });
    }

    public void pingPhone() {
        if (mGoogleApiClient == null) {
            System.out.println("pingPhone: null from GoogleApiClient.");
            return;
        }
        System.out.println("pingPhone: About to start method.");
        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        System.out.println("pingPhone: Registered for nodes through GoogleApi.");
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                System.out.println("pingPhone: Got nodes.");
                if (nodes != null) {
                    for (int i=0; i<nodes.size(); i++) {
                        final Node node = nodes.get(i);
                        byte[] measurement_bytes = convert2byte(measurements);
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/MESSAGE", measurement_bytes);
                        System.out.println("pingPhone: sent message " + measurement_bytes);
                    }
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (! recording) return;
        Sensor mySensor = event.sensor;
        // System.out.println("onSensorChanged: " + event.values[0]);
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            long curTime = System.currentTimeMillis();
            if ((curTime - lastUpdate) > 100) {
                long diffTime = (curTime - lastUpdate);
                lastUpdate = curTime;

                float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;

                if (speed > SHAKE_THRESHOLD) {
                    float[] data = {x, y, z};
                    measurements.add(data);
                }
            }
        }
    }

    public static byte[] convert2byte(ArrayList<float[]> vals) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        for( float[] coords : vals){
            for (int i= 0; i<coords.length; i++) {
                try {
                    dos.writeFloat(coords[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return baos.toByteArray();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
