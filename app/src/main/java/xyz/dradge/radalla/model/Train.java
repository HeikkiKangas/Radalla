package xyz.dradge.radalla.model;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import xyz.dradge.radalla.util.TimeUtil;

public class Train {
    private int trainNumber;
    private String departureDate;
    private String trainType;
    private long version;
    private List<TimeTableRow> timeTableRows;
    private boolean cancelled;
    private String trainCategory;
    private TimeTableRow arrivalRow;
    private TimeTableRow departureRow;
    private RailwayStation origin;
    private RailwayStation destination;
    private HashMap<String, RailwayStation> stations;
    private boolean passengerTrain;

    public void setStations(RailwayStation origin, RailwayStation destination, HashMap<String, RailwayStation> stations) {
        this.origin = origin;
        this.stations = stations;
        this.destination = destination;
        if (destination == null) this.destination = getLastStop();
        updateArrivalAndDepartureRows();
    }

    private void updateArrivalAndDepartureRows() {
        arrivalRow = getArrivalTimeTableRow(this.destination);
        departureRow = getDepartureTimeTableRow(origin);
    }

    public int getTrainNumber() {
        return trainNumber;
    }

    public void setTrainNumber(int trainNumber) {
        this.trainNumber = trainNumber;
    }

    public String getDepartureDate() {
        return departureDate;
    }

    public void setDepartureDate(String departureDate) {
        this.departureDate = departureDate;
    }

    public String getTrainType() {
        return trainType;
    }

    public void setTrainType(String trainType) {
        this.trainType = trainType;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public List<TimeTableRow> getTimeTableRows() {
        return timeTableRows;
    }

    public void setTimeTableRows(List<TimeTableRow> timeTableRows) {
        this.timeTableRows = timeTableRows;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getTrainCategory() {
        return trainCategory;
    }

    public void setTrainCategory(String trainCategory) {
        this.trainCategory = trainCategory;
        passengerTrain = trainCategory.equals("Commuter") || trainCategory.equals("Long-distance");
    }

    public TimeTableRow getArrivalRow() {
        return arrivalRow;
    }

    public TimeTableRow getDepartureRow() {
        return departureRow;
    }

    public RailwayStation getOrigin() {
        return origin;
    }

    public RailwayStation getDestination() {
        return destination;
    }

    public boolean isPassengerTrain() {
        return passengerTrain;
    }

    @Override
    public String toString() {
        return "Train{" +
                "trainNumber=" + trainNumber +
                ", departureDate='" + departureDate + '\'' +
                ", timeTableRowsCount=" + timeTableRows.size() +
                '}';
    }

    public TimeTableRow getDepartureTimeTableRow(RailwayStation station) {
        for (TimeTableRow r : timeTableRows) {
            if (station.getStationUICCode() == r.getStationUICCode() && r.getType().equals("DEPARTURE")) return r;
        }
        return null;
    }

    public TimeTableRow getArrivalTimeTableRow(RailwayStation station) {
        for (TimeTableRow r : timeTableRows) {
            if (station.getStationUICCode() == r.getStationUICCode() && r.getType().equals("ARRIVAL")) return r;
        }
        return null;
    }

    private String getTimeChangedText(String time1, String time2) {
        return time1 + " ➜ " + time2;
    }

    public TableRow getTimetableRow(Context context, boolean route) {
        TableRow row = new TableRow(context);
        TextView originDepartureTime = new TextView(context);
        TextView originTrack = new TextView(context);
        TextView destinationArrivalTime = new TextView(context);
        TextView destinationTrack = new TextView(context);
        TextView trainNumber = new TextView(context);


        row.addView(originTrack);
        row.addView(originDepartureTime);
        row.addView(trainNumber);
        row.addView(destinationTrack);
        row.addView(destinationArrivalTime);

        trainNumber.setText(trainType + ' ' + getTrainNumber());
        destinationTrack.setText("" + arrivalRow.getCommercialTrack());

        if (departureRow != null) {
            originTrack.setText("" + departureRow.getCommercialTrack());
            if (departureRow.getDifferenceInMinutes() < 1) {
                originDepartureTime
                        .setText(TimeUtil.utcToHoursAndMinutes(departureRow.getScheduledTime()));
            } else {
                originDepartureTime.setText(getTimeChangedText(
                            TimeUtil.utcToHoursAndMinutes(departureRow.getScheduledTime()),
                            TimeUtil.utcToHoursAndMinutes(departureRow.getUpdatedTime())
                ));
            }
        }

        if (arrivalRow != null) {
            if (arrivalRow.getDifferenceInMinutes() < 1) {
                destinationArrivalTime.setText(
                        TimeUtil.utcToHoursAndMinutes(arrivalRow.getScheduledTime()));
            } else {
                destinationArrivalTime.setText(getTimeChangedText(
                        TimeUtil.utcToHoursAndMinutes(arrivalRow.getScheduledTime()),
                        TimeUtil.utcToHoursAndMinutes(arrivalRow.getUpdatedTime())
                ));
            }
        }

        if (!route) {
            TextView destinationName = new TextView(context);
            destinationName.setText(destination.getStationFriendlyName());
            row.addView(destinationName);
        }
        Button trackBtn = new Button(context);
        trackBtn.setText("Track");
        trackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(getClass().getName(), "Track train, topic: " + getMQTTTopic());
            }
        });
        row.addView(trackBtn);
        return row;
    }

    private RailwayStation getLastStop() {
        for (int i = timeTableRows.size() - 1; i > 0; i--) {
            TimeTableRow row = timeTableRows.get(i);
            if (row.isTrainStopping()) return stations.get(row.getStationShortCode());
        }
        return null;
    }

    public Train updateTrain(String json) {
        ObjectReader objectReader = (new ObjectMapper()).readerForUpdating(this);
        try {
            objectReader.readValue(json);
        } catch (IOException e) {
            e.printStackTrace();
        }
        updateArrivalAndDepartureRows();
        return this;
    }

    public String getMQTTTopic() {
        return String.format(
                "trains/%s/%d",
                departureDate,
                trainNumber);
    }

    public String getLocationMQTTTopic() {
        return String.format(
                "train-locations/%s/%d",
                departureDate,
                trainNumber);
    }
}
