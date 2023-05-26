package com.dci.intellij.dbn.diagnostics;

import com.dci.intellij.dbn.DatabaseNavigator;
import com.dci.intellij.dbn.common.component.PersistentState;
import com.dci.intellij.dbn.common.component.ProjectComponentBase;
import com.dci.intellij.dbn.common.dispose.Disposer;
import com.dci.intellij.dbn.common.ui.form.DBNForm;
import com.dci.intellij.dbn.connection.ConnectionId;
import com.dci.intellij.dbn.connection.SessionId;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticBundle;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticCategory;
import com.dci.intellij.dbn.diagnostics.data.DiagnosticType;
import com.dci.intellij.dbn.diagnostics.options.ui.DiagnosticSettingsDialog;
import com.dci.intellij.dbn.diagnostics.ui.ConnectionDiagnosticsForm;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.dci.intellij.dbn.common.component.Components.projectService;

@State(
    name = DiagnosticsManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
public class DiagnosticsManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.DiagnosticsManager";
    public static final String TOOL_WINDOW_ID = "DB Diagnostics";
    private static final Key<DiagnosticCategory> CONTENT_CATEGORY_KEY = Key.create("CONTENT_TYPE");
    private static final Key<DBNForm> CONTENT_FORM_KEY = Key.create("CONTENT_FORM");

    private final Map<ConnectionId, DiagnosticBundle<String>> metadataInterfaceDiagnostics = new ConcurrentHashMap<>();
    private final Map<ConnectionId, DiagnosticBundle<SessionId>> connectivityDiagnostics = new ConcurrentHashMap<>();

    private DiagnosticsManager(@NotNull Project project) {
        super(project, COMPONENT_NAME);
    }

    public static DiagnosticsManager getInstance(@NotNull Project project) {
        return projectService(project, DiagnosticsManager.class);
    }

    public DiagnosticBundle<String> getMetadataInterfaceDiagnostics(ConnectionId connectionId) {
        return metadataInterfaceDiagnostics.
                computeIfAbsent(connectionId, id -> DiagnosticBundle.composite(DiagnosticType.METADATA_INTERFACE));
    }

    public DiagnosticBundle<SessionId> getConnectivityDiagnostics(ConnectionId connectionId) {
        return connectivityDiagnostics.
                computeIfAbsent(connectionId, id -> DiagnosticBundle.basic(DiagnosticType.DATABASE_CONNECTIVITY));
    }

    public void openDiagnosticsSettings() {
        DiagnosticSettingsDialog settingsDialog = new DiagnosticSettingsDialog(getProject());
        settingsDialog.show();
    }

    public void showConnectionDiagnostics() {
        showDiagnosticsConsole(DiagnosticCategory.CONNECTION, () -> new ConnectionDiagnosticsForm(getProject()));
    }

    @NotNull
    public <T extends DBNForm> T showDiagnosticsConsole(
            @NotNull DiagnosticCategory category,
            @NotNull Producer<T> componentProducer) {

        T form = getDiagnosticsForm(category);
        ToolWindow toolWindow = getDiagnosticsToolWindow();
        ContentManager contentManager = toolWindow.getContentManager();

        if (form == null) {
            form = componentProducer.produce();

            ContentFactory contentFactory = contentManager.getFactory();
            Content content = contentFactory.createContent(form.getComponent(), category.getName(), false);
            content.putUserData(CONTENT_CATEGORY_KEY, category);
            content.putUserData(CONTENT_FORM_KEY, form);
            content.setCloseable(true);
            contentManager.addContent(content);
            Disposer.register(content, form);
        }

        Content content = getDiagnosticsContent(category);
        if (content != null) {
            contentManager.setSelectedContent(content);
        }

        toolWindow.setAvailable(true, null);
        toolWindow.show(null);

        return form;

    }

    @Nullable
    private  <T extends DBNForm> T getDiagnosticsForm(@NotNull DiagnosticCategory category) {
        Content content = getDiagnosticsContent(category);
        if (content != null) {
            return (T) content.getUserData(CONTENT_FORM_KEY);
        }
        return null;
    }

    @Nullable
    private  Content getDiagnosticsContent(@NotNull DiagnosticCategory category) {
        ToolWindow toolWindow = getDiagnosticsToolWindow();
        ContentManager contentManager = toolWindow.getContentManager();
        Content[] contents = contentManager.getContents();
        for (Content content : contents) {
            if (content.getUserData(CONTENT_CATEGORY_KEY) == category) {
                return content;
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

    /*********************************************
     *            PersistentStateComponent       *
     *********************************************/
    @Nullable
    @Override
    public Element getComponentState() {
        return null;
    }

    @Override
    public void loadComponentState(@NotNull Element element) {
    }

    @Override
    protected void disposeInner() {
        metadataInterfaceDiagnostics.clear();
    }
}
