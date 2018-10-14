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

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
public class SharePointClient {

    private static final Logger logger = LoggerFactory.getLogger(SharePointClient.class);

    private final String WS_URL = "https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx";
    private final String CALLBACK_URL = "http://schemas.microsoft.com/sharepoint/soap/";
    private final String WORK_ITEMS = "WorkItems";

    private final WebServiceTemplate webServiceTemplate;
    private final ObjectFactory objectFactory;

    SharePointClient(WebServiceTemplate webServiceTemplate) {
        this.webServiceTemplate = webServiceTemplate;
        objectFactory = new ObjectFactory();
    }

    public ListViewId getList() {
        GetList request = objectFactory.createGetList();
        request.setListName(WORK_ITEMS);

        GetListResponse response = (GetListResponse) webServiceTemplate.marshalSendAndReceive(
                WS_URL,
                request,
                getSoapActionCallback("GetList")
        );

        Object content = response.getGetListResult().getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) content;
            Document document = element.getOwnerDocument();

            Node list = document.getChildNodes().item(0);
            String listId = list.getAttributes().getNamedItem("Name").getNodeValue();
            logger.info("<listId, viewId> = <{}, {}>", listId, null);

            return new ListViewId(listId, null);
        }
        throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");
    }

    private SoapActionCallback getSoapActionCallback(String actionName) {
        return new SoapActionCallback(CALLBACK_URL + actionName);
    }

    public ListViewId getListAndView() {
        final String actionName = "GetListAndView";

        GetListAndView request = objectFactory.createGetListAndView();
        request.setListName(WORK_ITEMS);
        request.setViewName("");

        GetListAndViewResponse response = (GetListAndViewResponse) webServiceTemplate.marshalSendAndReceive(
                WS_URL,
                request,
                getSoapActionCallback(actionName)
        );

        Object content = response.getGetListAndViewResult().getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) content;
            Document document = element.getOwnerDocument();

            Node listAndViewNode = document.getChildNodes().item(0);
            String listId = listAndViewNode.getChildNodes().item(0).getAttributes().getNamedItem("Name").getNodeValue();
            String viewId = listAndViewNode.getChildNodes().item(1).getAttributes().getNamedItem("Name").getNodeValue();
            logger.info("<listId, viewId> = <{}, {}>%n", listId, viewId);

            return new ListViewId(listId, viewId);
        }
        logger.error("Weird response from toolkit. Response is not a xml.");
        throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");
    }

    public String updateListItems(ListViewId listViewId) {
        String batchElement = XmlHelper.buildBatchElement(listViewId.viewId());

        UpdateListItems request = objectFactory.createUpdateListItems();
        request.setListName(listViewId.listId());
        UpdateListItems.Updates updates = objectFactory.createUpdateListItemsUpdates();
        updates.getContent().add(batchElement);
        request.setUpdates(updates);

        UpdateListItemsResponse response = (UpdateListItemsResponse) webServiceTemplate.marshalSendAndReceive(
                WS_URL,
                request,
                getSoapActionCallback("UpdateListItems")
        );

        Object content = response.getUpdateListItemsResult().getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) content;
            Document document = element.getOwnerDocument();
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
        request.setListName(WORK_ITEMS);
        request.setAttachment(attachment);

        AddAttachmentResponse response = (AddAttachmentResponse) webServiceTemplate.marshalSendAndReceive(
                WS_URL,
                request,
                getSoapActionCallback("AddAttachment")
        );

        logger.error("Diff upload status {}", response.getAddAttachmentResult());
    }
}
