package com.dci.intellij.dbn.common.pool;


import com.dci.intellij.dbn.common.dispose.AlreadyDisposedException;
import com.dci.intellij.dbn.common.dispose.SafeDisposer;
import com.dci.intellij.dbn.common.dispose.StatefulDisposable;
import com.dci.intellij.dbn.common.lookup.Visitor;
import com.intellij.openapi.Disposable;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public abstract class ObjectPool<T extends Disposable> extends StatefulDisposable.Base {
    private final List<T> objects = new CopyOnWriteArrayList<>();
    private final BlockingQueue<T> available = new LinkedBlockingQueue<>();

    public final T acquire(long timeout, TimeUnit timeUnit) {
        checkDisposed();

        T object = available.poll();
        if (object == null) {
            object = initialise();
            if (object != null) {
                return object;
            }
        }
        try {
            return available.poll(timeout, timeUnit);
        } catch (InterruptedException e) {
            throw AlreadyDisposedException.INSTANCE;
        }
    }

    private T initialise() {
        if (objects.size() < maxSize()) {
            synchronized (this) {
                if (objects.size() < maxSize()) {
                    checkDisposed();
                    T object = create();
                    objects.add(object);
                    return object;
                }
            }
        }
        return null;
    }

    public final void release(T object) {
        checkDisposed();
        if (isValid(object)) {
            available.offer(object);
        } else {
            SafeDisposer.dispose(object);
            objects.remove(object);
        }
    }

    public final void remove(T object) {
        objects.remove(object);
    }

    public final void visit(Visitor<T> visitor) {
        for (T object : objects) {
            visitor.visit(object);
        }
    }

    public List<T> getObjects() {
        return objects;
    }

    public final int size() {
        return objects.size();
    }

    abstract T create();

    abstract boolean isValid(T object);

    abstract int maxSize();




    @Override
    protected void disposeInner() {
        SafeDisposer.dispose(objects, false, true);
        available.clear();
    }
}
