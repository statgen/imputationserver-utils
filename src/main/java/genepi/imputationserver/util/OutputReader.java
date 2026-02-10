package genepi.imputationserver.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class OutputReader {

    private final List<String> lines = new ArrayList<>();

    public OutputReader(String filename) throws IOException {
        Files.lines(Paths.get(filename)).forEach(lines::add);
    }

    public boolean hasInMemory(String content) {
        for (String line : lines) {
            if (line.contains(content)) {
                return true;
            }
        }
        return false;
    }

    public void view() {
        for (String line : lines) {
            System.out.println(line);
        }
    }
}
