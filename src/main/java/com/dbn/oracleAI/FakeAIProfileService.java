package com.dbn.oracleAI;

import com.dbn.oracleAI.config.Profile;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class FakeAIProfileService implements AIProfileService {

    Type PROFILE_TYPE = new TypeToken<List<Profile>>() {
    }.getType();
    String profilesRepoFilename = System.getProperty("fake.services.profile.dump", "/var/tmp/profiles.json");
    //keep track of profiles
    // no synch needed
    List<Profile> profiles = null;

    @Override
    public CompletableFuture<Profile> get(String uuid) {
        assert false:"implement this !";
        return null;
    }

    @Override
    public CompletableFuture<List<Profile>> list() {
        if (profiles == null) {
            try {
                this.profiles = new Gson().fromJson(new FileReader(profilesRepoFilename), PROFILE_TYPE);
            } catch (FileNotFoundException e) {
                throw new RuntimeException("cannot read profile list " + e.getMessage());
            }
        }
        return CompletableFuture.completedFuture(this.profiles);
    }


    @Override
    public CompletableFuture<Void> delete(String profileName) {
        this.profiles.removeIf(profile -> profile.getProfileName().equalsIgnoreCase(profileName));
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> create(Profile profile) {
        this.profiles.add(profile);
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public CompletionStage<Void> update(Profile updatedProfile) {
        this.profiles.removeIf(p -> p.getProfileName().equalsIgnoreCase(updatedProfile.getProfileName()));
        this.profiles.add(updatedProfile);
        return CompletableFuture.completedFuture(null);
    }

}
