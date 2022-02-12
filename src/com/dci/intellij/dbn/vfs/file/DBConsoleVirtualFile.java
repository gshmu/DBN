package com.dci.intellij.dbn.vfs.file;

import com.dci.intellij.dbn.code.common.style.DBLCodeStyleManager;
import com.dci.intellij.dbn.code.common.style.options.CodeStyleCaseSettings;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.util.Strings;
import com.dci.intellij.dbn.connection.ConnectionHandler;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SchemaId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.connection.mapping.FileConnectionMapping;
import com.dci.intellij.dbn.connection.session.DatabaseSession;
import com.dci.intellij.dbn.database.DatabaseDebuggerInterface;
import com.dci.intellij.dbn.editor.code.content.SourceCodeContent;
import com.dci.intellij.dbn.language.common.DBLanguageDialect;
import com.dci.intellij.dbn.language.psql.PSQLLanguage;
import com.dci.intellij.dbn.language.sql.SQLFileType;
import com.dci.intellij.dbn.object.DBConsole;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.dci.intellij.dbn.vfs.DBConsoleType;
import com.dci.intellij.dbn.vfs.DBParseableVirtualFile;
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
import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

@Getter
public class DBConsoleVirtualFile extends DBObjectVirtualFile<DBConsole> implements DocumentListener, DBParseableVirtualFile, Comparable<DBConsoleVirtualFile> {
    private transient long modificationTimestamp = LocalTimeCounter.currentTime();
    private final SourceCodeContent content = new SourceCodeContent();
    private final FileConnectionMapping connectionMapping;

    public DBConsoleVirtualFile(@NotNull DBConsole console) {
        super(console.getProject(), DBObjectRef.of(console));

        ConnectionHandler connectionHandler = console.getConnection();
        ConnectionId connectionId = connectionHandler.getConnectionId();
        SchemaId schemaId = connectionHandler.getDefaultSchema();
        SessionId sessionId = connectionHandler.getSessionBundle().getMainSession().getId();
        connectionMapping = new FileConnectionMapping(this.getUrl(), connectionId, sessionId, schemaId);

        setCharset(connectionHandler.getSettings().getDetailSettings().getCharset());
    }

    public void setText(String text) {
        if (getObject().getConsoleType() == DBConsoleType.DEBUG && Strings.isEmpty(text)) {
            ConnectionHandler connectionHandler = getConnection();
            Project project = connectionHandler.getProject();

            DatabaseDebuggerInterface debuggerInterface = connectionHandler.getInterfaceProvider().getDebuggerInterface();
            CodeStyleCaseSettings styleCaseSettings = DBLCodeStyleManager.getInstance(project).getCodeStyleCaseSettings(PSQLLanguage.INSTANCE);
            text = debuggerInterface.getDebugConsoleTemplate(styleCaseSettings);
        }
        content.importContent(text);
    }

    @Override
    public PsiFile initializePsiFile(DatabaseFileViewProvider fileViewProvider, Language language) {
        ConnectionHandler connectionHandler = getConnection();
        DBLanguageDialect languageDialect = connectionHandler.resolveLanguageDialect(language);
        return languageDialect == null ? null : fileViewProvider.initializePsiFile(languageDialect);
    }

    public void setName(String name) {
        this.name = name;
        this.path = null;
        this.url = null;
   }

    @NotNull
    public DBConsole getConsole() {
        return getObject();
    }

    @Nullable
    @Override
    public Icon getIcon() {
        switch (getConsole().getConsoleType()) {
            case STANDARD: return Icons.FILE_SQL_CONSOLE;
            case DEBUG: return Icons.FILE_SQL_DEBUG_CONSOLE;
        }
        return null;
    }
    public void setDatabaseSchema(SchemaId schemaId) {
        connectionMapping.setSchemaId(schemaId);
    }

    public void setDatabaseSchemaName(String schemaName) {
        if (Strings.isEmpty(schemaName)) {
            setDatabaseSchema(null);
        } else {
            setDatabaseSchema(SchemaId.get(schemaName));
        }
    }

    public String getDatabaseSchemaName() {
        return connectionMapping.getSchemaName();
    }

    public void setDatabaseSessionId(SessionId sessionId) {
        connectionMapping.setSessionId(sessionId);
    }

    @Override
    public DatabaseSession getSession() {
        return connectionMapping.getSession();
    }

    public void setDatabaseSession(DatabaseSession databaseSession) {
        this.connectionMapping.setSessionId(databaseSession == null ? SessionId.MAIN : databaseSession.getId());;
    }

    @Override
    public boolean isValid() {
        return super.isValid() /*&& connectionHandlerRef.isValid()*/;
    }

    @Override
    @Nullable
    public SchemaId getSchemaId() {
        return connectionMapping.getSchemaId();
    }

    public DBConsoleType getType() {
        return getObject().getConsoleType();
    }

    @Override
    public boolean isWritable() {
        return true;
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    public boolean isDefault() {return Objects.equals(name, getConnection().getName());}

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

    @NotNull
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
    public void beforeDocumentChange(@NotNull DocumentEvent event) {

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
