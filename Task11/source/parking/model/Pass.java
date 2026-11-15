package parking.model;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Objects;

public interface Pass {
    boolean covers(LocalDate date);

    enum Role { STAFF, STUDENT, VISITOR }

    static Pass monthly(Role role, YearMonth month) {
        Objects.requireNonNull(role, "role");
        Objects.requireNonNull(month, "month");
        if (role == Role.VISITOR) throw new IllegalArgumentException("Monthly pass requires affiliation");
        return date -> YearMonth.from(date).equals(month);
    }
}
