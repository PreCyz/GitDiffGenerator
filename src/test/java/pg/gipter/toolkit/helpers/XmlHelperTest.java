package pg.gipter.toolkit.helpers;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

class XmlHelperTest {

    private Document readXml(String xmlFilePath) throws ParserConfigurationException, IOException, SAXException {
        Path fXmlFile = Paths.get(xmlFilePath);
        System.out.println(fXmlFile.toAbsolutePath().toString());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile.toFile());

        doc.getDocumentElement().normalize();
        return doc;
    }

    @Test
    void given_responseUpdateListItemResponse_when_extractListItemId_then_returnId4() throws Exception {
        String xmlFilePath = XmlHelper.getFullXmlPath("UpdateListItemsResponse.xml");

        String id = XmlHelper.extractListItemId(readXml(xmlFilePath));

        assertThat(id).isEqualTo("6371");
    }
}