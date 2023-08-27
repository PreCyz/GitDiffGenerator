package pg.gipter.core.dao;

import org.bson.Document;
import pg.gipter.core.model.CipherDetails;

public class CipherDetailsConverter {

    public Document convert(CipherDetails cipherDetails) {
        Document document = new Document();
        document.put("_id", cipherDetails.getId());
        document.put("cipherName", cipherDetails.getCipherName());
        document.put("iterationCount", cipherDetails.getIterationCount());
        document.put("keySpecValue", cipherDetails.getKeySpecValue());
        document.put("saltValue", cipherDetails.getSaltValue());
        return document;
    }

    public CipherDetails convert(Document document) {
        CipherDetails cipherDetails = new CipherDetails();
        cipherDetails.setId(document.getObjectId("_id"));
        cipherDetails.setCipherName(document.getString("cipherName"));
        cipherDetails.setIterationCount(document.getInteger("iterationCount"));
        cipherDetails.setKeySpecValue(document.getString("keySpecValue"));
        cipherDetails.setSaltValue(document.getString("saltValue"));
        return cipherDetails;
    }

}
