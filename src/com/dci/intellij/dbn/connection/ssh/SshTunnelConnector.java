package com.dci.intellij.dbn.connection.ssh;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.net.ServerSocket;

public class SshTunnelConnector {
    private String proxyHost;
    private int proxyPort;
    private String proxyUser;
    private String proxyPassword;
    private SshAuthType authType;
    private String keyFile;
    private String keyPassphrase;

    private String localHost = "localhost";
    private int localPort;

    private String remoteHost;
    private int remotePort;

    private Session session;

    public SshTunnelConnector(String proxyHost, int proxyPort, String proxyUser, SshAuthType authType, String keyFile, String keyPassphrase, String proxyPassword, String remoteHost, int remotePort) {
        this.proxyHost = proxyHost;
        this.proxyPort = proxyPort;
        this.proxyUser = proxyUser;
        this.proxyPassword = proxyPassword;

        this.authType = authType;
        this.keyFile = keyFile;
        this.keyPassphrase = keyPassphrase;

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

        if(authType == SshAuthType.KEY_PAIR) {
            if (StringUtils.isNotEmpty(keyPassphrase)) {
                jsch.addIdentity(keyFile, keyPassphrase);
            }
        } else {
            session.setPassword(proxyPassword);
        }

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
