package xyz.dradge.radalla;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
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
import xyz.dradge.radalla.services.MqttBinder;
import xyz.dradge.radalla.services.MqttService;
import xyz.dradge.radalla.services.RailwayStationFetchService;
import xyz.dradge.radalla.tabs.TabAdapter;

/**
 * The main activity, contains ViewPager for the tabs and
 * provides a list of available railway stations to fragments.
 */
public class MainActivity extends AppCompatActivity {
    private final String TAG = getClass().getName();
    private final List<String> TAB_NAMES = Arrays.asList("Station", "Route", "Settings");
    private static ObjectMapper objectMapper;
    private static Map<String, RailwayStation> railwayStations;
    private static Map<String, String> stationShortCodes;
    private static List<String> stationNames;
    private static MainActivity mainActivity;
    private MqttService mqttService;
    private MqttConnection mqttConnection;

    /**
     * Sets up the tabs, fetches list of railway stations and connects to MQTT service.
     * @param savedInstanceState saved state.
     */
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

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        tabLayout.selectTab(tabLayout.getTabAt(
                Integer.parseInt(sharedPreferences.getString("defaultView", "0"))
        ));

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        railwayStations = new HashMap<>();
        stationShortCodes = new HashMap<>();
        stationNames = new ArrayList<>();

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(
                new MainActivity.RailwayStationReceiver(),
                new IntentFilter("xyz.dradge.radalla.RailwayStationsFetched"));

        startService(new Intent(this, RailwayStationFetchService.class));

        mqttConnection = new MainActivity.MqttConnection();
        Intent i = new Intent(this, MqttService.class);
        bindService(i, mqttConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * ObjectMapper, used to map JSON String to RailwayStation objects.
     * @return the ObjectMapper.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Getter for Map of StationShortCodes and RailwayStation objects.
     * @return Map of StationShortCodes and RailwayStation objects.
     */
    public Map<String, RailwayStation> getRailwayStations() {
        return railwayStations;
    }

    /**
     * Getter for Map of RailwayStation names and ShortCodes.
     * @return Map of RailwayStation names and ShortCodes.
     */
    public Map<String, String> getStationShortCodes() {
        return stationShortCodes;
    }

    /**
     * Getter for list of railway station names.
     * @return list of railway station names.
     */
    public List<String> getStationNames() {
        return stationNames;
    }

    /**
     * Getter for the bound MQTT Service.
     * @return the bound MQTT Service.
     */
    public MqttService getMqttService() {
        return mqttService;
    }

    /**
     * Receiver for updates of RailwayStation objects.
     */
    public static class RailwayStationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(getClass().getName(), "onReceive() RailwayStations received");
            String json = intent.getStringExtra("json");
            if (json == null || json.isEmpty()) return;
            try {
                railwayStations.clear();
                stationShortCodes.clear();
                stationNames.clear();
                Arrays.asList(objectMapper.readValue(json, RailwayStation[].class))
                        .stream()
                        .filter(s -> s.isPassengerTraffic() && s.getCountryCode().equals("FI"))
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

    /**
     * Service connection for retrieving reference to the MQTT Service.
     */
    class MqttConnection implements ServiceConnection {
        /**
         * Retrieves reference to the MQTT Service from the binder.
         * @param name not used.
         * @param service the Binder.
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Log.d(TAG, "onServiceConnected()");
            MqttBinder binder = (MqttBinder) service;
            mqttService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    }

    /**
     * Unbinds the MQTT Service.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mqttConnection);
    }

    /**
     * Creates toast with given text.
     * @param text the text to show.
     */
    public void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }
}
