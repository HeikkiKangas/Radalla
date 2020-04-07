package xyz.dradge.radalla.services;

public interface MqttListener {
    void onUpdate(String msg);
    String getListenerId();
}
