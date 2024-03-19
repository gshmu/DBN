package com.dbn.connection.config.ui;

import com.dbn.common.options.ui.ConfigurationEditorForm;
import com.dbn.connection.config.AICloudSettings;
import com.dbn.connection.config.AIProfileSettings;
import com.intellij.openapi.options.ConfigurationException;

import javax.swing.*;

public class AICloudSettingsForm extends ConfigurationEditorForm<AICloudSettings> {

  public AICloudSettingsForm(final AICloudSettings aiCloudSettings){
    super(aiCloudSettings);
  }

  @Override
  public void applyFormChanges() throws ConfigurationException {

  }

  @Override
  public void resetFormChanges() {

  }

  @Override
  protected JComponent getMainComponent() {
    return new JPanel();
  }
}
