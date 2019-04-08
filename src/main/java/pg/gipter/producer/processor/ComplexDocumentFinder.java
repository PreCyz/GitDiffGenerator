package pg.gipter.producer.processor;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.dto.DocumentDetailsBuilder;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

class ComplexDocumentFinder extends AbstractDocumentFinder {

    ComplexDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public List<File> find() {
        try {
            Map<String, List<DocumentDetails>> projectDocsMap = new HashMap<>();
            for (String project : applicationProperties.projectPaths()) {
                List<DocumentDetails> documentDetails = new LinkedList<>();
                Set<String> listTitles = applicationProperties.toolkitProjectListNames();
                for (String listTitle : listTitles) {
                    JsonObject items = getItems(project, listTitle);
                    documentDetails.addAll(convertToDocumentDetails(items));
                }
                projectDocsMap.put(project, documentDetails);
            }

            if (projectDocsMap.isEmpty() || projectDocsMap.values().stream().mapToLong(Collection::size).sum() == 0) {
                throw new IllegalArgumentException("Can not find any documents.");
            }

            projectDocsMap = downloadVersions(projectDocsMap);

            Map<String, String> filesToDownload = getFilesToDownload(projectDocsMap);

            return downloadDocuments(filesToDownload);
        } catch (IOException ex) {
            logger.error("Can not find [{}] to upload as your copyright items.", UploadType.TOOLKIT_DOCS, ex);
            throw new IllegalArgumentException("Can not find items to upload.", ex);
        }
    }

    private Map<String, String> getFilesToDownload(Map<String, List<DocumentDetails>> projectDocsMap) {
        return new HashMap<>();
    }

    private Map<String, List<DocumentDetails>> downloadVersions(Map<String, List<DocumentDetails>> projectDocsMap) {
        //String url = "https://goto.netcompany.com/cases/GTE440/TOEDNLD/_api/Web/GetFileByServerRelativeUrl('%s')/Versions";
        final String expand = "$expand=CreatedBy";
        final String select = "$select=ModifiedBy,Modified,CheckInComment,Created,ID,IsCurrentVersion,Size,Url,VersionLabel," +
                "CreatedBy/Editor,CreatedBy/Id,CreatedBy/Email,CreatedBy/Title,CreatedBy/LoginName";

        List<DownloadVersionCall> downloadVersionCalls = Collections.emptyList();
        for (Map.Entry<String, List<DocumentDetails>> entry : projectDocsMap.entrySet()) {
            final String project = entry.getKey();
            List<DocumentDetails> docs = entry.getValue();
            downloadVersionCalls = docs.stream()
                    .map(doc -> new DownloadVersionCall(
                            project,
                            String.format("%s%s_api/Web/GetFileByServerRelativeUrl('%s')/Versions?%s&%s",
                                    applicationProperties.toolkitUrl(), project, doc.getFileRef(), select, expand),
                            doc.getGuid(),
                            applicationProperties
                    ))
                    .collect(toList());
        }

        List<DocumentDetails> docsWithoutVersions = projectDocsMap.values().stream().flatMap(Collection::stream).collect(toList());
        Map<String, List<DocumentDetails>> resultProjectDocsMap = projectDocsMap.keySet()
                .stream()
                .collect(toMap(key -> key, value -> new LinkedList<>()));

        CompletionService<CustomizedTriple> ecs = new ExecutorCompletionService<>(executor);
        downloadVersionCalls.forEach(ecs::submit);

        for (int i = 0; i < downloadVersionCalls.size(); i++) {
            try {
                CustomizedTriple customizedTriple = ecs.take().get();
                Optional<DocumentDetails> documentWithoutVersion = docsWithoutVersions.stream()
                        .filter(doc -> doc.getGuid().equals(customizedTriple.getGuid()))
                        .findAny();
                if (documentWithoutVersion.isPresent()) {
                    documentWithoutVersion.get().setVersions(convertToVersions(customizedTriple.getVersions()));
                    List<DocumentDetails> docsWithVersion = resultProjectDocsMap.get(customizedTriple.getProject());
                    docsWithVersion.add(documentWithoutVersion.get());
                }

            } catch (InterruptedException | ExecutionException e) {
                logger.error("Error when getting torrents.", e);
            }
        }
        return resultProjectDocsMap;
    }

    private JsonObject getItems(String project, String listTitle) throws IOException {
        String url = String.format("%s%s/_api/web/lists/GetByTitle('%s')/items",
                applicationProperties.toolkitUrl(),
                project,
                listTitle);
        String select = "$select=Title,Modified,GUID,Created,DocIcon,FileRef,FileLeafRef,OData__UIVersionString";
        String fullUrl = String.format("%s?%s", url, select);

        return httpRequester.executeGET(fullUrl);
    }

    @Override
    List<DocumentDetails> convertToDocumentDetails(JsonObject object) {
        JsonObject d = object.getAsJsonObject("d");
        JsonArray results = d.getAsJsonArray("results");
        List<DocumentDetails> result = new ArrayList<>(results.size());
        for (int i = 0; i < results.size(); i++) {
            JsonObject jsonObject = results.get(i).getAsJsonObject();

            LocalDateTime created = LocalDateTime.parse(jsonObject.get("Created").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            LocalDateTime modified = LocalDateTime.parse(jsonObject.get("Modified").getAsString(), DateTimeFormatter.ISO_DATE_TIME);
            String fileRef = jsonObject.get("FileRef").getAsString();
            String fileLeafRef = jsonObject.get("FileLeafRef").getAsString();
            String docIcon = jsonObject.get("DocIcon").getAsString();
            String currentVersion = jsonObject.get("OData__UIVersionString").getAsString();
            String guid = jsonObject.get("GUID").getAsString();
            String title = jsonObject.get("Title").getAsString();

            DocumentDetails dd = new DocumentDetailsBuilder()
                    .withCreated(created)
                    .withModified(modified)
                    .withFileRef(fileRef)
                    .withFileLeafRef(fileLeafRef)
                    .withDocType(docIcon)
                    .withCurrentVersion(currentVersion)
                    .withGuid(guid)
                    .withTitle(title)
                    .withServerRelativeUrl(fileRef)
                    .withVersions(new LinkedList<>())
                    .create();
            result.add(dd);
        }
        return result;
    }
}
