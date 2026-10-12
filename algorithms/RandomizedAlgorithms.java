package algorithms;

import java.util.*;

public class RandomizedAlgorithms {

    public <T> List<T> reservoirSample(List<T> items, int sampleSize) {
        if (sampleSize <= 0 || items.isEmpty()) {
            return new ArrayList<>();
        }

        sampleSize = Math.min(sampleSize, items.size());

        List<T> reservoir = new ArrayList<>();

        for (int i = 0; i < sampleSize; i++) {
            reservoir.add(items.get(i));
        }

        Random random = new Random();

        for (int i = sampleSize; i < items.size(); i++) {
            int j = random.nextInt(i + 1);

            if (j < sampleSize) {
                reservoir.set(j, items.get(i));
            }
        }

        return reservoir;
    }

    public int randomizedHash(String text) {
        if (text == null) {
            return 0;
        }

        int hash = 17;
        int randomMultiplier = 31 + new Random().nextInt(17);

        for (char c : text.toLowerCase().toCharArray()) {
            hash = hash * randomMultiplier + c;
        }

        return hash;
    }
}
