package stackpot.stackpot.common.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DdayCounter {
    private DdayCounter() {}
    public static String dDayCount(LocalDate deadLine){
        LocalDate today = LocalDate.now();
        LocalDate deadline = deadLine;

        long daysDiff = ChronoUnit.DAYS.between(today, deadline);

        String dDay;
        if (daysDiff == 0) {
            dDay = "D-Day";
        } else if (daysDiff > 0) {
            dDay = "D-" + daysDiff;
        } else {
            dDay = "D+" + Math.abs(daysDiff);
        }
        return dDay;
    }
}
