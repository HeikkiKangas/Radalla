package xyz.dradge.radalla.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Optional;

import javax.net.ssl.HttpsURLConnection;

public class JsonFetchUtil {
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
