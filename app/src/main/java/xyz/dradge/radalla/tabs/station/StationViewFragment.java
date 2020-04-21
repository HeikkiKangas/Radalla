package xyz.dradge.radalla.tabs.station;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import xyz.dradge.radalla.MainActivity;
import xyz.dradge.radalla.R;
import xyz.dradge.radalla.model.RailwayStation;
import xyz.dradge.radalla.model.Train;
import xyz.dradge.radalla.services.MqttService;
import xyz.dradge.radalla.services.TrainFetchService;
import xyz.dradge.radalla.util.Util;

/**
 * Fragment for searching trains that have stopped or will stop on given station.
 */
public class StationViewFragment extends Fragment implements MqttService.MqttListener {
    private final String TAG = getClass().getName();
    private static RecyclerView recyclerView;
    private static ObjectMapper objectMapper;
    private static MainActivity mainActivity;
    private static Map<String, RailwayStation> railwayStations;
    private static Map<String, String> stationShortCodes;
    private static ArrayList<String> stationNames;
    private static ArrayAdapter<String> autocompleteAdapter;
    private static StationAdapter recyclerAdapter;
    private static List<Train> trains;
    private static TextView header;
    private static StationViewFragment thisFragment;
    private AutoCompleteTextView stationNameField;
    private Button searchButton;

    /**
     * Inflates the fragment, initializes variables and sets click listeners for buttons.
     * @param inflater layout inflater.
     * @param container container for this fragment.
     * @param savedInstanceState not used.
     * @return the inflated fragment.
     */
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_station_view, container, false);
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(getActivity());

        mainActivity = (MainActivity) getActivity();
        objectMapper = mainActivity.getObjectMapper();
        railwayStations = mainActivity.getRailwayStations();
        stationShortCodes = mainActivity.getStationShortCodes();

        trains = new ArrayList<>();
        stationNames = new ArrayList<>();
        stationNames.addAll(stationShortCodes.keySet());
        Collections.sort(stationNames);

        recyclerView = v.findViewById(R.id.stationRecyclerView);

        stationNameField = v.findViewById(R.id.stationNameField);
        stationNameField.setText(sharedPreferences.getString("stationDefaultStation", ""));
        searchButton = v.findViewById(R.id.stationSearchButton);
        header = v.findViewById(R.id.stationHeader);

        autocompleteAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                stationNames
        );


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
            String stationName = Util.capitalizeString(
                    stationNameField.getText().toString().trim()
            );

            stationNameField.setText(stationName);
            stationNameField.clearFocus();

            if (stationName.isEmpty()) {
                stationNameField.requestFocus();
                mainActivity.makeToast("Station name required");
            }

            if (stationNames.contains(stationName)) {
                Intent i = new Intent(getContext(), TrainFetchService.class);
                i.putExtra("originShortCode", stationShortCodes.get(stationName));
                getActivity().startService(i);
            } else {
                mainActivity.makeToast("Station not found");
            }
        });

        return v;
    }

    /**
     * Unsubscribes all listened MQTT topics.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "onDestroy()");
        mainActivity.getMqttService().unsubscribeAllTopics(this);
    }

    /**
     * Sets adapters for autocomplete text field, updates reference to this fragment.
     */
    @Override
    public void onResume() {
        super.onResume();
        //Log.d(TAG, "onResume()");
        thisFragment = this;
        stationNameField.setAdapter(autocompleteAdapter);
    }

    /**
     * Handles MQTT messages, updates train items on the RecyclerView.
     * @param topic message topic, not used.
     * @param msg JSON String of the updated train object.
     * @param trainNumber Number of the updated train object.
     */
    @Override
    public void onUpdate(String topic, String msg, int trainNumber) {
        //Log.d(TAG, String.format("TrainNumber: %d, Topic: %s", trainNumber, topic));
        int trainIndex = trains.indexOf(
                trains.stream()
                        .filter(t -> t.getTrainNumber() == trainNumber)
                        .findFirst()
                        .get()
        );
        trains.get(trainIndex).updateTrain(msg);
        recyclerAdapter.notifyItemChanged(trainIndex);
    }

    /**
     * Updates RecyclerView's train list.
     */
    public static class TrainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(getClass().getName(), "Trains received.");
            String json = intent.getStringExtra("json");
            if (json.length() < 1) return;
            RailwayStation origin =
                    railwayStations.get(intent.getStringExtra("originShortCode"));
            if (intent.getStringExtra("destinationShortCode") != null) return;
            try {
                List<Train> newTrains =
                        Arrays.asList(objectMapper.readValue(json, Train[].class))
                                .stream()
                                .filter(t -> t.isPassengerTrain())
                                .collect(Collectors.toList());
                trains.forEach(t -> mainActivity.getMqttService().unsubscribe(t.getMQTTTopic(), thisFragment));
                trains.clear();
                trains.addAll(newTrains);
                trains.forEach(t -> t.setStations(origin, null, railwayStations));
                recyclerAdapter.notifyDataSetChanged();
                trains.forEach(t -> mainActivity.getMqttService().subscribe(t.getMQTTTopic(), thisFragment));
                header.setText(String.format("%s - Arriving and leaving trains", origin.getStationFriendlyName()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Updates AutoComplete text field's data set.
     */
    public static class RailwayStationUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //Log.d(getClass().getName(), "Stations updated.");
            railwayStations = mainActivity.getRailwayStations();
            stationShortCodes = mainActivity.getStationShortCodes();
            stationNames.clear();
            stationNames.addAll(stationShortCodes.keySet());
            Collections.sort(stationNames);
            autocompleteAdapter.notifyDataSetChanged();
        }
    }
}
