  package com.dbn.oracleAI;

  import com.dbn.DatabaseNavigator;
  import com.dbn.common.component.PersistentState;
  import com.dbn.common.component.ProjectComponentBase;
  import com.dbn.common.event.ProjectEvents;
  import com.dbn.connection.ConnectionHandler;
  import com.dbn.connection.ConnectionHandlerStatusListener;
  import com.dbn.connection.ConnectionRef;
  import com.dbn.connection.SessionId;
  import com.dbn.connection.jdbc.DBNConnection;
  import com.dbn.oracleAI.ui.OracleAIChatBox;
  import com.intellij.openapi.application.ApplicationManager;
  import com.intellij.openapi.components.State;
  import com.intellij.openapi.components.Storage;
  import com.intellij.openapi.project.Project;
  import com.intellij.openapi.wm.ToolWindow;
  import com.intellij.openapi.wm.ToolWindowManager;
  import com.intellij.ui.content.Content;
  import com.intellij.ui.content.ContentFactory;
  import com.intellij.ui.content.ContentManager;
  import org.jdom.Element;
  import org.jetbrains.annotations.NotNull;
  import org.jetbrains.annotations.Nullable;

  import java.sql.SQLException;
  import java.util.HashSet;
  import java.util.Set;

  import static com.dbn.common.component.Components.projectService;

  @State(
      name = com.dbn.debugger.DatabaseDebuggerManager.COMPONENT_NAME,
      storages = @Storage(DatabaseNavigator.STORAGE_FILE)
  )
  public class DatabaseOracleAIManager extends ProjectComponentBase implements PersistentState {
    public static final String COMPONENT_NAME = "DBNavigator.Project.OracleAIManager";
    public static final String TOOL_WINDOW_ID = "Oracle Companion";

//    private final Latent<ExecutionConsoleForm> oracleAIChatBox =
//        Latent.basic(() -> {
//          OracleAIChatBox chatBox = new OracleAIChatBox( getProject());
//          Disposer.register(this, chatBox);
//          return chatBox;
//        });
    public static ConnectionHandler currConnection;
    private final Set<ConnectionRef> activeChatSessions = new HashSet<>();

    private DatabaseOracleAIManager(Project project) {
      super(project, COMPONENT_NAME);
      System.out.println("currConnection");
      ProjectEvents.subscribe(project, this, ConnectionHandlerStatusListener.TOPIC, (connectionId) -> {
        currConnection = ConnectionHandler.get(connectionId);
      });
      System.out.println(currConnection);


    }


    private void showOracleAIWindow() {
      ToolWindow toolWindow = initOracleAIWindow();
      toolWindow.show(null);
    }

    public void hideOracleAIWindow() {
      ToolWindow toolWindow = getOracleAIWindow();
      toolWindow.getContentManager().removeAllContents(false);
      toolWindow.setAvailable(false, null);
    }

    public ToolWindow getOracleAIWindow() {
      Project project = getProject();
      ToolWindowManager toolWindowManager = ToolWindowManager.getInstance(project);
      return toolWindowManager.getToolWindow(TOOL_WINDOW_ID);
    }

    private ToolWindow initOracleAIWindow() {
      ToolWindow toolWindow = getOracleAIWindow();
      ContentManager contentManager = toolWindow.getContentManager();

      if (contentManager.getContents().length == 0) {
        OracleAIChatBox oracleAIChatBox = getOracleAIChatBox();
        ContentFactory contentFactory = contentManager.getFactory();
        Content content = contentFactory.createContent(oracleAIChatBox, null, true);
        contentManager.addContent(content);
        toolWindow.setAvailable(true, null);
      }
      return toolWindow;
    }


    @NotNull
    public OracleAIChatBox getOracleAIChatBox() {
      return new OracleAIChatBox(ensureProject());
    }

    public static String queryOracleAI(String text, String action){
      try {
        DBNConnection mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
        String output = currConnection.getOracleAIInterface().queryProfile(mainConnection, action, text).getQueryOutput();
        return output;
      } catch (SQLException e){
        System.out.println(e.getMessage());
        return null;
      }
    }

    public static String[] fetchProfiles(){
      try {
        DBNConnection mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
        String[] profiles = currConnection.getOracleAIInterface().listProfiles(mainConnection).getProfiles();
        return profiles;
      } catch (SQLException e){
        System.out.println(e.getMessage());
        return null;
      }
    }

    public void connectAI(ConnectionHandler connection){
      ApplicationManager.getApplication().invokeLater(() -> {
        initOracleAIWindow();
      });
      currConnection = connection;
      try {
        DBNConnection oracleAIConnection = connection.getConnection(SessionId.ORACLE_AI);
        connection.getOracleAIInterface().pickProfile(oracleAIConnection, "ayoubon");
      } catch (SQLException e){
        System.out.println(e.getMessage());
      }
    }

    //yoooooooooooooooooooooo
    public static DatabaseOracleAIManager getInstance(@NotNull Project project, ConnectionHandler connection) {
      return projectService(project, DatabaseOracleAIManager.class);
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
  }