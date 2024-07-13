package ru.demyan;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class ZombieComparator implements Comparator<Zombie> {
    private HashMap<String, Integer> typePriorityMap;

    public ZombieComparator() {
        HashMap<String, Integer> map = new HashMap<>();
        map.put("bomber", 0);
        map.put("juggernaut", 1);
        map.put("chaos_knight", 2);
        map.put("liner", 3);
        map.put("fast", 4);
        map.put("normal", 5);
        this.typePriorityMap = map;
    }

    @Override
    public int compare(Zombie z1, Zombie z2) {
        // Compare by type priority
        int typeComparison = Integer.compare(typePriorityMap.get(z1.getType()), typePriorityMap.get(z2.getType()));
        if (typeComparison != 0) {
            return typeComparison;
        }
        return 0;
    }
}
