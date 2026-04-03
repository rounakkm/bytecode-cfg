package com.bytecodecfg.rules;

import com.github.javaparser.ast.CompilationUnit;

import java.util.List;

public interface Rule {


    String getName();


    String getDescription();


    List<String> analyze(CompilationUnit cu);
}