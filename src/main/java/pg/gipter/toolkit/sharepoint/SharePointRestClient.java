package pg.gipter.toolkit.sharepoint;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**Created by Pawel Gawedzki on 15-Oct-2018.*/
public class SharePointRestClient {

    private static final Logger logger = LoggerFactory.getLogger(SharePointRestClient.class);

    private final String toolkitUrl;
    private final String apiPrefix = "/_api/web/lists/";
    private final String listName;

    public SharePointRestClient(String toolkitUrl, String listName) {
        this.toolkitUrl = toolkitUrl;
        this.listName = listName;
    }

    private String getByTitle() {
        return String.format("GetByTitle('%s')", listName);
    }

    /** Executing request under //https://goto.netcompany.com/cases/GTE106/NCSCOPY/_api/web/lists/GetByTitle('WorkItems')
     *
     */
    public void getListByTitle() {
        try (CloseableHttpClient httpclient = createHttpClient()) {
            HttpGet httpGet = new HttpGet(toolkitUrl + apiPrefix + getByTitle());
            Header[] headers = new Header[]{
                    new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json;odata=verbose")
            };
            httpGet.setHeaders(headers);
            executeRequest(httpclient, httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CloseableHttpClient createHttpClient() {
        //Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        //List<Header> headers = Arrays.asList(header);
        return HttpClients
                .custom()
                //.setDefaultHeaders(headers)
                .build();
    }

    private void executeRequest(CloseableHttpClient httpclient, HttpUriRequest httpRequest) throws IOException {
        try (CloseableHttpResponse response = httpclient.execute(httpRequest)) {
            System.out.println(response.getStatusLine());
            HttpEntity entity2 = response.getEntity();
            // do something useful with the response body
            // and ensure it is fully consumed
            EntityUtils.consume(entity2);
        }
    }

    //https://goto.netcompany.com/cases/GTE106/NCSCOPY/_api/web/getfolderbyserverrelativeurl('PAWG')/files/add(overwrite=true, url='fileName')",
    public void uploadFile(String attachmentPath) {

        byte[] attachment;
        try (InputStream is = new FileInputStream(attachmentPath)) {

            attachment = IOUtils.toByteArray(is);

        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new RuntimeException();
        }

        try (CloseableHttpClient httpclient = createHttpClient()) {
            HttpPost httpPost = new HttpPost("http://targethost/login");
            Header[] headers = new Header[]{
                    new BasicHeader("accept", "application/json;odata=verbose"),
                    new BasicHeader("X-RequestDigest", "#__REQUESTDIGEST!!!!!!!"),
                    new BasicHeader("content-length", String.valueOf(attachment.length)),
            };
            httpPost.setHeaders(headers);

            executeRequest(httpclient, httpPost);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
