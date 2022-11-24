package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.routine.ThrowableRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Synchronized {
	private static final Map<Object, SyncObject> LOCKS = new ConcurrentHashMap<>(100);

	public static <E extends Throwable> void on(Object owner, ThrowableRunnable<E> runnable) throws E{
		SyncObject lock = LOCKS.computeIfAbsent(owner, o -> new SyncObject());
		try {
			lock.execute(runnable);
		} finally {
			LOCKS.remove(owner);
		}
	}

	private static class SyncObject {
		private final Lock lock = new ReentrantLock();
		private volatile boolean executing = false;
		private volatile boolean executed = false;

		public <E extends Throwable> void execute(ThrowableRunnable<E> runnable) throws E{
			lock.lock();
			try {
				if (!executing && !executed) {
					try {
						executing = true;
						runnable.run();
					} finally {
						executed = true;
						executing = false;
					}
				}
			} finally {
				lock.unlock();
			}
		}
	}
}

