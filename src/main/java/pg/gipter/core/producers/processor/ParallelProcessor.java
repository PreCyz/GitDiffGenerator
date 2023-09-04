package pg.gipter.core.producers.processor;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.services.CookiesService;
import pg.gipter.toolkit.HttpRequester;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ParallelProcessor {

    private final static Logger logger = LoggerFactory.getLogger(ParallelProcessor.class);

    private final ExecutorService executor;
    private final ApplicationProperties applicationProperties;

    public ParallelProcessor(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    List<Path> downloadFiles(List<DownloadDetails> downloadDetails) {
        CompletionService<Path> ecs = new ExecutorCompletionService<>(executor);
        downloadDetails.forEach(downloadDetail ->
                ecs.submit(() -> new HttpRequester(applicationProperties).downloadFile(downloadDetail))
        );

        int numberOfCalls = downloadDetails.size();
        List<Path> result = new ArrayList<>(numberOfCalls);
        for (int i = 0; i < numberOfCalls; i++) {
            try {
                result.add(ecs.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when downloading files.", e);
            }
        }
        return result;
    }

    List<JsonObject> processConfigs(List<SharePointConfig> sharePointConfigs) {
        CompletionService<JsonObject> ecs = new ExecutorCompletionService<>(executor);
        sharePointConfigs.forEach(scp -> ecs.submit(() -> new HttpRequester(applicationProperties).executeGET(scp)));

        List<JsonObject> result = new LinkedList<>();
        for (int i = 0; i < sharePointConfigs.size(); i++) {
            try {
                result.add(ecs.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when getting items.", e);
            }
        }
        return result;
    }

    List<ItemCountResponse> processMap(Map<CustomizedTuple, String> projectUrlsMap) {
        CompletionService<ItemCountResponse> ecs = new ExecutorCompletionService<>(executor);

        projectUrlsMap.forEach((listAndProject, fullUrl) -> ecs.submit(() -> getItemCountResponse(listAndProject, fullUrl)));

        List<ItemCountResponse> result = new LinkedList<>();
        for (int i = 0; i < projectUrlsMap.size(); i++) {
            try {
                result.add(ecs.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when getting items.", e);
            }
        }
        return result;
    }

    private ItemCountResponse getItemCountResponse(CustomizedTuple listAndProject, String fullUrl) throws IOException {
        SharePointConfig sharePointConfig = new SharePointConfig(
                applicationProperties.toolkitHostUrl(),
                fullUrl,
                CookiesService.getFedAuthString()
        );
        return new ItemCountResponse(
                listAndProject.getProject(),
                listAndProject.getListName(),
                new HttpRequester(applicationProperties).executeGET(sharePointConfig)
        );
    }
}
