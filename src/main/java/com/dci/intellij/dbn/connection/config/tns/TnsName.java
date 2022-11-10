package com.dci.intellij.dbn.connection.config.tns;


import org.jetbrains.annotations.NotNull;

public class TnsName implements Comparable<TnsName> {

    private String name;
    private String protocol;
    private String host;
    private String port;
    private String server;
    private String sid;
    private String serviceName;
    private String globalName;
    private String failover;
    private String failoverType;
    private String failoverMethod;

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

    public String getName() {
        return name;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getServer() {
        return server;
    }

    public String getSid() {
        return sid;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getGlobalName() {
        return globalName;
    }

    public String getFailover() {
        return failover;
    }

    public String getFailoverType() {
        return failoverType;
    }

    public String getFailoverMethod() {
        return failoverMethod;
    }

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull TnsName o) {
        return name.compareTo(o.name);
    }
}
