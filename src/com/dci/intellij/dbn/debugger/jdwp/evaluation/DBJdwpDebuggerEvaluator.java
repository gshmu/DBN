package com.dci.intellij.dbn.debugger.jdwp.evaluation;

import java.util.List;
import org.jetbrains.annotations.NotNull;

import com.dci.intellij.dbn.common.util.CommonUtil;
import com.dci.intellij.dbn.debugger.common.evaluation.DBDebuggerEvaluator;
import com.dci.intellij.dbn.debugger.common.frame.DBDebugValue;
import com.dci.intellij.dbn.debugger.jdwp.frame.DBJdwpDebugStackFrame;
import com.dci.intellij.dbn.debugger.jdwp.frame.DBJdwpDebugValue;
import com.intellij.debugger.engine.JavaValue;
import com.intellij.xdebugger.evaluation.XDebuggerEvaluator;
import com.intellij.xdebugger.frame.XStackFrame;
import com.intellij.xdebugger.frame.XValue;
import com.intellij.xdebugger.frame.XValueNode;
import com.intellij.xdebugger.frame.XValuePlace;
import com.sun.jdi.Field;
import com.sun.jdi.Value;
import com.sun.tools.jdi.ClassTypeImpl;
import com.sun.tools.jdi.ObjectReferenceImpl;

public class DBJdwpDebuggerEvaluator extends DBDebuggerEvaluator<DBJdwpDebugStackFrame, DBJdwpDebugValue> {
    public DBJdwpDebuggerEvaluator(DBJdwpDebugStackFrame frame) {
        super(frame);
    }

    @Override
    public void computePresentation(@NotNull final DBJdwpDebugValue debugValue, @NotNull final XValueNode node, @NotNull XValuePlace place) {
        String variableName = debugValue.getVariableName();
        DBDebugValue parentValue = debugValue.getParentValue();
        String databaseVariableName = parentValue == null ? variableName : parentValue.getVariableName() + "." + variableName;

        XStackFrame underlyingFrame = debugValue.getStackFrame().getUnderlyingFrame();
        XDebuggerEvaluator evaluator = underlyingFrame.getEvaluator();
        //node.setPresentation(icon, null, "", childVariableNames != null);
        if (evaluator != null) {
            XDebuggerEvaluator.XEvaluationCallback evaluationCallback = new XDebuggerEvaluator.XEvaluationCallback() {
                @Override
                public void evaluated(@NotNull XValue result) {
                    try {
                        ObjectReferenceImpl value = (ObjectReferenceImpl) ((JavaValue) result).getDescriptor().getValue();
                        List<Field> fields = ((ClassTypeImpl) value.type()).fields();
                        String stringValue = "null";
                        String typeIdentifier = value.type().name();
                        String typeName = debugValue.getDebugProcess().getDebuggerInterface().getJdwpTypeName(typeIdentifier);

                        String typeLength = null;
                        for (Field field : fields) {
                            if (field.name().equals("_value")) {
                                Value fieldValue = value.getValue(field);
                                if  (fieldValue != null) {
                                    stringValue = fieldValue.toString();
                                }
                            } else if (field.name().equals("_maxLength")) {
                                Value fieldValue = value.getValue(field);
                                if  (fieldValue != null) {
                                    typeLength = fieldValue.toString();
                                }
                            }
                            else if (field.name().equals("_type")) {
                                Value fieldValue = value.getValue(field);
                                if  (fieldValue != null) {
                                    typeName = fieldValue.toString();
                                }
                                stringValue = "";
                            }
                        }
                        debugValue.setValue(stringValue);

                        if (typeLength != null) {
                            typeName = typeName + "(" + typeLength + ")";
                        }
                        debugValue.setType(typeName);
                    }catch (Exception e) {
                        debugValue.setValue("");
                        debugValue.setType("Error: " + e.getMessage());
                    }


                    node.setPresentation(
                            debugValue.getIcon(),
                            debugValue.getType(),
                            CommonUtil.nvl(debugValue.getValue(), "null"),
                            debugValue.getChildVariableNames() != null);
                }

                @Override
                public void errorOccurred(@NotNull String errorMessage) {
                    debugValue.setValue("");
                    debugValue.setType("could not resolve variable");
                    node.setPresentation(
                            debugValue.getIcon(),
                            debugValue.getType(),
                            CommonUtil.nvl(debugValue.getValue(), "null"),
                            debugValue.getChildVariableNames() != null);
                }
            };
            evaluator.evaluate(databaseVariableName.toUpperCase(), evaluationCallback, null);
        }
    }
}
