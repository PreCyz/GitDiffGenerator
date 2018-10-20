package pg.gipter.toolkit.helper;

import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;

class XmlHelperTest {

    private Document readXml(String xmlFilePath) throws ParserConfigurationException, IOException, SAXException {
        File fXmlFile = new File(xmlFilePath);
        System.out.println(fXmlFile.getAbsolutePath());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(fXmlFile);

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