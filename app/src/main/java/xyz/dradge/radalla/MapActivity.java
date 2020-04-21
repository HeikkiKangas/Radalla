package xyz.dradge.radalla;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

import java.io.IOException;
import java.util.List;

import xyz.dradge.radalla.model.TrainLocation;
import xyz.dradge.radalla.services.MqttBinder;
import xyz.dradge.radalla.services.MqttService;

/**
 * Activity for showing a marker at the train's location.
 */
public class MapActivity extends AppCompatActivity implements MqttService.MqttListener {
    private final String TAG = getClass().getName();
    private MapView mapView;
    private MapboxMap mapboxMap;
    private boolean mapReady;
    private MqttService mqttService;
    private MqttConnection mqttConnection;
    private ObjectMapper objectMapper;
    private String topic;
    private Marker marker;
    private int zoom = 10;
    private SeekBar zoomBar;
    private TextView speedText;
    private LatLng currentLocation;

    /**
     * Initializes views and sets SeekBarChangeListener.
     * @param savedInstanceState  saved state.
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, getString(R.string.mapbox_access_token));
        topic = getIntent().getExtras().getString("topic");
        //Log.d(TAG, topic);
        setContentView(R.layout.activity_map);
        speedText = findViewById(R.id.speedText);
        zoomBar = findViewById(R.id.zoomBar);
        zoomBar.setMin(1);
        zoomBar.setProgress(10);
        zoomBar.setMax(25);
        zoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                zoom = progress;
                if (mapReady) moveCamera(mapboxMap.getCameraPosition().target, 100);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        initMap(savedInstanceState);
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mqttConnection = new MapActivity.MqttConnection();
        Intent i = new Intent(this, MqttService.class);
        bindService(i, mqttConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * Initializes the map.
     * @param savedInstanceState saved state.
     */
    private void initMap(Bundle savedInstanceState) {
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(mapboxMap -> {
            MapActivity.this.mapboxMap = mapboxMap;
            mapboxMap.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    mapReady = true;
                    if (currentLocation != null) {
                        setMarker(currentLocation);
                        moveCamera(currentLocation, 1000);
                    }
                }
            });
        });
    }

    /**
     * Required for MapView.
     */
    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    /**
     * Required for MapView.
     */
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    /**
     * Required for MapView.
     */
    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    /**
     * Required for MapView.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    /**
     * Required for MapView.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    /**
     * Required for MapView.
     */
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    /**
     * Required for MapView, also unsubscribes all
     * MQTT topics and unbinds the MQTT Service.
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        mqttService.unsubscribe(topic, this);
        unbindService(mqttConnection);
    }

    /**
     * Updates the marker on the map and moves the
     * camera to the current location of the train.
     * @param topic not used.
     * @param msg the updated location of the train.
     * @param trainNumber number of the updated train.
     */
    @Override
    public void onUpdate(String topic, String msg, int trainNumber) {
        try {
            TrainLocation location = objectMapper.readValue(msg, TrainLocation.class);
            List<Double> coordinates = location.getLocation().getCoordinates();
            currentLocation = new LatLng(coordinates.get(1), coordinates.get(0));
            if (mapReady) {
                setMarker(currentLocation);
                moveCamera(currentLocation, 500);
                //Log.d(TAG, location.toString());
                speedText.setText(String.format("%d km/h", location.getSpeed()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates or moves the marker to given location depending on if there is already a marker.
     * @param position where to move or create the marker.
     */
    private void setMarker(LatLng position) {
        if (marker == null) {
            mapboxMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title("Location"));
            marker = mapboxMap.getMarkers().get(0);
        } else {
            marker.setPosition(position);
        }
    }

    /**
     * Moves camera to given position with given animation length.
     * @param position where to move the camera.
     * @param ms how fast will the camera move.
     */
    private void moveCamera(LatLng position, int ms) {
        mapboxMap.animateCamera(
                CameraUpdateFactory.newCameraPosition(getCameraPosition(position)),
                ms
        );
    }

    /**
     * Getter for a CameraPosition on a given location.
     * @param location where to move the camera.
     * @return new CameraPosition.
     */
    private CameraPosition getCameraPosition(LatLng location) {
        CameraPosition position = new CameraPosition.Builder()
                .target(location)
                .zoom(zoom)
                .bearing(0)
                .tilt(0)
                .build();
        return position;
    }

    /**
     * Service connection for retrieving reference to the MQTT Service.
     */
    class MqttConnection implements ServiceConnection {
        /**
         * Retrieves reference to the MQTT Service from the binder and
         * subscribes to location updates.
         * @param name not used.
         * @param service the Binder.
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Log.d(TAG, "onServiceConnected()");
            MqttBinder binder = (MqttBinder) service;
            mqttService = binder.getService();
            mqttService.subscribe(topic, MapActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {}
    }
}
