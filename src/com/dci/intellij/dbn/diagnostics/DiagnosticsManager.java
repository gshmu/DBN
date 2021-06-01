package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticType;
import com.dci.intellij.dbn.diagnostics.ui.DiagnosticsMonitorDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jdom.Element;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@State(
    name = DiagnosticsManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DiagnosticsManager extends AbstractProjectComponent implements PersistentStateComponent<Element> {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DiagnosticsManager";
    private final Map<ConnectionId, DiagnosticBundle> metadataInterfaceDiagnostics = new ConcurrentHashMap<>();
    private final DiagnosticBundle fileParserDiagnostics = new DiagnosticBundle(DiagnosticType.FILE_PARSER);

    public static DiagnosticsManager getInstance(@NotNull Project project) {
        return Failsafe.getComponent(project, DiagnosticsManager.class);
    }

    private DiagnosticsManager(@NotNull Project project) {
        super(project);
    }

    public DiagnosticBundle getMetadataInterfaceDiagnostics(ConnectionId connectionId) {
        return metadataInterfaceDiagnostics.
                computeIfAbsent(connectionId, connId -> new DiagnosticBundle(DiagnosticType.METADATA_INTERFACE));
    }

    public DiagnosticBundle getFileParserDiagnostics() {
        return fileParserDiagnostics;
    }

    public void openDiagnosticsMonitorDialog() {
        DiagnosticsMonitorDialog diagnosticsMonitorDialog = new DiagnosticsMonitorDialog(getProject());
        diagnosticsMonitorDialog.show();
    }


    @Override
    @NonNls
    @NotNull
    public String getComponentName() {
        return COMPONENT_NAME;
    }

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getState() {
        return null;
    }

    @Override
    public void loadState(@NotNull Element element) {
    }

    @Override
    protected void disposeInner() {
        metadataInterfaceDiagnostics.clear();
    }
}
