package com.bytecodecfg;

import com.bytecodecfg.analyzer.AnalyzerEngine;
import com.bytecodecfg.cfg.CfgEngine;
import com.bytecodecfg.cfg.GraphvizRenderer;
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

    public static final int EXIT_SUCCESS = 0;
    public static final int EXIT_ERROR = 1;

    public static void main(String[] args) {
        int exitCode = run(args, System.out, System.err);
        System.exit(exitCode);
    }

    public static int run(String[] args) {
        return run(args, System.out, System.err);
    }

    public static int run(String[] args, PrintStream out, PrintStream err) {
        String inputPath = null;
        String outputPath = null;
        String format = "json";
        String configPath = null;
        String graphOutputDir = null;  
        String renderFormat = null;
        
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];

            if ("-h".equals(arg) || "--help".equals(arg)) {
                printUsage(out);
                return EXIT_SUCCESS;
            } else if ("-i".equals(arg) || "--input".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Error: Missing value for required option " + arg);
                    printUsage(err);
                    return EXIT_ERROR;
                }
                inputPath = args[++i];
            } else if ("-o".equals(arg) || "--output".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Error: Missing value for option " + arg);
                    printUsage(err);
                    return EXIT_ERROR;
                }
                outputPath = args[++i];
            } else if ("--format".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Error: Missing value for option " + arg);
                    printUsage(err);
                    return EXIT_ERROR;
                }
                format = args[++i].toLowerCase();
            } else if ("-c".equals(arg) || "--config".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Error: Missing value for option " + arg);
                    printUsage(err);
                    return EXIT_ERROR;
                }
                configPath = args[++i];
            } else if ("--graph".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Error: Missing value for option " + arg);
                    printUsage(err);
                    return EXIT_ERROR;
                }
                graphOutputDir = args[++i];
            } else if ("--render".equals(arg)) {
                if (i + 1 >= args.length) {
                    err.println("Error: Missing value for option " + arg);
                    printUsage(err);
                    return EXIT_ERROR;
                }
                renderFormat = args[++i].toLowerCase();
            } else if (arg.startsWith("-")) {
                err.println("Error: Unknown option '" + arg + "'");
                printUsage(err);
                return EXIT_ERROR;
            } else {
                if (inputPath == null) {
                    inputPath = arg;
                } else {
                    err.println("Error: Unexpected positional argument '" + arg + "'");
                    printUsage(err);
                    return EXIT_ERROR;
                }
            }
        }

        if (renderFormat != null && graphOutputDir == null) {
            err.println("Error: --render requires --graph <dir> (rendering requires DOT files to exist)");
            printUsage(err);
            return EXIT_ERROR;
        }

        if (renderFormat != null && !"png".equals(renderFormat) && !"svg".equals(renderFormat)) {
            err.println("Error: Unsupported render format '" + renderFormat + "'. Expected png or svg.");
            printUsage(err);
            return EXIT_ERROR;
        }

        if (inputPath == null || inputPath.trim().isEmpty()) {
            err.println("Error: Missing required argument --input <path>");
            printUsage(err);
            return EXIT_ERROR;
        }

        File inputFile = new File(inputPath);
        if (!inputFile.exists()) {
            err.println("Error: Input path does not exist — " + inputPath);
            return EXIT_ERROR;
        }

        if (!"json".equals(format) && !"html".equals(format)) {
            err.println("Error: Unsupported format '" + format + "'. Expected json or html.");
            printUsage(err);
            return EXIT_ERROR;
        }

        if (renderFormat != null && !GraphvizRenderer.isDotAvailable()) {
            err.println("Error: Graphviz 'dot' not found on PATH — install it to use --render");
            return EXIT_ERROR;
        }

        Config config = null;
        if (configPath != null) {
            try {
                config = ConfigLoader.loadConfig(configPath);
            } catch (Exception e) {
                err.println("Error loading configuration: " + e.getMessage());
                return EXIT_ERROR;
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
                err.println("Error: Failed to write report to file " + outputPath + ": " + e.getMessage());
                return EXIT_ERROR;
            } finally {
                System.setOut(originalOut);
            }
        } else {
            out.println("Starting BytecodeCFG analysis on: " + inputPath);
            try {
                new AnalyzerEngine(inputPath, reporter, config).run();
            } catch (Exception e) {
                err.println("Error during analysis: " + e.getMessage());
                return EXIT_ERROR;
            }
        }


        if (graphOutputDir != null) {
            File graphDir = new File(graphOutputDir);
            try {
                new CfgEngine(inputPath).run(graphDir, renderFormat);
            } catch (Exception e) {
                err.println("Error during CFG generation: " + e.getMessage());
                return EXIT_ERROR;
            }
        }

        return EXIT_SUCCESS;
    }

    private static String extractReport(String capturedOutput, String format) {
        String marker = "html".equals(format) ? "<!doctype html>" : "{";
        int reportStart = capturedOutput.indexOf(marker);
        if (reportStart < 0) {
            throw new IllegalStateException("Reporter did not produce " + format + " output.");
        }
        return capturedOutput.substring(reportStart);
    }

  
    private static void printUsage(PrintStream ps) {
        ps.println("Usage: java -jar bytecode-cfg-runner.jar --input <path> [--output <path>] [--format json|html] [--config <path>] [--graph <dir>] [--render png|svg]");
        ps.println("Options:");
        ps.println("  -i, --input <path>   Path to the Java source file or directory to analyze (required)");
        ps.println("  -o, --output <path>  Path to save the generated report (optional, default: stdout)");
        ps.println("      --format <type>  Report format: json (default) or html");
        ps.println("  -c, --config <path>  Path to YAML configuration file (optional)");
        ps.println("      --graph <dir>    Output directory for Graphviz DOT files (one per method; additive with report)");
        ps.println("      --render <type>  Render DOT files to images (png or svg; requires --graph and system 'dot')");
        ps.println("  -h, --help           Show this help message and exit");
    }

    private static void printUsage() {
        printUsage(System.err);
    }
}
