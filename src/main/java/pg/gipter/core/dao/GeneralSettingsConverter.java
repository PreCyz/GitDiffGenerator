package pg.gipter.core.dao;

import org.bson.Document;
import pg.gipter.core.config.GeneralSettings;

public class GeneralSettingsConverter {

    public Document convert(GeneralSettings generalSettings) {
        Document document = new Document();
        document.put("_id", generalSettings.getId());
        document.put("githubToken", generalSettings.getGithubToken());
        return document;
    }

    public GeneralSettings convert(Document document) {
        GeneralSettings generalSettings = new GeneralSettings();
        generalSettings.setId(document.getObjectId("_id"));
        generalSettings.setGithubToken(document.getString("githubToken"));
        return generalSettings;
    }

}
