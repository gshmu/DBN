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

    public TnsName(
            String name,
            String protocol,
            String host,
            String port,
            String server,
            String sid,
            String serviceName,
            String globalName) {
        this.name = name;
        this.protocol = protocol;
        this.host = host;
        this.port = port;
        this.server = server;
        this.sid = sid;
        this.serviceName = serviceName;
        this.globalName = globalName;
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

    public String toString() {
        return name;
    }

    @Override
    public int compareTo(@NotNull TnsName o) {
        return name.compareTo(o.name);
    }
}
