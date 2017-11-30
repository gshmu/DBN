package com.dci.intellij.dbn.connection.jdbc;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.diagnostic.Logger;

@Deprecated
public abstract class ResourceStatusMonitor<T extends Resource>{
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private long lastAccessTimestamp;

    public void keepAlive() {
        lastAccessTimestamp = System.currentTimeMillis();
    }

    public int getIdleMinutes() {
        long idleTimeMillis = System.currentTimeMillis() - lastAccessTimestamp;
        return (int) (idleTimeMillis / TimeUtil.ONE_MINUTE);
    }


}
