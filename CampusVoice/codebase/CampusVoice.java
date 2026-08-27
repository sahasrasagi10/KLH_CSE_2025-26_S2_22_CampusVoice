package codebase;

import algorithms.KMPSearch;
import algorithms.ReadFileContents;

import java.io.File;
import java.util.List;
import java.util.Scanner;

public class CampusVoice {

    public static void main(String[] args) {

        Scanner sc = new Scanner(System.in);

        ReadFileContents reader = new ReadFileContents();
        KMPSearch kmp = new KMPSearch();

        // Corpus folder
        String corpusPath = "corpus";

        // Get all feedback files
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

        // Go through every feedback file
        for (File file : files) {

            String text = reader.readFile(file);

            // Split feedback into sentences
            String[] sentences = text.split("(?<=[.!?])\\s+");

            boolean fileFound = false;

            // Check every sentence using KMP
            for (String sentence : sentences) {

                String lowerSentence = sentence.toLowerCase();

                if (kmp.search(lowerSentence, keyword)) {

                    // Print filename only once
                    if (!fileFound) {

                        System.out.println(file.getName());
                        fileFound = true;
                        keywordFound = true;
                    }

                    // Print matching sentence
                    System.out.println("> " + sentence.trim());
                }
            }

            // Space between feedback files
            if (fileFound) {
                System.out.println();
            }
        }

        // If keyword doesn't exist anywhere
        if (!keywordFound) {

            System.out.println("No keyword found.");
        }

        sc.close();
    }
}