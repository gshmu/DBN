package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.load.ProgressMonitor;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.Savepoints;
import com.dci.intellij.dbn.connection.jdbc.DBNConnection;
import com.dci.intellij.dbn.database.DatabaseFeature;
import com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskCancelledException;
import com.intellij.openapi.progress.ProgressIndicator;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.dci.intellij.dbn.common.exception.Exceptions.causeOf;
import static com.dci.intellij.dbn.common.exception.Exceptions.toSqlException;

@Slf4j
public abstract class CancellableDatabaseCall<T> implements Callable<T> {

    private final long startTimestamp = System.currentTimeMillis();
    private final ThreadInfo invoker = ThreadMonitor.current();

    private final DBNConnection connection;
    private final int timeout;
    private final TimeUnit timeUnit;
    private final boolean createSavepoint;

    private transient Future<T> future;
    private transient boolean cancelled = false;
    private transient boolean cancelRequested = false;
    private Timer cancelCheckTimer;

    protected CancellableDatabaseCall(@Nullable ConnectionHandler connection, @Nullable DBNConnection conn, int timeout, TimeUnit timeUnit) {
        this.connection = conn;
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        createSavepoint = !DatabaseFeature.CONNECTION_ERROR_RECOVERY.isSupported(connection);
    }

    public void requestCancellation() {
        cancelRequested = true;
    }

    @Override
    public T call() throws Exception {
        return ThreadMonitor.surround(
                invoker.getProject(),
                invoker,
                ThreadProperty.CANCELABLE,
                null,
                () -> {
                    if (createSavepoint) {
                        AtomicReference<Exception> innerException = new AtomicReference<>();
                        T result = Savepoints.call(connection, () -> {
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
        return cancelRequested || ProgressMonitor.isCancelled();
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
                            log.warn("Error cancelling operation", e);
                        }
                        cancelCheckTimer.cancel();
                    } else {
                        ProgressIndicator progress = ProgressMonitor.getProgressIndicator();
                        if (progress != null && timeout > 0) {
                            String text = progress.getText();
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


                            progress.setText(text);
                        }
                    }
                }
            };

            cancelCheckTimer.schedule(cancelCheckTask, 100, 100);

            try {
                ExecutorService executorService = Threads.cancellableExecutor();
                future = executorService.submit(this);
                return timeout == 0 ?  future.get() : future.get(timeout, timeUnit);
            } finally {
                future = null;
                if (cancelCheckTimer != null) {
                    cancelCheckTimer.cancel();
                }
            }

        } catch (CancellationException | InterruptedException e) {
            throw InterfaceTaskCancelledException.INSTANCE;

        } catch (ExecutionException e) {
            Throwable cause = causeOf(e);
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
            throw InterfaceTaskCancelledException.INSTANCE;
        }
        try {
            cancel();
        } catch (Exception ce) {
            log.warn("Error cancelling operation", ce);
        }
        throw new SQLTimeoutException("Operation has timed out (" + timeout + "s). Check timeout settings");
    }

    public void handleException(Throwable e) throws SQLException{
        throw toSqlException(e);
    }

    public void cancelSilently() {
        try {
            cancel();
        } catch (Exception e) {
            log.error("Failed to cancel database call", e);
        }
    }

}
