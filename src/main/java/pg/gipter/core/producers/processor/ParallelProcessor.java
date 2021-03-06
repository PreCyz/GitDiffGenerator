package pg.gipter.core.producers.processor;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;

import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.*;

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
        downloadDetails.forEach(downloadDetail -> ecs.submit(new DownloadFileCall(downloadDetail, applicationProperties)));

        int numberOfCalls = downloadDetails.size();
        List<Path> result = new ArrayList<>(numberOfCalls);
        for (int i = 0; i < numberOfCalls; i++) {
            try {
                result.add(ecs.take().get());
            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when getting torrents.", e);
            }
        }
        return result;
    }

    List<JsonObject> processConfigs(List<SharePointConfig> sharePointConfigs) {
        CompletionService<JsonObject> ecs = new ExecutorCompletionService<>(executor);
        sharePointConfigs.forEach(scp -> ecs.submit(new GETCall(scp, applicationProperties)));

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
        projectUrlsMap.forEach((listAndProject, url) -> ecs.submit(new GETItemCountCall(url, listAndProject, applicationProperties)));

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
}
