package pg.gipter.toolkit.sharepoint.soap;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import pg.gipter.MockitoExtension;
import pg.gipter.toolkit.helpers.ListViewId;
import pg.gipter.toolkit.helpers.XmlHelper;
import pg.gipter.toolkit.ws.*;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SharePointSoapClientTest {

    private final String wsUrl = "url";
    private final String listName = "listName";
    private final ObjectFactory objectFactory = new ObjectFactory();
    @Mock
    private WebServiceTemplate webServiceTemplate;

    private SharePointSoapClient client;

    @BeforeEach
    private void setup() {
        client = spy(new SharePointSoapClient(webServiceTemplate, wsUrl, listName));
    }

    @Test
    void when_getListAndView_then_returnListViewId() throws Exception {
        GetListAndViewResponse.GetListAndViewResult result = objectFactory.createGetListAndViewResponseGetListAndViewResult();
        result.getContent().add(mockDocument());
        GetListAndViewResponse response = objectFactory.createGetListAndViewResponse();
        response.setGetListAndViewResult(result);
        when(webServiceTemplate.marshalSendAndReceive(
                eq(wsUrl), any(GetListAndView.class), any(SoapActionCallback.class)
        )).thenReturn(response);

        ListViewId actual = client.getListAndView();

        assertThat(actual.listId()).isEqualTo("{CA29D447-6D71-409B-8974-656EB5F4D1CF}");
        assertThat(actual.viewId()).isEqualTo("{3DEA3051-D9AA-42B5-BFD7-7FC8C59DC582}");
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(
                eq(wsUrl),
                argThat((ArgumentMatcher<GetListAndView>) request -> {
                    Assertions.assertThat(request.getListName()).isEqualTo(listName);
                    Assertions.assertThat(request.getViewName()).isEmpty();
                    return true;
                }), any(SoapActionCallback.class)
        );
    }

    private Element mockDocument() throws Exception {
        Document document = XmlHelper.getDocument(XmlHelper.getFullXmlPath("GetListAndView.xml"));
        Element element = document.getDocumentElement();
        for (int i = 0; i < element.getChildNodes().getLength(); i++) {
            Node item = element.getChildNodes().item(i);
            if (!"List".equals(item.getNodeName()) || !"View".equals(item.getNodeName())) {
                element.removeChild(item);
            }
        }
        return element;
    }

    @Test
    void given_wrongResponse_when_getListAndView_then_throwIllegalArgumentException() {
        GetListAndViewResponse.GetListAndViewResult result = objectFactory.createGetListAndViewResponseGetListAndViewResult();
        result.getContent().add("wrong response");
        GetListAndViewResponse response = objectFactory.createGetListAndViewResponse();
        response.setGetListAndViewResult(result);
        when(webServiceTemplate.marshalSendAndReceive(
                eq(wsUrl), any(GetListAndView.class), any(SoapActionCallback.class)
        )).thenReturn(response);
        try {
            client.getListAndView();
            fail("Should throw IllegalArgumentException with relevant message.");
        } catch (IllegalArgumentException ex) {
            assertThat(ex.getMessage()).isEqualTo("Weird response from toolkit. Response is not a xml.");
        }
    }

    @Test
    void given_idsAndAttributes_when_updateListItems_then_returnListItemId() throws Exception {
        ListViewId listViewId = new ListViewId("listId", "viewId");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Title", "title");
        attributes.put("Employee", "-1;#@toolkitEmail");
        attributes.put("SubmissionDate", "2018-10-19'T'20:54:24'Z'");
        attributes.put("Classification", "12;#Changeset (repository change report)");
        attributes.put("Body", "description");
        attributes.put("ViewName", listViewId.viewId());
        attributes.put("RootFolder", "toolkitUserFolder");
        attributes.put("Cmd", "New");

        UpdateListItemsResponse.UpdateListItemsResult result = objectFactory.createUpdateListItemsResponseUpdateListItemsResult();
        result.getContent().add(XmlHelper.getDocument(XmlHelper.getFullXmlPath("UpdateListItemsResponse.xml")).getDocumentElement());
        UpdateListItemsResponse response = objectFactory.createUpdateListItemsResponse();
        response.setUpdateListItemsResult(result);
        when(webServiceTemplate.marshalSendAndReceive(eq(wsUrl), any(UpdateListItems.class), any(SoapActionCallback.class)))
                .thenReturn(response);

        String listItemId = client.updateListItems(listViewId, attributes);

        assertThat(listItemId).isEqualTo("6371");
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(
                eq(wsUrl),
                argThat((ArgumentMatcher<UpdateListItems>) request -> assertRequest(listViewId, request, attributes)),
                any(SoapActionCallback.class)
        );
    }

    private boolean assertRequest(ListViewId listViewId, UpdateListItems request, Map<String, String> attributes) {
        assertThat(request.getListName()).isEqualTo(listViewId.listId());
        assertThat(request.getUpdates().getContent()).hasSize(1);
        Object object = request.getUpdates().getContent().get(0);
        Assertions.assertThat(object).isInstanceOf(Element.class);
        Element element = (Element) object;
        assertThat(element.getTagName()).isEqualTo("Batch");
        assertThat(element.getAttribute("ListVersion")).isEqualTo("1");
        assertThat(element.getAttribute("OnError")).isEqualTo("Continue");
        assertThat(element.getAttribute("ViewName")).isEqualTo(listViewId.viewId());
        assertThat(element.getAttribute("RootFolder")).isEqualTo("toolkitUserFolder");
        NodeList methodNd = element.getElementsByTagName("Method");
        assertThat(methodNd.getLength()).isEqualTo(1);
        Node method = methodNd.item(0);
        assertThat(method.getAttributes().getLength()).isEqualTo(2);
        assertThat(method.getAttributes().getNamedItem("Cmd").getTextContent()).isEqualTo("New");
        assertThat(method.getAttributes().getNamedItem("ID").getTextContent()).isEqualTo("1");
        NodeList fieldNL = element.getElementsByTagName("Field");
        assertThat(fieldNL.getLength()).isEqualTo(5);
        for (int i = 0; i < fieldNL.getLength(); i++) {
            Node field = fieldNL.item(i);
            Node nameAttr = field.getAttributes().getNamedItem("Name");
            if ("Title".equals(nameAttr.getNodeName())) {
                assertThat(nameAttr.getTextContent()).isEqualTo(attributes.get("Title"));
            } else if ("Employee".equals(nameAttr.getNodeName())) {
                assertThat(nameAttr.getTextContent()).isEqualTo(attributes.get("Employee"));
            } else if ("SubmissionDate".equals(nameAttr.getNodeName())) {
                assertThat(nameAttr.getTextContent()).isEqualTo(attributes.get("SubmissionDate"));
            } else if ("Classification".equals(nameAttr.getNodeName())) {
                assertThat(nameAttr.getTextContent()).isEqualTo(attributes.get("Classification"));
            } else if ("Body".equals(nameAttr.getNodeName())) {
                assertThat(nameAttr.getTextContent()).isEqualTo(attributes.get("Body"));
            }
        }
        return true;
    }

    @Test
    void given_idsAndAttributes_when_updateListItems_then_returnResponseWithErrorAndThrowExceptionWithRelevantMessage() throws Exception {
        ListViewId listViewId = new ListViewId("listId", "viewId");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Title", "title");
        attributes.put("Employee", "-1;#@toolkitEmail");
        attributes.put("SubmissionDate", "2018-10-19'T'20:54:24'Z'");
        attributes.put("Classification", "12;#Changeset (repository change report)");
        attributes.put("Body", "description");
        attributes.put("ViewName", "viewName");
        attributes.put("RootFolder", "toolkitUserFolder");
        attributes.put("Cmd", "New");

        UpdateListItemsResponse.UpdateListItemsResult result = objectFactory.createUpdateListItemsResponseUpdateListItemsResult();
        result.getContent().add(XmlHelper.getDocument(XmlHelper.getFullXmlPath("updateListItemError.xml")).getDocumentElement());
        UpdateListItemsResponse response = objectFactory.createUpdateListItemsResponse();
        response.setUpdateListItemsResult(result);
        when(webServiceTemplate.marshalSendAndReceive(eq(wsUrl), any(UpdateListItems.class), any(SoapActionCallback.class)))
                .thenReturn(response);

        try {
            client.updateListItems(listViewId, attributes);
            fail("Should throw RuntimeException with relevant message");
        } catch (RuntimeException ex) {
            String expectedMsg = "Access denied.\n\n            You do not have permission to perform this action or access this resource." +
                    "\n        ";
            assertThat(ex.getMessage()).isEqualTo(expectedMsg);
        }

        verify(webServiceTemplate, times(1)).marshalSendAndReceive(
                eq(wsUrl),
                any(UpdateListItems.class),
                any(SoapActionCallback.class)
        );
    }

    @Test
    void given_idsAndAttributesAndWrongResponse_when_updateListItems_then_throwExceptionWithRelevantMessage() throws Exception {
        ListViewId listViewId = new ListViewId("listId", "viewId");
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Title", "title");
        attributes.put("Employee", "-1;#@toolkitEmail");
        attributes.put("SubmissionDate", "2018-10-19'T'20:54:24'Z'");
        attributes.put("Classification", "12;#Changeset (repository change report)");
        attributes.put("Body", "description");
        attributes.put("ViewName", "viewName");
        attributes.put("RootFolder", "toolkitUserFolder");
        attributes.put("Cmd", "New");

        UpdateListItemsResponse.UpdateListItemsResult result = objectFactory.createUpdateListItemsResponseUpdateListItemsResult();
        result.getContent().add("wrong response");
        UpdateListItemsResponse response = objectFactory.createUpdateListItemsResponse();
        response.setUpdateListItemsResult(result);
        when(webServiceTemplate.marshalSendAndReceive(eq(wsUrl), any(UpdateListItems.class), any(SoapActionCallback.class)))
                .thenReturn(response);

        try {
            client.updateListItems(listViewId, attributes);
            fail("Should throw RuntimeException with relevant message");
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).isEqualTo("Weird response from toolkit. Response is not a xml.");
        }
        verify(webServiceTemplate, times(1)).marshalSendAndReceive(
                eq(wsUrl),
                any(UpdateListItems.class),
                any(SoapActionCallback.class)
        );
    }

    @Test
    void given_listItemIDAndAttachment_when_addAttachment_then_executeSuccessfulRequest() {
        AddAttachmentResponse response = objectFactory.createAddAttachmentResponse();
        response.setAddAttachmentResult("ok");
        when(webServiceTemplate.marshalSendAndReceive(eq(wsUrl), any(AddAttachment.class), any(SoapActionCallback.class)))
                .thenReturn(response);
        String attachmentPath = XmlHelper.getFullXmlPath("batchElement.xml");

        client.addAttachment("listItemId", "fileName", attachmentPath);

        verify(webServiceTemplate, times(1)).marshalSendAndReceive(
                eq(wsUrl),
                argThat((ArgumentMatcher<AddAttachment>) request -> {
                    Assertions.assertThat(request.getFileName()).isEqualTo("fileName");
                    Assertions.assertThat(request.getListItemID()).isEqualTo("listItemId");
                    Assertions.assertThat(request.getListName()).isEqualTo(listName);
                    Assertions.assertThat(request.getAttachment()).isNotEmpty();
                    return true;
                }), any(SoapActionCallback.class)
        );
    }

    @Test
    void given_listItemIDAndNoAttachment_when_addAttachment_then_throwFileNotFoundException() {
        AddAttachmentResponse response = objectFactory.createAddAttachmentResponse();
        response.setAddAttachmentResult("ok");
        when(webServiceTemplate.marshalSendAndReceive(eq(wsUrl), any(AddAttachment.class), any(SoapActionCallback.class)))
                .thenReturn(response);

        try {
            client.addAttachment("listItemId", "fileName", "attachment");
            fail("Should throw FileNotFoundException");
        } catch (RuntimeException ex) {
            assertThat(ex.getCause()).isInstanceOf(FileNotFoundException.class);
            //works on Ubuntu
            //assertThat(ex.getMessage()).isEqualTo("java.io.FileNotFoundException: attachment (No such file or directory)");
            assertThat(ex.getMessage()).startsWith("java.io.FileNotFoundException: attachment");
        }
    }

    @Test
    void given_existingAttachment_when_getAttachmentByteArray_then_returnByteArray() {
        String attachmentPath = XmlHelper.getFullXmlPath("batchElement.xml");

        byte[] actual = client.getAttachmentByteArray(attachmentPath);

        assertThat(actual).isNotEmpty();
    }

    @Test
    void given_notExistingAttachment_when_getAttachmentByteArray_then_returnByteArray() {
        try {
            client.getAttachmentByteArray("notExisting");
            fail("Should throw FileNotFoundException");
        } catch (RuntimeException ex) {
            assertThat(ex.getCause()).isInstanceOf(FileNotFoundException.class);
            //works on Ubuntu
            //assertThat(ex.getMessage()).isEqualTo("java.io.FileNotFoundException: notExisting (No such file or directory)");
            assertThat(ex.getMessage()).startsWith("java.io.FileNotFoundException: notExisting");
        }
    }
}