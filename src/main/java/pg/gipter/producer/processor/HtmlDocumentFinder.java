package pg.gipter.producer.processor;

import com.google.gson.JsonObject;
import org.apache.commons.io.FileUtils;
import pg.gipter.producer.command.UploadType;
import pg.gipter.settings.ApplicationProperties;
import pg.gipter.toolkit.dto.DocumentDetails;
import pg.gipter.toolkit.dto.VersionDetails;
import pg.gipter.utils.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

class HtmlDocumentFinder extends SimpleDocumentFinder {

    HtmlDocumentFinder(ApplicationProperties applicationProperties) {
        super(applicationProperties);
    }

    @Override
    public List<File> find() {
        List<String> urls = buildFullUrls();
        List<JsonObject> items = getItems(urls);
        List<DocumentDetails> documentDetails = items.stream()
                .map(this::convertToDocumentDetails)
                .flatMap(List::stream)
                .filter(dd -> !StringUtils.nullOrEmpty(dd.getDocType()))
                .collect(toList());
        if (documentDetails.isEmpty()) {
            logger.error("Can not find [{}] to upload as your copyright items.", UploadType.TOOLKIT_DOCS);
            throw new IllegalArgumentException("Can not find items to upload.");
        }

        List<HtmlDocument> htmlDocuments = getHtmlDocuments(documentDetails);
        try {
            return Stream.of(createFile(createHtml(htmlDocuments))).collect(toList());
        } catch (IOException e) {
            String errorMsg = "Can not create html file to upload as your copyright item.";
            logger.error(errorMsg, e);
            throw new IllegalArgumentException(errorMsg, e);
        }
    }

    List<HtmlDocument> getHtmlDocuments(List<DocumentDetails> documentDetails) {
        List<HtmlDocument> htmlDocuments = new LinkedList<>();
        for (DocumentDetails dd : documentDetails) {
            if (dd.getVersions().isEmpty() && dd.getLastModifier().getLoginName().equals(applicationProperties.toolkitUsername())) {
                HtmlDocument htmlDocument = new HtmlDocument(dd.getFileLeafRef(), Double.valueOf(dd.getCurrentVersion()), dd.getCreated(), dd.getFileRef());
                htmlDocuments.add(htmlDocument);
            } else if (!dd.getVersions().isEmpty()) {
                Optional<VersionDetails> minMe;
                double minMeCurrentVersion = 0;
                do {
                    final double currentVersion = minMeCurrentVersion;
                    minMe = dd.getVersions().stream()
                            .filter(vd -> vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                            .filter(vd -> vd.getCreated().isAfter(LocalDateTime.of(applicationProperties.startDate(), LocalTime.now())))
                            .filter(vd -> vd.getCreated().isBefore(LocalDateTime.of(applicationProperties.endDate(), LocalTime.now())))
                            .filter(vd -> vd.getVersionLabel() > currentVersion)
                            .min(Comparator.comparingDouble(VersionDetails::getVersionLabel));

                    if (minMe.isPresent()) {
                        final VersionDetails minMeV = minMe.get();
                        dd.getVersions().stream()
                                .filter(vd -> !vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                                .filter(vd -> vd.getVersionLabel() < minMeV.getVersionLabel())
                                .max(Comparator.comparingDouble(VersionDetails::getVersionLabel))
                                .map(versionDetails -> new HtmlDocument(
                                        dd.getFileLeafRef(),
                                        versionDetails.getVersionLabel(),
                                        versionDetails.getCreated(),
                                        getFullDownloadUrl(dd.getProject() + versionDetails.getDownloadUrl())
                                ))
                                .ifPresent(htmlDocuments::add);
                        double difference = 0.0;
                        Optional<VersionDetails> nextMinMe;
                        do {
                            final double diff = ++difference;
                            nextMinMe = dd.getVersions().stream()
                                    .filter(vd -> vd.getCreator().getLoginName().equalsIgnoreCase(applicationProperties.toolkitUsername()))
                                    .filter(vd -> vd.getCreated().isAfter(LocalDateTime.of(applicationProperties.startDate(), LocalTime.now())))
                                    .filter(vd -> vd.getCreated().isBefore(LocalDateTime.of(applicationProperties.endDate(), LocalTime.now())))
                                    .filter(vd -> vd.getVersionLabel() > minMeV.getVersionLabel())
                                    .filter(vd -> vd.getVersionLabel() - minMeV.getVersionLabel() == diff)
                                    .min(Comparator.comparingDouble(VersionDetails::getVersionLabel));
                            if (nextMinMe.isPresent()) {
                                minMe = nextMinMe;
                            }
                        } while (nextMinMe.isPresent() &&
                                nextMinMe.get().getVersionLabel() < Double.valueOf(dd.getCurrentVersion())
                        );

                        String downloadUrl = getFullDownloadUrl(dd.getProject() + minMe.get().getDownloadUrl());
                        if (minMe.get().getDownloadUrl().startsWith(dd.getProject())) {
                            downloadUrl = getFullDownloadUrl(minMe.get().getDownloadUrl());
                        }

                        htmlDocuments.add(new HtmlDocument(
                                dd.getFileLeafRef(),
                                minMe.get().getVersionLabel(),
                                minMe.get().getCreated(),
                                downloadUrl
                        ));

                        minMeCurrentVersion = minMe.get().getVersionLabel();
                    }
                } while (minMe.isPresent() && minMe.get().getVersionLabel() < Double.valueOf(dd.getCurrentVersion()));
            }
        }
        return htmlDocuments;
    }

    String createHtml(List<HtmlDocument> htmlDocuments) {
        StringBuilder builder = new StringBuilder("<!DOCTYPE html>");
        builder.append("<html>");
        builder.append("<head>")
                .append("<style>")
                    .append("table, th, td {border: 1px solid black; border-collapse: collapse;}")
                    .append("th, td { padding: 5px; text-align: left;}")
                .append("</style>")
        .append("</head>");

        builder.append("<body>");
        builder.append("<h2>Copyright item</h2>");
        builder.append("<p>")
                .append("Item generated from ")
                .append(applicationProperties.startDate().format(ApplicationProperties.yyyy_MM_dd))
                .append(" to ")
                .append(applicationProperties.endDate().format(ApplicationProperties.yyyy_MM_dd))
                .append(".</p>");

        builder.append("<table style=\"width:100%\">");
        builder.append("<tr>");
        builder.append("<th>Title</th>");
        builder.append("<th>Version</th>");
        builder.append("<th>Modification date</th>");
        builder.append("<th>Link</th>");
        builder.append("</tr>");

        for (HtmlDocument htmlDocument : htmlDocuments) {
            builder.append("<tr>");
            builder.append("<td>")
                    .append("<nobr>")
                        .append(htmlDocument.getTitle())
                        .append("</nobr>")
                    .append("</td>");
            builder.append("<td>").append(htmlDocument.getVersion()).append("</td>");
            builder.append("<td>").append(htmlDocument.getModificationDate().format(DateTimeFormatter.ISO_DATE_TIME)).append("</td>");
            builder.append("<td>")
                    .append("<a target=\"_blank\" href=\"")
                        .append(htmlDocument.getLink().replaceAll(" ", "%20"))
                        .append("\">")
                            .append(htmlDocument.getLink().replaceAll(" ", "%20"))
                        .append("</a>")
                    .append("</td>");
            builder.append("</tr>");
        }
        builder.append("</table></body></html>");

        return builder.toString();
    }

    File createFile(String content) throws IOException {
        File htmlFile = new File(applicationProperties.itemPath());
        FileUtils.write(htmlFile, content, StandardCharsets.UTF_8);
        return htmlFile;
    }
}
