package pg.gipter.core.config;

import org.bson.types.ObjectId;

public class GeneralSettings {

    public static final String COLLECTION_NAME = "settings";

    private ObjectId id;
    private String githubToken;

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }

    public String getGithubToken() {
        return githubToken;
    }

    public void setGithubToken(String githubToken) {
        this.githubToken = githubToken;
    }
}
