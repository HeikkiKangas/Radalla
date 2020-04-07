package xyz.dradge.radalla.services;

import android.os.Binder;

public class TrainMqttBinder extends Binder {
    private TrainMqttService service;

    public TrainMqttBinder(TrainMqttService service) {
        setService(service);
    }

    public void setService(TrainMqttService service) {
        this.service = service;
    }

    public TrainMqttService getService() {
        return service;
    }
}
