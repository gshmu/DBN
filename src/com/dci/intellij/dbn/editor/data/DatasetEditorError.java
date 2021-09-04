package com.dci.intellij.dbn.editor.data;

import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.database.DatabaseMessageParserInterface;
import com.dci.intellij.dbn.database.DatabaseObjectIdentifier;
import com.dci.intellij.dbn.object.common.DBObject;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.Nullable;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@EqualsAndHashCode
public class DatasetEditorError {
    private final String message;
    private final DBObject messageObject;
    private boolean dirty;
    private boolean notified;

    @EqualsAndHashCode.Exclude
    private final Set<ChangeListener> changeListeners = new HashSet<>();

    public DatasetEditorError(ConnectionHandler connectionHandler, Exception exception) {
        this.message = exception.getMessage();
        this.messageObject = resolveMessageObject(connectionHandler, exception);
    }

    @Nullable
    private static DBObject resolveMessageObject(ConnectionHandler connectionHandler, Exception exception) {
        DBObject messageObject = null;
        if (exception instanceof SQLException) {
            DatabaseMessageParserInterface messageParserInterface = connectionHandler.getInterfaceProvider().getMessageParserInterface();
            DatabaseObjectIdentifier objectIdentifier = messageParserInterface.identifyObject((SQLException) exception);
            if (objectIdentifier != null) {
                messageObject = connectionHandler.getObjectBundle().getObject(objectIdentifier);
            }
        }
        return messageObject;
    }

    public DatasetEditorError(String message, DBObject messageObject) {
        this.message = message;
        this.messageObject = messageObject;
    }

    public void addChangeListener(ChangeListener changeListener) {
        changeListeners.add(changeListener);
    }

    public void removeChangeListener(ChangeListener changeListener) {
        changeListeners.remove(changeListener);
    }

    public void markDirty() {
        dirty = true;
        ChangeEvent changeEvent = new ChangeEvent(this);
        for (ChangeListener changeListener: changeListeners) {
            changeListener.stateChanged(changeEvent);
        }
    }
}
