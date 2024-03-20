package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.ProviderType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Profile implements AttributeInput {

  private static final Gson gson = new GsonBuilder()
      .excludeFieldsWithoutExposeAnnotation()
      .create();

  private String profileName;
  private String description;

  @Expose private ProviderType provider;
  @Expose private String credentialName;
  @Expose private List<ObjectListItem> objectList;
  private Integer maxTokens;
  private List<String> stopTokens;
  @Expose private String model;
  private Double temperature;
  private Boolean comments;

  @Setter
  @Getter
  public static class ObjectListItem {
    private String owner;
    private String name;

    public ObjectListItem(String owner, String name) {
      this.owner = owner;
      this.name = name;
    }
  }

  @Override
  public boolean isValid() {
    return provider != null && credentialName != null;
  }

  @Override
  public String format() throws IllegalArgumentException {
    if (!isValid()) {
      throw new IllegalArgumentException("Invalid profile attributes.");
    }

    String attributesJson = gson.toJson(this).replace("'", "''");

    return String.format(
        "profile_name => '%s',\n" +
            "attributes => '%s',\n" +
            "description => '%s'",
        profileName,
        attributesJson,
        description
    );
  }
}
