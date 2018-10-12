package pg.gipter.toolkit.configuration;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import pg.gipter.toolkit.ws.*;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
public class SharePointClient {

    private static final String WS_URL = "https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx";
    private static final String CALLBACK_URL = "http://schemas.microsoft.com/sharepoint/soap/";

    private WebServiceTemplate webServiceTemplate;
    private final ObjectFactory objectFactory;

    public SharePointClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
        objectFactory = new ObjectFactory();
    }

    public void getList() {
        final String actionName = "GetList";

        GetList request = buildGetListRequest();
        GetListResponse response = (GetListResponse) webServiceTemplate.marshalSendAndReceive(
                WS_URL,
                request,
                getSoapActionCallback(actionName)
        );

        for (Object content : response.getGetListResult().getContent()) {
            writeToFile(content, actionName + ".xml");
        }
    }

    private SoapActionCallback getSoapActionCallback(String actionName) {
        return new SoapActionCallback(CALLBACK_URL + actionName);
    }

    private void writeToFile(Object content, String fileName) {
        ElementNSImpl eleNsImplObject = (ElementNSImpl) content;
        Document document = eleNsImplObject.getOwnerDocument();
        String xmlStr = documentToString(document);
        System.out.println(xmlStr);

        try (FileWriter fileWriter = new FileWriter(fileName);
             BufferedWriter writer = new BufferedWriter(fileWriter) ) {

            writer.write(xmlStr);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String documentToString(final Document doc) {
        // outputs a DOM structure to plain String

        try {
            StringWriter sw = new StringWriter();
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.transform(new DOMSource(doc), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    public void getListAndView() {
        final String actionName = "GetListAndView";

        GetListAndView request = objectFactory.createGetListAndView();
        request.setListName("WorkItems");
        request.setViewName("");
        GetListAndViewResponse response = (GetListAndViewResponse) webServiceTemplate.marshalSendAndReceive(
                WS_URL,
                request,
                getSoapActionCallback(actionName)
        );

        for (Object content : response.getGetListAndViewResult().getContent()) {
            writeToFile(content, actionName + ".xml");
        }
    }

    private GetList buildGetListRequest() {
        GetList getListRequest = objectFactory.createGetList();
        getListRequest.setListName("WorkItems");
        return getListRequest;
    }

}
