package pg.gipter.producer.processor;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.NTCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

abstract class AbstractDocumentFinder implements DocumentFinder {

    protected Logger logger;
    protected ApplicationProperties applicationProperties;

    AbstractDocumentFinder(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.logger = LoggerFactory.getLogger(getClass());
    }

    protected final String getFullDownloadUrl(String fileReference, String projectCase) {
        //String projectCase = "/cases/GTE440/TOEDNLD";
        if (fileReference.startsWith(projectCase)) {
            return String.format("%s%s",
                    applicationProperties.toolkitUrl(),
                    fileReference.replaceAll(" ", "%20")
            );
        }
        return String.format("%s%s/%s",
                applicationProperties.toolkitUrl(),
                projectCase,
                fileReference.replaceAll(" ", "%20")
        );
    }

    protected final CredentialsProvider getCredentialsProvider() {
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

    protected List<File> downloadFilesFast(Map<String, String> filesToDownload) {
        if (filesToDownload.isEmpty()) {
            throw new IllegalArgumentException("No files to download.");
        }
        ExecutorService executor = Executors.newFixedThreadPool(filesToDownload.size());
        CompletionService<File> ecs = new ExecutorCompletionService<>(executor);
        filesToDownload.entrySet().forEach(entry -> ecs.submit(new DownloadFileCall(entry, applicationProperties)));

        int numberOfCalls = filesToDownload.size();
        List<File> result = new ArrayList<>(numberOfCalls);
        for (int i = 0; i < numberOfCalls; i++) {
            try {
                result.add(ecs.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when getting torrents.", e);
            }
        }
        return result;
    }

    public abstract List<File> find();
}
