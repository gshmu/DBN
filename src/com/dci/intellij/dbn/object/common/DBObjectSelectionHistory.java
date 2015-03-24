package com.dci.intellij.dbn.object.common;

import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.Disposable;

public class DBObjectSelectionHistory implements Disposable{
    private List<DBObjectRef> history = new ArrayList<DBObjectRef>();
    private int offset;

    public void add(DBObject object) {
        DBObjectRef objectRef = DBObjectRef.from(object);
        if (objectRef != null) {
            if (history.size() > 0 && history.get(offset).equals(objectRef)) {
                return;
            }
            while (history.size() - 1  > offset) {
                history.remove(offset);
            }

            while (history.size() > 30) {
                history.remove(0);
            }
            history.add(objectRef);
            offset = history.size() -1;
        }
    }

    public void clear() {
        history.clear();
    }

    public boolean hasNext() {
        return offset < history.size()-1;
    }

    public boolean hasPrevious() {
        return offset > 0;
    }

    public @Nullable DBObject nextNoScroll() {
        return hasNext() ? DBObjectRef.get(history.get(offset + 1)) : null;
    }

    public @Nullable DBObject previousNoScroll() {
        return hasPrevious() ? DBObjectRef.get(history.get(offset - 1)) : null;
    }

    public @Nullable DBObject next() {
        if (offset < history.size() -1) {
            offset = offset + 1;
            DBObjectRef objectRef = history.get(offset);
            DBObject object = objectRef.get();
            if (object == null) {
                history.remove(objectRef);
                return next();
            }
            return object;
        }
        return null;
    }

    public @Nullable DBObject previous() {
        if (offset > 0) {
            offset = offset-1;
            DBObjectRef objectRef = history.get(offset);
            DBObject object = objectRef.get();
            if (object == null) {
                history.remove(objectRef);
                return previous();
            }
            return object;
        }
        return null;
    }

    @Override
    public void dispose() {
        history.clear();
    }
}
