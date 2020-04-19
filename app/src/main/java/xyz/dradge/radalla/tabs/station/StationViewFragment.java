package xyz.dradge.radalla.tabs.station;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import xyz.dradge.radalla.NewMainActivity;
import xyz.dradge.radalla.R;
import xyz.dradge.radalla.model.RailwayStation;
import xyz.dradge.radalla.model.Train;
import xyz.dradge.radalla.services.MqttBinder;
import xyz.dradge.radalla.services.MqttListener;
import xyz.dradge.radalla.services.MqttService;
import xyz.dradge.radalla.services.TrainFetchService;
import xyz.dradge.radalla.util.StringUtil;

public class StationViewFragment extends Fragment implements MqttListener {
    private final String TAG = getClass().getName();
    private static RecyclerView recyclerView;
    private static ObjectMapper objectMapper;
    private static NewMainActivity mainActivity;
    private static Map<String, RailwayStation> railwayStations;
    private static Map<String, String> stationShortCodes;
    private static ArrayList<String> stationNames;
    private static ArrayAdapter<String> autocompleteAdapter;
    private static StationAdapter recyclerAdapter;
    private static List<Train> trains;
    private static TextView header;
    private AutoCompleteTextView stationNameField;
    private Button searchButton;
    private MqttService mqttService;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_station_view, container, false);

        mainActivity = (NewMainActivity) getActivity();
        objectMapper = mainActivity.getObjectMapper();
        railwayStations = mainActivity.getRailwayStations();
        stationShortCodes = mainActivity.getStationShortCodes();

        trains = new ArrayList<>();
        stationNames = new ArrayList<>();
        stationNames.addAll(stationShortCodes.keySet());
        Collections.sort(stationNames);

        recyclerView = v.findViewById(R.id.stationRecyclerView);
        stationNameField = v.findViewById(R.id.stationNameField);
        searchButton = v.findViewById(R.id.stationSearchButton);
        header = v.findViewById(R.id.stationHeader);

        autocompleteAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                stationNames
        );

        stationNameField.setAdapter(autocompleteAdapter);

        recyclerAdapter = new StationAdapter(trains);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(recyclerAdapter);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(
                new StationViewFragment.RailwayStationUpdateReceiver(),
                new IntentFilter("xyz.dradge.radalla.RailwayStationsUpdated"));

        manager.registerReceiver(
                new StationViewFragment.TrainReceiver(),
                new IntentFilter("xyz.dradge.radalla.TrainsFetched"));

        searchButton.setOnClickListener(v1 -> {
            String stationName = StringUtil.capitalizeString(
                    stationNameField.getText().toString().trim()
            );

            stationNameField.setText(stationName);
            stationNameField.clearFocus();

            if (stationName.isEmpty()) {
                stationNameField.requestFocus();
                makeToast("Station name required");
            }

            if (stationNames.contains(stationName)) {
                Intent i = new Intent(getContext(), TrainFetchService.class);
                i.putExtra("originShortCode", stationShortCodes.get(stationName));
                getActivity().startService(i);
            } else {
                makeToast("Station not found");
            }
        });

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated()");

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView()");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy()");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume()");
    }

    @Override
    public void onUpdate(String topic, String msg) {
        try {
            Train updatedTrain = objectMapper.readValue(msg, Train.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getListenerId() {
        return null;
    }

    public static class TrainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getName(), "Trains received.");
            String json = intent.getStringExtra("json");
            if (json.length() < 1) return;
            RailwayStation origin =
                    railwayStations.get(intent.getStringExtra("originShortCode"));

            try {
                List<Train> newTrains =
                        Arrays.asList(objectMapper.readValue(json, Train[].class))
                                .stream()
                                .filter(t -> t.isPassengerTrain())
                                .collect(Collectors.toList());
                trains.clear();
                trains.addAll(newTrains);
                trains.forEach(t -> t.setStations(origin, null, railwayStations));
                recyclerAdapter.notifyDataSetChanged();
                header.setText(String.format("%s - Arriving and leaving trains", origin.getStationFriendlyName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static class RailwayStationUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getName(), "Stations updated.");
            railwayStations = mainActivity.getRailwayStations();
            stationShortCodes = mainActivity.getStationShortCodes();
            stationNames.clear();
            stationNames.addAll(stationShortCodes.keySet());
            Collections.sort(stationNames);
            autocompleteAdapter.notifyDataSetChanged();
        }
    }

    class MqttConnection implements ServiceConnection {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "onServiceConnected()");
            MqttBinder binder = (MqttBinder) service;
            mqttService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    }

    private void makeToast(String text) {
        Toast.makeText(getContext(), text, Toast.LENGTH_LONG).show();
    }
}
