package xyz.dradge.radalla.model;

import android.content.Context;
import android.widget.TableRow;
import android.widget.TextView;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import xyz.dradge.radalla.util.TimeUtil;

public class Train {
    private int trainNumber;
    private String departureDate;
    private String trainType;
    private long version;
    private List<TimeTableRow> timeTableRows;
    private boolean cancelled;
    private String trainCategory;
    private String commuterLineID;
    private boolean passengerTrain;
    private TimeTableRow arrivalRow;
    private TimeTableRow departureRow;
    private RailwayStation origin;
    private RailwayStation destination;
    private Map<String, RailwayStation> stations;

    public void setStations(RailwayStation origin, RailwayStation destination, Map<String, RailwayStation> stations) {
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
                ", timeTableRowsCount=" + timeTableRows +
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

    public String getCommuterLineID() {
        return commuterLineID;
    }

    public void setCommuterLineID(String commuterLineID) {
        this.commuterLineID = commuterLineID;
    }

    private RailwayStation getLastStop() {
        for (int i = timeTableRows.size() - 1; i > 0; i--) {
            TimeTableRow row = timeTableRows.get(i);
            if (row.isTrainStopping()) return stations.get(row.getStationShortCode());
        }
        return null;
    }

    public Train updateTrain(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        ObjectReader objectReader = objectMapper.readerForUpdating(this);
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
                "trains/%s/%d/#",
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
