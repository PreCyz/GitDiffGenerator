package pg.gipter.producer.processor;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class HttpRequester {

    protected static Logger logger = LoggerFactory.getLogger(HttpRequester.class);

    private final ApplicationProperties applicationProperties;

    HttpRequester(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    private String replaceSpaces(String fileReference) {
        return fileReference.replaceAll(" ", "%20");
    }

    JsonObject executeGET(String fullUrl) throws IOException {
        HttpGet httpget = new HttpGet(replaceSpaces(fullUrl));
        httpget.addHeader("accept", "application/json;odata=verbose");
        logger.info("Executing request {}", httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(applicationProperties))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {}", response.getStatusLine());
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), new File("itemsee.json"));
            Reader reader = new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8);
            JsonObject result = new Gson().fromJson(reader, JsonObject.class);
            EntityUtils.consume(response.getEntity());
            return result;
        }
    }

    private CredentialsProvider getCredentialsProvider(ApplicationProperties applicationProperties) {
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(
                new AuthScope(AuthScope.ANY),
                new NTCredentials(
                        applicationProperties.toolkitUsername(),
                        applicationProperties.toolkitPassword(),
                        applicationProperties.toolkitUrl(),
                        applicationProperties.toolkitDomain()
                )
        );
        return credentialsProvider;
    }

    File downloadFile(String fullUrl, String downloadedFileName) throws Exception {
        HttpGet httpget = new HttpGet(replaceSpaces(fullUrl));
        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        logger.info("Executing request {} {}", callId, httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider(applicationProperties))
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {} {}", callId, response.getStatusLine());
            String downloadFilePath = applicationProperties.itemPath()
                    .substring(0, applicationProperties.itemPath().lastIndexOf(File.separator));
            File downloadedFile = new File(downloadFilePath + File.separator + downloadedFileName);
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
            EntityUtils.consume(response.getEntity());
            return downloadedFile;
        }
    }
}
