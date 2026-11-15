package parking.model;

import java.time.LocalDateTime;

public final class EBike extends TwoWheeler {
    public EBike(String registration) { super(registration); }
    @Override protected long quote(LocalDateTime in, LocalDateTime out) { return 15; }
}
