package pg.gipter.toolkit.sharepoint;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Text;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Map;

public class BatchElement {

    public enum Mode {
        CREATE("New"), UPDATE("Update"), DELETE("Delete");

        private String cmd;

        Mode(String cmd) {

            this.cmd = cmd;
        }

        private String cmd() {
            return cmd;
        }
    }

    private Mode mode;
    private String viewId;
    private Document rootDocument;
    private Element rootDocContent;

    /**
     * @return the rootDocument
     */
    public Document getRootDocument() {
        return rootDocument;
    }

    /**
     * @return the rootDocContent
     */
    public Element getRootDocContent() {
        return rootDocContent;
    }

    /**
     * This class creates a generic XML SOAP request pre-formatted for SharePoint
     * Lists web services requests (aka CAML query). What remains to be added are
     * the specific parameters (XML Elements with attributes).
     * For an example of a CAML Doc http://msdn.microsoft.com/en-us/library/lists.lists.updatelistitems.aspx
     * @param mode Either New, Update or Delete
     * @throws Exception
     */
    public BatchElement(Mode mode, String viewId) {
        this.mode = mode;
        this.viewId = viewId;
    }

    public void init() {
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
            rootDocContent = rootDocument.createElement("Method");
            rootDocContent.setAttribute("Cmd", mode.cmd());
            rootDocContent.setAttribute("ID", "1");
            rootDocument.getElementsByTagName("Batch").item(0).appendChild(rootDocContent);
        } catch (ParserConfigurationException ex) {
            throw new RuntimeException(ex.toString());
        }
    }

    /**
     * Creates a SharePoint list item in the CAML format, and adds it to the rootRequest.
     * In SharePoint, this corresponds to a line in a list. The parameters given
     * here would correspond respectively to the name of the column where to
     * insert the info, and then the info itself.
     * The requestTypeElement should already be initialized before calling this
     * method.
     * XML example output:
     * < Field Name="ID" >4< Field >
     * < Field Name="Field_Name" >Value< /Field >
     * @param fields Contains a HashMap with attribute names as keys, and attributes
     * values as content
     * @return true if the item has been successfully added to the caml request
     */
    public boolean createListItem(Map<String, String> fields) {
        //params check
        if (getRootDocContent() != null && this.getRootDocument() != null && fields != null && !fields.isEmpty()) {
            Element createdElement;
            //Adds attribute by attribute to fields
            for (Map.Entry<String, String> aField : fields.entrySet()) {
                createdElement = getRootDocument().createElement("Field");
                createdElement.setAttribute("Name", aField.getKey());
                if ("Classification".equals(aField.getKey())) {
                    createdElement.setAttribute("Type", "Lookup");
                }
                Text attributeValue = getRootDocument().createTextNode("" + aField.getValue());
                createdElement.appendChild(attributeValue);
                getRootDocContent().appendChild(createdElement);
            }
            return true;
        }
        return false;
    }
}
