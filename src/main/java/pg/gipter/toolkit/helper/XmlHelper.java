package pg.gipter.toolkit.helper;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.StringWriter;
import java.util.Optional;

public final class XmlHelper {

    private static final Logger logger = LoggerFactory.getLogger(XmlHelper.class);

    private XmlHelper() { }

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

    private static void documentToXmlFile(final Source source, String fileName) {
        if (!fileName.endsWith(".xml")) {
            fileName += fileName + ".xml";
        }
        fileName = getFullXmlPath(fileName);
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
        logger.info("New item id: ows_ID = {}", owsId);
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

    public static String getFullXmlPath(String xmlFileName) {
        return String.format(".%ssrc%stest%sjava%sresources%sxml%s%s",
                File.separator, File.separator, File.separator, File.separator, File.separator, File.separator, xmlFileName);
    }

    private static Document xmlToDocument(final Source source) {
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
        return Optional.ofNullable(nodeList.item(0).getTextContent());
    }

    public static Document getDocument(String fullXmlPath) throws Exception {
        File file = new File(fullXmlPath);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(file);
        document.normalizeDocument();
        return document;
    }
}
