package com.dci.intellij.dbn.debugger.jdwp.config;

import com.intellij.util.Range;

public interface DBJdwpRunConfig {
    Range<Integer> getTcpPortRange();
}
