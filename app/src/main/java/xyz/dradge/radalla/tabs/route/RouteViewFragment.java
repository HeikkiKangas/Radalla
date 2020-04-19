package xyz.dradge.radalla.tabs.route;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;

import xyz.dradge.radalla.NewMainActivity;
import xyz.dradge.radalla.R;
import xyz.dradge.radalla.model.RailwayStation;

public class RouteViewFragment extends Fragment {
    private final String TAG = getClass().getName();
    private RecyclerView recyclerView;
    private static ObjectMapper objectMapper;
    private static NewMainActivity mainActivity;
    private static Map<String, RailwayStation> railwayStations;
    private static Map<String, String> stationShortCodes;
    private static ArrayList<String> stationNames;
    private AutoCompleteTextView originField;
    private AutoCompleteTextView destinationField;
    private static ArrayAdapter<String> autocompleteAdapter;
    private Button searchButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_route_view, container, false);
        recyclerView = v.findViewById(R.id.routeRecyclerView);

        mainActivity = (NewMainActivity) getActivity();
        objectMapper = mainActivity.getObjectMapper();
        railwayStations = mainActivity.getRailwayStations();
        stationShortCodes = mainActivity.getStationShortCodes();
        /*
        stationNames = new ArrayList<>();
        stationNames.addAll(stationShortCodes.keySet());
        Collections.sort(stationNames);
        */
        recyclerView = v.findViewById(R.id.stationRecyclerView);
        originField = v.findViewById(R.id.routeOriginField);
        destinationField = v.findViewById(R.id.routeDestinationField);
        searchButton = v.findViewById(R.id.stationSearchButton);

        autocompleteAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_dropdown_item_1line,
                mainActivity.getStationNames()
        );

        originField.setAdapter(autocompleteAdapter);
        destinationField.setAdapter(autocompleteAdapter);

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(getContext());
        manager.registerReceiver(
                new RouteViewFragment.RailwayStationUpdateReceiver(),
                new IntentFilter("xyz.dradge.radalla.RailwayStationsUpdated"));

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

    public static class TrainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    }

    public static class RailwayStationUpdateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(getClass().getName(), "Stations updated.");
            /*
            railwayStations = mainActivity.getRailwayStations();
            stationShortCodes = mainActivity.getStationShortCodes();
            stationNames.clear();
            stationNames.addAll(stationShortCodes.keySet());
            Collections.sort(stationNames);
            */
            autocompleteAdapter.notifyDataSetChanged();
        }
    }
}
