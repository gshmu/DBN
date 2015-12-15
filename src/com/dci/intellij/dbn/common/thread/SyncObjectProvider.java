package com.dci.intellij.dbn.common.thread;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import gnu.trove.THashMap;

public class SyncObjectProvider {
	private Map<String, AtomicInteger> SYNC_OBJECTS = new THashMap<String, AtomicInteger>();

	public synchronized Object get(String key) {
		if (key != null) {
			AtomicInteger syncObject = SYNC_OBJECTS.get(key);
			if (syncObject == null) {
				syncObject = new AtomicInteger(1);
				SYNC_OBJECTS.put(key, syncObject);
			} else {
				syncObject.incrementAndGet();
			}
			return syncObject;
		} else {
			return null;
		}
	}

	public synchronized void release(String key) {
		if (key != null) {
			AtomicInteger syncObject = SYNC_OBJECTS.get(key);
			if (syncObject.decrementAndGet() == 0 ) {
				SYNC_OBJECTS.remove(key);
			}
		}
	}
}

