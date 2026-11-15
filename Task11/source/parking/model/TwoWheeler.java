package parking.model;

public class TwoWheeler extends Vehicle {
    public TwoWheeler(String registration) { super(registration); }
    @Override protected long ratePerHour() { return 10; }
}
