package com.bytecodecfg.cfg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GraphvizRendererTest {

    @TempDir
    Path tempDir;

    @Test
    public void testIsDotAvailableOnNonExistentExecutable() {
        assertFalse(GraphvizRenderer.isDotAvailable("nonexistent_dot_binary_xyz"));
    }

    @Test
    public void testRenderWithInvalidFormat() throws IOException {
        Path dotFile = tempDir.resolve("sample.dot");
        Files.writeString(dotFile, "digraph G {}");

        GraphvizRenderer renderer = new GraphvizRenderer("dummy");
        assertThrows(IllegalArgumentException.class, () -> renderer.render(dotFile.toFile(), "jpg"));
        assertThrows(IllegalArgumentException.class, () -> renderer.render(dotFile.toFile(), null));
        assertThrows(IllegalArgumentException.class, () -> renderer.render(dotFile.toFile(), ""));
    }

    @Test
    public void testRenderWithNonExistentDotFile() {
        GraphvizRenderer renderer = new GraphvizRenderer("dummy");
        File nonExistent = tempDir.resolve("nonexistent.dot").toFile();
        assertThrows(IllegalArgumentException.class, () -> renderer.render(nonExistent, "png"));
        assertThrows(IllegalArgumentException.class, () -> renderer.render(null, "png"));
    }

    @Test
    public void testRenderSubprocessFailureWithFailingScript() throws IOException {
        Path scriptPath = tempDir.resolve("fake_failing_dot.sh");
        String scriptContent = "#!/bin/sh\necho 'syntax error in graph' >&2\nexit 1\n";
        Files.writeString(scriptPath, scriptContent);
        File scriptFile = scriptPath.toFile();
        scriptFile.setExecutable(true);

        Path dotPath = tempDir.resolve("test.dot");
        Files.writeString(dotPath, "digraph G { bad_syntax");

        GraphvizRenderer renderer = new GraphvizRenderer(scriptFile.getAbsolutePath());
        IOException thrown = assertThrows(IOException.class, () -> renderer.render(dotPath.toFile(), "png"));

        assertTrue(thrown.getMessage().contains("exit code 1"), "Error should report exit code");
        assertTrue(thrown.getMessage().contains("syntax error in graph"), "Error should report stderr content");
    }

    @Test
    public void testRenderSuccessWithMockScript() throws IOException, InterruptedException {
        Path scriptPath = tempDir.resolve("fake_dot.sh");
        String scriptContent = "#!/bin/sh\n" +
                "while [ \"$#\" -gt 0 ]; do\n" +
                "  if [ \"$1\" = \"-o\" ]; then\n" +
                "    touch \"$2\"\n" +
                "    shift 2\n" +
                "  else\n" +
                "    shift\n" +
                "  fi\n" +
                "done\n" +
                "exit 0\n";
        Files.writeString(scriptPath, scriptContent);
        File scriptFile = scriptPath.toFile();
        scriptFile.setExecutable(true);

        Path dotPath = tempDir.resolve("test.dot");
        Files.writeString(dotPath, "digraph G { B0 -> B1; }");

        GraphvizRenderer renderer = new GraphvizRenderer(scriptFile.getAbsolutePath());
        File rendered = renderer.render(dotPath.toFile(), "png");

        assertNotNull(rendered);
        assertEquals("test.png", rendered.getName());
        assertTrue(rendered.exists(), "Rendered file should have been created by mock script");
    }
}
