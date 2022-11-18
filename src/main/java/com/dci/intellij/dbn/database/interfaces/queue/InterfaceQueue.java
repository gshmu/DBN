package com.dci.intellij.dbn.database.interfaces.queue;

import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;
import com.dci.intellij.dbn.common.thread.Threads;
import com.dci.intellij.dbn.common.util.Consumer;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionRef;
import com.dci.intellij.dbn.database.interfaces.DatabaseInterfaceQueue;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import static com.dci.intellij.dbn.database.interfaces.queue.InterfaceTask.COMPARATOR;
import static com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskStatus.QUEUED;
import static com.dci.intellij.dbn.database.interfaces.queue.InterfaceTaskStatus.SCHEDULED;

@Slf4j
public class InterfaceQueue extends StatefulDisposable.Base implements DatabaseInterfaceQueue {
    private static final ExecutorService MONITORS = Threads.newCachedThreadPool("DBN - Database Interface Monitor", true);

    private final BlockingQueue<InterfaceTask<?>> queue = new PriorityBlockingQueue<>(11, COMPARATOR);
    private final Consumer<InterfaceTask<?>> consumer;
    private final Counters counters = new Counters();
    private final ConnectionRef connection;
    private volatile boolean stopped;


    public InterfaceQueue(ConnectionHandler connection) {
        this(connection, null);
    }

    InterfaceQueue(@Nullable ConnectionHandler connection, Consumer<InterfaceTask<?>> consumer) {
        this.connection = ConnectionRef.of(connection);
        this.consumer = consumer == null ? new InterfaceQueueConsumer(this) : consumer;
        this.counters.running().addListener(value -> {
            if (value > maxActiveTasks()) {
                log.warn("Active task limit exceeded: {} (expected max {})", value, maxActiveTasks());
            }
        });
        MONITORS.submit(this::monitorQueue);
    }

    @Override
    @NotNull
    public ConnectionHandler getConnection(){
        return connection.ensure();
    }

    @Override
    public int size() {
        return queue.size();
    }

    @Override
    public Counters counters() {
        return counters;
    }

    @Override
    public int maxActiveTasks() {
        if (connection == null) return 10;
        return getConnection().getSettings().getDetailSettings().getMaxConnectionPoolSize();
    }

    @Override
    public <R> R scheduleAndReturn(InterfaceTaskDefinition info, ThrowableCallable<R, SQLException> callable) throws SQLException {
        return queue(info, true, callable).getResponse();
    }

    @Override
    public void scheduleAndWait(InterfaceTaskDefinition info, ThrowableRunnable<SQLException> runnable) throws SQLException {
        queue(info, true, runnable.asCallable());
    }

    @Override
    public void scheduleAndForget(InterfaceTaskDefinition info, ThrowableRunnable<SQLException> runnable) throws SQLException {
        queue(info, false, runnable.asCallable());
    }

    @NotNull
    private <T> InterfaceTask<T> queue(InterfaceTaskDefinition info, boolean synchronous, ThrowableCallable<T, SQLException> callable) throws SQLException {
        InterfaceTask<T> task = new InterfaceTask<>(info, synchronous, callable);
        queue.add(task);
        counters.queued().increment();
        task.changeStatus(QUEUED);
        task.awaitCompletion();
        return task;
    }

    /**
     * Start monitoring the queue
     */
    private void monitorQueue() {
        Thread monitor = Thread.currentThread();
        counters.running().addListener(value -> {
            if (value < maxActiveTasks()) {
                LockSupport.unpark(monitor);
            }
        });

        while (!stopped) {
            InterfaceTask<?> task = nextTask();
            if (task == null) continue;
            counters.queued().decrement();
            consumer.accept(task);
            counters.running().increment();
            task.changeStatus(SCHEDULED);
        }
    }

    @SneakyThrows
    private InterfaceTask<?> nextTask() {
        checkDisposed();
        if (counters.running().get() >= maxActiveTasks()) {
            LockSupport.park();
            return null;
        }
        return queue.poll(1, TimeUnit.MINUTES);
    }

    void executeTask(InterfaceTask<?> task) {
        try {
            task.execute();
        } finally {
            counters.running().decrement();
            counters.finished().increment();
        }
    }

    @Override
    protected void disposeInner() {
        stopped = true;
        queue.clear();
        counters.queued().reset();
    }
}
