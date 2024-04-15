package com.dbn.oracleAI;

import com.dbn.oracleAI.config.ui.profiles.ProfileEditionGeneralStep;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionObjectListStep;
import com.dbn.oracleAI.config.ui.profiles.ProfileEditionProviderStep;
import lombok.Getter;
import lombok.ToString;

import javax.swing.JPanel;
import java.awt.CardLayout;
import java.util.LinkedList;

/**
 * Wizard for profile edition or creation.
 * This wizard is used to walk through all configuration steps
 * for profile edition or creation
 */
public class ProfileEditionWizard {


  private LinkedList<ProfileEditionWizardStep> steps;

  /**
   * Gets a view on wizards steps.
   * @return
   */
  public WizardStepView<ProfileEditionWizardStep> getView() {
    return new WizardStepView<>(this.steps);
  }

  /**
   * Populates this wizards on a panel.
   * The given panel is expected to be layout'ed by a <code>CardLayout</code>.
   * This panel (and so underneath CardLayout) will be populated with this wizard steps
   * @param panel a CardLayout'ed panel.
   */
  public void populateTo(JPanel panel) {
    assert panel.getLayout().getClass().equals(CardLayout.class):"wrong panel given ";

    this.steps.forEach(step -> {
      panel.add(step.getViewPort(),step.getTitle());
    });

  }

  /**
   * Step for profile edition.
   */
  @Getter @ToString static public class ProfileEditionWizardStep
    implements WizardStep {
    private String title;
    private WizardStepViewPortProvider provider;

    /**
     * Creates a new step
     * @param title title of the new step
     * @param provider a provider that provide the UI view (JPanel) of this step
     */
    public ProfileEditionWizardStep(String title,
                                    WizardStepViewPortProvider provider) {
      assert title != null:"title cannot be null";
      assert provider != null:"provider cannot be null";
      this.title = title;
      this.provider = provider;
    }

    /**
     * Gets the view port of this step
     * @return a ui panel that display all attribute of this step
     */
    @Override public JPanel getViewPort() {
      return this.provider.getPanel();
    }
  }

  /**
   * Creates a new wizrd
   */
  public ProfileEditionWizard() {
    // fake it for now
    this.steps = new LinkedList<>();
    this.steps.add(new ProfileEditionWizardStep("general",
                                                new ProfileEditionGeneralStep()));
    this.steps.add(new ProfileEditionWizardStep("provider",
                                                new ProfileEditionProviderStep()));
    this.steps.add(new ProfileEditionWizardStep("object list",
                                                new ProfileEditionObjectListStep()));

  }
}
