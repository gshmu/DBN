package com.dci.intellij.dbn.connection.jdbc;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.thread.SimpleBackgroundTask;
import com.dci.intellij.dbn.common.thread.SimpleTimeoutTask;
import com.dci.intellij.dbn.common.util.InitializationInfo;
import com.dci.intellij.dbn.common.util.TimeUtil;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;

abstract class DBNResource {
    private static final Logger LOGGER = LoggerFactory.createLogger();
    protected InitializationInfo initInfo = new InitializationInfo();

    private ResourceStatus<Closeable> CLOSED;
    private ResourceStatus<Invalidable> INVALID;
    private ResourceStatus<Cancellable> CANCELLED;


    DBNResource() {
        if (this instanceof Closeable) {
            final Closeable closeable = (Closeable) this;
            CLOSED = new ResourceStatus<Closeable>() {
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
            CANCELLED = new ResourceStatus<Cancellable>() {
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

        if (this instanceof Invalidable) {
            final Invalidable invalidable = (Invalidable) this;
            INVALID = new ResourceStatus<Invalidable>(TimeUtil.THIRTY_SECONDS) {
                @Override
                protected void attemptInner() throws SQLException {
                    invalidable.invalidateInner();
                }

                @Override
                protected boolean checkInner() throws SQLException {
                    return invalidable.isInvalidInner();
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

    public boolean isValid() {
        return isValid(2);
    }

    public boolean isValid(int timeout) {
        return !isInvalid();
    }

    public boolean isInvalid() {
        return INVALID.check();
    }

    public void invalidate() {
        INVALID.attempt();
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
