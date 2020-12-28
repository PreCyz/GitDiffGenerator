package pg.gipter.core.dao;

import org.bson.Document;
import pg.gipter.statistics.ExceptionDetails;

public class ExceptionDetailsConverter {

    public Document convert(ExceptionDetails exceptionDetails) {
        Document document = new Document();
        //document.put("_id", exceptionDetails.getId());
        document.put("cause", exceptionDetails.getCause());
        document.put("errorDate", exceptionDetails.getErrorDate());
        document.put("errorMsg", exceptionDetails.getErrorMsg());
        return document;
    }

    public ExceptionDetails convert(Document document) {
        ExceptionDetails exceptionDetails = new ExceptionDetails();
        //exceptionDetails.setId(document.getObjectId("_id"));
        exceptionDetails.setCause(document.getString("cause"));
        exceptionDetails.setErrorDate(document.getString("errorDate"));
        exceptionDetails.setErrorMsg(document.getString("errorMsg"));
        return exceptionDetails;
    }
}
