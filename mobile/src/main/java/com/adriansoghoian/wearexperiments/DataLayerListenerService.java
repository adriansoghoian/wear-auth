package com.adriansoghoian.wearexperiments;

import android.util.Xml;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;


public class DataLayerListenerService extends WearableListenerService {

    TextView status;
    float[] floatOutput;

    public DataLayerListenerService() {
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);
        if("/MESSAGE".equals(messageEvent.getPath())) {
            byte[] data = messageEvent.getData();
            System.out.println("onMessageReceived: buffer of length " + data.length);
            readByteArray(data);

        }
    }

    // This still seems to produce a lot of errors...
    private void readByteArray(byte[] data)
    {
        ByteArrayInputStream bais = new ByteArrayInputStream(data);
        DataInputStream dis = new DataInputStream(bais);
        String output = "";
        ArrayList<float[]> measurements = new ArrayList();
        for(int i=0; i < (data.length / 4); i++){
            float[] sample = new float[3];
            for(int j = 0; j < 3; j++){
                try {
                    sample[j] = dis.readFloat();
                    output += sample[j] + "\t";
                } catch (IOException e) {
                    System.out.println("readByteArray: error reading dis");
                }
            }
            measurements.add(sample);
        }
        System.out.println("readByteArray " + output);

        try {
            dis.close();
            bais.close();
        } catch (IOException e) {
            System.out.println("readByteArray: error closing dis/ bais");
        }
//        status = MyActivity.status;
//        status.setText(output);

    }

}
