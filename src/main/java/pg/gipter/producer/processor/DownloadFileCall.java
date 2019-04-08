package pg.gipter.producer.processor;

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
import java.util.Map;
import java.util.concurrent.Callable;

class DownloadFileCall implements Callable<File> {

    private static final Logger logger = LoggerFactory.getLogger(DownloadFileCall.class);

    private Map.Entry<String, String> entry;
    private ApplicationProperties applicationProperties;

    DownloadFileCall(Map.Entry<String, String> entry, ApplicationProperties applicationProperties) {
        this.entry = entry;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public File call() throws Exception {
        HttpGet httpget = new HttpGet(entry.getValue());
        String callId = this.toString().substring(this.toString().lastIndexOf("@") + 1);
        logger.info("Executing request {} {}", callId, httpget.getRequestLine());

        try (CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCredentialsProvider(getCredentialsProvider())
                .build();
             CloseableHttpResponse response = httpclient.execute(httpget)
        ) {
            logger.info("Response {} {}", callId, response.getStatusLine());
            String downloadFilePath = applicationProperties.itemPath().substring(0, applicationProperties.itemPath().lastIndexOf(File.separator));
            File downloadedFile = new File(downloadFilePath + File.separator + entry.getKey());
            FileUtils.copyInputStreamToFile(response.getEntity().getContent(), downloadedFile);
            EntityUtils.consume(response.getEntity());
            return downloadedFile;
        }
    }

    private CredentialsProvider getCredentialsProvider() {
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
}
