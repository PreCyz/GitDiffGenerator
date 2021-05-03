package pg.gipter.toolkit.sharepoint.soap;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BatchElementTest {

    @Test
    void given_attributes_when_create_then_returnProperElement() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("Title", "title");
        attributes.put("Employee", "-1;#@toolkitEmail");
        attributes.put("SubmissionDate", "2018-10-19'T'20:54:24'Z'");
        attributes.put("Classification", "12;#Changeset (repository change report)");
        attributes.put("Body", "description");
        attributes.put("ViewName", "viewName");
        attributes.put("RootFolder", "toolkitUserFolder");
        attributes.put("Cmd", "New");

        BatchElement batchElement = new BatchElement(attributes);
        Element actual = batchElement.create();

        assertThat(attributes).hasSize(5);
        assertBatch(actual);
        assertMethod(actual.getElementsByTagName("Method"));
        assertFields(actual.getElementsByTagName("Field"));
    }

    private void assertBatch(Element actual) {
        assertThat(actual.getTagName()).isEqualTo("Batch");
        assertThat(actual.getAttribute("ListVersion")).isEqualTo("1");
        assertThat(actual.getAttribute("OnError")).isEqualTo("Continue");
        assertThat(actual.getAttribute("ViewName")).isEqualTo("viewName");
        assertThat(actual.getAttribute("RootFolder")).isEqualTo("toolkitUserFolder");
    }

    private void assertMethod(NodeList method) {
        assertThat(method.getLength()).isEqualTo(1);
        Node node = method.item(0);
        NamedNodeMap attributes = node.getAttributes();
        assertThat(attributes.getLength()).isEqualTo(2);
        assertThat(attributes.getNamedItem("ID").getTextContent()).isEqualTo("1");
        assertThat(attributes.getNamedItem("Cmd").getTextContent()).isEqualTo("New");
    }

    private void assertFields(NodeList fields) {
        assertThat(fields.getLength()).isEqualTo(5);
        for (int i = 0; i < fields.getLength(); i++) {
            Node field = fields.item(i);
            NamedNodeMap attributes = field.getAttributes();
            assertThat(attributes.getLength()).isEqualTo(1);
            Node name = attributes.getNamedItem("Name");
            if ("Title".equals(name.getTextContent())) {
                assertThat(field.getTextContent()).isEqualTo("title");
            } else if ("Employee".equals(name.getTextContent())) {
                assertThat(field.getTextContent()).isEqualTo("-1;#@toolkitEmail");
            } else if ("SubmissionDate".equals(name.getTextContent())) {
                assertThat(field.getTextContent()).isEqualTo("2018-10-19'T'20:54:24'Z'");
            } else if ("Classification".equals(name.getTextContent())) {
                assertThat(field.getTextContent()).isEqualTo("12;#Changeset (repository change report)");
            } else if ("Body".equals(name.getTextContent())) {
                assertThat(field.getTextContent()).isEqualTo("description");
            }
        }
    }
}