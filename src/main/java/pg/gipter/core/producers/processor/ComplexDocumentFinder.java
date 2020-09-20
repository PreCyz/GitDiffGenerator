package pg.gipter.core.producers.processor;

import com.google.gson.JsonObject;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.SharePointConfig;
import pg.gipter.core.producers.command.ItemType;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.utils.StringUtils;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.stream.Collectors.toList;

class ComplexDocumentFinder extends AbstractDocumentFinder {

    private final int TOP_LIMIT = 100;

    ComplexDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public List<Path> find() {
        List<ItemCountResponse> itemCounts = getItemCount();
        List<SharePointConfig> sharePointConfigs = buildSharePointConfigs(itemCounts);

        List<JsonObject> items = getItems(sharePointConfigs);
        List<DocumentDetails> documentDetails = items.stream()
                .map(this::convertToDocumentDetails)
                .flatMap(List::stream)
                .filter(dd -> !StringUtils.nullOrEmpty(dd.getDocType()))
                .collect(toList());
        if (documentDetails.isEmpty()) {
            logger.error("Can not find [{}] to upload as your copyright items.", ItemType.TOOLKIT_DOCS);
            throw new IllegalArgumentException("Can not find items to upload.");
        }
        Map<String, String> filesToDownload = new HashMap<>(getFilesToDownload(documentDetails));
        List<DownloadDetails> downloadDetails = createDownloadDetails(sharePointConfigs, filesToDownload);
        return downloadDocuments(downloadDetails);
    }

    List<ItemCountResponse> getItemCount() {
        Map<CustomizedTuple, String> projectUrlsMap = new HashMap<>();
        for (String project : applicationProperties.projectPaths()) {
            for (String list : applicationProperties.toolkitProjectListNames()) {
                String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/ItemCount",
                        applicationProperties.toolkitUrl(),
                        project,
                        list
                );
                projectUrlsMap.put(new CustomizedTuple(project, list), fullUrl);
            }
        }

        return parallelProcessor.processMap(projectUrlsMap);
    }

    List<SharePointConfig> buildSharePointConfigs(List<ItemCountResponse> responses) {
        List<SharePointConfig> sharePointConfigs = new LinkedList<>();
        for (ItemCountResponse response : responses) {
            if (response.getItemCount() > 0) {
                int numberOfPages = response.getItemCount() / TOP_LIMIT;
                if (response.getItemCount() % TOP_LIMIT > 0) {
                    ++numberOfPages;
                }
                for (int i = 0; i < numberOfPages; ++i) {
                    String fullRequestUrl = buildPageableUrl(response.getProject(), response.getListName(), TOP_LIMIT * i);
                    SharePointConfig sharePointConfig = new SharePointConfig(
                            applicationProperties.toolkitUsername(),
                            applicationProperties.toolkitPassword(),
                            applicationProperties.toolkitDomain(),
                            applicationProperties.toolkitUrl(),
                            fullRequestUrl
                    );
                    sharePointConfigs.add(sharePointConfig);
                }
            }
        }
        return sharePointConfigs;
    }

    String buildPageableUrl(String project, String listTitle, int documentId) {
        String select = "$select=Id,Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString," +
                "File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel," +
                "File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email," +
                "File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email," +
                "File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size,File/Versions/Url,File/Versions/VersionLabel," +
                "File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email";
        String filter = String.format("$filter=Created+lt+datetime'%s'+or+Modified+ge+datetime'%s'",
                LocalDateTime.of(applicationProperties.endDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.of(applicationProperties.startDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME)
        );
        String top = "$top=" + TOP_LIMIT;
        String expand = "$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy";


        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items",
                applicationProperties.toolkitUrl(),
                project,
                listTitle
        );
        String paging = "&$skiptoken=Paged=TRUE&p_SortBehavior=0&p_ID=" + documentId;
        if (documentId == 0) {
            paging = "";
        }
        String pagableUrl = String.format("%s?%s&%s&%s&%s%s", url, select, filter, expand, top, paging);
        return pagableUrl;
    }
}
