package com.dci.intellij.dbn.object.common.status;

import com.dci.intellij.dbn.editor.DBContentType;
import gnu.trove.THashSet;

import java.util.Set;

public class DBObjectStatusHolder {
    private DBContentType mainContentType;
    private Set<DBObjectStatusEntry> statusEntries;

    public DBObjectStatusHolder(DBContentType mainContentType) {
        this.mainContentType = mainContentType;
    }

    private synchronized DBObjectStatusEntry get(DBContentType contentType, DBObjectStatus status, boolean create) {
        if (statusEntries != null) {
            for (DBObjectStatusEntry statusEntry : statusEntries) {
                if (statusEntry.getContentType() == contentType && statusEntry.getStatusType() == status) {
                    return statusEntry;
                }
            }
        }

        if (create) {
            DBObjectStatusEntry statusEntry = new DBObjectStatusEntry(contentType, status);
            if (statusEntries == null) statusEntries = new THashSet<DBObjectStatusEntry>();
            statusEntries.add(statusEntry);
            return statusEntry;
        }
        return null;
    }


    public boolean set(DBContentType contentType, DBObjectStatus status, boolean value) {
        DBObjectStatusEntry statusEntry = get(contentType, status, true);
        return statusEntry.setValue(value);
    }

    public boolean set(DBObjectStatus status, boolean value) {
        DBContentType[] subContentTypes = mainContentType.getSubContentTypes();
        if (subContentTypes.length > 0) {
            boolean hasChanged = false;
            for (DBContentType contentType : subContentTypes) {
                if (set(contentType, status, value)) {
                    hasChanged = true;
                }
            }
            return hasChanged;
        } else {
            return set(mainContentType, status, value);
        }
    }

    public boolean is(DBObjectStatus status) {
        DBContentType[] subContentTypes = mainContentType.getSubContentTypes();
        if (subContentTypes.length > 0) {
            for (DBContentType contentType : subContentTypes) {
                if (status.isPropagable()) {
                    if (!is(contentType, status)) return false;
                } else {
                    if (is(contentType, status)) return true;
                }
            }
            return status.isPropagable();
        } else {
            return is(mainContentType, status);
        }
    }

    public boolean is(DBContentType contentType, DBObjectStatus status) {
        DBObjectStatusEntry statusEntry = get(contentType, status, false);
        return statusEntry == null ?
                status.getDefaultValue() :
                statusEntry.getValue();
    }

    public boolean has(DBContentType contentType, DBObjectStatus status) {
        if (statusEntries != null)  {
            for (DBObjectStatusEntry statusEntry : statusEntries) {
                if (statusEntry.getContentType() == contentType && statusEntry.getStatusType() == status) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean has(DBObjectStatus status) {
        if (statusEntries != null) {
            for (DBObjectStatusEntry statusEntry : statusEntries) {
                if (statusEntry.getStatusType() == status) {
                    return true;
                }
            }
        }
        return false;
    }

    public class DBObjectStatusEntry {
        private DBObjectStatus status;
        private DBContentType contentType;
        private boolean value;

        public DBObjectStatusEntry(DBContentType contentType, DBObjectStatus status) {
            this.contentType = contentType;
            this.status = status;
        }

        public DBObjectStatus getStatusType() {
            return status;
        }

        public DBContentType getContentType() {
            return contentType;
        }

        public boolean getValue() {
            return value;
        }

        /**
         * returns true if status has changed
         */
        public boolean setValue(boolean value) {
            if (this.value != value) {
                this.value = value;
                return true;
            }
            return false;
        }

        @Override
        public String toString() {
            return contentType + " " + status + ": " + value;
        }
    }
}
