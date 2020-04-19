package xyz.dradge.radalla.services;

public interface MqttListener {
    void onUpdate(String topic, String msg);
    String getListenerId();
}
