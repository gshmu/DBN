package com.dbn.oracleAI.enums;

import lombok.Getter;

@Getter
public enum ActionAIType {
  SHOWSQL("showsql"),
  NARRATE("narrate"),
  CHAT("chat");

  private final String action;

  ActionAIType(String action) {
    this.action = action;
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
