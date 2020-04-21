package xyz.dradge.radalla.model;

/**
 * This class matches API's TimetableRow objects.
 * Used for Jackson JSON parsing.
 */
public class TimeTableRow {
    private String stationShortCode;
    private int stationUICCode;
    private String type;
    private String scheduledTime;
    private boolean trainStopping;
    private boolean cancelled;
    private String actualTime;
    private String liveEstimateTime;
    private int differenceInMinutes;
    private int commercialTrack;

    /**
     * Returns actual or estimated departure/arrival time if available.
     * @return actual or estimated time if available, otherwise empty String.
     */
    public String getUpdatedTime() {
        if (actualTime != null) return actualTime;
        else if (liveEstimateTime != null) return liveEstimateTime;
        return "";
    }

    public String getStationShortCode() {
        return stationShortCode;
    }

    public void setStationShortCode(String stationShortCode) {
        this.stationShortCode = stationShortCode;
    }

    public int getStationUICCode() {
        return stationUICCode;
    }

    public void setStationUICCode(int stationUICCode) {
        this.stationUICCode = stationUICCode;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public boolean isTrainStopping() {
        return trainStopping;
    }

    public void setTrainStopping(boolean trainStopping) {
        this.trainStopping = trainStopping;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public String getActualTime() {
        return actualTime;
    }

    public void setActualTime(String actualTime) {
        this.actualTime = actualTime;
    }

    public int getDifferenceInMinutes() {
        return differenceInMinutes;
    }

    public void setDifferenceInMinutes(int differenceInMinutes) {
        this.differenceInMinutes = differenceInMinutes;
    }

    public String getLiveEstimateTime() {
        return liveEstimateTime;
    }

    public void setLiveEstimateTime(String liveEstimateTime) {
        this.liveEstimateTime = liveEstimateTime;
    }

    public int getCommercialTrack() {
        return commercialTrack;
    }

    public void setCommercialTrack(int commercialTrack) {
        this.commercialTrack = commercialTrack;
    }

    @Override
    public String toString() {
        return "TimeTableRow{" +
                "stationShortCode='" + stationShortCode + '\'' +
                ", stationUICCode='" + stationUICCode + '\'' +
                ", type='" + type + '\'' +
                ", scheduledTime='" + scheduledTime + '\'' +
                ", trainStopping=" + trainStopping +
                '}';
    }
}
