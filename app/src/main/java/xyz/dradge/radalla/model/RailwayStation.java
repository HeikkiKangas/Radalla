package xyz.dradge.radalla.model;

import java.util.Objects;

/**
 * This class matches API's RailwayStation objects.
 * Used for Jackson JSON parsing.
 */
public class RailwayStation {
    private boolean passengerTraffic;
    private String type;
    private String stationName;
    private String stationFriendlyName;
    private String stationShortCode;
    private String countryCode;
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
        setStationFriendlyName(stationName.replace(" asema", ""));
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

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
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
