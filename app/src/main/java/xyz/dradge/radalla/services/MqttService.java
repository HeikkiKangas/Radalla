package xyz.dradge.radalla.services;

public interface MqttService {
    void subscribe(String topic, MqttListener listener);
    void unsubscribe(String topic, MqttListener listener);
    void setListener(MqttListener listener);
}
