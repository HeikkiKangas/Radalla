package xyz.dradge.radalla;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import xyz.dradge.radalla.model.RailwayStation;
import xyz.dradge.radalla.model.Train;
import xyz.dradge.radalla.services.RailwayStationFetchService;
import xyz.dradge.radalla.services.TrainFetchService;

public class MainActivity extends AppCompatActivity {
    private AutoCompleteTextView originField;
    private AutoCompleteTextView destinationField;
    private TableLayout trainTable;
    private TextView header;
    private HashMap<String, RailwayStation> stations;
    private HashMap<String, String> shortCodesToNames;
    private ArrayList<String> passengerStationNames;
    private ObjectMapper objectMapper;
    private ArrayAdapter<String> autoCompleteAdapter;
    private List<Train> trains;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passengerStationNames = new ArrayList<>();
        stations = new HashMap<>();
        shortCodesToNames = new HashMap<>();
        trains = new ArrayList<>();
        autoCompleteAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                passengerStationNames
        );

        originField = findViewById(R.id.originField);
        originField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v);
                return true;
            }
            return false;
        });
        destinationField = findViewById(R.id.destinationField);
        destinationField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                search(v);
                return true;
            }
            return false;
        });
        trainTable = findViewById(R.id.trainTable);
        header = findViewById(R.id.header);
        originField.setAdapter(autoCompleteAdapter);
        destinationField.setAdapter(autoCompleteAdapter);

        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(
                new TrainReceiver(),
                new IntentFilter("xyz.dradge.radalla.TrainsFetched"));

        manager.registerReceiver(
                new RailwayStationReceiver(),
                new IntentFilter("xyz.dradge.radalla.RailwayStationsFetched"));

        startService(new Intent(this, RailwayStationFetchService.class));
    }

    public HashMap<String, RailwayStation> getStations() {
        return stations;
    }

    public void swapDirection(View v) {
        String temp = originField.getText().toString();
        originField.setText(destinationField.getText());
        destinationField.setText(temp);
        if (!originField.getText().toString().isEmpty()) search(v);
    }

    public void search(View v) {
        if (originField.getText().toString().isEmpty()) {
            originField.requestFocus();
            makeToast("Origin station required.");
            return;
        }
        RailwayStation origin = stations.get(shortCodesToNames.get(originField.getText().toString().trim()));
        RailwayStation destination = stations.get(shortCodesToNames.get(destinationField.getText().toString().trim()));
        Intent i = new Intent(this, TrainFetchService.class);
        if (origin == null) return;
        i.putExtra("originShortCode", origin.getStationShortCode());
        if (destination != null) i.putExtra("destinationShortCode", destination.getStationShortCode());
        startService(i);
    }

    private void makeToast(String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    private RailwayStation findStationByShortCode(String shortCode) {
        for (RailwayStation s : stations.values()) {
            if (s.getStationShortCode().equals(shortCode)) return s;
        }
        return null;
    }

    public class TrainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String json = intent.getStringExtra("json");
            RailwayStation origin = findStationByShortCode(intent.getStringExtra("originShortCode"));
            RailwayStation destination = findStationByShortCode(intent.getStringExtra("destinationShortCode"));
            Log.d(getClass().getName(), "route: " + (destination != null));
            if (json.length() < 1) return;

            try {
                trains.clear();
                trains.addAll(
                        Arrays.asList(objectMapper.readValue(json, Train[].class))
                                .stream()
                                .filter(t -> t.isPassengerTrain())
                                .collect(Collectors.toList())
                );
                trains.forEach(t -> t.setStations(origin, destination, stations));

                trainTable.removeAllViews();
                trains.forEach(t -> trainTable.addView(t.getTimetableRow(context, destination != null)));
                header.setText(destination == null
                        ? origin.getStationFriendlyName() + " - Leaving and arriving trains."
                        : origin.getStationFriendlyName() + " - " + destination.getStationFriendlyName());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class RailwayStationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getName(), "onReceive() RailwayStations received");
            String json = intent.getStringExtra("json");
            if (json.length() < 1) return;
            try {
                Arrays.asList(objectMapper.readValue(json, RailwayStation[].class))
                    .stream()
                    .forEach(s -> stations.put(s.getStationShortCode(), s));
                stations.values()
                        .stream()
                        .forEach(s -> shortCodesToNames.put(s.getStationFriendlyName(), s.getStationShortCode()));
                passengerStationNames.clear();
                passengerStationNames.addAll(stations.values()
                        .stream()
                        .filter(s -> s.isPassengerTraffic())
                        .map(s -> s.getStationFriendlyName())
                        .collect(Collectors.toList())
                );
                autoCompleteAdapter.notifyDataSetChanged();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}