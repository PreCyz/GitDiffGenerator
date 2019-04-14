package pg.gipter.producer.processor;

import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pg.gipter.settings.ApplicationProperties;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

class ParallelProcessor {
    private final static Logger logger = LoggerFactory.getLogger(ParallelProcessor.class);

    private final int NUMBER_OF_THREADS = 10;
    private final ExecutorService executor;
    private final ApplicationProperties applicationProperties;


    public ParallelProcessor(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        this.executor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);
    }

    List<File> downloadFiles(Map<String, String> filesToDownload) {
        CompletionService<File> ecs = new ExecutorCompletionService<>(executor);
        filesToDownload.forEach((downloadedFileName, fullUrl) ->
                ecs.submit(new DownloadFileCall(fullUrl, downloadedFileName, applicationProperties))
        );

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

    List<JsonObject> processUrls(List<String> urls) {
        CompletionService<JsonObject> ecs = new ExecutorCompletionService<>(executor);
        urls.forEach(url -> ecs.submit(new GETCall(url, applicationProperties)));

        List<JsonObject> result = new LinkedList<>();
        for (int i = 0; i < urls.size(); i++) {
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
