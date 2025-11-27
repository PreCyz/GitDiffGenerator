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
import java.util.*;
import java.util.concurrent.*;

class ParallelProcessor {

    private final static Logger logger = LoggerFactory.getLogger(ParallelProcessor.class);

    private final ApplicationProperties applicationProperties;

    public ParallelProcessor(ApplicationProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
    }

    List<Path> downloadFiles(List<DownloadDetails> downloadDetails) {
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        ExecutorCompletionService<Path> ecs = new ExecutorCompletionService<>(executor);
        for (DownloadDetails downloadDetail : downloadDetails) {
            ecs.submit(() -> new HttpRequester(applicationProperties).downloadFile(downloadDetail));
        }
        executor.shutdown();

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
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletionService<JsonObject> ecs = new ExecutorCompletionService<>(executor);
        sharePointConfigs.forEach(scp -> ecs.submit(() -> new HttpRequester(applicationProperties).executeGET(scp)));
        executor.shutdown();

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
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
        CompletionService<ItemCountResponse> ecs = new ExecutorCompletionService<>(executor);
        projectUrlsMap.forEach((listAndProject, fullUrl) -> ecs.submit(() -> getItemCountResponse(listAndProject, fullUrl)));
        executor.shutdown();

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
                CookiesService.getFedAuthString().orElse(""),
                CookiesService.getGotoString().orElse("")
        );
        return new ItemCountResponse(
                listAndProject.getProject(),
                listAndProject.getListName(),
                new HttpRequester(applicationProperties).executeGET(sharePointConfig)
        );
    }
}
