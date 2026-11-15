package parking.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import parking.model.Chargeable;
import parking.model.Pass;
import parking.model.Ticket;
import parking.model.Vehicle;

public final class Facility {
    private final Ticket[][] slots;
    private final Map<LocalDate, Long> collections = new HashMap<>();

    public Facility(int... sizes) {
        if (sizes.length == 0) throw new IllegalArgumentException("At least one floor required");
        slots = new Ticket[sizes.length][];
        for (int floor = 0; floor < sizes.length; floor++) {
            if (sizes[floor] <= 0) throw new IllegalArgumentException("Empty floor");
            slots[floor] = new Ticket[sizes[floor]];
        }
    }

    // The last slot on each floor is reserved for charging.
    private boolean charger(int floor, int slot) {
        return slot == slots[floor].length - 1;
    }

    public synchronized Ticket enter(Vehicle vehicle, LocalDateTime in, Pass pass, boolean charge) {
        Objects.requireNonNull(vehicle, "vehicle");
        Objects.requireNonNull(in, "entry time");
        if (charge && !(vehicle instanceof Chargeable)) {
            throw new IllegalArgumentException("Vehicle cannot charge");
        }
        int freeFloor = -1;
        int freeSlot = -1;
        for (int floor = 0; floor < slots.length; floor++) {
            for (int slot = 0; slot < slots[floor].length; slot++) {
                Ticket active = slots[floor][slot];
                if (active != null && active.vehicle().equals(vehicle)) {
                    throw new IllegalArgumentException("Duplicate registration");
                }
                if (freeFloor < 0 && active == null && charge == charger(floor, slot)) {
                    freeFloor = floor;
                    freeSlot = slot;
                }
            }
        }
        if (freeFloor < 0) throw new IllegalStateException("No suitable slot");
        Ticket ticket = new Ticket(vehicle, in, freeFloor, freeSlot, pass);
        slots[freeFloor][freeSlot] = ticket;
        return ticket;
    }

    public synchronized long exit(int floor, int slot, long id, LocalDateTime out, int kWh) {
        Objects.requireNonNull(out, "exit time");
        if (floor < 0 || floor >= slots.length || slot < 0 || slot >= slots[floor].length) {
            throw new IllegalArgumentException("Invalid coordinate");
        }
        Ticket ticket = slots[floor][slot];
        if (ticket == null || ticket.id() != id) throw new IllegalArgumentException("Invalid ticket");
        if (out.isBefore(ticket.entered())) throw new IllegalArgumentException("Exit precedes entry");
        if (kWh < 0 || (kWh > 0 && !charger(floor, slot))) {
            throw new IllegalArgumentException("Invalid charging usage");
        }
        long parking = ticket.passCovered() ? 0 : ticket.vehicle().calculateFee(ticket.entered(), out);
        long energy = kWh == 0 ? 0 : ((Chargeable) ticket.vehicle()).chargingFee(kWh);
        long fee = Math.addExact(parking, energy);
        collections.merge(out.toLocalDate(), fee, Math::addExact);
        slots[floor][slot] = null;
        return fee;
    }

    public synchronized long collection(LocalDate date) {
        return collections.getOrDefault(date, 0L);
    }
}
