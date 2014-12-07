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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);

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
                System.out.println("We're in the on create method");

                authenticate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pingPhone();
                    }
                });
            }
        });
    }

    public void pingPhone() {
        if (mGoogleApiClient == null)
            return;
        final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
        nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

            @Override
            public void onResult(NodeApi.GetConnectedNodesResult result) {
                final List<Node> nodes = result.getNodes();
                if (nodes != null) {
                    for (int i=0; i<nodes.size(); i++) {
                        final Node node = nodes.get(i);
//                        byte[] myvar = "Hi, just testing this!".getBytes();
                        data = new float[] { last_x, last_y, last_z };
                        byte[] myvar = convert2byte(data);
                        Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), "/MESSAGE", myvar);
                    }
                }
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;
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
                    last_x = x;
                    last_y = y;
                    last_z = z;
                }
            }
        }
    }

    public static byte[] convert2byte(float[] vals) {
        int j = 0;
        int length = vals.length;
        byte[] outData = new byte[ length*4 ];
        for (int i= 0; i<length; i++) {
            int data = Float.floatToIntBits(vals[i]);
            outData[j++]=(byte)(data>>>24);
            outData[j++]=(byte)(data>>>16);
            outData[j++]=(byte)(data>>>8);
            outData[j++]=(byte)(data>>>0);
        }
        return outData;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
