package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.AbstractProjectComponent;
import com.dci.intellij.dbn.common.dispose.Failsafe;
import com.dci.intellij.dbn.common.ui.DBNForm;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticCategory;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticType;
import com.dci.intellij.dbn.diagnostics.options.ui.DiagnosticSettingsDialog;
import com.dci.intellij.dbn.diagnostics.ui.DiagnosticsMonitorDialog;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import com.intellij.util.Producer;
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
    public static final String TOOL_WINDOW_ID = "DBNavigator.ToolWindow.DatabaseDiagnostics";
    private static final Key<DiagnosticCategory> CONTENT_CATEGORY_KEY = Key.create("CONTENT_TYPE");
    private static final Key<DBNForm> CONTENT_FORM_KEY = Key.create("CONTENT_FORM");


    private final Map<ConnectionId, DiagnosticBundle> metadataInterfaceDiagnostics = new ConcurrentHashMap<>();
    private final Map<ConnectionId, DiagnosticBundle> connectivityDiagnostics = new ConcurrentHashMap<>();

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

    public DiagnosticBundle getConnectivityDiagnostics(ConnectionId connectionId) {
        return connectivityDiagnostics.
                computeIfAbsent(connectionId, connId -> new DiagnosticBundle(DiagnosticType.DATABASE_CONNECTIVITY));
    }

    public void openDiagnosticsMonitorDialog() {
        DiagnosticsMonitorDialog monitorDialog = new DiagnosticsMonitorDialog(getProject());
        monitorDialog.show();
    }

    public void openDiagnosticsSettingsDialog() {
        DiagnosticSettingsDialog settingsDialog = new DiagnosticSettingsDialog(getProject());
        settingsDialog.show();
    }

    @NotNull
    public <T extends DBNForm> T showDiagnosticsConsole(
            @NotNull DiagnosticCategory category,
            @NotNull Producer<T> componentProducer) {

        T form = getDiagnosticsForm(category);
        ToolWindow toolWindow = getDiagnosticsToolWindow();
        if (form == null) {
            form = componentProducer.produce();

            ContentManager contentManager = toolWindow.getContentManager();
            ContentFactory contentFactory = contentManager.getFactory();
            Content content = contentFactory.createContent(form.getComponent(), category.getName(), false);
            content.putUserData(CONTENT_CATEGORY_KEY, category);
            content.putUserData(CONTENT_FORM_KEY, form);
            contentManager.addContent(content);
        }
        toolWindow.setAvailable(true, null);
        toolWindow.show(null);
        return form;
    }

    @Nullable
    public <T extends DBNForm> T getDiagnosticsForm(@NotNull DiagnosticCategory category) {
        ToolWindow toolWindow = getDiagnosticsToolWindow();
        ContentManager contentManager = toolWindow.getContentManager();
        Content[] contents = contentManager.getContents();
        for (Content content : contents) {
            if (content.getUserData(CONTENT_CATEGORY_KEY) == category) {
                return (T) content.getUserData(CONTENT_FORM_KEY);
            }
        }
        return null;
    }


    public void closeDiagnosticsConsole(DiagnosticCategory category) {
        ToolWindow toolWindow = getDiagnosticsToolWindow();
        ContentManager contentManager = toolWindow.getContentManager();
        Content[] contents = contentManager.getContents();
        for (Content content : contents) {
            if (content.getUserData(CONTENT_CATEGORY_KEY) == category) {
                contentManager.removeContent(content, true);
            }
        }

        if (contentManager.getContents().length == 0) {
            toolWindow.setAvailable(false, null);
        }
    }

    public ToolWindow getDiagnosticsToolWindow() {
        Project project = getProject();
        ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
        return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
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
