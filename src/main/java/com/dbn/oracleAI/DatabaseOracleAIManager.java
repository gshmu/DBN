package com.dbn.oracleAI;

import com.dbn.DatabaseNavigator;
import com.dbn.common.component.PersistentState;
import com.dbn.common.component.ProjectComponentBase;
import com.dbn.connection.*;
import com.dbn.connection.jdbc.DBNConnection;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.exceptions.DatabaseOperationException;
import com.dbn.oracleAI.config.exceptions.QueryExecutionException;
import com.dbn.oracleAI.enums.ActionAIType;
import com.dbn.oracleAI.ui.OracleAIChatBox;
import com.dbn.oracleAI.ui.OracleAIChatBoxState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.ui.content.ContentManager;
import lombok.extern.slf4j.Slf4j;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@State(
    name = com.dbn.debugger.DatabaseDebuggerManager.COMPONENT_NAME,
    storages = @Storage(DatabaseNavigator.STORAGE_FILE)
)
@Slf4j
public class DatabaseOracleAIManager extends ProjectComponentBase implements PersistentState {
  public static final String COMPONENT_NAME = "DBNavigator.Project.OracleAIManager";
  public static final String TOOL_WINDOW_ID = "Oracle Companion";
  public ConnectionHandler currConnection;
  private static OracleAIChatBox oracleAIChatBox;
  private static volatile DatabaseOracleAIManager manager;
  private final Map<ConnectionId, OracleAIChatBoxState> chatBoxStates = new ConcurrentHashMap<>();

  private DatabaseOracleAIManager(Project project) {
    super(project, COMPONENT_NAME);
    ApplicationManager.getApplication().invokeLater(this::initOracleAIWindow);
  }

  public void switchToConnection(ConnectionId connectionId) {
    if (currConnection != null && oracleAIChatBox != null) {
      chatBoxStates.put(currConnection.getConnectionId(), oracleAIChatBox.captureState(ConnectionHandler.get(connectionId).toString()));
    }

    currConnection = ConnectionHandler.get(connectionId);
    oracleAIChatBox.updateForConnection(currConnection.getConnection().toString());
    OracleAIChatBoxState newState = chatBoxStates.get(connectionId);
    if (newState != null) {
      oracleAIChatBox.restoreState(newState);
    } else {
      oracleAIChatBox.restoreState(new OracleAIChatBoxState(currConnection.getConnection().toString()));
    }
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
      oracleAIChatBox = getOracleAIChatBox();
      oracleAIChatBox.currManager = this;
      ContentFactory contentFactory = contentManager.getFactory();
      Content content = contentFactory.createContent(oracleAIChatBox, null, true);
      contentManager.addContent(content);
      toolWindow.setAvailable(true, null);
    }
    return toolWindow;
  }


  @NotNull
  public OracleAIChatBox getOracleAIChatBox() {
    return new OracleAIChatBox();
  }

  public String queryOracleAI(String text, ActionAIType action){
    String output;
    try {
      DBNConnection mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
      output = currConnection.getOracleAIInterface().executeQuery(mainConnection, action, "ayoubon", text).getQueryOutput();
      return output;
    } catch (QueryExecutionException e){
      output = ""+e.getErrorCode();
      System.out.println(output);
      return output;
    } catch (SQLException e) {
      output = e.getMessage();
      System.out.println(e);
      return output;
    }
  }

  public List<Profile> fetchProfiles(){
    try {
      DBNConnection mainConnection = currConnection.getConnection(SessionId.ORACLE_AI);
      List<Profile> profiles = currConnection.getOracleAIInterface().listProfiles(mainConnection);
      return profiles;
    } catch (DatabaseOperationException e) {
      throw new RuntimeException(e);
    } catch (SQLException e){
      System.out.println(e.getMessage());
      return null;
    }
  }

  public static com.dbn.oracleAI.DatabaseOracleAIManager getInstance(@NotNull Project project) {
    if(manager == null){
      synchronized (DatabaseOracleAIManager.class) {
        if(manager == null){
          manager = new DatabaseOracleAIManager(project);
        }
      }
    }
    return  manager;
  }



  /*********************************************
   *            PersistentStateComponent       *
   *********************************************/

  @Override
  public Element getComponentState() {
    Element stateElement = new Element("OracleAIChatBoxState");
    chatBoxStates.forEach((connectionId, chatBoxState) -> {
      Element chatBoxStateElement = chatBoxState.toElement();
      chatBoxStateElement.setAttribute("connectionId", connectionId.toString());
      stateElement.addContent(chatBoxStateElement);
    });
    return stateElement;
  }

  @Override
  public void loadComponentState(@NotNull Element element) {
    List<Element> chatBoxStateElements = element.getChildren("OracleAIChatBoxState");
    chatBoxStateElements.forEach(chatBoxStateElement -> {
      ConnectionId connectionId = ConnectionId.get(chatBoxStateElement.getAttributeValue("connectionId"));
      OracleAIChatBoxState chatBoxState = OracleAIChatBoxState.fromElement(chatBoxStateElement);
      chatBoxStates.put(connectionId, chatBoxState);
    });
  }
}