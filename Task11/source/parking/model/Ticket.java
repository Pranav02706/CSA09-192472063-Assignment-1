package parking.model;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public final class Ticket {
    private static final AtomicLong SEQUENCE = new AtomicLong();
    private final long id;
    private final Vehicle vehicle;
    private final LocalDateTime entered;
    private final int floor;
    private final int slot;
    private final boolean passCovered;

    public Ticket(Vehicle vehicle, LocalDateTime entered, int floor, int slot) {
        this(vehicle, entered, floor, slot, null);
    }

    public Ticket(Vehicle vehicle, LocalDateTime entered, int floor, int slot, Pass pass) {
        this.vehicle = Objects.requireNonNull(vehicle, "vehicle");
        this.entered = Objects.requireNonNull(entered, "entered");
        if (floor < 0 || slot < 0) throw new IllegalArgumentException("Negative coordinate");
        this.floor = floor;
        this.slot = slot;
        passCovered = pass != null && pass.covers(entered.toLocalDate());
        id = SEQUENCE.updateAndGet(Math::incrementExact);
    }

    public long id() { return id; }
    public Vehicle vehicle() { return vehicle; }
    public LocalDateTime entered() { return entered; }
    public int floor() { return floor; }
    public int slot() { return slot; }
    public boolean passCovered() { return passCovered; }
}
