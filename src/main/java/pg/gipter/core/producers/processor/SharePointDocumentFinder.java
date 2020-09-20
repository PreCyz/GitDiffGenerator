package pg.gipter.core.producers.processor;

import com.google.gson.JsonObject;
import pg.gipter.core.ApplicationProperties;
import pg.gipter.core.model.RunConfig;
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

class SharePointDocumentFinder extends AbstractDocumentFinder {

    SharePointDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
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
            logger.error("Can not find [{}] to upload as your copyright items.", ItemType.SHARE_POINT_DOCS);
            throw new IllegalArgumentException("Can not find items to upload.");
        }
        Map<String, String> filesToDownload = new HashMap<>(getFilesToDownload(documentDetails));
        List<DownloadDetails> downloadDetails = createDownloadDetails(sharePointConfigs, filesToDownload);
        return downloadDocuments(downloadDetails);
    }

    List<SharePointConfig> buildSharePointConfigs() {
        String select = "$select=Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString," +
                "File/ServerRelativeUrl,File/TimeLastModified,File/Title,File/Name,File/MajorVersion,File/MinorVersion,File/UIVersionLabel," +
                "File/Author/Id,File/Author/LoginName,File/Author/Title,File/Author/Email," +
                "File/ModifiedBy/Id,File/ModifiedBy/LoginName,File/ModifiedBy/Title,File/ModifiedBy/Email," +
                "File/Versions/CheckInComment,File/Versions/Created,File/Versions/ID,File/Versions/IsCurrentVersion,File/Versions/Size,File/Versions/Url,File/Versions/VersionLabel," +
                "File/Versions/CreatedBy/Id,File/Versions/CreatedBy/LoginName,File/Versions/CreatedBy/Title,File/Versions/CreatedBy/Email";
        String filter = String.format("$filter=Modified+ge+datetime'%s'+and+Modified+le+datetime'%s'",
                LocalDateTime.of(applicationProperties.startDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME),
                LocalDateTime.of(applicationProperties.endDate(), LocalTime.now()).format(DateTimeFormatter.ISO_DATE_TIME)
        );
        String expand = "$expand=File,File/Author,File/ModifiedBy,File/Versions,File/Versions/CreatedBy";

        List<SharePointConfig> result = new LinkedList<>();
        List<SharePointConfig> sharePointConfigs = applicationProperties.getRunConfigMap()
                .values()
                .stream()
                .filter(rc -> rc.getItemType() == ItemType.SHARE_POINT_DOCS)
                .map(RunConfig::getSharePointConfigs)
                .flatMap(Collection::stream)
                .collect(toList());
        for (SharePointConfig sharePointConfig : sharePointConfigs) {
            for (String listTitle : sharePointConfig.getListNames()) {
                String fullUrl = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items?%s&%s&%s",
                        sharePointConfig.getUrl(),
                        sharePointConfig.getProject(),
                        listTitle,
                        select,
                        filter,
                        expand
                );
                SharePointConfig spc = new SharePointConfig(sharePointConfig);
                spc.setFullRequestUrl(fullUrl);
                result.add(spc);
            }
        }
        return result;
    }

}
