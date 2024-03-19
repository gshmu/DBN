package com.dbn.connection.config;

import com.dbn.common.options.BasicProjectConfiguration;
import com.dbn.connection.ConnectionId;
import com.dbn.connection.config.ui.AICredentialSettingsForm;
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
public class AICredentialSettings extends BasicProjectConfiguration<ConnectionSettings, AICredentialSettingsForm> {
  private String credentialName;
  private String apiUsername;
  private String apiPassword;

  AICredentialSettings(ConnectionSettings parent) {
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
  public AICredentialSettingsForm createConfigurationEditor() {
    return new AICredentialSettingsForm(this);
  }

  @Override
  public String getConfigElementName() {
    return "oracle-companion-settings";
  }

  @Override
  public void readConfiguration(Element element) {
    credentialName = getString(element, "credential-name", credentialName);
    apiUsername = getString(element, "api-username", apiUsername);
    apiPassword = Passwords.decodePassword(getString(element, "api-password", apiPassword));
  }

  @Override
  public void writeConfiguration(Element element) {
    setString(element, "credential-name", credentialName);
    setString(element, "api-username", apiUsername);
    setString(element, "api-password", Passwords.encodePassword(apiPassword));
  }

  public ConnectionId getConnectionId() {
    return getParent().getConnectionId();
  }
}
