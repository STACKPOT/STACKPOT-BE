package stackpot.stackpot.common.util;

import java.time.format.DateTimeFormatter;

public class DateFormatter {
    private DateFormatter() {}

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static String dotFormatter(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");
    public static String koreanFormatter(java.time.LocalDateTime date) {
        return (date != null) ? date.format(DATE_TIME_FORMATTER) : "N/A";
    }

}
