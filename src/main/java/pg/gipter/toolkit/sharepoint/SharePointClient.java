package pg.gipter.toolkit.sharepoint;

import com.sun.org.apache.xerces.internal.dom.ElementNSImpl;
import org.apache.commons.io.IOUtils;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.toolkit.ws.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**Created by Pawel Gawedzki on 12-Oct-2018.*/
public class SharePointClient {

    private final String WS_URL = "https://goto.netcompany.com/cases/GTE106/NCSCOPY/_vti_bin/lists.asmx";
    private final String CALLBACK_URL = "http://schemas.microsoft.com/sharepoint/soap/";
    private final String WORK_ITEMS = "WorkItems";

    private WebServiceTemplate webServiceTemplate;
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

        ElementNSImpl eleNsImplObject = (ElementNSImpl) response.getGetListResult().getContent().get(0);
        Document document = eleNsImplObject.getOwnerDocument();

        Node list = document.getChildNodes().item(0);
        String listId = list.getAttributes().getNamedItem("Name").getNodeValue();
        System.out.printf("<listId, viewId> = <%s, %s>%n", listId, null);

        return new ListViewId(listId, null);
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

        ElementNSImpl eleNsImplObject = (ElementNSImpl) response.getGetListAndViewResult().getContent().get(0);
        Document document = eleNsImplObject.getOwnerDocument();

        Node listAndViewNode = document.getChildNodes().item(0);
        String listId = listAndViewNode.getChildNodes().item(0).getAttributes().getNamedItem("Name").getNodeValue();
        String viewId = listAndViewNode.getChildNodes().item(1).getAttributes().getNamedItem("Name").getNodeValue();
        System.out.printf("<listId, viewId> = <%s, %s>%n", listId, viewId);

        return new ListViewId(listId, viewId);
    }

    public String updateListItems(ListViewId listViewId) {
        String batchElement = XmlHelper.buildBatchElement(listViewId.viewId());

        UpdateListItems updateRequest = objectFactory.createUpdateListItems();
        updateRequest.setListName(listViewId.listId());
        UpdateListItems.Updates updates = objectFactory.createUpdateListItemsUpdates();
        updates.getContent().add(batchElement);
        updateRequest.setUpdates(updates);

        UpdateListItemsResponse updateResponse = (UpdateListItemsResponse) webServiceTemplate.marshalSendAndReceive(
                WS_URL,
                updateRequest,
                getSoapActionCallback("UpdateListItems")
        );

        UpdateListItemsResponse.UpdateListItemsResult updateResult = updateResponse.getUpdateListItemsResult();
        ElementNSImpl eleNsImplObject = (ElementNSImpl) updateResult.getContent().get(0);
        Document document = eleNsImplObject.getOwnerDocument();

        return XmlHelper.extractListItemId(document);
    }

    public void addAttachment(String listItemId, String fileName, String attachmentPath) {
        byte[] attachment;
        try (InputStream is = new FileInputStream(attachmentPath)) {

            attachment = IOUtils.toByteArray(is);

        } catch (IOException e) {
            e.printStackTrace();
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

        System.out.printf("Diff upload status %s%n", response.getAddAttachmentResult());
    }
}
