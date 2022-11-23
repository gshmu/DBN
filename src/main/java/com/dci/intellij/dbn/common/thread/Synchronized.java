package com.dci.intellij.dbn.common.thread;

import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.routine.ThrowableRunnable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


public class Synchronized {
	private static final Map<Object, AtomicInteger> SYNC_OBJECTS = new ConcurrentHashMap<>(100);
	private static final Map<Object, SyncObject> LOCKS = new ConcurrentHashMap<>(100);

	private static Object init(Object owner) {
		AtomicInteger syncObject = SYNC_OBJECTS.computeIfAbsent(owner, k -> new AtomicInteger(0));
		syncObject.incrementAndGet();
		return syncObject;
	}

	private static void release(Object owner) {
		AtomicInteger syncObject = SYNC_OBJECTS.get(owner);
		if (syncObject.decrementAndGet() == 0 ) {
			SYNC_OBJECTS.remove(owner);
		}
	}

	public static <E extends Throwable> void on0(Object owner, ThrowableRunnable<E> runnable) throws E{
		Object lock = init(owner);
		try {
			synchronized (lock) {
				runnable.run();
			}
		} finally {
			release(owner);
		}
	}


	public static <E extends Throwable> void on(Object owner, ThrowableRunnable<E> runnable) throws E{
		SyncObject lock = LOCKS.computeIfAbsent(owner, o -> new SyncObject());
		try {
			if (lock.init()) {
				runnable.run();
			} else {
				lock.sync();
			}
		} finally {
			if (lock.release()) {
				LOCKS.remove(owner);
			}
		}
	}


	private static class SyncObject {
		private final Lock lock = new ReentrantLock();
		private final AtomicInteger count = new AtomicInteger();

		public boolean init() {
			int count = this.count.incrementAndGet();
			if (count == 1) {
				lock.lock();
				return true;
			}
			return false;
		}

		public void sync() {
			try {
				boolean loaded = lock.tryLock(3, TimeUnit.MINUTES);
				if (!loaded) {
					throw AlreadyDisposedException.INSTANCE;
				}
			} catch (Exception e) {
				throw AlreadyDisposedException.INSTANCE;
			}
		}

		public boolean release() {
			int count = this.count.decrementAndGet();
			if (count == 0) {
				lock.unlock();
				return true;
			}
			return false;
		}
	}
}

