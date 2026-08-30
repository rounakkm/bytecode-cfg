package com.bytecodecfg.reporter;

import java.util.List;


public interface Reporter {

    void report(List<String> violations);
}