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

package com.dbn.oracleAI.config.providers.ui;


import com.dbn.common.options.ui.CompositeConfigurationEditorForm;
import com.dbn.oracleAI.config.providers.AIProviderSettings;

import javax.swing.*;
import java.awt.*;


public class AIProvidersSettingsForm extends CompositeConfigurationEditorForm<AIProviderSettings> {
  private JPanel mainPanel;
  private JPanel credentialsPanel;

  public AIProvidersSettingsForm(AIProviderSettings settings) {
    super(settings);
    credentialsPanel.add(settings.getCredentialSettings().createComponent(), BorderLayout.CENTER);
  }

  @Override
  protected JComponent getMainComponent() {
    return mainPanel;
  }
}
