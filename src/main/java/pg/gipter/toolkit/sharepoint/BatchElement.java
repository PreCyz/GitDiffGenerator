package pg.gipter.toolkit.sharepoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

class BatchElement {

    public enum Mode {
        CREATE("New"), UPDATE("Update"), DELETE("Delete");
        private String cmd;
        Mode(String cmd) {
            this.cmd = cmd;
        }
    }

    private Mode mode;
    private String viewId;
    private String rootFolder;
    private Document rootDocument;
    private Element methodElement;

    BatchElement(Mode mode, String viewId, String rootFolder) {
        this.mode = mode;
        this.viewId = viewId;
        this.rootFolder = rootFolder;
    }

    Element getBatchElement() {
        return rootDocument.getDocumentElement();
    }

    private void init() {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            rootDocument = docBuilder.newDocument();

            Element batch = rootDocument.createElement("Batch");
            batch.setAttribute("ListVersion", "1");
            batch.setAttribute("OnError", "Continue");
            batch.setAttribute("ViewName", viewId);
            batch.setAttribute("RootFolder", rootFolder);

            methodElement = rootDocument.createElement("Method");
            methodElement.setAttribute("Cmd", mode.cmd);
            methodElement.setAttribute("ID", "1");


            batch.appendChild(methodElement);
            rootDocument.appendChild(batch);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    void createListItem(Map<String, String> fields) {
        init();
        if (fields != null && !fields.isEmpty()) {
            Element createdElement;
            for (Map.Entry<String, String> aField : fields.entrySet()) {
                createdElement = rootDocument.createElement("Field");
                createdElement.setAttribute("Name", aField.getKey());
                Text attributeValue = rootDocument.createTextNode("" + aField.getValue());
                createdElement.appendChild(attributeValue);
                methodElement.appendChild(createdElement);
            }
        }
    }
}
