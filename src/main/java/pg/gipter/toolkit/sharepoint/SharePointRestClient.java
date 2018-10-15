package pg.gipter.toolkit.sharepoint;

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

import java.io.IOException;

/**Created by Pawel Gawedzki on 15-Oct-2018.*/
public class SharePointRestClient {

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

    public void getListByTitle() {
        /*Header header = new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        List<Header> headers = Lists.newArrayList(header);
        HttpClient client = HttpClients.custom().setDefaultHeaders(headers).build();
        HttpUriRequest request = RequestBuilder.get().setUri(SAMPLE_URL).build();
        client.execute(request);*/

        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpGet = new HttpGet(toolkitUrl + apiPrefix + getByTitle());
            Header[] headers = new Header[]{
                    new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json")
            };
            httpGet.setHeaders(headers);
            executeRequest(httpclient, httpGet);
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    public void uploadFile(String filePath) {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost("http://targethost/login");
            /*List<NameValuePair> nvps = new ArrayList<>();
            nvps.add(new BasicNameValuePair("username", "vip"));
            nvps.add(new BasicNameValuePair("password", "secret"));
            httpPost.setEntity(new UrlEncodedFormEntity(nvps));*/

            executeRequest(httpclient, httpPost);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
