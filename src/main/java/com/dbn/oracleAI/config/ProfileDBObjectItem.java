package com.dbn.oracleAI.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.annotations.Expose;
import com.google.gson.reflect.TypeToken;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

import java.io.Reader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

/**
 * POJO class for profile object list
 */
@Getter
@AllArgsConstructor
@ToString
public class ProfileDBObjectItem {
    @NotNull
    @Expose
    public String owner;
    //profile object name, if null means all object of that owner
    @Expose
    public String name;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ProfileDBObjectItem that = (ProfileDBObjectItem) o;
    // object name and owner ae case in-sensitive in Oracle DB
    // name can be null

    return owner.equalsIgnoreCase(that.owner) && (name != null && name.equalsIgnoreCase(that.name));
  }


    @Override
    public int hashCode() {
        return Objects.hash(owner, name);
    }

    public boolean isEquivalentTo(DBObjectItem other) {
        if (!this.owner.equalsIgnoreCase(other.getOwner())) {
            return false;
        }
        // name in DBObjectItem are never null
        if (this.name != null && !this.name.equalsIgnoreCase(other.getName())) {
            return false;
        }
        return true;
    }

    public static List<ProfileDBObjectItem> fromStream(Reader reader) {
        return deserializer.fromJson(reader,listType);
    }

    private static final Type listType = new TypeToken<List<ProfileDBObjectItem>>() {}.getType();
    private static final Gson deserializer = new GsonBuilder().registerTypeAdapter(
            ProfileDBObjectItem.class,
            new ProfileDeserializer()).create();

    /**
     * Used to deserialized JSON coming for the remote server.
     * It seems that we cannot trust what returned and that Gson/lombok Notnull is not enough
     */
    private static class ProfileDeserializer implements JsonDeserializer<ProfileDBObjectItem> {
        @Override
        public ProfileDBObjectItem deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            JsonObject jObject = jsonElement.getAsJsonObject();
            JsonElement owner  = jObject.get("owner");
            if (owner == null || owner.getAsString().isEmpty()) {
                throw new JsonParseException("profile db object owner cannot be null nor empty");
            }

            String name = jObject.get("name") != null?jObject.get("name").getAsString():null;

            return new ProfileDBObjectItem(owner.getAsString(), name);
        }
    }
}
