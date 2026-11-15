package parking.model;

public final class ElectricCar extends Car implements Chargeable {
    public ElectricCar(String registration) { super(registration); }

    @Override public long chargingFee(int kWh) {
        if (kWh < 0) throw new IllegalArgumentException("Negative energy");
        return 12L * kWh;
    }
}
