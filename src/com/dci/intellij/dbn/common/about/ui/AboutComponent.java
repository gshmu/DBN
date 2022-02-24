package com.dci.intellij.dbn.common.about.ui;

import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.ui.form.DBNFormBase;
import com.dci.intellij.dbn.common.ui.listener.PopupCloseListener;
import com.dci.intellij.dbn.common.ui.util.Borders;
import com.dci.intellij.dbn.common.ui.util.Mouse;
import com.intellij.ide.BrowserUtil;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.editor.colors.CodeInsightColors;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class AboutComponent extends DBNFormBase {
    public static final String PAYPAL_URL = "https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=3QAPZFCCARA4J";
    private JPanel mainPanel;
    private JLabel splashLabel;
    private JLabel donateLabel;
    private JLabel downloadPageLinkLabel;
    private JLabel supportPageLinkLabel;
    private JLabel requestTrackerPageLinkLabel;
    private JLabel buildLabel;
    private JPanel linksPanel;

    public AboutComponent(Project project) {
        super(null, project);
        Cursor handCursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);

        splashLabel.setIcon(Icons.DATABASE_NAVIGATOR);
        splashLabel.setText("");
        linksPanel.setBorder(Borders.BOTTOM_LINE_BORDER);

        donateLabel.setIcon(Icons.DONATE_DISABLED);
        donateLabel.setText("");
        donateLabel.setCursor(handCursor);

        donateLabel.addMouseListener(Mouse.listener().
                onClick(e -> BrowserUtil.browse(PAYPAL_URL)).
                onEnter(e -> donateLabel.setIcon(Icons.DONATE)).
                onExit(e -> donateLabel.setIcon(Icons.DONATE_DISABLED)));

        downloadPageLinkLabel.setForeground(CodeInsightColors.HYPERLINK_ATTRIBUTES.getDefaultAttributes().getForegroundColor());
        downloadPageLinkLabel.setCursor(handCursor);
        downloadPageLinkLabel.addMouseListener(Mouse.listener().onClick(e ->
                BrowserUtil.browse("http://plugins.jetbrains.com/plugin/?id=1800")));

        supportPageLinkLabel.setForeground(CodeInsightColors.HYPERLINK_ATTRIBUTES.getDefaultAttributes().getForegroundColor());
        supportPageLinkLabel.setCursor(handCursor);
        supportPageLinkLabel.addMouseListener(Mouse.listener().onClick(e ->
                BrowserUtil.browse("http://confluence.jetbrains.com/display/CONTEST/Database+Navigator")));

        requestTrackerPageLinkLabel.setForeground(CodeInsightColors.HYPERLINK_ATTRIBUTES.getDefaultAttributes().getForegroundColor());
        requestTrackerPageLinkLabel.setCursor(handCursor);
        requestTrackerPageLinkLabel.addMouseListener(Mouse.listener().onClick(e ->
                BrowserUtil.browse("https://database-navigator.atlassian.net/issues/?filter=10104")));

        IdeaPluginDescriptor ideaPluginDescriptor = PluginManager.getPlugin(PluginId.getId("DBN"));
        String version = ideaPluginDescriptor == null ? "3.1.9999.0" : ideaPluginDescriptor.getVersion();
        buildLabel.setText("Build: " + version.substring(4, 8));
    }

    @NotNull
    @Override
    public JPanel getMainComponent() {
        return mainPanel;
    }

    public void showPopup(Project project) {
        ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(mainPanel, null);
        JBPopup popup = popupBuilder.createPopup();
        popup.addListener(PopupCloseListener.create(this));
        popup.showCenteredInCurrentWindow(project);

    }
}
