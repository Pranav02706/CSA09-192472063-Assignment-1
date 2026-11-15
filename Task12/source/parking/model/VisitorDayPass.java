package parking.model;

import java.time.LocalDate;
import java.util.Objects;

public final class VisitorDayPass implements Pass {
    private final LocalDate validDate;

    public VisitorDayPass(LocalDate validDate) {
        this.validDate = Objects.requireNonNull(validDate);
    }

    @Override public boolean covers(LocalDate date) { return validDate.equals(date); }
}
