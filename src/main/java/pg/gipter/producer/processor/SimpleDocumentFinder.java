package pg.gipter.producer.processor;

import com.google.gson.JsonObject;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.stream.Collectors.toList;

class SimpleDocumentFinder extends AbstractDocumentFinder {

    SimpleDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public List<File> find() {
        try {
            Map<String, String> filesToDownload = new HashMap<>();
            for (String project : applicationProperties.projectPaths()) {
                Set<String> listTitles = applicationProperties.toolkitProjectListNames();
                for (String listTitle : listTitles) {
                    JsonObject itemsWithVersions = getItemsWithVersions(project, listTitle);
                    List<DocumentDetails> documentDetails = convertToDocumentDetails(itemsWithVersions);
                    documentDetails = documentDetails.stream()
                            .filter(dd -> StringUtils.nullOrEmpty(dd.getDocType())).collect(toList());
                    filesToDownload.putAll(getFilesToDownload(project, documentDetails));
                }
            }
            return downloadDocuments(filesToDownload);
        } catch (IOException ex) {
            logger.error("Can not find [{}] to upload as your copyright items.", UploadType.TOOLKIT_DOCS);
            throw new IllegalArgumentException("Can not find items to upload.");
        }
    }

    JsonObject getItemsWithVersions(String project, String listTitle) throws IOException {
        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items",
                applicationProperties.toolkitUrl(),
                project,
                listTitle);
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
        String fullUrl = String.format("%s?%s&%s&%s", url, select, filter, expand);

        return httpRequester.executeGET(fullUrl);
    }

}
