package codebase;

import algorithms.KMPSearch;
import algorithms.LevenshteinDistance;
import algorithms.ReadFileContents;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class CampusVoice {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ReadFileContents reader = new ReadFileContents();
        KMPSearch kmp = new KMPSearch();
        LevenshteinDistance levenshtein = new LevenshteinDistance();

        String corpusPath = "corpus";

        List<File> files = reader.getCorpusFiles(corpusPath);

        System.out.print("Enter keyword: ");

        String keyword = sc.nextLine().trim().toLowerCase();

        if (keyword.isEmpty()) {
            System.out.println("No keyword found.");
            sc.close();
            return;
        }

        boolean keywordFound = false;

        System.out.println("\nSearch Results:\n");

        for (File file : files) {

            String text = reader.readFile(file);

            String[] sentences = text.split("(?<=[.!?])\\s+");

            boolean fileFound = false;

            for (String sentence : sentences) {

                String lowerSentence = sentence.toLowerCase();

                if (kmp.search(lowerSentence, keyword)) {

                    if (!fileFound) {
                        System.out.println(file.getName());
                        fileFound = true;
                        keywordFound = true;
                    }

                    System.out.println("> " + sentence.trim());
                }
            }

            if (fileFound) {
                System.out.println();
            }
        }

        if (!keywordFound) {
            System.out.println("No keyword found.");
        }

        System.out.println("--------------------------------");

        System.out.print("Enter a new suggestion to check for duplicates: ");

        String newSuggestion = sc.nextLine().trim().toLowerCase();

        if (!newSuggestion.isEmpty()) {

            double threshold = 0.50;

            boolean similarFound = false;

            System.out.println("\nChecking for similar suggestions...\n");

            for (File file : files) {

                String text = reader.readFile(file);

                String[] sentences = text.split("(?<=[.!?])\\s+");

                for (String sentence : sentences) {

                    String existingSuggestion = sentence.trim().toLowerCase();

                    double similarity = levenshtein.similarity(newSuggestion, existingSuggestion);

                    if (similarity >= threshold) {

                        similarFound = true;

                        System.out.println(file.getName());
                        System.out.println("> " + sentence.trim());
                        System.out.printf(
                                "Similarity: %.2f%%%n",
                                similarity * 100);
                        System.out.println("Status: Similar suggestion found\n");
                    }
                }
            }

            if (!similarFound) {
                System.out.println("No similar suggestion found.");
                System.out.println("You can submit this as a new suggestion.");
            }
        }

        sc.close();
    }
}