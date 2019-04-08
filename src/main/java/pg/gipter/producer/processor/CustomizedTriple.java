package pg.gipter.producer.processor;

import com.google.gson.JsonObject;

class CustomizedTriple {
    private final String project;
    private final String guid;
    private final JsonObject versions;

    CustomizedTriple(String project, String guid, JsonObject versions) {
        this.project = project;
        this.guid = guid;
        this.versions = versions;
    }

    public String getProject() {
        return project;
    }

    public String getGuid() {
        return guid;
    }

    public JsonObject getVersions() {
        return versions;
    }
}
