package com.dci.intellij.dbn.object.action;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Point;
import java.util.List;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.common.thread.TaskInstructions;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;

public abstract class ObjectListShowAction extends AnAction {
    protected DBObjectRef sourceObjectRef;
    protected RelativePoint popupLocation;

    public ObjectListShowAction(String text, DBObject sourceObject) {
        super(text);
        sourceObjectRef = DBObjectRef.from(sourceObject);
    }

    public void setPopupLocation(RelativePoint popupLocation) {
        this.popupLocation = popupLocation;
    }

    public @Nullable List<? extends DBObject> getRecentObjectList() {return null;}
    public abstract List<? extends DBObject> getObjectList();
    public abstract String getTitle();
    public abstract String getEmptyListMessage();
    public abstract String getListName();

    @NotNull
    public DBObject getSourceObject() {
        return DBObjectRef.getnn(sourceObjectRef);
    }

    public final void actionPerformed(@NotNull final AnActionEvent e) {
        TaskInstructions taskInstructions = new TaskInstructions("Loading " + getListName(), TaskInstruction.CANCELLABLE);
        DBObject sourceObject = getSourceObject();
        new ConnectionAction("loading " + getListName(), sourceObject, taskInstructions) {
            @Override
            protected void execute() {
                if (!isCancelled()) {
                    final List<? extends DBObject> recentObjectList = getRecentObjectList();
                    final List<? extends DBObject> objects = getObjectList();
                    if (!isCancelled()) {
                        new SimpleLaterInvocator() {
                            @Override
                            protected void execute() {
                                if (objects.size() > 0) {
                                    ObjectListActionGroup actionGroup = new ObjectListActionGroup(ObjectListShowAction.this, objects, recentObjectList);
                                    JBPopup popup = JBPopupFactory.getInstance().createActionGroupPopup(
                                            ObjectListShowAction.this.getTitle(),
                                            actionGroup,
                                            e.getDataContext(),
                                            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH,
                                            true, null, 10);

                                    popup.getContent().setBackground(Colors.LIGHT_BLUE);
                                    showPopup(popup);
                                }
                                else {
                                    JLabel label = new JLabel(getEmptyListMessage(), Icons.EXEC_MESSAGES_INFO, SwingConstants.LEFT);
                                    label.setBorder(JBUI.Borders.empty(3));
                                    JPanel panel = new JPanel(new BorderLayout());
                                    panel.add(label);
                                    panel.setBackground(Colors.LIGHT_BLUE);
                                    ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null);
                                    JBPopup popup = popupBuilder.createPopup();
                                    showPopup(popup);
                                }
                            }
                        }.start();
                    }
                }
            }
        }.start();
    }

    private void showPopup(JBPopup popup) {
        if (popupLocation == null) {
            DBObject sourceObject = getSourceObject();
            DatabaseBrowserManager browserManager = DatabaseBrowserManager.getInstance(sourceObject.getProject());
            DatabaseBrowserTree activeBrowserTree = browserManager.getActiveBrowserTree();
            if (activeBrowserTree != null) {
                popupLocation = TreeUtil.getPointForSelection(activeBrowserTree);
                Point point = popupLocation.getPoint();
                point.setLocation(point.getX() + 20, point.getY() + 4);
            }
        }
        if (popupLocation != null) {
            popup.show(popupLocation);
        }
    }

    @Override
    public void update(AnActionEvent e) {
        super.update(e);
    }

    protected abstract AnAction createObjectAction(DBObject object);
}
