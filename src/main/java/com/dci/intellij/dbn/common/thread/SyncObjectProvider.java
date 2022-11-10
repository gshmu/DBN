package com.dci.intellij.dbn.common.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncObjectProvider {
	private final Map<String, AtomicInteger> SYNC_OBJECTS = new HashMap<>();

	public Object get(String key) {
		if (key != null) {
			synchronized (this) {
				AtomicInteger syncObject = SYNC_OBJECTS.computeIfAbsent(key, k -> new AtomicInteger(0));
				syncObject.incrementAndGet();
				return syncObject;
			}
		} else {
			return null;
		}
	}

	public void release(String key) {
		if (key != null) {
			synchronized (this) {
				AtomicInteger syncObject = SYNC_OBJECTS.get(key);
				if (syncObject.decrementAndGet() == 0 ) {
					SYNC_OBJECTS.remove(key);
				}
			}
		}
	}
}

