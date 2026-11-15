package parking.model;

import java.time.LocalDateTime;
import java.time.LocalTime;

public final class DeliveryVan extends Car {
    public DeliveryVan(String registration) { super(registration); }

    @Override protected long quote(LocalDateTime in, LocalDateTime out) {
        long total = 0;
        for (LocalDateTime block = in; block.isBefore(out); block = block.plusHours(1)) {
            LocalTime start = block.toLocalTime();
            boolean peak = !start.isBefore(LocalTime.of(9, 0)) && start.isBefore(LocalTime.of(17, 0));
            total = Math.addExact(total, ratePerHour() * (peak ? 2 : 1));
        }
        return total;
    }
}
