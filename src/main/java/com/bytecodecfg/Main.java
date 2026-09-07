package com.bytecodecfg;

import com.bytecodecfg.analyzer.AnalyzerEngine;
import com.bytecodecfg.config.Config;
import com.bytecodecfg.config.ConfigLoader;
import com.bytecodecfg.reporter.HtmlReporter;
import com.bytecodecfg.reporter.JsonReporter;
import com.bytecodecfg.reporter.Reporter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;


public class Main {

    private static final int EXIT_SUCCESS = 0;
    private static final int EXIT_ERROR = 1;

    public static void main(String[] args) {
        String inputPath = null;
        String outputPath = null;
        String format = "json";
        String configPath = null;

        
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
            } else if ("--format".equals(arg)) {
                if (i + 1 >= args.length) {
                    System.err.println("Error: Missing value for option " + arg);
                    printUsage();
                    System.exit(EXIT_ERROR);
                }
                format = args[++i].toLowerCase();
            } else if ("-c".equals(arg) || "--config".equals(arg)) {
                if (i + 1 >= args.length) {
                    System.err.println("Error: Missing value for option " + arg);
                    printUsage();
                    System.exit(EXIT_ERROR);
                }
                configPath = args[++i];
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

        if (!"json".equals(format) && !"html".equals(format)) {
            System.err.println("Error: Unsupported format '" + format + "'. Expected json or html.");
            printUsage();
            System.exit(EXIT_ERROR);
        }

        Config config = null;
        if (configPath != null) {
            try {
                config = ConfigLoader.loadConfig(configPath);
            } catch (Exception e) {
                System.err.println("Error loading configuration: " + e.getMessage());
                System.exit(EXIT_ERROR);
            }
        }

        Reporter reporter = "html".equals(format) ? new HtmlReporter(inputPath) : new JsonReporter();

    
        if (outputPath != null) {
            File outputFile = new File(outputPath);
            File parentDir = outputFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            PrintStream originalOut = System.out;

            try (ByteArrayOutputStream capturedOut = new ByteArrayOutputStream();
                 PrintStream captureStream = new PrintStream(capturedOut, true, StandardCharsets.UTF_8);
                 PrintStream fileOut = new PrintStream(new FileOutputStream(outputFile), true, StandardCharsets.UTF_8)) {
                System.setOut(captureStream);
                new AnalyzerEngine(inputPath, reporter, config).run();
                fileOut.print(extractReport(capturedOut.toString(StandardCharsets.UTF_8), format));
            } catch (Exception e) {
                System.err.println("Error: Failed to write report to file " + outputPath + ": " + e.getMessage());
                System.exit(EXIT_ERROR);
            } finally {
                System.setOut(originalOut);
            }
        } else {
            System.out.println("Starting BytecodeCFG analysis on: " + inputPath);
            try {
                new AnalyzerEngine(inputPath, reporter, config).run();
            } catch (Exception e) {
                System.err.println("Error during analysis: " + e.getMessage());
                System.exit(EXIT_ERROR);
            }
        }

        System.exit(EXIT_SUCCESS);
    }

    private static String extractReport(String capturedOutput, String format) {
        String marker = "html".equals(format) ? "<!doctype html>" : "{";
        int reportStart = capturedOutput.indexOf(marker);
        if (reportStart < 0) {
            throw new IllegalStateException("Reporter did not produce " + format + " output.");
        }
        return capturedOutput.substring(reportStart);
    }

  
    private static void printUsage() {
        System.err.println("Usage: java -jar bytecode-cfg-runner.jar --input <path> [--output <path>] [--format json|html] [--config <path>]");
        System.err.println("Options:");
        System.err.println("  -i, --input <path>   Path to the Java source file or directory to analyze (required)");
        System.err.println("  -o, --output <path>  Path to save the generated report (optional, default: stdout)");
        System.err.println("      --format <type>  Report format: json (default) or html");
        System.err.println("  -c, --config <path>  Path to YAML configuration file (optional)");
        System.err.println("  -h, --help           Show this help message and exit");
    }
}
