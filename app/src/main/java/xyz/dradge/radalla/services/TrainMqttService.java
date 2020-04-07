package xyz.dradge.radalla.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrainMqttService extends Service implements MqttService {
    private final String TAG = getClass().getName();

    MqttAndroidClient mqttClient;
    IBinder binder;
    HashMap<String, List<String>> topicsListened;
    HashMap<String, MqttListener> listeners;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        String id = intent.getExtras().getString("id");
        topicsListened.keySet().forEach(topic -> {
            topicsListened.get(topic).remove(id);
            if (topicsListened.get(topic).size() < 1) {
                topicsListened.remove(topic);
                listeners.remove(id);
            }
        });
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        binder = new TrainMqttBinder(this);
        topicsListened = new HashMap<>();
        listeners = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void connect() {
        if (mqttClient == null) {
            String id = UUID.randomUUID().toString();
            mqttClient = new MqttAndroidClient(getApplicationContext(), "tcp://rata-mqtt.digitraffic.fi:1883", id);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(getClass().getName(), "Connection lost.");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(getClass().getName(), "Message arrived.");
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    Log.d(getClass().getName(), "Delivery complete.");
                }
            });
            Log.d(getClass().getName(), "connect()");
        }
        if (mqttClient != null && !mqttClient.isConnected()) {
            MqttConnectOptions options = new MqttConnectOptions();
            // May have to clear listened topics and notify listeners when disconnected.
            try {
                mqttClient.connect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribe(String topic, MqttListener listener) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            connect();
        }

        if (!topicsListened.containsKey(topic)) {
            try {
                topicsListened.put(topic, new ArrayList<>());
                topicsListened.get(topic).add(listener.getListenerId());
                if (!listeners.containsValue(listener))
                    listeners.put(listener.getListenerId(), listener);
                if (mqttClient != null && mqttClient.isConnected()) {
                    mqttClient.subscribe(
                            topic,
                            0,
                            (msgTopic, msg) -> topicsListened.get(msgTopic)
                                    .forEach(l -> listeners.get(l).onUpdate(msg.toString()))
                    );
                    Log.d(getClass().getName(), "subscribe() topic: " + topic);
                }
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void unsubscribe(String topic, MqttListener listener) {
        for (Map.Entry<String, List<String>> entry : topicsListened.entrySet()) {
            if (entry.getValue().equals(topic)) return;
        }

        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void unsubscribe(String topic, String listener) {
        topicsListened.get(listener).remove(topic);

        for (Map.Entry<String, List<String>> entry : topicsListened.entrySet()) {
            if (entry.getValue().equals(topic)) return;
        }

        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void unsubscribe(String topic) {
        try {
            mqttClient.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
