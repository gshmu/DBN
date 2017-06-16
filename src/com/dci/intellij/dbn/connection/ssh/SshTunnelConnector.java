package com.dci.intellij.dbn.connection.ssh;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

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
        JSch.setConfig("kex", "diffie-hellman-group1-sha1,diffie-hellman-group14-sha1,diffie-hellman-group-exchange-sha1,diffie-hellman-group-exchange-sha256");
        session = jsch.getSession(proxyUser, proxyHost, proxyPort);

        if(authType == SshAuthType.KEY_PAIR) {
            jsch.addIdentity(keyFile, CommonUtil.nvl(keyPassphrase, ""));;
        } else {
            session.setPassword(proxyPassword);
        }

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        config.put("TCPKeepAlive", "yes");
        session.setConfig(config);
        session.setServerAliveInterval((int) TimeUnit.MINUTES.toMillis(2L));
        session.setServerAliveCountMax(1000);
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
