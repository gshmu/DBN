package com.dci.intellij.dbn.connection.config.tns;


import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class TnsName implements Comparable<TnsName> {

    private final String name;
    private final String protocol;
    private final String host;
    private final String port;
    private final String server;
    private final String sid;
    private final String serviceName;
    private final String globalName;
    private final String failover;
    private final String failoverType;
    private final String failoverMethod;

    TnsName(
            String name,
            String protocol,
            String host,
            String port,
            String server,
            String sid,
            String serviceName,
            String globalName,
            String failover,
            String failoverType,
            String failoverMethod) {
        this.name = name;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.server = server;
        this.sid = sid;
        this.serviceName = serviceName;
        this.globalName = globalName;
        this.failover = failover;
        this.failoverType = failoverType;
        this.failoverMethod = failoverMethod;
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull TnsName o) {
        return name.compareTo(o.name);
    }
}
