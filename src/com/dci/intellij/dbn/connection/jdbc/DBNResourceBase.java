package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutTask;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

public class DBNResourceBase {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private ResourceStatusAdapter<Closeable> CLOSED;
    private ResourceStatusAdapter<Cancellable> VALID;
    private ResourceStatusAdapter<Cancellable> CANCELLED;

    public DBNResourceBase() {
        if (this instanceof Closeable) {
            final Closeable closeable = (Closeable) this;
            CLOSED = new ResourceStatusAdapter<Closeable>() {
                @Override
                protected void attemptInner() throws SQLException {
                    close(closeable, true);
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return closeable.isClosedInner();
                }
            };
        }

        if (this instanceof Cancellable) {
            final Cancellable cancellable = (Cancellable) this;
            CANCELLED = new ResourceStatusAdapter<Cancellable>() {
                @Override
                protected void attemptInner() throws SQLException {
                    cancellable.cancelInner();
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return cancellable.isCancelledInner();
                }
            };
        }
    }

    public boolean isClosed() {
        return CLOSED.check();
    }

    public void close() {
        CLOSED.attempt();
    }

    public boolean isCancelled() {
        return CANCELLED.check();
    }

    public void cancel() {
        CANCELLED.attempt();
    }

    public static void close(final Closeable closeable, boolean background) {
        if (closeable != null) {
            if (background || ApplicationManager.getApplication().isDispatchThread()) {
                new SimpleBackgroundTask("close resource") {
                    @Override
                    protected void execute() {
                        close(closeable, false);
                    }
                }.start();
            } else {
                new SimpleTimeoutTask(10, TimeUnit.SECONDS) {
                    @Override
                    public void run() {
                        try {
                            closeable.closeInner();
                        } catch (Throwable e) {
                            LOGGER.warn("Error closing resource: " + e.getMessage());
                        }
                    }
                }.start();
            }
        }
    }
}
