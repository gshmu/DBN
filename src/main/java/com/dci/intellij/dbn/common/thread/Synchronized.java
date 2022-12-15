package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableCallable;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;


public class Synchronized {
	static final Map<Object, SyncObject> LOCKS = new ConcurrentHashMap<>(100);

	public static <E extends Throwable> void on(Object owner, ThrowableRunnable<E> runnable) throws E{
		on(owner, () -> {
			runnable.run();
			return null;
		});
	}

	public static <R, E extends Throwable> R on(Object owner, ThrowableCallable<R, E> callable) throws E{
		if (owner == null) return callable.call();

		SyncObject<R> lock = acquire(owner);
		try {
			return lock.execute(callable);
		} finally {
			release(owner, lock);
		}
	}

	private static synchronized <R> SyncObject<R> acquire(Object owner) {
		return cast(LOCKS.computeIfAbsent(owner, o -> new SyncObject()));
	}

	private synchronized static void release(Object owner, SyncObject<?> lock) {
		if (lock.isFree()) LOCKS.remove(owner);
	}

	private static class SyncObject<R> {
		private final Lock lock = new ReentrantLock();
		private final AtomicInteger invokers = new AtomicInteger();

		public <E extends Throwable> R execute(ThrowableCallable<R, E> callable) throws E{
			invokers.incrementAndGet();
			lock.lock();
			try {
				return callable.call();
			} finally {
				invokers.decrementAndGet();
				lock.unlock();
			}
		}

		public boolean isFree() {
			return invokers.get() == 0;
		}
	}
}

