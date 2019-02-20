package com.dci.intellij.dbn.object.action;

import com.dci.intellij.dbn.browser.DatabaseBrowserManager;
import com.dci.intellij.dbn.browser.ui.DatabaseBrowserTree;
import com.dci.intellij.dbn.common.Colors;
import com.dci.intellij.dbn.common.Icons;
import com.dci.intellij.dbn.common.thread.SimpleLaterInvocator;
import com.dci.intellij.dbn.common.thread.TaskInstruction;
import com.dci.intellij.dbn.connection.ConnectionAction;
import com.dci.intellij.dbn.object.common.DBObject;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.DumbAwareAction;
import com.intellij.openapi.ui.popup.ComponentPopupBuilder;
import com.intellij.openapi.ui.popup.JBPopup;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.ui.awt.RelativePoint;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.tree.TreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.dci.intellij.dbn.common.thread.TaskInstructions.instructions;

public abstract class ObjectListShowAction extends DumbAwareAction {
    private DBObjectRef sourceObjectRef;
    private RelativePoint popupLocation;

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

    @Override
    public final void actionPerformed(@NotNull AnActionEvent e) {
        DBObject sourceObject = getSourceObject();
        String listName = getListName();
        ConnectionAction.invoke(
                instructions("Loading " + listName, TaskInstruction.CANCELLABLE),
                "loading " + listName,
                sourceObject,
                action -> {
                    if (!action.isCancelled()) {
                        List<? extends DBObject> recentObjectList = getRecentObjectList();
                        List<? extends DBObject> objects = getObjectList();
                        if (!action.isCancelled()) {
                            SimpleLaterInvocator.invoke(ModalityState.NON_MODAL, () -> {
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
                                } else {
                                    JLabel label = new JLabel(getEmptyListMessage(), Icons.EXEC_MESSAGES_INFO, SwingConstants.LEFT);
                                    label.setBorder(JBUI.Borders.empty(3));
                                    JPanel panel = new JPanel(new BorderLayout());
                                    panel.add(label);
                                    panel.setBackground(Colors.LIGHT_BLUE);
                                    ComponentPopupBuilder popupBuilder = JBPopupFactory.getInstance().createComponentPopupBuilder(panel, null);
                                    JBPopup popup = popupBuilder.createPopup();
                                    showPopup(popup);
                                }
                            });
                        }
                    }
                });
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
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
    }

    protected abstract AnAction createObjectAction(DBObject object);
}
