package xyz.dradge.radalla.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

/**
 * Utilities for handling JSON fetching,
 * timestamp to HH:MM conversion and capitalization of Strings.
 */
public class Util {
    final public static DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Converts given timestamp to HH:MM.
     * @param s the timestamp to convert.
     * @return the converted String.
     */
    public static String utcToHoursAndMinutes(String s) {
        ZonedDateTime utcTime = ZonedDateTime.parse(s);
        return utcTime.withZoneSameInstant(ZoneId.systemDefault()).format(HH_MM);
    }

    /**
     * Capitalizes first letter of the given String.
     * @param s the String to capitalize.
     * @return the capitalized String.
     */
    public static String capitalizeString(String s) {
        if (s.length() > 1) return s.substring(0, 1).toUpperCase() + s.substring(1);
        return s.toUpperCase();
    }

    /**
     * Fetches JSON String from given url.
     * @param urlString url to fetch.
     * @return the fetched JSON String.
     */
    public static Optional<String> fetchJSONString(String urlString) {
        HttpsURLConnection connection = null;
        BufferedReader reader = null;
        Optional<String> jsonString = Optional.empty();

        try {
            URL url = new URL(urlString);
            connection = (HttpsURLConnection) url.openConnection();
            connection.connect();

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuffer buffer = new StringBuffer();
            String line;

            while ((line = reader.readLine()) != null) buffer.append(line);

            jsonString = Optional.of(buffer.toString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) connection.disconnect();
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return jsonString;
    }
}
