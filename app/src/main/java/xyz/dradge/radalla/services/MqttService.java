package xyz.dradge.radalla.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.ArraySet;
import android.util.Log;

import androidx.annotation.Nullable;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for listening digitraffic API's MQTT topics.
 * Messages are sent only to clients that have subscribed to said topic.
 */
public class MqttService extends Service {
    private final String TAG = getClass().getName();
    private final String URI = "tcp://rata-mqtt.digitraffic.fi:1883";
    private final String ID = UUID.randomUUID().toString();

    private MqttAndroidClient mqttClient;
    private IBinder binder;
    private HashMap<String, Set<MqttListener>> topicsListened;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind()");
        if (mqttClient == null || !mqttClient.isConnected()) connect();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate()");
        binder = new MqttBinder(this);
        topicsListened = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void connect() {
        if (mqttClient == null) {
            mqttClient = new MqttAndroidClient(getApplicationContext(), URI, ID);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    Log.d(getClass().getName(), "Connection lost. Trying to reconnect.");
                    connect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    Log.d(getClass().getName(), "Message arrived.");
                    //topicsListened.get(topic).forEach(id -> listeners.get(id).onUpdate(message.toString()));
                    topicsListened.get(topic).forEach(l -> l.onUpdate(topic, message.toString()));
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
            options.setAutomaticReconnect(true);
            options.setCleanSession(false);
            try {
                mqttClient.connect(options, null, new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                        disconnectedBufferOptions.setBufferEnabled(true);
                        disconnectedBufferOptions.setBufferSize(100);
                        disconnectedBufferOptions.setPersistBuffer(false);
                        disconnectedBufferOptions.setDeleteOldestMessages(false);
                        mqttClient.setBufferOpts(disconnectedBufferOptions);
                        Log.d(getClass().getName(), "Successfully connected.");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(getClass().getName(), "Failed to connected.");
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void subscribe(String topic, MqttListener listener) {
        if (mqttClient == null || !mqttClient.isConnected()) {
            connect();
        }

        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.subscribe(
                        topic,
                        0,
                        null,
                        new IMqttActionListener() {
                            @Override
                            public void onSuccess(IMqttToken asyncActionToken) {
                                Log.d(TAG, "Successfully subscribed to: " + topic);
                                if (!topicsListened.containsKey(topic)) {
                                    topicsListened.put(topic, new ArraySet<>());
                                }
                                topicsListened.get(topic).add(listener);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                Log.d(TAG, "Failed to subscribe to topic: " + topic);
                            }
                        });
            }
        } catch (MqttException e) {
                e.printStackTrace();
        }
    }

    public void unsubscribe(String topic, MqttListener listener) {
        topicsListened.get(topic).remove(listener);
        if (topicsListened.get(topic).size() < 1) {
            try {
                mqttClient.unsubscribe(topic);
                topicsListened.remove(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void unsubscribeAllTopics(MqttListener listener) {
        ArrayList<String> toBeRemoved = new ArrayList<>();
        topicsListened.entrySet().forEach(entry -> entry.getValue().remove(listener));
        topicsListened.entrySet()
                .stream()
                .filter(e -> e.getValue().size() > 0)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}
