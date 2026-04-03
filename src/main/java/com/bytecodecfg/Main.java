package com.bytecodecfg;

import com.bytecodecfg.analyzer.AnalyzerEngine;

public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println(".");
            System.exit(1);
        }

        String targetPath = args[0];
        System.out.println("Starting BytecodeCFG analysis on: " + targetPath);

        AnalyzerEngine engine = new AnalyzerEngine(targetPath);
        engine.run();
    }
}