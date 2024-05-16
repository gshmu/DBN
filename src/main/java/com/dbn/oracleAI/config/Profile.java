package com.dbn.oracleAI.config;

import com.dbn.oracleAI.types.ProviderModel;
import com.dbn.oracleAI.types.ProviderType;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;
import com.intellij.openapi.diagnostic.Logger;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Type;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

@Getter
@Setter
@Builder
@ToString
public class Profile implements AttributeInput {

  private static final Logger LOGGER = Logger.getInstance("com.dbn.oracleAI");

  private final String PROFILE_NAME_ATTR_NAME = "name";
  private final String PROFILE_OWNER_ATTR_NAME = "owner";
  @NotNull
  private String profileName;

  private String description;

  @Expose
  private ProviderType provider;
  @SerializedName("credential_name")
  @Expose
  private String credentialName;
  @Builder.Default
  @SerializedName("object_list")
  @Expose
  private List<ProfileDBObjectItem> objectList = Collections.emptyList();
  private Integer maxTokens;
  @Builder.Default
  private List<String> stopTokens = Collections.emptyList();
  @Expose
  @JsonAdapter(ProviderModelSerializer.class)
  private ProviderModel model;
  @Builder.Default
  @Expose
  private Double temperature = 0.0;
  private boolean isEnabled;
  private Boolean comments;

  @Override
  public void validate() {
    // TODO implement this
  }

  @Override
  public String toAttributeMap() throws IllegalArgumentException {
    Gson gson = new GsonBuilder()
        .excludeFieldsWithoutExposeAnnotation()
        .registerTypeAdapter(ProviderModel.class, new ProviderModelSerializer())
        .create();

    String attributesJson = gson.toJson(this).replace("'", "''");

    return String.format(
        "profile_name => '%s',\n" +
            "attributes => '%s',\n" +
            "description => '%s'\n",
        profileName,
        attributesJson,
        description);
  }

   public static Object clobToObject(String attributeName, Clob clob) throws SQLException ,IOException ,JsonParseException {

    try (Reader reader = clob.getCharacterStream();
         BufferedReader br = new BufferedReader(reader)) {
      if ("object_list".equals(attributeName)) {
        return ProfileDBObjectItem.fromStream(br);
      } else {
        StringBuilder sb = new StringBuilder();
        int b;
        while (-1 != (b = br.read())) {
          sb.append((char) b);
        }
        return sb.toString();
      }
    }
  }

  // Inner class to handle the JSON serialization
  private static class ProviderModelSerializer implements JsonSerializer<ProviderModel> {
    @Override
    public JsonElement serialize(ProviderModel src, Type typeOfSrc, JsonSerializationContext context) {
      return new JsonPrimitive(src.getApiName());
    }
  }
}
