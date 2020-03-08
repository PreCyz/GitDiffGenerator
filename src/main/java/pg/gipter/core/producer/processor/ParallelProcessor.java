package pg.gipter.core.producer.processor;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

class ParallelProcessor {

    private final static Logger logger = LoggerFactory.getLogger(ParallelProcessor.class);

    private final ExecutorService executor;
    private final ApplicationProperties applicationProperties;

    public ParallelProcessor(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    List<File> downloadFiles(List<DownloadDetails> downloadDetails) {
        CompletionService<File> ecs = new ExecutorCompletionService<>(executor);
        downloadDetails.forEach(downloadDetail -> ecs.submit(new DownloadFileCall(downloadDetail, applicationProperties)));

        int numberOfCalls = downloadDetails.size();
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
