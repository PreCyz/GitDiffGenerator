package pg.gipter.core.dao;

import org.bson.Document;
import pg.gipter.core.model.CipherDetails;
import pg.gipter.users.SuperUser;

import java.util.Optional;

public class SuperUserConverter {

    public Document convert(SuperUser superUser) {
        final Document document = new Document();
        document.put("_id", superUser.getId());
        document.put("username", superUser.getUsername());
        document.put("password", superUser.getPassword());

        Optional.ofNullable(superUser.getCipherDetails())
                .ifPresent(cd -> document.put(CipherDetails.CIPHER_DETAILS, new CipherDetailsConverter().convert(cd)));
        return document;
    }

    public SuperUser convert(Document document) {
        final SuperUser superUser = new SuperUser();
        superUser.setId(document.getObjectId("_id"));
        superUser.setUsername(document.getString("username"));
        superUser.setPassword(document.getString("password"));
        Document cypherDetails = (Document) document.get(CipherDetails.CIPHER_DETAILS);
        Optional.ofNullable(document.get(CipherDetails.CIPHER_DETAILS))
                .map(obj -> (Document) obj)
                .ifPresent(cd -> superUser.setCipherDetails(new CipherDetailsConverter().convert(cypherDetails)));
        return superUser;
    }

}
