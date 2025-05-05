package stackpot.stackpot.common.util;

import java.time.format.DateTimeFormatter;

public class DateFormatter {
    private DateFormatter() {}

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy. MM. dd");
    public static String dotFormatter(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }

}
