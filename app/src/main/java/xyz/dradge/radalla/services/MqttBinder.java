package xyz.dradge.radalla.services;

import android.os.Binder;

public class MqttBinder extends Binder {
    private MqttService service;

    public MqttBinder(MqttService service) {
        setService(service);
    }

    public void setService(MqttService service) {
        this.service = service;
    }

    public MqttService getService() {
        return service;
    }
}
