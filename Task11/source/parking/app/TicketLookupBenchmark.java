package parking.app;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import parking.model.Car;
import parking.model.Ticket;

public final class TicketLookupBenchmark {
    private static volatile long sink;

    private interface Lookup { Ticket get(String registration); }

    private static long trial(Lookup lookup, String[] keys) {
        long total = 0;
        long start = System.nanoTime();
        for (String key : keys) total += lookup.get(key).id();
        long elapsed = System.nanoTime() - start;
        sink = total;
        return elapsed;
    }

    public static void main(String[] args) {
        System.out.println("tickets,structure,repetition,lookups,elapsed_ns");
        for (int size : new int[]{20, 150, 500}) {
            Ticket[] array = new Ticket[size];
            List<Ticket> list = new ArrayList<>(size);
            Map<String, Ticket> map = new HashMap<>();
            for (int i = 0; i < size; i++) {
                String registration = "REG" + i;
                Ticket t = new Ticket(new Car(registration), LocalDateTime.of(2026, 1, 1, 0, 0), 0, i);
                array[i] = t;
                list.add(t);
                map.put(registration, t);
            }
            Lookup[] methods = {
                key -> {
                    for (Ticket t : array) if (t.vehicle().registration().equals(key)) return t;
                    throw new AssertionError(key);
                },
                key -> {
                    for (int i = 0; i < list.size(); i++) {
                        Ticket t = list.get(i);
                        if (t.vehicle().registration().equals(key)) return t;
                    }
                    throw new AssertionError(key);
                },
                map::get
            };
            String[] names = {"array", "array_list", "hash_map"};
            String[] keys = new String[20_000];
            java.util.Random random = new java.util.Random(31);
            for (int i = 0; i < keys.length; i++) keys[i] = "REG" + random.nextInt(size);
            for (int repetition = -3; repetition < 5; repetition++) {
                long checksum = -1;
                for (int index = 0; index < methods.length; index++) {
                    int m = Math.floorMod(index + repetition, methods.length);
                    long nanos = trial(methods[m], keys);
                    if (checksum == -1) checksum = sink;
                    if (sink != checksum) throw new AssertionError("Lookup disagreement");
                    if (repetition >= 0) System.out.printf("%d,%s,%d,%d,%d%n",
                            size, names[m], repetition + 1, keys.length, nanos);
                }
            }
        }
    }
}
