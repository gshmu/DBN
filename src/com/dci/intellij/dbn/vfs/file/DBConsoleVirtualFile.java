package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.DocumentUtil;
import com.dci.intellij.dbn.common.util.StringUtil;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionHandlerRef;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockMarkers;
import com.dci.intellij.dbn.editor.code.content.GuardedBlockType;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
import com.dci.intellij.dbn.vfs.DBVirtualFileImpl;
import com.dci.intellij.dbn.vfs.DatabaseFileViewProvider;
import com.intellij.lang.Language;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.RangeMarker;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.DocumentEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

public class DBConsoleVirtualFile extends DBVirtualFileImpl implements DocumentListener, DBParseableVirtualFile, Comparable<DBConsoleVirtualFile> {
    private long modificationTimestamp = LocalTimeCounter.currentTime();
    private SourceCodeContent content = new SourceCodeContent();
    private ConnectionHandlerRef connectionHandlerRef;
    private SchemaId databaseSchema;
    private DatabaseSession databaseSession;
    private final DBConsoleType type;

    public DBConsoleVirtualFile(ConnectionHandler connectionHandler, String name, DBConsoleType type) {
        super(connectionHandler.getProject());
        this.type = type;
        connectionHandlerRef = connectionHandler.getRef();
        databaseSession = connectionHandler.getSessionBundle().getMainSession();
        setDatabaseSchema(connectionHandler.getDefaultSchema());
        setName(name);
        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
    }

    public SourceCodeContent getContent() {
        return content;
    }

    public void setText(String text) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        Project project = connectionHandler.getProject();
        if (type == DBConsoleType.DEBUG && StringUtil.isEmpty(text)) {
            DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
            CodeStyleCaseSettings styleCaseSettings = DBLCodeStyleManager.getInstance(project).getCodeStyleCaseSettings(PSQLLanguage.INSTANCE);
            text = debuggerInterface.getDebugConsoleTemplate(styleCaseSettings);
        }
        content.importContent(text);


        Document document = DocumentUtil.getDocument(this);
        if (document != null) {
            //DocumentUtil.setText(document, content.getText());
            GuardedBlockMarkers guardedBlocks = content.getOffsets().getGuardedBlocks();
            if (!guardedBlocks.isEmpty()) {
                DocumentUtil.removeGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION);
                DocumentUtil.createGuardedBlocks(document, GuardedBlockType.READONLY_DOCUMENT_SECTION, guardedBlocks, null);
            }
        }
    }

    @Override
    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnectionHandler();
        DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    public void setName(String name) {
        this.name = name;
        path = null;
        url = null;
    }

    @Override
    public Icon getIcon() {
        switch (type) {
            case STANDARD: return Icons.FILE_SQL_CONSOLE;
            case DEBUG: return Icons.FILE_SQL_DEBUG_CONSOLE;
        }
        return null;
    }

    @NotNull
    @Override
    public ConnectionId getConnectionId() {
        return connectionHandlerRef.getConnectionId();
    }

    @Override
    @NotNull
    public ConnectionHandler getConnectionHandler() {
        return connectionHandlerRef.ensure();
    }

    public void setDatabaseSchema(SchemaId currentSchema) {
        this.databaseSchema = currentSchema;
    }

    public void setDatabaseSchemaName(String currentSchemaName) {
        if (StringUtil.isEmpty(currentSchemaName)) {
            this.databaseSchema = null;
        } else {
            this.databaseSchema = SchemaId.get(currentSchemaName);
        }
    }

    public String getDatabaseSchemaName() {
        return this.databaseSchema == null ? null : this.databaseSchema.id();
    }

    public void setDatabaseSessionId(SessionId sessionId) {
        databaseSession = getConnectionHandler().getSessionBundle().getSession(sessionId);
    }

    @Override
    public DatabaseSession getDatabaseSession() {
        return databaseSession;
    }

    public void setDatabaseSession(DatabaseSession databaseSession) {
        this.databaseSession = databaseSession;
    }

    @Override
    public boolean isValid() {
        return super.isValid() && connectionHandlerRef.isValid();
    }

    @Override
    @Nullable
    public SchemaId getSchemaId() {
        return databaseSchema;
    }

    @NotNull
    @Override
    public String getName() {
        return name;
    }

    public DBConsoleType getType() {
        return type;
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public boolean isDefault() {return name.equals(getConnectionHandler().getName());}

    @Override
    public VirtualFile getParent() {
        return null;
    }

    @Override
    public VirtualFile[] getChildren() {
        return VirtualFile.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public FileType getFileType() {
        return SQLFileType.INSTANCE;
    }

    @Override
    @NotNull
    public OutputStream getOutputStream(Object requestor, final long modificationTimestamp, long newTimeStamp) throws IOException {
        return new ByteArrayOutputStream() {
            @Override
            public void close() {
                DBConsoleVirtualFile.this.modificationTimestamp = modificationTimestamp;
                content.setText(toString());
            }
        };
    }

    @Override
    @NotNull
    public byte[] contentsToByteArray() throws IOException {
        Charset charset = getCharset();
        return content.getBytes(charset);
    }

    @Override
    public long getTimeStamp() {
        return 0;
    }

  @Override
  public long getModificationStamp() {
    return modificationTimestamp;
  }

    @Override
    public long getLength() {
        try {
            return contentsToByteArray().length;
        } catch (IOException e) {
            return 0;
        }
    }

    @Override
    public void refresh(boolean asynchronous, boolean recursive, Runnable postRunnable) {
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(contentsToByteArray());
    }

    @Override
    public String getExtension() {
        return "sql";
    }

    @Override
    public int compareTo(@NotNull DBConsoleVirtualFile o) {
        return name.compareTo(o.name);
    }

    @Override
    public void beforeDocumentChange(DocumentEvent event) {

    }

    @Override
    public void documentChanged(DocumentEvent event) {
        Document document = event.getDocument();
        content.setText(document.getCharsSequence());
        if (document instanceof DocumentEx) {
            DocumentEx documentEx = (DocumentEx) document;
            List<RangeMarker> blocks = documentEx.getGuardedBlocks();
            if (!blocks.isEmpty()) {
                content.getOffsets().setGuardedBlocks(blocks);
            }
        }
    }
}
