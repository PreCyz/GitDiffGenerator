import org.apache.commons.io.FileUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
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
import pg.gipter.toolkit.helper.ListViewId;
import pg.gipter.toolkit.helper.XmlHelper;
import pg.gipter.toolkit.sharepoint.soap.SharePointConfiguration;
import pg.gipter.toolkit.sharepoint.soap.SharePointSoapClient;
import pg.gipter.toolkit.ws.GetVersionCollection;
import pg.gipter.toolkit.ws.GetVersionCollectionResponse;
import pg.gipter.toolkit.ws.ObjectFactory;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by Pawel Gawedzki on 02-Apr-2019.
 */
@ExtendWith(MockitoExtension.class)
class GetDocumentVersion {

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
        toolkitProperties.put("toolkit.password", "JanuarY12!@");
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
        ListViewId listAndView = bean.getListAndView();
        bean.getListItems(listAndView.listId(), listAndView.viewId(), "");

        getVersionCollection();
    }

    private void getVersionCollection() {
        GetVersionCollection request = objectFactory.createGetVersionCollection();
        request.setStrlistID("Deliverables");
        request.setStrlistItemID("1416");
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
            XmlHelper.documentToXmlFile(document, "GetVersionCollectionResponse.xml");

            Node listAndViewNode = document.getChildNodes().item(0);
            /*String listId = listAndViewNode.getChildNodes().item(0).getAttributes().getNamedItem("Name").getNodeValue();
            String viewId = listAndViewNode.getChildNodes().item(1).getAttributes().getNamedItem("Name").getNodeValue();
            logger.info("<listId, viewId> = <{}, {}>%n", listId, viewId);

            return new ListViewId(listId, viewId);*/
        }
        //logger.error("Weird response from toolkit. Response is not a xml.");
        //throw new IllegalArgumentException("Weird response from toolkit. Response is not a xml.");*/
    }

    @Test
    void downloadSpecificVersion() throws Exception {
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY),
                new NTCredentials("pawg", "", "https://goto.netcompany.com", "NCDMZ"));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {
            HttpGet httpget = new HttpGet("https://goto.netcompany.com/cases/GTE440/TOEDNLD/_vti_history/14848/Deliverables/D0180%20-%20Integration%20design/Topdanmark%20integrations/D0180%20-%20Integration%20Design%20-%20Topdanmark%20integrations%20-%20Party%20Master.docx");

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                FileUtils.copyInputStreamToFile(response.getEntity().getContent(), new File("tmp.docx"));
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Test
    void restApiTest_items() throws IOException {
        String url = "https://goto.netcompany.com/cases/GTE440/TOEDNLD/_api/web/lists/GetByTitle('Deliverables')/items";
        String select = "$select=Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString," +
                "File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel," +
                "File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email," +
                "File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email," +
                "File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size,File/Versions/Url,File/Versions/VersionLabel" +
                "File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email,File/Versions/VersionLabel";
        //String select = "$select=*";
        String filter = "$filter=Modified+gt+datetime'2019-04-01T00:00:00Z'+and+Modified+lt+datetime'2019-04-06T00:00:00Z'";
        String expand = "$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy";

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY),
                new NTCredentials("pawg", "JanuarY12!@", "https://goto.netcompany.com", "NCDMZ"));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {
            HttpGet httpget = new HttpGet(url + "?" + select + "&" + filter + "&" + expand);
            httpget.addHeader("accept", "application/json;odata=verbose");

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                FileUtils.copyInputStreamToFile(response.getEntity().getContent(), new File("items.json"));
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }

    @Test
    void restVersion() throws IOException {
        String url = "https://goto.netcompany.com/cases/GTE440/TOEDNLD/_api/Web/GetFileByServerRelativeUrl('/cases/GTE440/TOEDNLD/Deliverables/D0180%20-%20Integration%20design/Topdanmark%20integrations/D0180%20-%20Integration%20Design%20-%20Topdanmark%20integrations%20-%20Party%20Master.docx')/Versions";
        String expand = "$expand=CreatedBy";
        String select = "$select=ModifiedBy,Modified,CheckInComment,Created,ID,IsCurrentVersion,Size,Url,VersionLabel,CreatedBy/Editor,CreatedBy/Id,CreatedBy/Email,CreatedBy/Title,CreatedBy/LoginName";
        String orderBy = "$orderby=Created+desc";

        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(
                new AuthScope(AuthScope.ANY),
                new NTCredentials("pawg", "JanuarY12!@", "https://goto.netcompany.com", "NCDMZ"));
        CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(credsProvider)
                .build();
        try {
            HttpGet httpget = new HttpGet(url + "?" + expand + "&" + select + "&" + orderBy);
            httpget.addHeader("accept", "application/json;odata=verbose");

            System.out.println("Executing request " + httpget.getRequestLine());
            CloseableHttpResponse response = httpclient.execute(httpget);
            try {
                System.out.println("----------------------------------------");
                System.out.println(response.getStatusLine());
                FileUtils.copyInputStreamToFile(response.getEntity().getContent(), new File("versions.json"));
                EntityUtils.consume(response.getEntity());
            } finally {
                response.close();
            }
        } finally {
            httpclient.close();
        }
    }
}
