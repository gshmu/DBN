package com.dci.intellij.dbn.connection.ssh;

import java.io.IOException;
import java.net.ServerSocket;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class SshTunnelConnector {
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;

    private String localHost = "localhost";
    private int localPort;

    private String remoteHost;
    private int remotePort;

    private Session session;

    public SshTunnelConnector(String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String remoteHost, int remotePort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;

        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
    }

    public Session createTunnel() throws Exception {
        try {
            ServerSocket serverSocket = new ServerSocket(0);
            try {
                localPort = serverSocket.getLocalPort();
            } finally {
                serverSocket.close();
            }
        } catch (IOException e) {
            throw new JSchException("Can\'t find a free port", e);
        }

        JSch jsch = new JSch();
        session = jsch.getSession(proxyUser, proxyHost, proxyPort);
        session.setPassword(proxyPassword);
        session.setConfig("StrictHostKeyChecking", "no");
        session.connect();

        session.setPortForwardingL(localPort, remoteHost, remotePort);
        return session;
    }

    public boolean isConnected() {
        return session != null && session.isConnected();
    }

    public String getLocalHost() {
        return localHost;
    }

    public int getLocalPort() {
        return localPort;
    }
}
