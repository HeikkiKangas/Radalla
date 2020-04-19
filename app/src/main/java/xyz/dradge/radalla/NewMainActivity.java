package xyz.dradge.radalla;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.widget.ViewPager2;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.dradge.radalla.model.RailwayStation;
import xyz.dradge.radalla.services.RailwayStationFetchService;
import xyz.dradge.radalla.tabs.TabAdapter;

public class NewMainActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private final List<String> TAB_NAMES = Arrays.asList("Station", "Route", "Train");
    private static ObjectMapper objectMapper;
    private static Map<String, RailwayStation> railwayStations;
    private static Map<String, String> stationShortCodes;
    private static List<String> stationNames;
    private static NewMainActivity mainActivity;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        TabAdapter tabAdapter = new TabAdapter(this);
        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(tabAdapter);
        TabLayout tabLayout = findViewById(R.id.tabLayout);

        new TabLayoutMediator(
                tabLayout,
                viewPager,
                (tab, position) -> tab.setText(TAB_NAMES.get(position))
        ).attach();

        //tabLayout.selectTab(tabLayout.getTabAt(1));

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        railwayStations = new HashMap<>();
        stationShortCodes = new HashMap<>();
        stationNames = new ArrayList<>();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(
                new NewMainActivity.RailwayStationReceiver(),
                new IntentFilter("xyz.dradge.radalla.RailwayStationsFetched"));

        startService(new Intent(this, RailwayStationFetchService.class));
    }

    public void fobOnClick(View v) {
        // startActivity(new Intent());
        Log.d(TAG, "fobOnClick()");
    }

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public Map<String, RailwayStation> getRailwayStations() {
        return railwayStations;
    }

    public Map<String, String> getStationShortCodes() {
        return stationShortCodes;
    }

    public List<String> getStationNames() {
        return stationNames;
    }

    public static class RailwayStationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getName(), "onReceive() RailwayStations received");
            String json = intent.getStringExtra("json");
            if (json == null || json.isEmpty()) return;
            try {
                railwayStations.clear();
                stationShortCodes.clear();
                stationNames.clear();
                Arrays.asList(objectMapper.readValue(json, RailwayStation[].class))
                        .stream()
                        .filter(s -> s.isPassengerTraffic())
                        .forEach(s -> {
                            railwayStations.put(s.getStationShortCode(), s);
                            stationShortCodes.put(
                                    s.getStationFriendlyName(),
                                    s.getStationShortCode());
                        });
                stationNames.addAll(stationShortCodes.keySet());
                Collections.sort(stationNames);
                Intent i = new Intent("xyz.dradge.radalla.RailwayStationsUpdated");
                LocalBroadcastManager.getInstance(mainActivity).sendBroadcast(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
