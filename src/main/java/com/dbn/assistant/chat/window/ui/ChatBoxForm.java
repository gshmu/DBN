/*
 * Copyright (c) 2024, Oracle and/or its affiliates.
 *
 * This software is dual-licensed to you under the Universal Permissive License
 * (UPL) 1.0 as shown at https://oss.oracle.com/licenses/upl or Apache License
 * 2.0 as shown at http://www.apache.org/licenses/LICENSE-2.0. You may choose
 * either license.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.dbn.assistant.chat.window.ui;

import com.dbn.assistant.DatabaseAssistantManager;
import com.dbn.assistant.chat.message.AuthorType;
import com.dbn.assistant.chat.message.ChatMessageContext;
import com.dbn.assistant.chat.message.PersistentChatMessage;
import com.dbn.assistant.chat.window.PromptAction;
import com.dbn.assistant.chat.window.util.RollingMessageContainer;
import com.dbn.assistant.entity.AIProfileItem;
import com.dbn.assistant.entity.Profile;
import com.dbn.assistant.init.ui.AssistantIntroductionForm;
import com.dbn.assistant.provider.ProviderModel;
import com.dbn.assistant.service.AIProfileService;
import com.dbn.assistant.state.AssistantState;
import com.dbn.common.action.DataKeys;
import com.dbn.common.event.ProjectEvents;
import com.dbn.common.message.MessageType;
import com.dbn.common.thread.Background;
import com.dbn.common.thread.Dispatch;
import com.dbn.common.ui.form.DBNFormBase;
import com.dbn.common.ui.form.DBNHeaderForm;
import com.dbn.common.ui.util.UserInterface;
import com.dbn.common.util.Actions;
import com.dbn.common.util.Strings;
import com.dbn.connection.ConnectionHandler;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.ConnectionRef;
import com.dbn.connection.ConnectionStatusListener;
import com.dbn.object.event.ObjectChangeListener;
import com.dbn.object.type.DBObjectType;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.project.Project;
import com.intellij.util.ui.AsyncProcessIcon;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.dbn.assistant.state.AssistantStatus.*;
import static com.dbn.common.feature.FeatureAcknowledgement.ENGAGED;
import static com.dbn.common.util.Commons.nvl;

/**
 * Database Assistant ChatBox component
 *
 * @author Ayoub Aarrasse (Oracle)
 * @author Emmanuel Jannetti (Oracle)
 * @author Dan Cioca (Oracle)
 */
@Slf4j
public class ChatBoxForm extends DBNFormBase {
  private JPanel mainPanel;
  private JPanel chatPanel;
  private JScrollPane chatScrollPane;
  private JPanel profileActionsPanel;
  private JPanel headerPanel;
  private JPanel typeActionsPanel;
  private JPanel inputFieldPanel;
  private JPanel chatActionsPanel;
  private JPanel introPanel;
  private JPanel chatBoxPanel;
  private JPanel initializingIconPanel;
  private JPanel initializingPanel;
  private JPanel helpActionPanel;

  private RollingMessageContainer messageContainer;
  private final ConnectionRef connection;
  private ChatBoxInputField inputField;

  public ChatBoxForm(ConnectionHandler connection) {
    super(connection, connection.getProject());
    this.connection = connection.ref();

    initializingIconPanel.add(new AsyncProcessIcon("Loading"), BorderLayout.CENTER);

    // hide all panels until availability status is known
    this.introPanel.setVisible(false);
    this.chatBoxPanel.setVisible(false);

    initHeaderForm();
    initIntroForm();
    initChatBoxForm();

    initChangeListener();
    initConnectivityListener();
  }

  private void initChangeListener() {
    ProjectEvents.subscribe(ensureProject(), this, ObjectChangeListener.TOPIC, (connectionId, ownerId, objectType) -> {
      if (connectionId != getConnectionId()) return;
      if (objectType != DBObjectType.PROFILE) return;
      reloadProfiles();
    });
  }

  /**
   * Attempts to load the profiles when connectivity is restored if the connection was down on first initialization attempt
   */
  private void initConnectivityListener() {
    ProjectEvents.subscribe(ensureProject(), this, ConnectionStatusListener.TOPIC, (connectionId, sessionId) -> {
      if (connectionId != this.getConnectionId()) return;
      if (getAssistantState().isNot(UNAVAILABLE)) return;

      loadProfiles();
    });
  }

  private void initIntroForm() {
    if (hasUserEngaged()) return;

    AssistantIntroductionForm introductionForm = new AssistantIntroductionForm(this);
    introPanel.add(introductionForm.getComponent(), BorderLayout.CENTER);
    introPanel.setVisible(true);
  }

  private void initChatBoxForm() {
    if (!hasUserEngaged()) return;
    chatBoxPanel.setVisible(true);

    createActionPanels();
    createInputField();
    configureConversationPanel();
    loadProfiles();
    restoreMessages();
  }

  private boolean hasUserEngaged() {
    return getAssistantState().getAcknowledgement() == ENGAGED;
  }

  private void initHeaderForm() {
    ConnectionHandler connection = getConnection();
    DBNHeaderForm headerForm = new DBNHeaderForm(this, connection);
    headerPanel.add(headerForm.getComponent(), BorderLayout.CENTER);
  }

  private void createActionPanels() {
    ActionToolbar profileActions = Actions.createActionToolbar(profileActionsPanel, "DBNavigator.ActionGroup.AssistantChatBoxProfiles", "", true);
    this.profileActionsPanel.add(profileActions.getComponent(), BorderLayout.CENTER);

/*
    ActionToolbar helpActions = Actions.createActionToolbar(helpActionPanel, "DBNavigator.ActionGroup.AssistantChatBoxHelp", "", true);
    this.helpActionPanel.add(helpActions.getComponent(), BorderLayout.CENTER);
*/

    ActionToolbar typeActions = Actions.createActionToolbar(typeActionsPanel, "DBNavigator.ActionGroup.AssistantChatBoxTypes", "", true);
    this.typeActionsPanel.add(typeActions.getComponent(), BorderLayout.CENTER);

    ActionToolbar chatActions = Actions.createActionToolbar(chatActionsPanel, "DBNavigator.ActionGroup.AssistantChatBoxPrompt", "", true);
    this.chatActionsPanel.add(chatActions.getComponent(), BorderLayout.CENTER);
  }

  private void createInputField() {
    inputField = new ChatBoxInputField(this);
    inputFieldPanel.add(inputField, BorderLayout.CENTER);
  }

  private void restoreMessages() {
    List<PersistentChatMessage> messages = getAssistantState().getMessages();
    messageContainer.addAll(messages, this);
    Dispatch.run(() -> scrollConversationDown());
  }

  @NotNull
  public ConnectionHandler getConnection() {
    return connection.ensure();
  }

  public AssistantState getAssistantState() {
    return getManager().getAssistantState(getConnectionId());
  }

  public void acknowledgeIntro() {
    getAssistantState().setAcknowledgement(ENGAGED);
    initChatBoxForm();
    introPanel.setVisible(false);
    chatBoxPanel.setVisible(true);
  }

  public void selectProfile(AIProfileItem profile) {
    getAssistantState().setSelectedProfile(profile);
  }

  public void selectModel(ProviderModel model) {
    AIProfileItem profile = getAssistantState().getSelectedProfile();
    if (profile == null) return;

    profile.setModel(model);
  }

  public void selectAction(PromptAction action) {
    getAssistantState().setSelectedAction(action);
  }

  public void clearConversation() {
    messageContainer.clear();
    getAssistantState().clearMessages();
  }

  /**
   * Initializes the panel to display messages
   */
  private void configureConversationPanel() {
    chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
    messageContainer = new RollingMessageContainer(AssistantState.MAX_CHAR_MESSAGE_COUNT, chatPanel);
  }

  public void submitPrompt(String question) {
    Background.run(getProject(), () -> processQuery(question));
  }

  public void submitPrompt() {
    submitPrompt(null);
  }

  private void processQuery(String question) {
    AssistantState state = getAssistantState();
    if (!state.isPromptingAvailable()) return;

    AIProfileItem profile = state.getSelectedProfile();
    if (profile == null) return;

    question = nvl(question, inputField.getAndClearText());
    if (Strings.isEmptyOrSpaces(question)) return;

    state.set(QUERYING, true);
    inputField.setReadonly(true);
    ProviderModel model = profile.getModel();

    PromptAction actionType = state.getSelectedAction();

    ChatMessageContext context = new ChatMessageContext(profile.getName(), model, actionType);
    PersistentChatMessage inputChatMessage = new PersistentChatMessage(MessageType.NEUTRAL, question, AuthorType.USER, context);
    inputChatMessage.setProgress(true);
    appendMessageToChat(inputChatMessage);

    DatabaseAssistantManager manager = getManager();
    manager.queryAssistant(getConnectionId(), question, actionType.getId(), profile.getName(), model.getApiName())
        .thenAccept((output) -> {
          state.set(QUERYING, false);
          inputField.setReadonly(false);
          PersistentChatMessage outPutChatMessage = new PersistentChatMessage(MessageType.NEUTRAL, output, AuthorType.AGENT, context);
          appendMessageToChat(outPutChatMessage);
          log.debug("Query processed successfully.");
        })
        .exceptionally(e -> {
          state.set(QUERYING, false);
          inputField.setReadonly(false);
          log.warn("Error processing query", e);
          String message = manager.getPresentableMessage(getConnectionId(), e);
          PersistentChatMessage errorMessage = new PersistentChatMessage(MessageType.ERROR, message, AuthorType.SYSTEM, context);
          appendMessageToChat(errorMessage);
          return null;
        })
        .thenRun(() -> {
          //promptTextArea.setEnabled(true);
        });
  }


  /**
   * Updates profile combobox box model by fetching
   * list of available profiles for the current connection
   */
  public CompletableFuture<Map<String, Profile>> updateProfiles() {
    return getProfileService().list().thenApply(pl -> pl.stream()
        .collect(Collectors.toMap(Profile::getProfileName,
            Function.identity(),
            (existing, replacement) -> existing)));
  }


  public void reloadProfiles() {
    getProfileService().reset();
    loadProfiles();
  }

  /**
   * Initializes the profile dropdowns for the chat box
   */
  public void loadProfiles() {
    if (getAssistantState().is(INITIALIZING)) return;
    beforeProfileLoad();
    updateProfiles().thenAccept(profiles -> {
      try {
        applyProfiles(profiles);
        afterProfileLoad(null);
      } catch (Throwable t) {
        log.warn("Failed to fetch profiles", t);
        afterProfileLoad(t);
      }
    }).exceptionally(e -> {
      log.warn("Failed to fetch profiles", e);
      afterProfileLoad(e);
      return null;
    });
  }

  private void beforeProfileLoad() {
    initializingPanel.setVisible(true);
    inputField.setReadonly(true);
    AssistantState state = getAssistantState();
    state.set(INITIALIZING, true);
    state.set(UNAVAILABLE, false);
    notifyStateListeners();
  }

  private void afterProfileLoad(@Nullable Throwable e) {
    initializingPanel.setVisible(false);
    AssistantState state = getAssistantState();
    state.set(INITIALIZING, false);
    if (e != null) {
      state.set(UNAVAILABLE, true);
      showErrorHeader(e);
    }
    notifyStateListeners();

    inputField.setReadonly(!state.isPromptingAvailable());
    inputField.requestFocus();
    UserInterface.visitRecursively(chatBoxPanel,  c -> UserInterface.repaint(c));
  }

  private void showErrorHeader(Throwable cause) {
    // TODO show error bar (similar to editor error headers)
  }

  private void applyProfiles(Map<String, Profile> profiles) {
    AssistantState state = getAssistantState();
    List<AIProfileItem> profileItems = new ArrayList<>();
    String selectedProfile = state.getSelectedProfileName();
    profiles.forEach((pn, p) -> profileItems.add(new AIProfileItem(p, p.getProfileName().equalsIgnoreCase(selectedProfile))));
    state.setProfiles(profileItems);
    notifyStateListeners();
  }

  private void appendMessageToChat(PersistentChatMessage message) {
    List<PersistentChatMessage> messages = List.of(message);
    getAssistantState().addMessages(messages);
    Dispatch.run(() -> messageContainer.addAll(messages, this));
    Dispatch.run(() -> scrollConversationDown());
  }

  private void scrollConversationDown() {
    chatScrollPane.validate();
    JScrollBar verticalBar = chatScrollPane.getVerticalScrollBar();
    verticalBar.setValue(verticalBar.getMaximum());
  }

  private void notifyStateListeners() {
    getManager().notifyStateListeners(getConnectionId());
  }

  private ConnectionId getConnectionId() {
    return connection.getConnectionId();
  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }

  private DatabaseAssistantManager getManager() {
    Project project = ensureProject();
    return DatabaseAssistantManager.getInstance(project);
  }

  private AIProfileService getProfileService() {
    return AIProfileService.getInstance(getConnection());
  }

  @Nullable
  @Override
  public Object getData(@NotNull String dataId) {
    if (DataKeys.ASSISTANT_CHAT_BOX.is(dataId)) return this;
    return null;
  }
}
