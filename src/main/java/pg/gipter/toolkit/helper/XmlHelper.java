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

            Element employee = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Employee");
            employee.setAttributeNode(name);
            employee.setTextContent(employeeTxt);

            Element submissionDate = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("SubmissionDate");
            submissionDate.setAttributeNode(name);
            submissionDate.setTextContent(LocalDate.now().format(yyyy_MM_dd));

            Element classification = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Classification");
            classification.setAttributeNode(name);
            classification.setTextContent("12"); //Changeset (repository change report)

            Element description = document.createElement("Field");
            name = document.createAttribute("Name");
            name.setValue("Body");
            description.setAttributeNode(name);
            description.setTextContent("Git diff file.");

            method.appendChild(title);
            method.appendChild(employee);
            method.appendChild(submissionDate);
            method.appendChild(classification);
            method.appendChild(description);

            batch.appendChild(method);
            batch.normalize();

            //documentToXmlFile(document, "batchElement.xml");

            logger.info("Done creating XML for upload list.");
            return documentToString(document);
        } catch (ParserConfigurationException pce) {
            logger.error("Error when building batchElement.", pce);
            throw new RuntimeException(pce);
        }
    }

    public static String documentToString(final Document document) {
        return documentToString(new DOMSource(document));
    }

    private static String documentToString(final Source source) {
        try {
            StringWriter sw = new StringWriter();
            getTransformer().transform(source, new StreamResult(sw));
            return sw.toString();
        } catch (Exception ex) {
            logger.error("Error converting to string.", ex);
            throw new RuntimeException("Error converting to String", ex);
        }
    }

    private static Transformer getTransformer() throws TransformerConfigurationException {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        return transformer;
    }

    public static void documentToXmlFile(final Document document, String fileName) {
        documentToXmlFile(new DOMSource(document), fileName);
    }

    public static void documentToXmlFile(final Source source, String fileName) {
        if (!fileName.endsWith(".xml")) {
            fileName += fileName + ".xml";
        }
        fileName = getFullXmlDirPath(fileName);
        try {
            StreamResult streamResult = new StreamResult(new File(fileName));
            getTransformer().transform(source, streamResult);
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

    public static String getFullXmlDirPath(String xmlFileName) {
        return String.format(".%ssrc%stest%sjava%sresources%sxml%s%s",
                File.separator, File.separator, File.separator, File.separator, File.separator, File.separator, xmlFileName);
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
