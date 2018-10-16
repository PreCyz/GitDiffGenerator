package pg.gipter.toolkit.helper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.Optional;

import static pg.gipter.Main.yyyy_MM_dd;

public final class XmlHelper {

    private static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

    private XmlHelper() { }

    public static String buildBatchElement(String viewId, String titleTxt, String employeeTxt) {
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

            Element title = document.createElement("Field");
            Attr name = document.createAttribute("Name");
            name.setValue("Title");
            title.setAttributeNode(name);
            title.setTextContent(titleTxt);

            Element approver = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("AssignedTo");
            approver.setAttributeNode(name);
            approver.setTextContent("Steffen Bra Andersen");

            Element description = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Body");
            description.setAttributeNode(name);
            description.setTextContent("Git diff file");

            Element startDate = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("StartDate");
            startDate.setAttributeNode(name);
            startDate.setTextContent(LocalDate.now().format(yyyy_MM_dd));

            Element employee = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Employee");
            employee.setAttributeNode(name);
            employee.setTextContent(employeeTxt);

            /*Element portal = document.createElement("Field");
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
            status.setTextContent("10 - Ny");*/

            method.appendChild(title);
            method.appendChild(approver);
            method.appendChild(description);
            method.appendChild(startDate);
            method.appendChild(employee);

            batch.appendChild(method);

            //documentToFile(document, "newItem");
            logger.info("Done creating XML for upload list.");

            return documentToString(document);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        return "";
    }

    public static String documentToString(final Document document) {
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

    public static void documentToXmlFile(final Document document, String filePath) {
        if (!filePath.endsWith(".xml")) {
            filePath += filePath + ".xml";
        }
        try {
            StreamResult streamResult = new StreamResult(new File(filePath));
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
            } else {
                retValue = result[1];
            }
        } else {
            retValue = value;
        }
        return retValue;
    }

    public static Document xmlToDocument(final Source source) {
        try {
            StringWriter stringWriter = new StringWriter();
            StreamResult streamResult = new StreamResult(stringWriter);
            getTransformer().transform(source, streamResult);

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder parser = factory.newDocumentBuilder();
            return parser.parse(IOUtils.toInputStream(stringWriter.toString(), "UTF-8"));
        } catch (Exception ex) {
            logger.error("Error converting to String.", ex);
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    public static String extractErrorMessage(Source source) {
        Document document = xmlToDocument(source);
        NodeList errorStr = document.getElementsByTagName("errorstring");
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < errorStr.getLength(); i++) {
            stringBuilder.append(",").append(errorStr.item(i).getTextContent());
        }
        if (stringBuilder.length() > 0) {
            return stringBuilder.toString().substring(1);
        }
        return stringBuilder.toString();
    }

    public static Optional<String> extractValue(Document document, String tagName) {
        NodeList nodeList = document.getElementsByTagName(tagName);
        for (int i = 0; i < nodeList.getLength(); i++) {
            return Optional.ofNullable(nodeList.item(i).getTextContent());
        }
        return Optional.empty();
    }
}
