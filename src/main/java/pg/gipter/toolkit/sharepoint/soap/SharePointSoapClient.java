package pg.gipter.toolkit.sharepoint.soap;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.toolkit.ws.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Created by Pawel Gawedzki on 12-Oct-2018. */
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

            Node listAndViewNode = document.getChildNodes().item(0);
            String listId = listAndViewNode.getChildNodes().item(0).getAttributes().getNamedItem("Name").getNodeValue();
            String viewId = listAndViewNode.getChildNodes().item(1).getAttributes().getNamedItem("Name").getNodeValue();
            logger.info("<listId, viewId> = <{}, {}>%n", listId, viewId);

            return new ListViewId(listId, viewId);
        }
        logger.error("Weird response from toolkit. Response is not a xml.");
        throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");
    }

    public String updateListItems(ListViewId listViewId, Map<String, String> itemAttributes) {
        BatchElement batchElement = new BatchElement(itemAttributes);
        UpdateListItems.Updates updates = objectFactory.createUpdateListItemsUpdates();
        updates.getContent().add(batchElement.create());

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

            Optional<String> errorCode = XmlHelper.extractValue(document, "ErrorCode");
            String codeOfSuccess = "0x00000000";
            if (errorCode.isPresent() && !codeOfSuccess.equals(errorCode.get())) {
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
        AddAttachment request = objectFactory.createAddAttachment();
        request.setListItemID(listItemId);
        request.setFileName(fileName);
        request.setListName(listName);
        request.setAttachment(getAttachmentByteArray(attachmentPath));

        AddAttachmentResponse response = (AddAttachmentResponse) webServiceTemplate.marshalSendAndReceive(
                wsUrl,
                request,
                getSoapActionCallback("AddAttachment")
        );

        logger.info("Your item was uploaded [{}].", response.getAddAttachmentResult());
    }

    byte[] getAttachmentByteArray(String attachmentPath) {
        byte[] attachment;
        try (InputStream is = new FileInputStream(attachmentPath)) {

            attachment = IOUtils.toByteArray(is);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return attachment;
    }

    public void getListItems(String listName, String viewName, String title) {
        GetListItems.Query query = objectFactory.createGetListItemsQuery();
        query.getContent().add(modificationQuery());

        GetListItems.QueryOptions queryOptions = objectFactory.createGetListItemsQueryOptions();
        queryOptions.getContent().add(queryOptions());

        Set<String> fieldRefs = Stream.of(
                "ColName", "Description", "DisplayName=", "FromBaseType", "ID", "Name", "Required", "Sealed", "SourceID",
                "StaticName", "Type"
        ).collect(Collectors.toSet());
        GetListItems.ViewFields viewFields = objectFactory.createGetListItemsViewFields();
        viewFields.getContent().add(GetListItemsElement.viewFields(fieldRefs));

        GetListItems request = objectFactory.createGetListItems();
        request.setListName(listName);
        request.setViewName(viewName);
        request.setQuery(query);
        request.setViewFields(viewFields);
        request.setRowLimit("100");
        request.setQueryOptions(queryOptions);
        request.setWebID(null);

        GetListItemsResponse response = (GetListItemsResponse) webServiceTemplate.marshalSendAndReceive(
                wsUrl,
                request,
                getSoapActionCallback("GetListItems")
        );

        Object content = response.getGetListItemsResult().getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) content;
            Document document = element.getOwnerDocument();
            XmlHelper.documentToXmlFile(document, "GetListItemsResponse.xml");
        }
        logger.error("Weird response from toolkit. Response is not a xml.");
        throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");
    }

    private static Document initNewDocument() throws ParserConfigurationException {
        DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
        return docBuilder.newDocument();
    }

    static Element modificationQuery() {
        try {
            Document document = initNewDocument();
            Element query = document.createElement("Query");

            Element where = document.createElement("Where");

            Element and = document.createElement("And");

            Element geq = document.createElement("Geq");
            Element fieldRefStart = document.createElement("FieldRef");
            fieldRefStart.setAttribute("Name", "Modified");
            Element startDate = document.createElement("Value");
            startDate.setAttribute("Type", "DateTime");
            startDate.setAttribute("IncludeTimeValue", "True");
            Text startDateNode = document.createTextNode("2019-04-01T00:00:00Z");

            Element leq = document.createElement("Leq");
            Element endFieldRef = document.createElement("FieldRef");
            endFieldRef.setAttribute("Name", "Modified");
            Element endDate = document.createElement("Value");
            endDate.setAttribute("Type", "DateTime");
            endDate.setAttribute("IncludeTimeValue", "True");
            Text endDateNode = document.createTextNode("2019-04-05T00:00:00Z");

            //title.appendChild(titleNode);
            //eq.appendChild(titleFieldRef);
            //eq.appendChild(title);

            endDate.appendChild(endDateNode);
            leq.appendChild(endFieldRef);
            leq.appendChild(endDate);

            startDate.appendChild(startDateNode);
            geq.appendChild(fieldRefStart);
            geq.appendChild(startDate);

            and.appendChild(leq);
            and.appendChild(geq);

            where.appendChild(and);
            //where.appendChild(geq);
            query.appendChild(where);

            return query;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    static Element queryOptions() {
        try {
            Document document = initNewDocument();
            Element queryOptions = document.createElement("QueryOptions");
            Element viewAttributes = document.createElement("ViewAttributes");
            viewAttributes.setAttribute("Scope", "Recursive");
            Text textNode = document.createTextNode("");
            viewAttributes.appendChild(textNode);
            queryOptions.appendChild(viewAttributes);

            return queryOptions;
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

}
