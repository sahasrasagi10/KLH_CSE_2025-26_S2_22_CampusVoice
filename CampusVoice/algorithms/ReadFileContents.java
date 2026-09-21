package algorithms;

import java.io.*;
import java.util.*;

public class ReadFileContents {

    // Get all .txt files from corpus folder
    public List<File> getCorpusFiles(String folderPath) {

        List<File> files = new ArrayList<>();

        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            return files;
        }

        File[] allFiles = folder.listFiles();

        if (allFiles != null) {

            for (File file : allFiles) {

                if (file.isFile()
                        && file.getName().toLowerCase().endsWith(".txt")) {

                    files.add(file);
                }
            }
        }

        // Keep Feedback01, Feedback02, ... in order
        Collections.sort(files);

        return files;
    }

    // Read complete file
    public String readFile(File file) {

        StringBuilder content = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {

            String line;

            while ((line = br.readLine()) != null) {

                content.append(line).append(" ");
            }

        } catch (IOException e) {

            System.out.println("Error reading: " + file.getName());
        }

        return content.toString();
    }
}