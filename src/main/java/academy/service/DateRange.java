package academy.service;

import java.time.LocalDate;
import java.time.ZonedDateTime;

public record DateRange(LocalDate from, LocalDate to) {

    public boolean includes(ZonedDateTime timestamp) {
        LocalDate date = timestamp.toLocalDate();
        if (from != null && date.isBefore(from)) {
            return false;
        }
        if (to != null && date.isAfter(to)) {
            return false;
        }
        return true;
    }
}

