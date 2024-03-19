package com.dbn.connection.config;

import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ui.AICloudSettingsForm;
import com.dbn.connection.config.ui.AIProfileSettingsForm;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import static com.dbn.common.options.setting.Settings.getString;
import static com.dbn.common.options.setting.Settings.setString;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class AICloudSettings extends BasicProjectConfiguration<ConnectionSettings, AICloudSettingsForm> {
  private String profileName;
  private String credentialBoxName;
  private String provider;
  private String model;
  private String tables;

  AICloudSettings(ConnectionSettings parent) {
    super(parent);
  }

  @Override
  public String getDisplayName() {
    return "Oracle Companion Settings";
  }

  @Override
  public String getHelpTopic() {
    return "oracleCompanionSettings";
  }

  @NotNull
  @Override
  public AICloudSettingsForm createConfigurationEditor() {
    return new AICloudSettingsForm(this);
  }

  @Override
  public String getConfigElementName() {
    return "oracle-companion-settings";
  }

  @Override
  public void readConfiguration(Element element) {
    tables = getString(element, "tables", tables);
    profileName = getString(element, "profile-name", profileName);
    credentialBoxName = getString(element, "credential-box-name", credentialBoxName);
    provider = getString(element, "provider", provider);
    model = getString(element, "model", model);
  }

  @Override
  public void writeConfiguration(Element element) {
    setString(element, "tables", tables);
    setString(element, "profile-name", profileName);
    setString(element, "credential-box-name", credentialBoxName);
    setString(element, "provider", provider);
    setString(element, "model", model);
  }

  public ConnectionId getConnectionId() {
    return getParent().getConnectionId();
  }
}
