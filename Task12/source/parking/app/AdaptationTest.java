package parking.app;

import java.time.LocalDateTime;
import parking.model.*;
import parking.service.Facility;

public final class AdaptationTest {
    private static long park(Facility f, Vehicle v, LocalDateTime in, int minutes, Pass pass) {
        Ticket t = f.enter(v, in, pass, false);
        return f.exit(t.floor(), t.slot(), t.id(), in.plusMinutes(minutes), 0);
    }

    public static void main(String[] args) {
        Facility f = new Facility(2, 3, 4, 5, 6);
        LocalDateTime date = LocalDateTime.of(2026, 11, 15, 0, 0);
        ParkingTest.equal("e-bike flat", 15, park(f, new EBike("E1"), date, 245, null));
        ParkingTest.equal("van at 09:00", 60, park(f, new DeliveryVan("V1"), date.plusHours(9), 60, null));
        ParkingTest.equal("van at 17:00", 30, park(f, new DeliveryVan("V2"), date.plusHours(17), 60, null));
        ParkingTest.equal("van crossing 17:00", 90, park(f, new DeliveryVan("V3"), date.plusHours(16), 61, null));
        Pass visitor = new VisitorDayPass(date.toLocalDate());
        ParkingTest.equal("valid one-day pass", 0, park(f, new Car("A1"), date, 60, visitor));
        ParkingTest.equal("expired one-day pass", 30, park(f, new Car("A2"), date.plusDays(1), 60, visitor));
        Ticket last = null;
        for (int i = 0; i < 15; i++) last = f.enter(new Car("C" + i), date, null, false);
        ParkingTest.equal("last regular floor on jagged grid", 4, last.floor());
        ParkingTest.equal("last regular slot on jagged grid", 4, last.slot());
    }
}
