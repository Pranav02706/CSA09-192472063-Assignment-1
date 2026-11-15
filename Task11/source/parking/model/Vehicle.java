package parking.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

public abstract class Vehicle {
    private final String registration;

    protected Vehicle(String registration) {
        if (registration == null || registration.trim().isEmpty()) {
            throw new IllegalArgumentException("Registration is required");
        }
        this.registration = registration.trim().toUpperCase(Locale.ROOT);
    }

    public final String registration() { return registration; }
    protected abstract long ratePerHour();

    public static long hours(long minutes) {
        if (minutes < 0) throw new IllegalArgumentException("Negative duration");
        return minutes / 60 + (minutes % 60 == 0 ? 0 : 1);
    }

    public final long calculateFee(int minutes) {
        return Math.multiplyExact(hours(minutes), ratePerHour());
    }

    public final long calculateFee(LocalDateTime in, LocalDateTime out) {
        if (out.isBefore(in)) throw new IllegalArgumentException("Exit precedes entry");
        return quote(in, out);
    }

    protected long quote(LocalDateTime in, LocalDateTime out) {
        long minutes = Duration.between(in, out).toMinutes();
        return Math.multiplyExact(hours(minutes), ratePerHour());
    }

    @Override public final boolean equals(Object other) {
        return other instanceof Vehicle && registration.equals(((Vehicle) other).registration);
    }

    @Override public final int hashCode() { return registration.hashCode(); }
    @Override public String toString() { return getClass().getSimpleName() + " " + registration; }
}
