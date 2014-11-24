package com.adriansoghoian.wearexperiments;

import android.util.Xml;
import android.widget.TextView;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

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
            System.out.println(data);
        }
    }

}
