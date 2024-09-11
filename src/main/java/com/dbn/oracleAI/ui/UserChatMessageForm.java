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

package com.dbn.oracleAI.ui;

import com.dbn.common.ui.util.Borders;
import com.dbn.common.util.Actions;
import com.dbn.oracleAI.model.PersistentChatMessage;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.ui.JBColor;
import com.intellij.util.ui.JBUI;

import javax.swing.*;
import java.awt.*;

public class UserChatMessageForm extends ChatMessageForm {
    private static final Color BACKGROUND = new JBColor(new Color(218, 234, 255), new Color(68, 95, 128));

    public JPanel mainPanel;
    private JProgressBar progressBar;
    private JPanel actionPanel;
    private JTextPane messageTextPane;

    public UserChatMessageForm(ChatBoxForm parent, PersistentChatMessage message) {
        super(parent);
        messageTextPane.setText(message.getContent());
        progressBar.setVisible(message.isProgress());
        progressBar.setIndeterminate(true);

        initLayout();
        initActionToolbar(message);
    }

    private void initLayout() {
        actionPanel.setBorder(JBUI.Borders.empty(4));
        progressBar.setBorder(JBUI.Borders.empty(0, 8, 8, 8));
    }

    private void createUIComponents() {
        mainPanel = new MessagePanel();
        mainPanel.setBackground(getBackground());
    }

    @Override
    protected JComponent getMainComponent() {
        return mainPanel;
    }

    @Override
    protected Color getBackground() {
        return BACKGROUND;
    }

    private void initActionToolbar(PersistentChatMessage message) {
        ActionToolbar actionToolbar = Actions.createActionToolbar(actionPanel, "", false, new CopyContentAction(message.getContent()));
        JComponent component = actionToolbar.getComponent();
        component.setOpaque(false);
        component.setBorder(Borders.EMPTY_BORDER);
        actionPanel.add(component, BorderLayout.NORTH);
    }
}
