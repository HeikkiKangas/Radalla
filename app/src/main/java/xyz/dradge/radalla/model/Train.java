package xyz.dradge.radalla.model;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import xyz.dradge.radalla.util.Util;

/**
 * This class matches API's Train objects.
 * Used for Jackson JSON parsing.
 */
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
    private boolean runningCurrently;
    private TimeTableRow arrivalRow;
    private TimeTableRow departureRow;
    private RailwayStation origin;
    private RailwayStation destination;
    private Map<String, RailwayStation> stations;

    /**
     * Sets the origin and destination stations also updates
     * origin departure- and destination arrival times.
     * @param origin Origin station.
     * @param destination Destination station.
     * @param stations List of all stations.
     */
    public void setStations(RailwayStation origin, RailwayStation destination, Map<String, RailwayStation> stations) {
        this.origin = origin;
        this.stations = stations;
        this.destination = destination;
        if (destination == null) this.destination = getLastStop();
        updateArrivalAndDepartureRows();
    }

    /**
     * Updates destination arrival and origin departure timetable rows.
     */
    private void updateArrivalAndDepartureRows() {
        arrivalRow = getArrivalTimeTableRow(destination);
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

    /**
     * Setter for trainCategory, also sets passengerTrain
     * boolean depending on train category.
     * @param trainCategory
     */
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

    public boolean isRunningCurrently() {
        return runningCurrently;
    }

    public void setRunningCurrently(boolean runningCurrently) {
        this.runningCurrently = runningCurrently;
    }

    @Override
    public String toString() {
        return "Train{" +
                "trainNumber=" + trainNumber +
                ", departureDate='" + departureDate + '\'' +
                ", timeTableRowsCount=" + timeTableRows +
                '}';
    }

    /**
     * Getter for departure timetable row of given station.
     * @param station The station which timetable row will be returned if available.
     * @return Departure timetable row of given station.
     */
    public TimeTableRow getDepartureTimeTableRow(RailwayStation station) {
        for (TimeTableRow r : timeTableRows) {
            if (station.getStationUICCode() == r.getStationUICCode() && r.getType().equals("DEPARTURE")) return r;
        }
        return null;
    }

    /**
     * Getter for arrival timetable row for given station.
     * @param station The station which timetable row will be returned if available.
     * @return Arrival timetable row of given station.
     */
    public TimeTableRow getArrivalTimeTableRow(RailwayStation station) {
        for (TimeTableRow r : timeTableRows) {
            if (station.getStationUICCode() == r.getStationUICCode() && r.getType().equals("ARRIVAL")) return r;
        }
        return null;
    }

    /**
     * Adds right facing arrow between two given Strings.
     * @param time1 String number one.
     * @param time2 String number two.
     * @return Single String with arrow between two given Strings.
     */
    private String getTimeChangedText(String time1, String time2) {
        return time1 + " ➜ " + time2;
    }

    /**
     * Getter for origin departure time.
     * @return Scheduled departure time and actual/estimated departure time if available.
     */
    public String getOriginDepartureTime() {
        String scheduledTime = Util.utcToHoursAndMinutes(departureRow.getScheduledTime());
        String updatedTime = departureRow.getUpdatedTime();
        if (!updatedTime.equals("")) updatedTime = Util.utcToHoursAndMinutes(updatedTime);
        if (!scheduledTime.equals(updatedTime) &&
                !updatedTime.isEmpty()) {
            return getTimeChangedText(scheduledTime, updatedTime);
        }
        return scheduledTime;
    }

    /**
     * Getter for destination arrival time.
     * @return Scheduled arrival time and actual/estimated arrival time if available.
     */
    public String getDestinationArrivalTime() {
        String scheduledTime = Util.utcToHoursAndMinutes(arrivalRow.getScheduledTime());
        String updatedTime = arrivalRow.getUpdatedTime();
        if (!updatedTime.equals("")) updatedTime = Util.utcToHoursAndMinutes(updatedTime);
        if (!scheduledTime.equals(updatedTime) &&
                !updatedTime.isEmpty()) {
            return getTimeChangedText(scheduledTime, updatedTime);
        }
        return scheduledTime;
    }

    public String getCommuterLineID() {
        return commuterLineID;
    }

    public void setCommuterLineID(String commuterLineID) {
        this.commuterLineID = commuterLineID;
    }

    /**
     * Getter for last station of the train.
     * @return the last station.
     */
    private RailwayStation getLastStop() {
        for (int i = timeTableRows.size() - 1; i > 0; i--) {
            TimeTableRow row = timeTableRows.get(i);
            if (row.isTrainStopping()) return stations.get(row.getStationShortCode());
        }
        return null;
    }

    /**
     * Updates train's data to match given JSON String.
     * @param json JSON String of updated train.
     * @return the updated train.
     */
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

    /**
     * Getter for MQTT topic to track changes to this train.
     * @return MQTT topic.
     */
    public String getMQTTTopic() {
        return String.format(
                "trains/%s/%d/#",
                departureDate,
                trainNumber);
    }

    /**
     * Getter for MQTT topic to track location changes to this train.
     * @return MQTT topic.
     */
    public String getLocationMQTTTopic() {
        return String.format(
                "train-locations/%s/%d",
                departureDate,
                trainNumber);
    }
}
