package com.dci.intellij.dbn.execution.explain.ui;

import javax.swing.JTree;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import java.math.BigDecimal;
import java.util.List;
import org.jetbrains.generate.tostring.util.StringUtil;

import com.dci.intellij.dbn.execution.explain.ExplainPlanEntry;
import com.dci.intellij.dbn.execution.explain.ExplainPlanResult;
import com.dci.intellij.dbn.object.lookup.DBObjectRef;
import com.intellij.ui.treeStructure.treetable.TreeTableModel;

public class ExplainPlanTreeTableModel implements TreeTableModel{
    private ExplainPlanResult result;

    public ExplainPlanTreeTableModel(ExplainPlanResult result) {
        this.result = result;
    }

    private static Column[] COLUMNS = new Column[]{
            new Column("OPERATION", String.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    String options = entry.getOperationOptions();
                    return entry.getOperation() + (StringUtil.isEmpty(options) ? "" : "(" + options + ")");
                }
            },
            new Column("OBJECT", DBObjectRef.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getObjectRef();
                }
            },
            new Column("DEPTH", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getDepth();
                }
            },
            new Column("POSITION", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getPosition();
                }
            },
            new Column("CARDINALITY", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getCardinality();
                }
            },
            new Column("BYTES", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getBytes();
                }
            },
            new Column("COST", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getCost();
                }
            },
            new Column("CPU_COST", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getCpuCost();
                }
            },
            new Column("IO_COST", BigDecimal.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getIoCost();
                }
            },
            new Column("ACCESS_PREDICATES", String.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getAccessPredicates();
                }
            },
            new Column("FILTER_PREDICATES", String.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getFilterPredicates();
                }
            },
            new Column("PROJECTION", String.class) {
                @Override
                public Object getValue(ExplainPlanEntry entry) {
                    return entry.getProjection();
                }
            }

    };

    /***************************************************************
     *                         TableModel                          *
     ***************************************************************/

    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName(int column) {
        return COLUMNS[column].getName();
    }

    @Override
    public Class getColumnClass(int column) {
        return COLUMNS[column].getClazz();
    }

    @Override
    public Object getValueAt(Object node, int column) {
        if (node instanceof ExplainPlanEntry) {
            ExplainPlanEntry entry = (ExplainPlanEntry) node;
            COLUMNS[column].getValue(entry);
        }
        return null;
    }

    @Override public boolean isCellEditable(Object node, int column) {return false;}
    @Override public void setValueAt(Object aValue, Object node, int column) {}
    @Override public void setTree(JTree tree) {}

    /***************************************************************
     *                          TreeModel                          *
     ***************************************************************/
    @Override
    public Object getRoot() {
        return result.getRoot();
    }

    @Override
    public Object getChild(Object parent, int index) {
        if (parent instanceof ExplainPlanEntry) {
            ExplainPlanEntry entry = (ExplainPlanEntry) parent;
            entry.getChildren().get(index);
        }
        return null;
    }

    @Override
    public int getChildCount(Object parent) {
        if (parent instanceof ExplainPlanEntry) {
            ExplainPlanEntry entry = (ExplainPlanEntry) parent;
            List<ExplainPlanEntry> children = entry.getChildren();
            return children == null ? 0 : children.size();
        }
        return 0;
    }

    @Override
    public boolean isLeaf(Object node) {
        return getChildCount(node) == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {}

    @Override
    public int getIndexOfChild(Object parent, Object child) {
        if (parent instanceof ExplainPlanEntry && child instanceof ExplainPlanEntry) {
            ExplainPlanEntry parentEntry = (ExplainPlanEntry) parent;
            ExplainPlanEntry childEntry = (ExplainPlanEntry) child;
            return parentEntry.getChildren().indexOf(childEntry);
        }
        return -1;
    }

    @Override public void addTreeModelListener(TreeModelListener l) {}
    @Override public void removeTreeModelListener(TreeModelListener l) {}

    public static abstract class Column {
        private String name;
        private Class clazz;

        public Column(String name, Class clazz) {
            this.name = name;
            this.clazz = clazz;
        }

        public String getName() {
            return name;
        }

        public Class getClazz() {
            return clazz;
        }

        public abstract Object getValue(ExplainPlanEntry entry);
    }
}
