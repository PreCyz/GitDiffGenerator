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
    private Element rootDocContent;

    BatchElement(Mode mode, String viewId, String rootFolder) {
        this.mode = mode;
        this.viewId = viewId;
        this.rootFolder = rootFolder;
    }

    Document getRootDocument() {
        return rootDocument;
    }

    void init() {
        try {
            DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
            rootDocument = docBuilder.newDocument();

            //Creates the root element
            Element rootElement = rootDocument.createElement("Batch");
            rootDocument.appendChild(rootElement);

            //Creates the batch attributes
            rootElement.setAttribute("ListVersion", "1");
            rootElement.setAttribute("OnError", "Continue");
            rootElement.setAttribute("ViewName", viewId);
            rootElement.setAttribute("RootFolder", rootFolder);
            rootDocContent = rootDocument.createElement("Method");
            rootDocContent.setAttribute("Cmd", mode.cmd);
            rootDocContent.setAttribute("ID", "1");
            rootDocument.getElementsByTagName("Batch").item(0).appendChild(rootDocContent);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    void createListItem(Map<String, String> fields) {
        //params check
        if (rootDocContent != null && this.getRootDocument() != null && fields != null && !fields.isEmpty()) {
            Element createdElement;
            //Adds attribute by attribute to fields
            for (Map.Entry<String, String> aField : fields.entrySet()) {
                createdElement = getRootDocument().createElement("Field");
                createdElement.setAttribute("Name", aField.getKey());
                Text attributeValue = getRootDocument().createTextNode("" + aField.getValue());
                createdElement.appendChild(attributeValue);
                rootDocContent.appendChild(createdElement);
            }
        }
    }
}
