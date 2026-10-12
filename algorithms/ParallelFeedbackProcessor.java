package algorithms;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class ParallelFeedbackProcessor {

    private final LevenshteinDistance levenshtein = new LevenshteinDistance();

    public List<String> findSimilarFeedbacks(
            List<File> files,
            String newFeedback,
            double threshold) throws InterruptedException {

        if (files.isEmpty()) {
            return new ArrayList<>();
        }

        int threadCount = Math.min(
                Math.max(2, Runtime.getRuntime().availableProcessors()),
                files.size()
        );

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        List<Future<String>> futures = new ArrayList<>();

        for (File file : files) {
            futures.add(executor.submit(() -> {
                try {
                    String existing = java.nio.file.Files.readString(file.toPath());
                    double similarity = levenshtein.similarity(newFeedback, existing);

                    if (similarity >= threshold) {
                        return file.getName() + " (" + String.format("%.2f", similarity * 100) + "% similar)";
                    }
                } catch (Exception ignored) {
                }

                return null;
            }));
        }

        List<String> result = new ArrayList<>();

        for (Future<String> future : futures) {
            try {
                String value = future.get();
                if (value != null) {
                    result.add(value);
                }
            } catch (ExecutionException ignored) {
            }
        }

        executor.shutdown();
        return result;
    }
}
