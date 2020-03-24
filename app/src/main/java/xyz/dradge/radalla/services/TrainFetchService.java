package xyz.dradge.radalla.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.util.Optional;

import xyz.dradge.radalla.util.JsonFetchUtil;

public class TrainFetchService extends IntentService {
    private final String BASE_URL = "https://rata.digitraffic.fi/api/v1/live-trains/station/";

    public TrainFetchService() {
        super("TrainFetchService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Bundle extras = intent.getExtras();
        String originShortCode = extras.getString("originShortCode", null);
        String destinationShortCode = extras.getString("destinationShortCode", null);

        if (originShortCode != null) {
            String url = "";
            if (destinationShortCode == null) {
                url = BASE_URL + originShortCode;
            } else {
                url = BASE_URL + originShortCode + '/' + destinationShortCode;
            }

            Optional<String> json = JsonFetchUtil.fetchJSONString(url);
            if (json.isPresent()) {
                Intent i = new Intent("xyz.dradge.radalla.TrainsFetched");
                i.putExtra("json", json.get());
                i.putExtra("originShortCode", originShortCode);
                i.putExtra("destinationShortCode", destinationShortCode);
                LocalBroadcastManager.getInstance(this).sendBroadcast(i);
            }
        }
    }
}
