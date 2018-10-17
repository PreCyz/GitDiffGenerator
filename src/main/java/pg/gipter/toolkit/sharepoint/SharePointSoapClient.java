package pg.gipter.toolkit.sharepoint;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.toolkit.ws.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
public class SharePointSoapClient {

    private static final Logger logger = LoggerFactory.getLogger(SharePointSoapClient.class);

    private final WebServiceTemplate webServiceTemplate;
    private final String wsUrl;
    private final String listName;
    private final ObjectFactory objectFactory;

    SharePointSoapClient(WebServiceTemplate webServiceTemplate, String wsUrl, String listName) {
        this.webServiceTemplate = webServiceTemplate;
        this.wsUrl = wsUrl;
        this.listName = listName;
        objectFactory = new ObjectFactory();
    }

    private SoapActionCallback getSoapActionCallback(String actionName) {
        return new SoapActionCallback("http://schemas.microsoft.com/sharepoint/soap/" + actionName);
    }

    public ListViewId getListAndView() {
        GetListAndView request = objectFactory.createGetListAndView();
        request.setListName(listName);
        request.setViewName("");

        GetListAndViewResponse response = (GetListAndViewResponse) webServiceTemplate.marshalSendAndReceive(
                wsUrl,
                request,
                getSoapActionCallback("GetListAndView")
        );

        Object content = response.getGetListAndViewResult().getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) content;
            Document document = element.getOwnerDocument();

            //XmlHelper.documentToXmlFile(document, "GetListAndView.xml");

            Node listAndViewNode = document.getChildNodes().item(0);
            String listId = listAndViewNode.getChildNodes().item(0).getAttributes().getNamedItem("Name").getNodeValue();
            String viewId = listAndViewNode.getChildNodes().item(1).getAttributes().getNamedItem("Name").getNodeValue();
            logger.info("<listId, viewId> = <{}, {}>%n", listId, viewId);

            return new ListViewId(listId, viewId);
        }
        logger.error("Weird response from toolkit. Response is not a xml.");
        throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");
    }

    public String updateListItems(ListViewId listViewId, String title, String employee) {
        Map<String, String> itemAttributes = new HashMap<>();
        itemAttributes.put("Title", title);
        itemAttributes.put("Employee", employee);
        itemAttributes.put("SubmissionDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-M-dd'T'HH:mm:ss'Z'")));
        itemAttributes.put("Classification", "12");
        itemAttributes.put("Body", "Git diff file");

        //Building the CAML query with one item to add, and printing request
        BatchElement batchElement = new BatchElement(BatchElement.Mode.CREATE, listViewId.viewId());
        batchElement.init();
        batchElement.createListItem(itemAttributes);
        logger.info("REQUEST: {}", XmlHelper.documentToString(batchElement.getRootDocument()));

        UpdateListItems.Updates updates = objectFactory.createUpdateListItemsUpdates();
        //Preparing the request for the update
        Object docObj = batchElement.getRootDocument().getDocumentElement();
        updates.getContent().add(0, docObj);

        UpdateListItems request = objectFactory.createUpdateListItems();
        request.setListName(listViewId.listId());
        request.setUpdates(updates);

        UpdateListItemsResponse response = (UpdateListItemsResponse) webServiceTemplate.marshalSendAndReceive(
                wsUrl,
                request,
                getSoapActionCallback("UpdateListItems")
        );

        Object content = response.getUpdateListItemsResult().getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) content;
            Document document = element.getOwnerDocument();
            XmlHelper.documentToXmlFile(document, "updateList.xml");
            Optional<String> errorCode = XmlHelper.extractValue(document, "ErrorCode");
            if (errorCode.isPresent() && !"0x00000000".equals(errorCode.get())) {
                Optional<String> errorText = XmlHelper.extractValue(document, "ErrorText");
                errorText.ifPresent(txt -> {
                    logger.error("Error when UpdateListItems. Response error code {} with message {}", errorCode.get(), txt);
                    throw new RuntimeException(errorText.get());
                });
                throw new RuntimeException(errorCode.get());
            }
            return XmlHelper.extractListItemId(document);
        }
        logger.error("Weird response from toolkit. Response is not a xml.");
        throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");
    }

    public void addAttachment(String listItemId, String fileName, String attachmentPath) {
        byte[] attachment;
        try (InputStream is = new FileInputStream(attachmentPath)) {

            attachment = IOUtils.toByteArray(is);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException();
        }

        AddAttachment request = objectFactory.createAddAttachment();
        request.setListItemID(listItemId);
        request.setFileName(fileName);
        request.setListName(listName);
        request.setAttachment(attachment);

        AddAttachmentResponse response = (AddAttachmentResponse) webServiceTemplate.marshalSendAndReceive(
                wsUrl,
                request,
                getSoapActionCallback("AddAttachment")
        );

        logger.error("Diff upload status {}", response.getAddAttachmentResult());
    }
}
