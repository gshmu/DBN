package com.dci.intellij.dbn.common.thread;

import gnu.trove.THashMap;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SyncObjectProvider {
	private Map<String, AtomicInteger> SYNC_OBJECTS = new THashMap<>();

	public Object get(String key) {
		if (key != null) {
			synchronized (this) {
				AtomicInteger syncObject = SYNC_OBJECTS.get(key);
				if (syncObject == null) {
					syncObject = new AtomicInteger(1);
					SYNC_OBJECTS.put(key, syncObject);
				} else {
					syncObject.incrementAndGet();
				}
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

