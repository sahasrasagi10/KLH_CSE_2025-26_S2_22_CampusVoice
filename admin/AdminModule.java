package admin;

import algorithms.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class AdminModule {
    private static final String CORPUS = "corpus";
    private static final String VOTES = "data/votes.txt";
    private static final String STATUS = "data/status.txt";

    private final Scanner sc;
    private final ReadFileContents reader = new ReadFileContents();

    public AdminModule(Scanner sc) {
        this.sc = sc;
        new File("data").mkdirs();

        try {
            if (!Files.exists(Path.of(VOTES))) Files.createFile(Path.of(VOTES));
            if (!Files.exists(Path.of(STATUS))) Files.createFile(Path.of(STATUS));
        } catch (IOException ignored) {}
    }

    public void start() {
        while (true) {
            System.out.println("\n========== ADMIN MODULE ==========");
            System.out.println("1. View All Feedback");
            System.out.println("2. Update Feedback Status");
            System.out.println("3. Priority Analysis (CO5)");
            System.out.println("4. Feedback Review Schedule (CO4)");
            System.out.println("5. Random Feedback Audit (CO6)");
            System.out.println("6. Back");
            System.out.print("Choice: ");

            switch (sc.nextLine().trim()) {
                case "1" -> view();
                case "2" -> statusUpdate();
                case "3" -> priority();
                case "4" -> reviewSchedule();
                case "5" -> randomAudit();
                case "6" -> { return; }
                default -> System.out.println("Invalid choice.");
            }
        }
    }

    private void view() {
        System.out.println("\n========== ALL FEEDBACK ==========");

        for (File f : reader.getCorpusFiles(CORPUS)) {
            try {
                System.out.println("\nFeedback ID: " + extractId(f)
                        + " | Votes: " + votes(f)
                        + " | Status: " + status(f));
                System.out.println("--------------------------------");
                System.out.println(reader.readFile(f));
            } catch (IOException ignored) {}
        }
    }

    private void statusUpdate() {
        view();

        System.out.print("\nEnter Feedback ID: ");
        Integer id = parseId(sc.nextLine().trim());

        if (id == null) {
            System.out.println("Invalid Feedback ID.");
            return;
        }

        File file = feedbackFile(id);

        if (!file.exists()) {
            System.out.println("Feedback ID not found.");
            return;
        }

        System.out.println("1. Pending");
        System.out.println("2. In Progress");
        System.out.println("3. Completed");
        System.out.print("Choice: ");

        String s = sc.nextLine().trim();

        String newStatus =
                s.equals("1") ? "Pending" :
                s.equals("2") ? "In Progress" :
                s.equals("3") ? "Completed" : null;

        if (newStatus == null) {
            System.out.println("Invalid status.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Path.of(STATUS));
            boolean updated = false;

            for (int i = 0; i < lines.size(); i++) {
                String[] p = lines.get(i).split("\\|", -1);

                if (p.length == 2 && p[0].equalsIgnoreCase(file.getName())) {
                    lines.set(i, file.getName() + "|" + newStatus);
                    updated = true;
                    break;
                }
            }

            if (!updated) {
                lines.add(file.getName() + "|" + newStatus);
            }

            Files.write(
                    Path.of(STATUS),
                    lines,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING
            );

            System.out.println("Status updated for Feedback ID " + id + ".");

        } catch (IOException e) {
            System.out.println("Could not update status.");
        }
    }

    private void priority() {
        Map<String, Set<String>> map = new HashMap<>();

        try {
            for (String line : Files.readAllLines(Path.of(VOTES))) {
                String[] p = line.split("\\|", -1);

                if (p.length == 2) {
                    map.computeIfAbsent(p[1], k -> new HashSet<>()).add(p[0]);
                }
            }
        } catch (IOException ignored) {}

        List<PriorityOptimizer.Issue> issues = new ArrayList<>();

        for (File f : reader.getCorpusFiles(CORPUS)) {
            Set<String> students =
                    map.getOrDefault(f.getName(), new HashSet<>());

            issues.add(new PriorityOptimizer.Issue(
                    f.getName(),
                    students,
                    students.size()
            ));
        }

        System.out.print("How many issues can administration handle? ");

        int k;

        try {
            k = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return;
        }

        List<PriorityOptimizer.Issue> chosen =
                new PriorityOptimizer().greedyMaximumCoverage(issues, k);

        System.out.println("\nPriority issues:");

        for (PriorityOptimizer.Issue issue : chosen) {
            System.out.println(
                    "Feedback ID: " + extractId(issue.getFeedbackId())
                            + " | Students: " + issue.getStudents().size()
                            + " | Votes: " + issue.getUpvotes()
            );
        }
    }

    private void reviewSchedule() {
        List<File> all = reader.getCorpusFiles(CORPUS);
        List<File> pending = new ArrayList<>();

        for (File f : all) {
            if (status(f).equalsIgnoreCase("Pending")) {
                pending.add(f);
            }
        }

        if (pending.isEmpty()) {
            System.out.println("\nNo pending feedback available for scheduling.");
            return;
        }

        System.out.println("\n========== FEEDBACK REVIEW SCHEDULE ==========");
        System.out.println("Pending Feedback:");

        for (int i = 0; i < pending.size(); i++) {
            System.out.println(
                    (i + 1) + ". Feedback ID: " + extractId(pending.get(i))
            );
        }

        System.out.print("\nHow many review slots are available? ");

        int slotCount;

        try {
            slotCount = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid number of slots.");
            return;
        }

        if (slotCount <= 0) {
            System.out.println("Invalid number of slots.");
            return;
        }

        List<String> slots = new ArrayList<>();

        for (int i = 0; i < slotCount; i++) {
            System.out.print(
                    "Enter review slot " + (i + 1)
                            + " (example: Monday 10 AM): "
            );

            String slot = sc.nextLine().trim();

            if (slot.isEmpty()) {
                slot = "Slot " + (i + 1);
            }

            slots.add(slot);
        }

        int feedbackCount = pending.size();
        BipartiteMatching matcher =
                new BipartiteMatching(feedbackCount, slotCount);

        for (int i = 0; i < feedbackCount; i++) {
            for (int j = 0; j < slotCount; j++) {
                matcher.connect(i, j);
            }
        }

        int matches = matcher.maximumMatching();
        int[] assignment = matcher.getMatches();

        System.out.println("\n========== GENERATED REVIEW SCHEDULE ==========");

        for (int slot = 0; slot < slotCount; slot++) {
            if (assignment[slot] != -1) {
                System.out.println(
                        "Feedback ID " + extractId(pending.get(assignment[slot]))
                                + " ---------> " + slots.get(slot)
                );
            } else {
                System.out.println(
                        "No feedback ---------> " + slots.get(slot)
                );
            }
        }

        System.out.println("\nMaximum feedbacks scheduled: " + matches);
    }

    private void randomAudit() {
        List<File> fs = reader.getCorpusFiles(CORPUS);

        System.out.print("Sample size: ");

        int k;

        try {
            k = Integer.parseInt(sc.nextLine().trim());
        } catch (Exception e) {
            System.out.println("Invalid number.");
            return;
        }

        System.out.println("\nRandom audit sample:");

        for (File f : new RandomizedAlgorithms().reservoirSample(fs, k)) {
            System.out.println("Feedback ID: " + extractId(f));
        }
    }

    private int votes(File file) {
        int count = 0;

        try {
            for (String line : Files.readAllLines(Path.of(VOTES))) {
                String[] p = line.split("\\|", -1);

                if (p.length == 2
                        && p[1].equalsIgnoreCase(file.getName())) {
                    count++;
                }
            }
        } catch (IOException ignored) {}

        return count;
    }

    private String status(File file) {
        try {
            for (String line : Files.readAllLines(Path.of(STATUS))) {
                String[] p = line.split("\\|", -1);

                if (p.length == 2
                        && p[0].equalsIgnoreCase(file.getName())) {
                    return p[1];
                }
            }
        } catch (IOException ignored) {}

        return "Pending";
    }

    private File feedbackFile(int id) {
        return new File(CORPUS, String.format("Feedback%02d.txt", id));
    }

    private Integer parseId(String input) {
        try {
            int id = Integer.parseInt(input);

            if (id <= 0) return null;

            return id;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int extractId(File file) {
        return extractId(file.getName());
    }

    private int extractId(String filename) {
        String name = filename
                .replace("Feedback", "")
                .replace(".txt", "");

        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
