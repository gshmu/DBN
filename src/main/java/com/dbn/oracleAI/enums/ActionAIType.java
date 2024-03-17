package com.dbn.oracleAI.enums;

public enum ActionAIType {
  SHOWSQL("showsql"),
  NARRATE("narrate"),
  CHAT("chat");

  private final String action;

  ActionAIType(String action) {
    this.action = action;
  }

  public String getAction() {
    return action;
  }

  public static ActionAIType getByAction(String action) {
    for (ActionAIType type : values()) {
      if (type.getAction().equals(action)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Invalid action: " + action);
  }
}
