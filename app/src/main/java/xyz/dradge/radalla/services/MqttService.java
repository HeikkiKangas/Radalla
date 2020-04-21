package xyz.dradge.radalla.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.ArraySet;

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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private Pattern trainNumberPattern = Pattern.compile(".+\\/\\d{4}-\\d{2}-\\d{2}\\/(\\d+)\\/?.*");
    private Pattern trainTopicPattern = Pattern.compile("(.+\\/\\d{4}-\\d{2}-\\d{2}\\/\\d+\\/).*");

    /**
     * Ran when activity or fragment binds to this service.
     * @param intent no extras used.
     * @return Binder used to retrieve reference to this service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(TAG, "onBind()");
        if (mqttClient == null || !mqttClient.isConnected()) connect();
        return binder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    /**
     * Initialize the Binder and Map of topics listened and their listeners.
     */
    @Override
    public void onCreate() {
        //Log.d(TAG, "onCreate()");
        binder = new MqttBinder(this);
        topicsListened = new HashMap<>();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    /**
     * Connects to digitraffic API.
     */
    private void connect() {
        if (mqttClient == null) {
            mqttClient = new MqttAndroidClient(getApplicationContext(), URI, ID);
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //Log.d(getClass().getName(), "Connection lost. Trying to reconnect.");
                    connect();
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    //Log.d(getClass().getName(), "Message arrived. Topic: " + topic);
                    String fixedTopic = topic;
                    if (topic.contains("trains")) {
                        Matcher m = trainTopicPattern.matcher(topic);
                        if (m.matches()) fixedTopic = m.group(1) + "#";
                    }

                    String finalTopic = fixedTopic;
                    Matcher m = trainNumberPattern.matcher(topic);
                    int trainNumber = m.matches()
                            ? Integer.parseInt(m.group(1))
                            : -1;
                    //Log.d(TAG, "topic: " + finalTopic + " trainNumber: " + trainNumber);
                    topicsListened
                            .get(finalTopic)
                            .forEach(l -> l.onUpdate(finalTopic, message.toString(), trainNumber));
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //Log.d(getClass().getName(), "Delivery complete.");
                }
            });
            //Log.d(getClass().getName(), "connect()");
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
                        //Log.d(getClass().getName(), "Successfully connected.");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        //Log.d(getClass().getName(), "Failed to connected.");
                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Subscribes to given topic on digitraffic API.
     * @param topic the topic to listen to.
     * @param listener the listener to add to given topic.
     */
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
                                //Log.d(TAG, "Successfully subscribed to: " + topic);
                                if (!topicsListened.containsKey(topic)) {
                                    topicsListened.put(topic, new ArraySet<>());
                                }
                                topicsListened.get(topic).add(listener);
                            }

                            @Override
                            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                                //Log.d(TAG, "Failed to subscribe to topic: " + topic);
                            }
                        });
            }
        } catch (MqttException e) {
                e.printStackTrace();
        }
    }

    /**
     * Unsubscribes given listener from given topic.
     * @param topic the topic to unsubscribe from.
     * @param listener the listener that's unsubscribing from the given topic.
     */
    public void unsubscribe(String topic, MqttListener listener) {
        if (topicsListened.containsKey(topic)) {
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
    }

    /**
     * Unsubscribes given listener from all listened topics.
     * @param listener the listener to unsubscribe.
     */
    public void unsubscribeAllTopics(MqttListener listener) {
        ArrayList<String> toBeRemoved = new ArrayList<>();

        for (Map.Entry<String, Set<MqttListener>> entry : topicsListened.entrySet()) {
            entry.getValue().remove(listener);
            if (entry.getValue().size() < 1) toBeRemoved.add(entry.getKey());
        }
        toBeRemoved.forEach(t -> topicsListened.remove(t));
        toBeRemoved.forEach(t -> {
            try {
                mqttClient.unsubscribe(t);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Disconnects from the digitraffic API.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            mqttClient.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Interface that activities and fragments listening for MQTT updates have to implement.
     */
    public interface MqttListener {
        void onUpdate(String topic, String msg, int trainNumber);
    }
}
