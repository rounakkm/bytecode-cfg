package com.bytecodecfg.analyzer;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class Parser {

    private final String targetPath;

    public Parser(String targetPath) {
        this.targetPath = targetPath;
    }


    public CompilationUnit parseFile(File file) throws FileNotFoundException {
        return StaticJavaParser.parse(file);
    }


    public List<CompilationUnit> parseDirectory(File directory) {
        List<CompilationUnit> units = new ArrayList<>();

        File[] files = directory.listFiles();
        if (files == null) return units;

        for (File file : files) {
            if (file.isDirectory()) {
                units.addAll(parseDirectory(file));  // recurse into subdirectories error 
            } else if (file.getName().endsWith(".java")) {
                try {
                    units.add(parseFile(file));
                    System.out.println("Parsed: " + file.getPath());
                } catch (FileNotFoundException e) {
                    System.err.println("Could not parse: " + file.getPath());
                }
            }
        }

        return units;
    }

    
//not detecting whether the target is a file or directory.
    public List<CompilationUnit> parse() {
        File target = new File(targetPath);
        List<CompilationUnit> units = new ArrayList<>();

        if (!target.exists()) {
            System.err.println("Error: path does not exist — " + targetPath);
            return units;
        }

        if (target.isDirectory()) {
            units.addAll(parseDirectory(target));
        } else if (target.getName().endsWith(".java")) {
            try {
                units.add(parseFile(target));
            } catch (FileNotFoundException e) {
                System.err.println("Error: file not found — " + targetPath);
            }
        } else {
            System.err.println("Error: target is not a .java file or directory.");
        }

        return units;
    }
}
