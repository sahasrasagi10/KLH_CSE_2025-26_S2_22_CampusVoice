package algorithms;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ReadFileContents {

    public List<File> getCorpusFiles(String corpusPath) {
        File folder = new File(corpusPath);

        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] fileArray = folder.listFiles((dir, name) ->
                name.toLowerCase().endsWith(".txt")
                        && name.toLowerCase().startsWith("feedback"));

        List<File> files = new ArrayList<>();

        if (fileArray != null) {
            for (File file : fileArray) {
                if (file.isFile()) {
                    files.add(file);
                }
            }
        }

        files.sort(Comparator.comparingInt(this::extractFeedbackNumber));
        return files;
    }

    public String readFile(File file) throws IOException {
        return Files.readString(file.toPath());
    }

    public void writeFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content);
    }

    private int extractFeedbackNumber(File file) {
        String name = file.getName()
                .replace("Feedback", "")
                .replace(".txt", "");

        try {
            return Integer.parseInt(name);
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
