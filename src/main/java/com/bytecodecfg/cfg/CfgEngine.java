package com.bytecodecfg.cfg;

import com.bytecodecfg.analyzer.Parser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CfgEngine {

    private final String targetPath;

    public CfgEngine(String targetPath) {
        this.targetPath = targetPath;
    }


    public void run(File outputDir) {
     
        if (!outputDir.exists() && !outputDir.mkdirs()) {
            System.err.println("CFG Warning: could not create output directory: " + outputDir);
            return;
        }

        Parser parser = new Parser(targetPath);
        List<CompilationUnit> compilationUnits = parser.parse();

        if (compilationUnits.isEmpty()) {
            System.out.println("CFG: No Java files found to analyze.");
            return;
        }

        CfgBuilder builder  = new CfgBuilder();
        DotExporter exporter = new DotExporter();

        int methodCount = 0;
        int fileCount   = 0;

        for (CompilationUnit cu : compilationUnits) {
            
            List<MethodWithClass> methods = collectMethods(cu);

     
            Map<String, Integer> nameFrequency = countNameFrequency(methods);

            for (MethodWithClass mwc : methods) {
                String methodId = buildMethodId(mwc, nameFrequency);
                CfgResult result = builder.build(mwc.method, methodId);

                String dot = exporter.export(result);
                File outFile = new File(outputDir, methodId + ".dot");

                try (PrintStream ps = new PrintStream(
                        new FileOutputStream(outFile), true, StandardCharsets.UTF_8)) {
                    ps.print(dot);
                    System.out.println("CFG written: " + outFile.getPath());
                    methodCount++;
                } catch (IOException e) {
                    System.err.println("CFG Error: could not write " + outFile + ": " + e.getMessage());
                }
            }
            fileCount++;
        }

        System.out.println("CFG generation complete: "
                + methodCount + " method graph(s) written from "
                + fileCount + " file(s) to " + outputDir.getPath());
    }

    
    private static class MethodWithClass {
        final String className;
        final MethodDeclaration method;

        MethodWithClass(String className, MethodDeclaration method) {
            this.className = className;
            this.method    = method;
        }
    }


    private List<MethodWithClass> collectMethods(CompilationUnit cu) {
        List<MethodWithClass> result = new ArrayList<>();

        for (MethodDeclaration method : cu.findAll(MethodDeclaration.class)) {
            
            String className = method.findAncestor(ClassOrInterfaceDeclaration.class)
                    .map(ClassOrInterfaceDeclaration::getNameAsString)
                    .orElseGet(() -> cu.getPrimaryTypeName().orElse("UnknownClass"));

            result.add(new MethodWithClass(className, method));
        }

        return result;
    }


    private Map<String, Integer> countNameFrequency(List<MethodWithClass> methods) {
        Map<String, Integer> freq = new HashMap<>();
        for (MethodWithClass mwc : methods) {
            String key = mwc.className + "_" + mwc.method.getNameAsString();
            freq.merge(key, 1, Integer::sum);
        }
        return freq;
    }


    private String buildMethodId(MethodWithClass mwc, Map<String, Integer> nameFrequency) {
        String base = mwc.className + "_" + mwc.method.getNameAsString();
        if (nameFrequency.getOrDefault(base, 1) > 1) {
            int paramCount = mwc.method.getParameters().size();
            return base + "_" + paramCount + "params";
        }
        return base;
    }
}
