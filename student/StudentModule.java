package student;

import algorithms.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

public class StudentModule {

    private static final String CORPUS = "corpus";
    private static final String VOTES = "data/votes.txt";
    private static final double THRESHOLD = 0.60;

    private final Scanner sc;
    private final ReadFileContents reader = new ReadFileContents();
    private final LevenshteinDistance edit = new LevenshteinDistance();
    private final KMPSearch kmp = new KMPSearch();

    public StudentModule(Scanner sc) {
        this.sc = sc;

        new File(CORPUS).mkdirs();
        new File("data").mkdirs();

        try {
            if (!Files.exists(Path.of(VOTES))) {
                Files.createFile(Path.of(VOTES));
            }
        } catch (IOException ignored) {
        }
    }

    public void start() {

        while (true) {

            System.out.println("\n========== STUDENT MODULE ==========");
            System.out.println("1. Submit Feedback");
            System.out.println("2. Top Voted Feedback");
            System.out.println("3. Search Feedback");
            System.out.println("4. Back");
            System.out.print("Choice: ");

            switch (sc.nextLine().trim()) {

                case "1" -> submit();

                case "2" -> top();

                case "3" -> search();

                case "4" -> {
                    return;
                }

                default -> System.out.println("Invalid choice.");
            }
        }
    }

    // =========================================================
    // SUBMIT FEEDBACK
    // =========================================================

    private void submit() {

        System.out.print("\nEnter feedback: ");
        String input = sc.nextLine().trim();

        if (input.isEmpty()) {
            System.out.println("Feedback cannot be empty.");
            return;
        }

        File best = null;
        double bestScore = 0.0;

        /*
         * Compare the new feedback with ALL existing feedbacks.
         * The feedback with the highest similarity is selected.
         */

        for (File file : reader.getCorpusFiles(CORPUS)) {

            try {

                String existingFeedback = reader.readFile(file);

                double score = edit.similarity(
                        input,
                        existingFeedback);

                if (score > bestScore) {
                    bestScore = score;
                    best = file;
                }

            } catch (IOException ignored) {
            }
        }

        System.out.printf(
                "\nHighest similarity: %.2f%%%n",
                bestScore * 100);

        /*
         * 60% or more means the feedback is considered
         * similar to an existing feedback.
         */

        if (best != null && bestScore >= THRESHOLD) {

            int id = extractId(best);

            System.out.println(
                    "\nSIMILAR FEEDBACK FOUND");

            System.out.println(
                    "--------------------------------");

            System.out.println(
                    "Feedback ID: " + id);

            System.out.printf(
                    "Similarity: %.2f%%%n",
                    bestScore * 100);

            System.out.println(
                    "\nExisting feedback:");

            try {
                System.out.println(
                        reader.readFile(best));
            } catch (IOException ignored) {
            }

            System.out.print(
                    "\nWould you like to upvote this feedback? (y/n): ");

            String answer = sc.nextLine().trim();

            if (answer.equalsIgnoreCase("y")) {
                vote(id);
            }

        } else {

            /*
             * No existing feedback reached 60%.
             * Therefore create a new feedback file.
             */

            int id = nextId();

            File newFile = feedbackFile(id);

            try {

                reader.writeFile(
                        newFile,
                        input);

                System.out.println(
                        "\nNo similar feedback found.");

                System.out.println(
                        "Created Feedback ID: " + id);

            } catch (IOException e) {

                System.out.println(
                        "Could not save feedback.");
            }
        }
    }

    // =========================================================
    // TOP VOTED FEEDBACK
    // =========================================================

    private void top() {

        List<File> files = reader.getCorpusFiles(CORPUS);

        files.sort(
                (a, b) -> Integer.compare(
                        votes(b),
                        votes(a)));

        System.out.println(
                "\n========== TOP VOTED FEEDBACK ==========");

        if (files.isEmpty()) {

            System.out.println(
                    "No feedback found.");

            return;
        }

        for (int i = 0; i < files.size(); i++) {

            File file = files.get(i);

            try {

                System.out.println(
                        "\n" +
                                (i + 1) +
                                ". Feedback ID: " +
                                extractId(file) +
                                " | Votes: " +
                                votes(file));

                System.out.println(
                        reader.readFile(file));

            } catch (IOException ignored) {
            }
        }

        System.out.print(
                "\nEnter Feedback ID to upvote, or 0: ");

        String input = sc.nextLine().trim();

        if (input.equals("0")) {
            return;
        }

        Integer id = parseId(input);

        if (id != null) {
            vote(id);
        } else {
            System.out.println(
                    "Invalid Feedback ID.");
        }
    }

    // =========================================================
    // SEARCH FEEDBACK
    // =========================================================

    private void search() {

        System.out.print(
                "\nEnter keyword: ");

        String key = sc.nextLine().trim();

        if (key.isEmpty()) {
            return;
        }

        boolean found = false;

        for (File file : reader.getCorpusFiles(CORPUS)) {

            try {

                String text = reader.readFile(file);

                if (kmp.search(text, key)) {

                    found = true;

                    System.out.println(
                            "\nFeedback ID: " +
                                    extractId(file) +
                                    " | Votes: " +
                                    votes(file));

                    System.out.println(text);
                }

            } catch (IOException ignored) {
            }
        }

        if (!found) {

            System.out.println(
                    "No feedback found.");
        }
    }

    // =========================================================
    // VOTING
    // =========================================================

    private void vote(int id) {

        File file = feedbackFile(id);

        if (!file.exists()) {

            System.out.println(
                    "Feedback ID not found.");

            return;
        }

        System.out.print(
                "Enter Student ID: ");

        String student = sc.nextLine().trim();

        if (student.isEmpty()) {

            System.out.println(
                    "Student ID cannot be empty.");

            return;
        }

        try {

            /*
             * Prevent the same student from voting
             * for the same feedback more than once.
             */

            for (String line : Files.readAllLines(
                    Path.of(VOTES))) {

                String[] parts = line.split("\\|", -1);

                if (parts.length == 2 &&
                        parts[0].equalsIgnoreCase(student) &&
                        parts[1].equalsIgnoreCase(
                                file.getName())) {

                    System.out.println(
                            "You already voted for this feedback.");

                    return;
                }
            }

            Files.writeString(

                    Path.of(VOTES),

                    student +
                            "|" +
                            file.getName() +
                            System.lineSeparator(),

                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND);

            System.out.println(
                    "Vote recorded. Total votes: " +
                            votes(file));

        } catch (IOException e) {

            System.out.println(
                    "Could not record vote.");
        }
    }

    // =========================================================
    // COUNT VOTES
    // =========================================================

    private int votes(File file) {

        int count = 0;

        try {

            for (String line : Files.readAllLines(
                    Path.of(VOTES))) {

                String[] parts = line.split("\\|", -1);

                if (parts.length == 2 &&
                        parts[1].equalsIgnoreCase(
                                file.getName())) {

                    count++;
                }
            }

        } catch (IOException ignored) {
        }

        return count;
    }

    // =========================================================
    // FIND NEXT FEEDBACK ID
    // =========================================================

    private int nextId() {

        int max = 0;

        for (File file : reader.getCorpusFiles(CORPUS)) {

            int id = extractId(file);

            if (id > max) {
                max = id;
            }
        }

        return max + 1;
    }

    // =========================================================
    // GET FEEDBACK FILE USING ID
    // =========================================================

    private File feedbackFile(int id) {

        return new File(
                CORPUS,
                String.format(
                        "Feedback%02d.txt",
                        id));
    }

    // =========================================================
    // PARSE NUMERIC ID
    // =========================================================

    private Integer parseId(String input) {

        try {

            int id = Integer.parseInt(input);

            if (id <= 0) {
                return null;
            }

            return id;

        } catch (NumberFormatException e) {

            return null;
        }
    }

    // =========================================================
    // EXTRACT ID FROM FILE NAME
    // =========================================================

    private int extractId(File file) {

        String name = file.getName()
                .replace("Feedback", "")
                .replace(".txt", "");

        try {

            return Integer.parseInt(name);

        } catch (NumberFormatException e) {

            return -1;
        }
    }
}