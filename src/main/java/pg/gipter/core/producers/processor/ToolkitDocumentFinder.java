package pg.gipter.core.producers.processor;

import com.google.gson.JsonObject;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.services.CookiesService;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.users.SuperUserService;
import pg.gipter.utils.StringUtils;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

class ToolkitDocumentFinder extends AbstractDocumentFinder {

    private CookiesService cookiesService;

    ToolkitDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
        this.cookiesService = new CookiesService(applicationProperties);
    }

    @Override
    public List<Path> find() {
        List<SharePointConfig> sharePointConfigs = buildSharePointConfigs();
        List<JsonObject> items = getItems(sharePointConfigs);
        List<DocumentDetails> documentDetails = items.stream()
                .map(this::convertToDocumentDetails)
                .flatMap(List::stream)
                .filter(dd -> !StringUtils.nullOrEmpty(dd.getDocType()))
                .collect(toList());
        if (documentDetails.isEmpty()) {
            logger.error("Could not find [{}] to upload as your copyright items.", ItemType.TOOLKIT_DOCS);
            throw new IllegalArgumentException("Could not find items to upload.");
        }
        Map<String, String> filesToDownload = new HashMap<>(getFilesToDownload(documentDetails));
        List<DownloadDetails> downloadDetails = createDownloadDetails(sharePointConfigs, filesToDownload);
        return downloadDocuments(downloadDetails);
    }

    List<SharePointConfig> buildSharePointConfigs() {
        List<SharePointConfig> result = new LinkedList<>();
        for (String project : applicationProperties.projectPaths()) {
            Set<String> listTitles = applicationProperties.toolkitProjectListNames();
            for (String listTitle : listTitles) {
                String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?%s&%s&%s",
                        applicationProperties.toolkitWSUrl(),
                        project,
                        listTitle,
                        select(),
                        filter(),
                        expand()
                );
                SharePointConfig sharePointConfig = new SharePointConfig();
                sharePointConfig.setFullRequestUrl(fullUrl);;
                sharePointConfig.setProject(project);
                sharePointConfig.setUrl(applicationProperties.toolkitRESTUrl());
                sharePointConfig.setDomain(applicationProperties.toolkitDomain());
                sharePointConfig.setListNames(Stream.of(listTitle).collect(toSet()));
                sharePointConfig.setUsername(SuperUserService.getInstance().getUserName());
                sharePointConfig.setPassword(SuperUserService.getInstance().getPassword());
                sharePointConfig.setFedAuth(cookiesService.getFedAuthString());
                result.add(sharePointConfig);
            }
        }
        return result;
    }

}
