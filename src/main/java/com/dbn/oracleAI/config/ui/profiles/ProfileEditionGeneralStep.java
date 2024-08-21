package com.dbn.oracleAI.config.ui.profiles;

import com.dbn.connection.ConnectionHandler;
import com.dbn.oracleAI.AICredentialService;
import com.dbn.oracleAI.config.Credential;
import com.dbn.oracleAI.config.Profile;
import com.dbn.oracleAI.config.ui.ProfileNameVerifier;
import com.intellij.icons.AllIcons;
import com.intellij.ui.wizard.WizardNavigationState;
import com.intellij.ui.wizard.WizardStep;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.event.ItemEvent;
import java.util.List;

import static com.dbn.nls.NlsResources.txt;

/**
 * Profile edition general step for edition wizard.
 *
 * @see com.dbn.oracleAI.ProfileEditionWizard
 */
public class ProfileEditionGeneralStep extends WizardStep<ProfileEditionWizardModel> {
  private JPanel profileEditionGeneralMainPane;
  private JTextField nameTextField;
  private JComboBox<String> credentialComboBox;
  private JTextField descriptionTextField;
  private final AICredentialService credentialSvc;

  private final Profile profile;
  private final List<String> existingProfileNames;

  private final boolean isUpdate;

  public ProfileEditionGeneralStep(ConnectionHandler connection, Profile profile, List<String> existingProfileNames, boolean isUpdate) {
    super(txt("profile.mgmt.general_step.title"),
            txt("profile.mgmt.general_step.explaination"),
            AllIcons.General.Settings);
    this.profile = profile;
    this.existingProfileNames = existingProfileNames;
    this.isUpdate = isUpdate;

    this.credentialSvc = AICredentialService.getInstance(connection);

    initializeUI();
    addValidationListener();
    if (!isUpdate) populateCredentials();
  }

  private void initializeUI() {
    if (isUpdate) {
      nameTextField.setText(profile.getProfileName());
      descriptionTextField.setText(profile.getDescription());
      credentialComboBox.addItem(profile.getCredentialName());
      nameTextField.setEnabled(false);
      credentialComboBox.setEnabled(true);
      descriptionTextField.setEnabled(false);
    }
  }

  private void addValidationListener() {
    nameTextField.setInputVerifier(new ProfileNameVerifier(existingProfileNames, isUpdate));
    credentialComboBox.setInputVerifier(new CredentialSelectedVerifier());
    nameTextField.getDocument().addDocumentListener(new DocumentListener() {
      public void changedUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }

      public void removeUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }

      public void insertUpdate(DocumentEvent e) {
        nameTextField.getInputVerifier().verify(nameTextField);
      }
    });
    credentialComboBox.addItemListener(e -> {
      if (e.getStateChange() == ItemEvent.SELECTED) {
        InputVerifier verifier = credentialComboBox.getInputVerifier();
        if (verifier != null) {
          verifier.verify(credentialComboBox);
        }
      }
    });

  }

  private void populateCredentials() {
    credentialSvc.getCredentials().thenAccept(credentialProviderList -> {
      SwingUtilities.invokeLater(() -> {
        credentialComboBox.removeAllItems();
        for (Credential credential : credentialProviderList) {
          credentialComboBox.addItem(credential.getCredentialName());
        }
        credentialComboBox.setSelectedIndex(0);
      });
    });
  }

  @Override
  public JComponent prepare(WizardNavigationState wizardNavigationState) {
    return profileEditionGeneralMainPane;
  }

  @Override
  public @Nullable JComponent getPreferredFocusedComponent() {
    return nameTextField;
  }

  @Override
  public WizardStep<ProfileEditionWizardModel> onNext(ProfileEditionWizardModel model) {
    nameTextField.getInputVerifier().verify(nameTextField);
    credentialComboBox.getInputVerifier().verify(credentialComboBox);
    profile.setProfileName(nameTextField.getText());
    profile.setCredentialName((String) credentialComboBox.getSelectedItem());
    // special case for description: null and empty string is the same
    //    do not confuse Profile.equals() because of that
    if (descriptionTextField.getText().isEmpty()) {
      // did the user really remove the description or was it missing
      // from the beginning ?
      if (profile.getDescription() != null && !profile.getDescription().isEmpty()) {
        profile.setDescription(descriptionTextField.getText());
      }
    } else {
      // set it in any case
      profile.setDescription(descriptionTextField.getText());
    }



    return super.onNext(model);
  }

}
