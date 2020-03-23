package xyz.dradge.radalla.model;

import java.util.Objects;

public class RailwayStation {

    private boolean passengerTraffic;
    private String type;
    private String stationName;
    private String stationFriendlyName;
    private String stationShortCode;
    private int stationUICCode;

    public boolean isPassengerTraffic() {
        return passengerTraffic;
    }

    public void setPassengerTraffic(boolean passengerTraffic) {
        this.passengerTraffic = passengerTraffic;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStationName() {
        return stationName;
    }

    public void setStationName(String stationName) {
        if (stationName.contains(" asema")) setStationFriendlyName(stationName.replace(" asema", ""));
        else setStationFriendlyName(stationName);
        this.stationName = stationName;
    }

    public String getStationFriendlyName() {
        return stationFriendlyName;
    }

    public void setStationFriendlyName(String stationFriendlyName) {
        this.stationFriendlyName = stationFriendlyName;
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

    @Override
    public String toString() {
        return "RailwayStation{" +
                "stationName='" + stationName + '\'' +
                ", stationShortCode='" + stationShortCode + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RailwayStation that = (RailwayStation) o;
        return passengerTraffic == that.passengerTraffic &&
                stationUICCode == that.stationUICCode &&
                type.equals(that.type) &&
                stationName.equals(that.stationName) &&
                stationShortCode.equals(that.stationShortCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(passengerTraffic, type, stationName, stationShortCode, stationUICCode);
    }
}
