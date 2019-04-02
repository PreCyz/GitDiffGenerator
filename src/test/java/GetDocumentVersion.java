import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.ws.client.core.WebServiceTemplate;
import org.springframework.ws.soap.client.core.SoapActionCallback;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import pg.gipter.MockitoExtension;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.toolkit.sharepoint.SharePointConfiguration;
import pg.gipter.toolkit.sharepoint.SharePointSoapClient;
import pg.gipter.toolkit.ws.GetVersionCollection;
import pg.gipter.toolkit.ws.GetVersionCollectionResponse;
import pg.gipter.toolkit.ws.ObjectFactory;

import java.util.Properties;

/**
 * Created by Pawel Gawedzki on 02-Apr-2019.
 */
@ExtendWith(MockitoExtension.class)
public class GetDocumentVersion {

    private final ObjectFactory objectFactory = new ObjectFactory();
    private ApplicationContext springContext;

    @BeforeEach
    private void setutp() {
        springContext = initSpringApplicationContext();
    }

    ApplicationContext initSpringApplicationContext() {
        AnnotationConfigApplicationContext applicationContext = new AnnotationConfigApplicationContext();
        setToolkitProperties(applicationContext.getEnvironment());
        applicationContext.register(SharePointConfiguration.class);
        applicationContext.refresh();
        return applicationContext;
    }

    void setToolkitProperties(ConfigurableEnvironment environment) {
        Properties toolkitProperties = new Properties();
        toolkitProperties.put("toolkit.username", "PAWG");
        toolkitProperties.put("toolkit.password", "");
        toolkitProperties.put("toolkit.domain", "NCDMZ");
        toolkitProperties.put("toolkit.WSUrl", "https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_bin/lists.asmx");
        toolkitProperties.put("toolkit.listName", "Deliverables");
        environment.getPropertySources().addLast(new PropertiesPropertySource("toolkit", toolkitProperties));

        //https://goto.netcompany.com/cases/GTE440/TOEDNLD/Deliverables/Forms/AllItems.aspx
    }

    private SoapActionCallback getSoapActionCallback(String actionName) {
        return new SoapActionCallback("http://schemas.microsoft.com/sharepoint/soap/" + actionName);
    }

    @Test
    void versionControl() {
        SharePointSoapClient bean = springContext.getBean(SharePointSoapClient.class);
        //ListViewId listAndView = bean.getListAndView();

        //bean.getListItems(listAndView.listId(), listAndView.viewId(), "");

        GetVersionCollection request = objectFactory.createGetVersionCollection();
        request.setStrlistID("Deliverables");
        request.setStrlistItemID("1");
        request.setStrFieldName("Version");

        WebServiceTemplate webServiceTemplate = springContext.getBean(WebServiceTemplate.class);
        GetVersionCollectionResponse response = (GetVersionCollectionResponse) webServiceTemplate.marshalSendAndReceive(
                "https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_bin/lists.asmx",
                request,
                getSoapActionCallback("GetVersionCollection")
        );

        Object content = response.getGetVersionCollectionResult().getContent().get(0);
        if (content instanceof Element) {
            Element element = (Element) content;
            Document document = element.getOwnerDocument();
            String xml = XmlHelper.documentToString(document);
            System.out.println(xml);

            Node listAndViewNode = document.getChildNodes().item(0);
            /*String listId = listAndViewNode.getChildNodes().item(0).getAttributes().getNamedItem("Name").getNodeValue();
            String viewId = listAndViewNode.getChildNodes().item(1).getAttributes().getNamedItem("Name").getNodeValue();
            logger.info("<listId, viewId> = <{}, {}>%n", listId, viewId);

            return new ListViewId(listId, viewId);*/
        }
        //logger.error("Weird response from toolkit. Response is not a xml.");
        //throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");*/
    }
}
