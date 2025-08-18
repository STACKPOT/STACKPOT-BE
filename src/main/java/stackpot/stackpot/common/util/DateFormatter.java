package stackpot.stackpot.common.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class DateFormatter {
    private DateFormatter() {}

    private static final DateTimeFormatter INPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter OUTPUT_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy.MM.dd");
    public static String dotFormatter(java.time.LocalDate date) {
        return (date != null) ? date.format(DATE_FORMATTER) : "N/A";
    }

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy년 M월 d일 H:mm");
    public static String koreanFormatter(java.time.LocalDateTime date) {
        return (date != null) ? date.format(DATE_TIME_FORMATTER) : "N/A";
    }



    public static String format(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            LocalDate date = LocalDate.parse(dateStr, INPUT_FORMATTER);
            return date.format(DATE_FORMATTER);
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }

    // yyyy.MM 형식 변환
    public static String formatToMonth(String dateStr) {
        if (dateStr == null || dateStr.isBlank()) return null;
        try {
            LocalDate date = LocalDate.parse(dateStr, INPUT_FORMATTER);
            return date.format(OUTPUT_FORMATTER);
        } catch (DateTimeParseException e) {
            return dateStr;
        }
    }



}
