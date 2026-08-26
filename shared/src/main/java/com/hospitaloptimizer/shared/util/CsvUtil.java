package com.hospitaloptimizer.shared.util;

import java.io.*;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

/** Minimal CSV reader/writer. No external dependency -- built for this project. */
public final class CsvUtil {

    private CsvUtil() {}

    /** Reads a CSV (classpath resource, falling back to filesystem path). Returns rows including header row at index 0. */
    public static List<String[]> read(String pathOrResource) {
        List<String[]> rows = new ArrayList<>();
        try (BufferedReader br = openReader(pathOrResource)) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                rows.add(splitCsvLine(line));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to read CSV: " + pathOrResource, e);
        }
        return rows;
    }

    private static BufferedReader openReader(String pathOrResource) throws IOException {
        InputStream in = CsvUtil.class.getClassLoader().getResourceAsStream(pathOrResource);
        if (in != null) {
            return new BufferedReader(new InputStreamReader(in));
        }
        return Files.newBufferedReader(Paths.get(pathOrResource));
    }

    private static String[] splitCsvLine(String line) {
        // simple split; our generated data has no embedded commas/quotes
        return line.split(",", -1);
    }

    public static void appendRow(String filePath, String... fields) {
        try {
            File f = new File(filePath);
            f.getParentFile().mkdirs();
            boolean writeHeader = !f.exists();
            try (BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))) {
                bw.write(String.join(",", fields));
                bw.newLine();
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write CSV: " + filePath, e);
        }
    }
}
