package com.bytecodecfg;

import com.bytecodecfg.analyzer.AnalyzerEngine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;


public class Main {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_ERROR = 1;

    public static void main(String[] args) {
        String inputPath = null;
        String outputPath = null;

        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("-h".equals(arg) || "--help".equals(arg)) {
                printUsage();
                System.exit(EXIT_SUCCESS);
            } else if ("-i".equals(arg) || "--input".equals(arg)) {
                if (i + 1 >= args.length) {
                    System.err.println("Error: Missing value for required option " + arg);
                    printUsage();
                    System.exit(EXIT_ERROR);
                }
                inputPath = args[++i];
            } else if ("-o".equals(arg) || "--output".equals(arg)) {
                if (i + 1 >= args.length) {
                    System.err.println("Error: Missing value for option " + arg);
                    printUsage();
                    System.exit(EXIT_ERROR);
                }
                outputPath = args[++i];
            } else if (arg.startsWith("-")) {
                System.err.println("Error: Unknown option '" + arg + "'");
                printUsage();
                System.exit(EXIT_ERROR);
            } else {
                if (inputPath == null) {
                    inputPath = arg;
                } else {
                    System.err.println("Error: Unexpected positional argument '" + arg + "'");
                    printUsage();
                    System.exit(EXIT_ERROR);
                }
            }
        }

        if (inputPath == null || inputPath.trim().isEmpty()) {
            System.err.println("Error: Missing required argument --input <path>");
            printUsage();
            System.exit(EXIT_ERROR);
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            System.err.println("Error: Input path does not exist — " + inputPath);
            System.exit(EXIT_ERROR);
        }

    
        if (outputPath != null) {
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            PrintStream originalOut = System.out;
            try (PrintStream fileOut = new PrintStream(new FileOutputStream(outputFile))) {
                System.setOut(fileOut);
                AnalyzerEngine engine = new AnalyzerEngine(inputPath);
                engine.run();
            } catch (Exception e) {
                System.err.println("Error: Failed to write report to file " + outputPath + ": " + e.getMessage());
                System.exit(EXIT_ERROR);
            } finally {
                System.setOut(originalOut);
            }
        } else {
            System.out.println("Starting BytecodeCFG analysis on: " + inputPath);
            try {
                AnalyzerEngine engine = new AnalyzerEngine(inputPath);
                engine.run();
            } catch (Exception e) {
                System.err.println("Error during analysis: " + e.getMessage());
                System.exit(EXIT_ERROR);
            }
        }

        System.exit(EXIT_SUCCESS);
    }

  
    private static void printUsage() {
        System.err.println("Usage: java -jar bytecode-cfg-runner.jar --input <path> [--output <path>]");
        System.err.println("Options:");
        System.err.println("  -i, --input <path>   Path to the Java source file or directory to analyze (required)");
        System.err.println("  -o, --output <path>  Path to save the generated JSON report (optional, default: stdout)");
        System.err.println("  -h, --help           Show this help message and exit");
    }
}