package com.bytecodecfg.cfg;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CfgEngineTest {

    @TempDir
    Path tempDir;

    @Test
    public void testCfgEngineWritesDotFiles() throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, "public class Sample { public int add(int a, int b) { return a + b; } }");

        Path graphDir = tempDir.resolve("graphs");
        CfgEngine engine = new CfgEngine(javaFile.toString());
        engine.run(graphDir.toFile());

        File expectedDot = graphDir.resolve("Sample_add.dot").toFile();
        assertTrue(expectedDot.exists(), "Sample_add.dot should exist");
        String dotContent = Files.readString(expectedDot.toPath());
        assertTrue(dotContent.contains("digraph Sample_add"));
    }

    @Test
    public void testCfgEngineWithRenderWhenDotNotAvailableThrows() throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, "public class Sample { public void test() {} }");

        Path graphDir = tempDir.resolve("graphs");
        CfgEngine engine = new CfgEngine(javaFile.toString(), new GraphvizRenderer("nonexistent_dot_xyz"));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                engine.run(graphDir.toFile(), "png"));
        assertTrue(ex.getMessage().contains("Graphviz 'dot' not found on PATH"));
    }

    @Test
    public void testCfgEngineWithMockSuccessfulRenderer() throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, "public class Sample { public void greet() {} }");

        Path graphDir = tempDir.resolve("graphs");
        Path scriptPath = tempDir.resolve("mock_dot.sh");
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
        scriptPath.toFile().setExecutable(true);

        GraphvizRenderer mockRenderer = new GraphvizRenderer(scriptPath.toAbsolutePath().toString()) {
            @Override
            public boolean isAvailable() {
                return true;
            }
        };

        CfgEngine engine = new CfgEngine(javaFile.toString(), mockRenderer);
        assertDoesNotThrow(() -> engine.run(graphDir.toFile(), "png"));

        File rendered = graphDir.resolve("Sample_greet.png").toFile();
        assertTrue(rendered.exists(), "Sample_greet.png should have been created by mock renderer");
    }

    @Test
    public void testCfgEngineWithFailingRendererThrows() throws IOException {
        Path javaFile = tempDir.resolve("Sample.java");
        Files.writeString(javaFile, "public class Sample { public void failMethod() {} }");

        Path graphDir = tempDir.resolve("graphs");
        Path scriptPath = tempDir.resolve("mock_failing_dot.sh");
        String scriptContent = "#!/bin/sh\necho 'render failure' >&2\nexit 1\n";
        Files.writeString(scriptPath, scriptContent);
        scriptPath.toFile().setExecutable(true);

        GraphvizRenderer mockRenderer = new GraphvizRenderer(scriptPath.toAbsolutePath().toString()) {
            @Override
            public boolean isAvailable() {
                return true;
            }
        };

        CfgEngine engine = new CfgEngine(javaFile.toString(), mockRenderer);
        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                engine.run(graphDir.toFile(), "png"));
        assertTrue(ex.getMessage().contains("failed to render"));
    }
}
