package xyz.dradge.radalla.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class TimeUtil {
    final public static DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");

    public static String utcToHoursAndMinutes(String s) {
        ZonedDateTime utcTime = ZonedDateTime.parse(s);
        return utcTime.withZoneSameInstant(ZoneId.systemDefault()).format(HH_MM);
    }
}
