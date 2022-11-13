package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.common.navigation.NavigationInstructions;
import com.dci.intellij.dbn.editor.EditorProviderId;
import com.dci.intellij.dbn.editor.EditorStateManager;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public final class DBFileOpenHandle<T extends DBObject> {
    private static final Set<DBObjectRef<?>> REGISTRY = ConcurrentHashMap.newKeySet();

    private final DBObjectRef<T> object;
    private EditorProviderId editorProviderId;
    private NavigationInstructions browserInstructions = NavigationInstructions.NONE;
    private NavigationInstructions editorInstructions = NavigationInstructions.NONE;

    private DBFileOpenHandle(@NotNull T object) {
        this.object = DBObjectRef.of(object);
    }

    public static <T extends DBObject> DBFileOpenHandle<?> create(@NotNull T object) {
        return new DBFileOpenHandle<>(object);
    }

    public DBFileOpenHandle<?> withBrowserInstructions(@NotNull NavigationInstructions browserInstructions) {
        this.browserInstructions = browserInstructions;
        return this;
    }

    public DBFileOpenHandle<?> withEditorInstructions(@NotNull NavigationInstructions editorInstructions) {
        this.editorInstructions = editorInstructions;
        return this;
    }

    public DBFileOpenHandle<?> withEditorProviderId(EditorProviderId editorProviderId) {
        this.editorProviderId = editorProviderId;
        return this;
    }

    public EditorProviderId getEditorProviderId() {
        if (editorProviderId == null) {
            EditorStateManager editorStateManager = EditorStateManager.getInstance(getObject().getProject());
            editorProviderId = editorStateManager.getEditorProvider(object.getObjectType());
        }
        return editorProviderId;
    }

    @NotNull
    public T getObject() {
        return DBObjectRef.ensure(object);
    }

    public static boolean isFileOpening(@NotNull DBObject object) {
        return REGISTRY.contains(object.ref());
    }

    public void init() {
        REGISTRY.add(object);
    }

    public void release() {
        REGISTRY.remove(object);
    }
}
