package com.dbn.oracleAI.ui;

import lombok.Getter;
import lombok.Setter;
import org.jdom.Element;

import java.util.Objects;

@Setter
@Getter
public class OracleAIChatBoxState {
  private String selectedOption = "showsql";
  private String inputText;
  private String displayText;
  private String currConnection;

  public OracleAIChatBoxState(String connection) {
    setCurrConnection(connection);
  }

  public Element toElement() {
    Element stateElement = new Element("OracleAIChatBoxState");
    stateElement.addContent(new Element("currConnection").setText(Objects.toString(currConnection, "")));
    stateElement.addContent(new Element("selectedOption").setText(Objects.toString(selectedOption, "")));
    stateElement.addContent(new Element("inputText").setText(Objects.toString(inputText, "")));
    stateElement.addContent(new Element("displayText").setText(Objects.toString(displayText, "")));
    return stateElement;
  }

  public static OracleAIChatBoxState fromElement(Element stateElement) {
    OracleAIChatBoxState state = new OracleAIChatBoxState(stateElement.getChildText("currConnection"));
    state.setSelectedOption(stateElement.getChildText("selectedOption"));
    state.setInputText(stateElement.getChildText("inputText"));
    state.setDisplayText(stateElement.getChildText("displayText"));
    return state;
  }
}
