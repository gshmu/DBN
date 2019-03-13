package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.LoggerFactory;
import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.connection.transaction.ConnectionSavepoint;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public abstract class CancellableDatabaseCall<T> implements Callable<T> {
    private static final Logger LOGGER = LoggerFactory.createLogger();

    private long startTimestamp = System.currentTimeMillis();
    private ThreadInfo invoker = ThreadMonitor.current();
    private transient ProgressIndicator progressIndicator = ProgressMonitor.getProgressIndicator();


    private DBNConnection connection;
    private int timeout;
    private TimeUnit timeUnit;
    private boolean createSavepoint;

    private transient Future<T> future;
    private transient boolean cancelled = false;
    private transient boolean cancelRequested = false;
    private Timer cancelCheckTimer;

    protected CancellableDatabaseCall(@Nullable ConnectionHandler connectionHandler, @Nullable DBNConnection connection, int timeout, TimeUnit timeUnit) {
        this.connection = connection;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        createSavepoint = !DatabaseFeature.CONNECTION_ERROR_RECOVERY.isSupported(connectionHandler);
    }

    public void requestCancellation() {
        cancelRequested = true;
    }

    @Override
    public T call() throws Exception {
        return ThreadMonitor.call(
                invoker,
                ThreadProperty.CANCELABLE_PROCESS,
                null,
                () -> {
                    if (createSavepoint) {
                        AtomicReference<Exception> innerException = new AtomicReference<>();
                        T result = ConnectionSavepoint.call(connection, () -> {
                            try {
                                return CancellableDatabaseCall.this.execute();
                            } catch (SQLException e) {
                                throw e;
                            } catch (Exception e) {
                                innerException.set(e);
                                return null;
                            }
                        });

                        Exception exception = innerException.get();
                        if (exception != null) {
                            throw exception;
                        }
                        return result;

                    } else {
                        return execute();
                    }
                });
    }

    public abstract T execute() throws Exception;

    public abstract void cancel() throws Exception;

    public boolean isCancelRequested() {
        return cancelRequested || (progressIndicator != null && progressIndicator.isCanceled());
    }


    public final T start() throws SQLException {
        try {
            cancelCheckTimer = new Timer("DBN - Execution Cancel Watcher");
            TimerTask cancelCheckTask = new TimerTask() {
                @Override
                public void run() {
                    if (!cancelled && isCancelRequested()) {
                        cancelled = true;
                        if (future != null) future.cancel(true);
                        try {
                            CancellableDatabaseCall.this.cancel();
                        } catch (Exception e) {
                            LOGGER.warn("Error cancelling operation", e);
                        }
                        cancelCheckTimer.cancel();
                    } else if (progressIndicator != null && timeout > 0) {
                        String text = progressIndicator.getText();
                        int index = text.indexOf(" (timing out in ");
                        if (index > -1) {
                            text = text.substring(0, index);
                        }

                        long runningForSeconds = TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTimestamp);
                        long timeoutSeconds = timeUnit.toSeconds(timeout);
                        long timingOutIn = timeoutSeconds - runningForSeconds;
                        if (timingOutIn < 60)
                            text = text + " (timing out in " + timingOutIn + " seconds) "; else
                            text = text + " (timing out in " + TimeUnit.SECONDS.toMinutes(timingOutIn) + " minutes) ";

                        progressIndicator.setText(text);
                    }
                }
            };

            cancelCheckTimer.schedule(cancelCheckTask, 100, 100);

            T result = null;
            try {
                ExecutorService executorService = ThreadFactory.cancellableExecutor();
                future = executorService.submit(this);
                result = timeout == 0 ?  future.get() : future.get(timeout, timeUnit);
            } finally {
                progressIndicator = null;
                future = null;
                if (cancelCheckTimer != null) {
                    cancelCheckTimer.cancel();
                }
            }

            return result;
        } catch (CancellationException e) {
            throw new ProcessCanceledException();
        } catch (InterruptedException e) {
            throw new ProcessCanceledException();
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof SQLTimeoutException) {
                handleTimeout();
            } else {
                handleException(cause);
            }

        } catch (TimeoutException e)  {
            handleTimeout();
        }
        return null;
    }

    public void handleTimeout() throws SQLTimeoutException {
        if (cancelled) {
            throw new ProcessCanceledException();
        }
        try {
            cancel();
        } catch (Exception ce) {
            LOGGER.warn("Error cancelling operation", ce);
        }
        throw new SQLTimeoutException("Operation has timed out (" + timeout + "s). Check timeout settings");
    }

    public void handleException(Throwable e) throws SQLException{
        if (e instanceof SQLException) {
            throw (SQLException) e;
        } else {
            throw new SQLException(e.getMessage(), e);
        }
    }

    public void cancelSilently() {
        try {
            cancel();
        } catch (Exception e) {
            LOGGER.error("Failed to cancel database call", e);
        }
    }

}
