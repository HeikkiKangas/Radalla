package xyz.dradge.radalla.services;

import android.os.Binder;

/**
 * Binder class for MqttService.
 * Used by fragments and activities to retrieve reference to the MqttService.
 */
public class MqttBinder extends Binder {
    private MqttService service;

    /**
     * Sets the service to which activities and fragments bind to.
     * @param service the service to bind to.
     */
    public MqttBinder(MqttService service) {
        setService(service);
    }

    /**
     * Setter for the service.
     * @param service the service to bind to.
     */
    public void setService(MqttService service) {
        this.service = service;
    }

    /**
     * Getter for the bound service.
     * @return the bound service.
     */
    public MqttService getService() {
        return service;
    }
}
