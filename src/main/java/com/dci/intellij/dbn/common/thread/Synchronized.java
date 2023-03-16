package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ParametricCallable;
import com.dci.intellij.dbn.common.routine.ParametricRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import static com.dci.intellij.dbn.common.util.Unsafe.cast;


public class Synchronized {
	static final Map<Object, SyncObject> LOCKS = new ConcurrentHashMap<>(100);

	public static <O, E extends Throwable> void on(O owner, Function<O, Boolean> condition, ParametricRunnable<O, E> runnable) throws E{
		if (condition.apply(owner)) {
			on(owner, o -> {
				if (condition.apply(o)) {
					runnable.run(o);
				}
				return null;
			});
		}
	}
	public static <O, E extends Throwable> void on(O owner, ParametricRunnable<O, E> runnable) throws E{
		on(owner, o -> {
			runnable.run(o);
			return null;
		});
	}

	public static <O, R, E extends Throwable> R on(O owner, ParametricCallable<O, R, E> callable) throws E{
		if (owner == null) return callable.call(null);

		SyncObject<R> lock = acquire(owner);
		try {
			return lock.execute(owner, callable);
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

		public <O, E extends Throwable> R execute(O owner, ParametricCallable<O, R, E> callable) throws E{
			invokers.incrementAndGet();
			lock.lock();
			try {
				return callable.call(owner);
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

