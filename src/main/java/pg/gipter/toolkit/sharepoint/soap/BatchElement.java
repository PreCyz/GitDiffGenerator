package pg.gipter.toolkit.sharepoint.soap;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import java.util.Map;

class BatchElement {

    private final Map<String, String> attributes;

    BatchElement(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    Element create() {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            Document rootDocument = docBuilder.newDocument();

            Element batch = rootDocument.createElement("Batch");
            batch.setAttribute("ListVersion", "1");
            batch.setAttribute("OnError", "Continue");
            batch.setAttribute("ViewName", attributes.get("ViewName"));
            batch.setAttribute("RootFolder", attributes.get("RootFolder"));

            Element methodElement = rootDocument.createElement("Method");
            methodElement.setAttribute("Cmd", attributes.get("Cmd"));
            methodElement.setAttribute("ID", "1");

            batch.appendChild(methodElement);
            rootDocument.appendChild(batch);

            attributes.remove("ViewName");
            attributes.remove("RootFolder");
            attributes.remove("Cmd");

            Element createdElement;
            for (Map.Entry<String, String> aField : attributes.entrySet()) {
                createdElement = rootDocument.createElement("Field");
                createdElement.setAttribute("Name", aField.getKey());
                Text attributeValue = rootDocument.createTextNode("" + aField.getValue());
                createdElement.appendChild(attributeValue);
                methodElement.appendChild(createdElement);
            }
            return rootDocument.getDocumentElement();
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex.toString());
        }
    }
}
