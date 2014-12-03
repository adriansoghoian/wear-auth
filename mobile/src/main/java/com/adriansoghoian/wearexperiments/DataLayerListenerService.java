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
        if("/AUTHENTICATE".equals(messageEvent.getPath())) {
            byte[] data = messageEvent.getData();
            System.out.println("onMessageReceived: buffer of length " + data.length);
            ArrayList<float[]> authentication_sequence = readByteArray(data);
            float score = dtw.compareMeasurements(authentication_sequence, MyActivity.training_sequence);
            System.out.println("Matching training seq: " + printArrayFloat(MyActivity.training_sequence));
            System.out.println("Matching auth seq: " + printArrayFloat(authentication_sequence));
            System.out.println("DTW score: " + score);
        }
        if("/TRAIN".equals(messageEvent.getPath())) {
            byte[] data = messageEvent.getData();
            System.out.println("onMessageReceived: buffer of length " + data.length);
            MyActivity.training_sequence = readByteArray(data);
            System.out.println("Saved new training sequence.");
        }
    }

    // This still seems to produce a lot of errors...
    private ArrayList<float[]> readByteArray(byte[] data)
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
//                    System.out.println("readByteArray: error reading dis");
                }
            }
            output += "\n";
            measurements.add(sample);
        }
        System.out.println("readByteArray " + output);

        try {
            dis.close();
            bais.close();
        } catch (IOException e) {
            System.out.println("readByteArray: error closing dis/ bais");
        }
        return measurements;
//        status = MyActivity.status;
//        status.setText(output);

    }

    private String printArrayFloat(ArrayList<float[]> sequence){
        String output = "";
        for (float[] test : sequence){
            for(int i = 0; i < test.length; i++){
                output += test[i] + "\t";
            }
            output += " \n";
        }
        return  output;
    }

}
