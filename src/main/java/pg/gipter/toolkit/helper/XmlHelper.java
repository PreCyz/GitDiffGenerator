package pg.gipter.toolkit.helper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;

public final class XmlHelper {

    private static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

    private XmlHelper() { }

    public static String buildBatchElement(String viewId) {
        try {
            DocumentBuilderFactory documentFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentFactory.newDocumentBuilder();
            Document document = documentBuilder.newDocument();
            // root element

            Element batch = document.createElement("Batch");
            document.appendChild(batch);
            Attr onError = document.createAttribute("OnError");
            onError.setValue("Continue");
            Attr listVersion = document.createAttribute("ListVersion");
            listVersion.setValue("1");
            Attr viewName = document.createAttribute("ViewName");
            viewName.setValue(viewId);

            batch.setAttributeNode(onError);
            batch.setAttributeNode(listVersion);
            batch.setAttributeNode(viewName);

            // employee element
            Element method = document.createElement("Method");
            Attr id = document.createAttribute("ID");
            id.setValue("4");
            Attr cmd = document.createAttribute("Cmd");
            cmd.setValue("New");
            method.setAttributeNode(id);
            method.setAttributeNode(cmd);

            Element caseType = document.createElement("Field");
            Attr name = document.createAttribute("Name");
            name.setValue("CaseType");
            caseType.setAttributeNode(name);
            caseType.setTextContent("Service Request");

            Element portal = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Oprindelse");
            portal.setAttributeNode(name);
            portal.setTextContent("Portal");

            Element team = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Team");
            team.setAttributeNode(name);
            team.setTextContent("4");

            Element priority = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Priority");
            priority.setAttributeNode(name);
            priority.setTextContent("C - Medium");

            Element status = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Status");
            status.setAttributeNode(name);
            status.setTextContent("10 - Ny");

            method.appendChild(caseType);
            method.appendChild(portal);
            method.appendChild(team);
            method.appendChild(priority);
            method.appendChild(status);

            batch.appendChild(method);

            //documentToFile(document, "newItem");
            logger.info("Done creating XML for upload list.");

            return documentToString(document);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        return "";
    }

    private static String documentToString(final Document document) {
        try {
            StringWriter sw = new StringWriter();
            getTransformer().transform(new DOMSource(document), new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            logger.error("Error converting to string.", ex);
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        return transformer;
    }

    private static void documentToFile(final Document document, String fileName) {
        try {
            StreamResult streamResult = new StreamResult(new File(fileName + ".xml"));
            getTransformer().transform(new DOMSource(document), streamResult);
        } catch (Exception ex) {
            logger.error("Error converting to String.", ex);
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    public static String extractListItemId(Document document) {
        String owsId = "";
        NodeList nodeList = document.getElementsByTagName("z:row");
        for (int i = 0; i < nodeList.getLength(); i++) {
            NamedNodeMap attributes = nodeList.item(i).getAttributes();
            Node owsID = attributes.getNamedItem("ows_ID");
            if (owsID != null) {
                owsId = removeSharePointSigns(owsID.getNodeValue());
                break;
            }
        }
        logger.info("ows_ID = {}", owsId);
        return owsId;
    }

    private static String removeSharePointSigns(String value) {
        if (value == null) {
            return "";
        }

        String retValue;
        if (value.contains(";#")) {
            String[] result = value.split(";#");
            if (result.length > 2) {
                retValue = String.join("; ", result);
            }
            else {
                retValue = result[1];
            }
        } else {
            retValue = value;
        }
        return retValue;
    }

}
