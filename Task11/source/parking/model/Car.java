package parking.model;

public class Car extends Vehicle {
    public Car(String registration) { super(registration); }
    @Override protected long ratePerHour() { return 30; }
}
