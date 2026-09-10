package com.bytecodecfg.cfg;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class GraphvizRenderer {

    private final String dotExecutable;

    public GraphvizRenderer() {
        this("dot");
    }

    public GraphvizRenderer(String dotExecutable) {
        this.dotExecutable = dotExecutable != null ? dotExecutable : "dot";
    }

    public boolean isAvailable() {
        return isDotAvailable(dotExecutable);
    }

    public static boolean isDotAvailable() {
        return isDotAvailable("dot");
    }

    public static boolean isDotAvailable(String dotExecutable) {
        try {
            Process process = new ProcessBuilder(dotExecutable, "-V")
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                    .redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
            boolean finished = process.waitFor(3, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public File render(File dotFile, String format) throws IOException, InterruptedException {
        if (dotFile == null || !dotFile.exists()) {
            throw new IllegalArgumentException("DOT file does not exist: " + dotFile);
        }
        if (format == null || (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("svg"))) {
            throw new IllegalArgumentException("Unsupported render format: " + format + ". Expected png or svg.");
        }

        String lowerFormat = format.toLowerCase();
        String dotFileName = dotFile.getName();
        String baseName = dotFileName.endsWith(".dot")
                ? dotFileName.substring(0, dotFileName.length() - 4)
                : dotFileName;
        File outputFile = new File(dotFile.getParentFile(), baseName + "." + lowerFormat);

        List<String> command = List.of(
                dotExecutable,
                "-T" + lowerFormat,
                dotFile.getAbsolutePath(),
                "-o",
                outputFile.getAbsolutePath()
        );

        ProcessBuilder pb = new ProcessBuilder(command);
        pb.redirectErrorStream(true);
        Process process;
        try {
            process = pb.start();
        } catch (IOException e) {
            throw new IOException("Failed to execute '" + dotExecutable + "': " + e.getMessage(), e);
        }

        String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8).trim();

        boolean finished = process.waitFor(30, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            throw new IOException("Graphviz 'dot' process timed out while rendering " + dotFile.getName());
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            String errorMsg = output.isEmpty() ? "Process exited with code " + exitCode : output;
            throw new IOException("dot failed with exit code " + exitCode + ": " + errorMsg);
        }

        return outputFile;
    }
}
