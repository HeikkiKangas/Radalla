package xyz.dradge.radalla.model;

import java.util.List;

/**
 * This class matches API's Train location objects.
 * Used for Jackson JSON parsing.
 */
public class TrainLocation {
    private int trainNumber;
    private String departureDate;
    private String timestamp;
    private Location location;
    private int speed;

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

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /**
     * Simplified version of GeoJSON used by digitraffic API for train locations.
     */
    public class Location {
        private String type;
        private List<Double> coordinates;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<Double> getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(List<Double> coordinates) {
            this.coordinates = coordinates;
        }



        @Override
        public String toString() {
            return "Location{" +
                    "type='" + type + '\'' +
                    ", coordinates=" + coordinates +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "TrainLocation{" +
                "trainNumber=" + trainNumber +
                ", departureDate='" + departureDate + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", location=" + location.toString() +
                ", speed=" + speed +
                '}';
    }
}
