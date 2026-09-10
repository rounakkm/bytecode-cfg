package com.bytecodecfg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class MainTest {

    @TempDir
    Path tempDir;

    private record RunResult(int exitCode, String stdout, String stderr) {}

    private RunResult runCli(String... args) {
        ByteArrayOutputStream outBaos = new ByteArrayOutputStream();
        ByteArrayOutputStream errBaos = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(outBaos, true, StandardCharsets.UTF_8);
        PrintStream err = new PrintStream(errBaos, true, StandardCharsets.UTF_8);

        int code = Main.run(args, out, err);
        return new RunResult(code,
                outBaos.toString(StandardCharsets.UTF_8),
                errBaos.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testHelpOptionDisplaysRender() {
        RunResult result = runCli("--help");
        assertEquals(Main.EXIT_SUCCESS, result.exitCode());
        assertTrue(result.stdout().contains("--render <type>"), "Help text should document --render");
    }

    @Test
    public void testRenderWithoutGraphFails() {
        RunResult result = runCli("--render", "png");
        assertEquals(Main.EXIT_ERROR, result.exitCode());
        assertTrue(result.stderr().contains("Error: --render requires --graph <dir> (rendering requires DOT files to exist)"));
    }

    @Test
    public void testRenderWithoutGraphWhenInputProvidedFails() throws Exception {
        Path dummyJava = tempDir.resolve("Test.java");
        Files.writeString(dummyJava, "public class Test {}");

        RunResult result = runCli("--input", dummyJava.toString(), "--render", "png");
        assertEquals(Main.EXIT_ERROR, result.exitCode());
        assertTrue(result.stderr().contains("Error: --render requires --graph <dir> (rendering requires DOT files to exist)"));
    }

    @Test
    public void testRenderWithUnsupportedFormatFails() {
        RunResult result = runCli("--graph", tempDir.toString(), "--render", "bmp");
        assertEquals(Main.EXIT_ERROR, result.exitCode());
        assertTrue(result.stderr().contains("Error: Unsupported render format 'bmp'. Expected png or svg."));
    }

    @Test
    public void testRenderMissingValueFails() {
        RunResult result = runCli("--graph", tempDir.toString(), "--render");
        assertEquals(Main.EXIT_ERROR, result.exitCode());
        assertTrue(result.stderr().contains("Error: Missing value for option --render"));
    }

    @Test
    public void testRenderWhenDotNotAvailableFailsWithClearMessage() throws Exception {
        Path dummyJava = tempDir.resolve("Test.java");
        Files.writeString(dummyJava, "public class Test { void foo() {} }");

        RunResult result = runCli("--input", dummyJava.toString(), "--graph", tempDir.toString(), "--render", "png");
        assertEquals(Main.EXIT_ERROR, result.exitCode());
        assertTrue(result.stderr().contains("Error: Graphviz 'dot' not found on PATH — install it to use --render"),
                "Error output was: " + result.stderr());
    }
}
