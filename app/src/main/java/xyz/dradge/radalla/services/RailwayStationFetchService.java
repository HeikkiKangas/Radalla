package xyz.dradge.radalla.services;

import android.app.IntentService;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Optional;

import xyz.dradge.radalla.util.Util;

/**
 * Service that fetches list of all railway stations in Finland.
 */
public class RailwayStationFetchService extends IntentService {
    private final String URL = "https://rata.digitraffic.fi/api/v1/metadata/stations";

    public RailwayStationFetchService () {
        super("RailwayStationFetchService");
    }

    /**
     * Fetches list of all railway stations in Finland.
     * @param intent not used.
     */
    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Optional<String> json = Util.fetchJSONString(URL);
        if (json.isPresent()) {
            Intent i = new Intent("xyz.dradge.radalla.RailwayStationsFetched");
            i.putExtra("json", json.get());
            LocalBroadcastManager.getInstance(this).sendBroadcast(i);
        }
    }
}
