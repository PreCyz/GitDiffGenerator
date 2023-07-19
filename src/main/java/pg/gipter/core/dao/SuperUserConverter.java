package pg.gipter.core.dao;

import org.bson.Document;
import pg.gipter.users.SuperUser;

public class SuperUserConverter {

    public Document convert(SuperUser superUser) {
        Document document = new Document();
        document.put("_id", superUser.getId());
        document.put("username", superUser.getUsername());
        document.put("password", superUser.getPassword());
        return document;
    }

    public SuperUser convert(Document document) {
        SuperUser superUser = new SuperUser();
        superUser.setId(document.getObjectId("_id"));
        superUser.setUsername(document.getString("username"));
        superUser.setPassword(document.getString("password"));
        return superUser;
    }

}
