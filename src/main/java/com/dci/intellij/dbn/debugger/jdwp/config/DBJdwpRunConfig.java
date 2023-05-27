package com.dci.intellij.dbn.debugger.jdwp.config;

import com.intellij.util.Range;

import java.net.InetAddress;

public interface DBJdwpRunConfig {
    Range<Integer> getTcpPortRange();

    default InetAddress getDebuggerHostIPAddr() {
        return null;
    }
}
