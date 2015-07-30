package com.dci.intellij.dbn.debugger.jdwp.config;

import com.intellij.util.Range;

public interface DBProgramJdwpRunConfig {
    Range<Integer> getTcpPortRange();
}
