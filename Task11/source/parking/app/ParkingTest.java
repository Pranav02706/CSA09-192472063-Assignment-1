package parking.app;

import java.time.LocalDateTime;
import java.time.YearMonth;
import parking.model.*;
import parking.service.Facility;

public final class ParkingTest {
    private static final LocalDateTime IN = LocalDateTime.of(2026, 11, 15, 10, 0);

    public static void equal(String label, long expected, long actual) {
        System.out.printf("%s | expected=%d | observed=%d%n", label, expected, actual);
        if (expected != actual) throw new AssertionError(label);
    }

    private static void reject(String label, Class<? extends RuntimeException> type, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException ex) {
            if (!type.isInstance(ex)) throw ex;
            System.out.printf("%s | expected=%s | observed=%s%n", label, type.getSimpleName(),
                    ex.getClass().getSimpleName());
            return;
        }
        throw new AssertionError(label + " did not reject");
    }

    private static Vehicle create(String kind, String registration) {
        switch (kind) {
            case "car": return new Car(registration);
            case "bike": return new TwoWheeler(registration);
            case "ev": return new ElectricCar(registration);
            default: throw new IllegalArgumentException("Unknown vehicle type");
        }
    }

    private static void capacity(int floors, int size) {
        int[] sizes = new int[floors];
        java.util.Arrays.fill(sizes, size);
        Facility facility = new Facility(sizes);
        Ticket last = null;
        for (int i = 0; i < floors * (size - 1); i++) {
            last = facility.enter(create("car", "CAR" + i), IN, null, false);
        }
        equal(floors + "x" + size + " last regular floor", floors - 1, last.floor());
        equal(floors + "x" + size + " last regular slot", size - 2, last.slot());
        reject("regular pool full", IllegalStateException.class,
                () -> facility.enter(new Car("EXTRA"), IN, null, false));
        for (int i = 0; i < floors; i++) {
            last = facility.enter(new ElectricCar("EV" + i), IN, null, true);
        }
        equal("last charging floor", floors - 1, last.floor());
        equal("last charging slot", size - 1, last.slot());
        reject("charging pool full", IllegalStateException.class,
                () -> facility.enter(new ElectricCar("X"), IN, null, true));
    }

    public static void main(String[] args) {
        capacity(1, 20);
        capacity(3, 50);
        capacity(5, 100);
        Facility f = new Facility(5);
        Ticket car = f.enter(create("car", "C1"), IN, null, false);
        Ticket bike = f.enter(create("bike", "B1"), IN, null, false);
        Ticket ev = f.enter(create("ev", "E1"), IN, null, true);
        Ticket staff = f.enter(new Car("S1"), IN, Pass.monthly(Pass.Role.STAFF, YearMonth.from(IN)), false);
        Ticket zero = f.enter(new Car("Z1"), IN, null, false);
        reject("duplicate registration", IllegalArgumentException.class,
                () -> f.enter(new Car(" c1 "), IN, null, false));
        reject("unknown vehicle type", IllegalArgumentException.class, () -> create("truck", "T"));
        reject("out-of-bounds exit", IllegalArgumentException.class, () -> f.exit(0, 5, car.id(), IN, 0));
        reject("wrong ticket", IllegalArgumentException.class, () -> f.exit(0, car.slot(), bike.id(), IN, 0));
        reject("exit before entry", IllegalArgumentException.class,
                () -> f.exit(0, car.slot(), car.id(), IN.minusMinutes(1), 0));
        equal("one-hour car", 30, f.exit(0, car.slot(), car.id(), IN.plusMinutes(60), 0));
        equal("61-minute bike", 20, f.exit(0, bike.slot(), bike.id(), IN.plusMinutes(61), 0));
        equal("EV with 10 kWh", 210, f.exit(0, ev.slot(), ev.id(), IN.plusMinutes(130), 10));
        equal("eligible monthly pass", 0, f.exit(0, staff.slot(), staff.id(), IN.plusHours(2), 0));
        equal("zero duration", 0, f.exit(0, zero.slot(), zero.id(), IN, 0));
        reject("double release", IllegalArgumentException.class, () -> f.exit(0, zero.slot(), zero.id(), IN, 0));
        equal("manual daily collection", 260, f.collection(IN.toLocalDate()));
        Facility south = new Facility(2);
        Ticket next = south.enter(new Car("N1"), IN, null, false);
        equal("unique ID across gates", 1, next.id() > zero.id() ? 1 : 0);
    }
}
