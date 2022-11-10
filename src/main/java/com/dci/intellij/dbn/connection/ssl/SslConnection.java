package com.dci.intellij.dbn.connection.ssl;

import java.io.File;

public class SslConnection {
    private File certificateAuthorityFile;
    private File clientCertificateFile;
    private File clientKeyFile;

    public SslConnection(File certificateAuthorityFile, File clientCertificateFile, File clientKeyFile) {
        this.certificateAuthorityFile = certificateAuthorityFile;
        this.clientCertificateFile = clientCertificateFile;
        this.clientKeyFile = clientKeyFile;
    }

    public File getCertificateAuthorityFile() {
        return certificateAuthorityFile;
    }

    public File getClientCertificateFile() {
        return clientCertificateFile;
    }

    public File getClientKeyFile() {
        return clientKeyFile;
    }

    public boolean isConnected() {
        return false;
    }

    public void connect() {

    }
}
